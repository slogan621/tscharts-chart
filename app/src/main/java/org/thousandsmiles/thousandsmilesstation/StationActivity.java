/*
 * (C) Copyright Syd Logan 2017-2020
 * (C) Copyright Thousand Smiles Foundation 2017-2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmilesstation;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.SearchReturnToClinicStationHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class StationActivity extends AppCompatActivity {

    private enum StationState {
        ACTIVE,
        WAITING,
        AWAY,
    }

    private ItemDetailFragment m_fragment = null;
    private StationState m_state = StationState.WAITING; // the status of this station
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    AsyncTask m_task = null;
    private PatientItemRecyclerViewAdapter m_waitingAdapter = null;
    private PatientItemRecyclerViewAdapter m_activeAdapter = null;
    private AppListItems m_appListItems = new AppListItems();
    private boolean m_isActive = false;
    private boolean m_isAway = false;
    private boolean m_isDental = false;
    private int m_currentPatient = 0;
    public static StationActivity instance = null;  // hack to let me get at the activity
    private boolean m_showingAppFragment = false;
    private String m_fragmentName;
    private int m_waitingUpdateCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        View button_bar_item = findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        hideButtonBarButtons();
        m_appListItems.setContext(getApplicationContext());
        m_appListItems.init();
    }

    private boolean isReturnToClinicStation() {
      return m_sess.isDentalStation() || m_sess.isXRayStation() || m_sess.isENTStation() || m_sess.isAudiologyStation();
    }

    private void markPriorityPatients(List<PatientItem> patients) {
        View titleView = findViewById(R.id.waiting_item_list_title);
        titleView.setBackgroundColor(titleView.getResources().getColor(R.color.colorYellow));
        for (int i = 0; i < patients.size(); i++) {
            PatientItem item = patients.get(i);

            SearchReturnToClinicStationHelper searchHelper = new SearchReturnToClinicStationHelper();
            searchHelper.setState("scheduled_return");
            searchHelper.setContext(getApplicationContext());
            searchHelper.setRequestingStation(m_sess.getClinicStationId());
            searchHelper.setClinic(m_sess.getClinicId());
            searchHelper.setPatient(Integer.parseInt(item.id));
            SearchReturnToClinicStation rtc = new SearchReturnToClinicStation();
            rtc.setUpdateTitleOnly(true);
            rtc.setTitle(titleView);
            searchHelper.addListener(rtc);
            AsyncTask task = searchHelper;
            try {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
            } catch (Exception ex) {
                // ignore, try again on the next pass. Might see this if server is not responding in a timely manner
            }

            SearchReturnToClinicStationHelper searchHelper2 = new SearchReturnToClinicStationHelper();
            searchHelper2.setState("scheduled_dest");
            searchHelper2.setContext(getApplicationContext());
            searchHelper2.setStation(m_sess.getStationStationId());
            searchHelper2.setClinic(m_sess.getClinicId());
            searchHelper2.setPatient(Integer.parseInt(item.id));
            SearchReturnToClinicStation rtc2 = new SearchReturnToClinicStation();
            rtc2.setUpdateTitleOnly(true);
            rtc2.setTitle(titleView);
            searchHelper2.addListener(rtc2);
            AsyncTask task2 = searchHelper2;
            try {
                task2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
            } catch (Exception ex) {
                // ignore, try again on the next pass. Might see this if server is not responding in a timely manner
            }
        }
    }

    private class UpdatePatientLists extends AsyncTask<Object, Object, Object>  {

        private int m_titleUpdateCountdown = 0;

        @Override
        protected String doInBackground(Object... params) {
            boolean first = true;

            while (true) {
                m_sess.updateClinicStationData();
                if (m_sess.isActive() == false) {
                    m_sess.updateQueues();
                }
                m_sess.updateActivePatientList();
                m_sess.updateWaitingPatientList();
                if (first) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            setupRecyclerViews();
                            createAppList();
                            View v = findViewById(R.id.waiting_item_list_title);
                            v.setBackgroundColor(v.getResources().getColor(R.color.colorYellow));
                        }
                    });
                    first = false;
                }

                m_sess.getActivePatientItem();

                if (m_currentPatient == -1 || m_sess.getDisplayPatientId() != m_currentPatient) {
                    m_currentPatient = m_sess.getDisplayPatientId();
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            updatePatientDetail();   // only if not in an application
                        }
                    });
                }

                /* handle the case that we signed into a clinic station that already has an
                   active patient signed in */

                int patientId = m_sess.getDisplayPatientId();
                if (patientId != -1) {
                    int routingSlipEntryId = m_sess.getDisplayRoutingSlipEntryId();
                    if (routingSlipEntryId == -1) {
                        m_sess.updateQueues();  // unlikely there is a queue entry, but try anyway
                        routingSlipEntryId = m_sess.setDisplayRoutingSlipEntryIdForPatient(patientId);
                        if (routingSlipEntryId == -1) { // most likely we didn't find one
                            ArrayList<RoutingSlipEntry> rseList = m_sess.getRoutingSlipEntries(m_sess.getClinicId(), patientId);
                            for (int i = 0; i < rseList.size(); i++) {
                                RoutingSlipEntry e = rseList.get(i);
                                if (e.getStation() == m_sess.getStationStationId()) {
                                    m_sess.setDisplayRoutingSlipEntryId(e.getId());
                                    break;
                                }
                            }
                        }
                        routingSlipEntryId = m_sess.getDisplayRoutingSlipEntryId();
                        if (routingSlipEntryId == -1) {
                            StationActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(StationActivity.this, StationActivity.this.getString(R.string.msg_unable_to_update_routing_slip_entry), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateStationDetail();
                        updateViewVisibilities();
                    }
                });

                if (m_waitingUpdateCount == 0) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            setWaitingPatientListData();
                            setActivePatientListData();
                        }
                    });
                    m_waitingUpdateCount = 30;
                } else {
                    m_waitingUpdateCount--;
                    if (m_waitingUpdateCount < 0) {
                        m_waitingUpdateCount = 0;
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                }
            }
        }

        private void setWaitingPatientListData()
        {
            List<PatientItem> items;

            items = m_sess.getWaitingPatientListData();
            if (isReturnToClinicStation() == true) {
                if (m_titleUpdateCountdown == 0) {
                    markPriorityPatients(items);
                    m_titleUpdateCountdown = 15;
                } else {
                    m_titleUpdateCountdown--;
                }
            }
            m_waitingAdapter.swap(items);
        }

        private void setActivePatientListData()
        {
            List<PatientItem> items;

            items = m_sess.getActivePatientListData();
            m_activeAdapter.swap(items);
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }

    private void onSendToStationPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (m_sess.isENTStation()) {
            alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_are_you_sure_you_want_to_send_patient_to_audiology)));
        } else {
            alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_are_you_sure_you_want_to_send_patient_to_xray)));
        }
        alertDialogBuilder.setPositiveButton(R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        setButtonEnabled(false);
                        CheckoutParams params = new CheckoutParams();
                        params.setReturnToClinicStation(true);
                        if (m_sess.isENTStation()) {
                            params.setStationId(m_sess.getStationIdFromName("Audiology"));
                        } else {
                            params.setStationId(m_sess.getStationIdFromName("X-Ray"));
                        }
                        params.setRequestingClinicStationId(m_sess.getClinicStationId());
                        CheckoutPatient task = new CheckoutPatient();

                        task.setStationActivity(StationActivity.this);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) params);
                    }
                });

        alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(StationSelectorActivity.this,"Please select another station.",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void showReturnToClinic()
    {
        if (m_showingAppFragment == true) {
            // bring down the current fragment
            // this will trigger onPause in current fragment which will allow for unsaved changes.
            AppBlankFragment fragment = new AppBlankFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.app_panel, fragment)
                    .commit();
            m_showingAppFragment = false;
        }

        ReturnToClinicDialogFragment rtc = new ReturnToClinicDialogFragment();
        rtc.setStationActivity(this);
        rtc.setPatientId(m_sess.getActivePatientId());
        rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.title_return_to_clinic));
    }

    void showAway()
    {
        AwayDialogFragment rtc = new AwayDialogFragment();
        rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_away));
    }

    private void setButtonBarCallbacks()
    {
        View button_bar_item = findViewById(R.id.away_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showAway();
            }
        });
        button_bar_item = findViewById(R.id.back_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                AsyncTask task = new StationReturn();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
            }
        });
        button_bar_item = findViewById(R.id.checkin_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (m_sess.getWaitingIsFromActiveList() == true) {
                    Toast.makeText(StationActivity.this, R.string.msg_stealing_unsupported, Toast.LENGTH_SHORT).show();
                } else {
                    setButtonEnabled(false);
                    CheckinPatient task = new CheckinPatient();
                    task.setStationActivity(StationActivity.this);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
                }
            }
        });
        button_bar_item = findViewById(R.id.sendtostation_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onSendToStationPressed();
            }
        });
        button_bar_item = findViewById(R.id.checkout_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setButtonEnabled(false);
                showReturnToClinic();
            }
        });
    }

    private void updatePatientDetail()
    {
        Bundle arguments = new Bundle();
        m_fragment = new ItemDetailFragment();
        m_fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, m_fragment)
                .commitAllowingStateLoss();
    }

    private void setAwayButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.away_button);
        button_bar_item.setEnabled(enable);
    }

    private void setBackButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.back_button);
        button_bar_item.setEnabled(enable);
    }

    private void setCheckinButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.checkin_button);
        button_bar_item.setEnabled(enable);
    }

    private void setCheckoutButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.checkout_button);
        button_bar_item.setEnabled(enable);
    }

    private void setSendToStationButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.sendtostation_button);
        button_bar_item.setEnabled(enable);
    }

    public void setButtonEnabled(boolean enable)
    {
        setAwayButtonEnabled(enable);
        setBackButtonEnabled(enable);
        setCheckinButtonEnabled(enable);
        setCheckoutButtonEnabled(enable);
        setSendToStationButtonEnabled(enable);
    }

    private void updateViewVisibilities()
    {
        JSONObject activeObject = m_sess.getClinicStationData();
        try {
            m_isActive = activeObject.getBoolean("active");
            m_isAway = activeObject.getBoolean("away");
            m_isDental = true; // XXX

            View button_bar_item;
            View id_panel;

            id_panel = findViewById(R.id.id_panel);
            if (id_panel != null)
            {
                if (m_isActive) {
                    id_panel.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                } else if (m_isAway) {
                    id_panel.setBackgroundColor(getResources().getColor(R.color.skyBlue));
                }
                else {
                    id_panel.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                }
            }

            if (m_isActive) {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.GONE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.GONE);
                View checkbox = findViewById(R.id.show_all_waiting_box);
                if (checkbox.getVisibility() == View.VISIBLE)
                    checkbox.setVisibility(View.INVISIBLE);
                View listView = findViewById(R.id.app_item_list);
                if (recycler.getVisibility() == View.GONE)
                    listView.setVisibility(View.VISIBLE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.GONE)
                    app.setVisibility(View.VISIBLE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
                button_bar_item = findViewById(R.id.checkin_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);

                /* if station is dental or ENT, otherwise set to invisible */

                if (m_sess.isDentalStation()) {
                    button_bar_item = findViewById(R.id.sendtostation_button);
                    if (button_bar_item.getVisibility() == View.INVISIBLE) {
                        ImageView iv = findViewById(R.id.sendtostation_image);
                        if (iv != null) {
                            iv.setImageResource(R.drawable.sendtoxray_selector);
                        }
                        TextView tv = findViewById(R.id.sendtostation_label);
                        tv.setText(R.string.button_send_to_xray);
                        button_bar_item.setVisibility(View.VISIBLE);
                    }
                } else if (m_sess.isENTStation()) {
                    button_bar_item = findViewById(R.id.sendtostation_button);
                    if (button_bar_item.getVisibility() == View.INVISIBLE) {
                        ImageView iv = findViewById(R.id.sendtostation_image);
                        if (iv != null) {
                            iv.setImageResource(R.drawable.sendtoaudiology_selector);
                        }
                        button_bar_item.setVisibility(View.VISIBLE);
                        TextView tv = findViewById(R.id.sendtostation_label);
                        tv.setText(R.string.button_send_to_audiology);
                    }
                } else {
                    button_bar_item = findViewById(R.id.sendtostation_button);
                    button_bar_item.setVisibility(View.INVISIBLE);
                }

                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.INVISIBLE) {
                    button_bar_item.setVisibility(View.VISIBLE);
                }

            } else if (m_isAway == true ) {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.INVISIBLE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.INVISIBLE);
                View checkbox = findViewById(R.id.show_all_waiting_box);
                if (checkbox.getVisibility() == View.VISIBLE)
                    checkbox.setVisibility(View.INVISIBLE);
                View listView = findViewById(R.id.app_item_list);
                if (listView.getVisibility() == View.VISIBLE)
                    listView.setVisibility(View.INVISIBLE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.VISIBLE)
                    app.setVisibility(View.GONE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.INVISIBLE)
                    button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item = findViewById(R.id.checkin_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
                button_bar_item = findViewById(R.id.sendtostation_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);
            } else {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.INVISIBLE)
                    recycler.setVisibility(View.VISIBLE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.INVISIBLE)
                    recycler.setVisibility(View.VISIBLE);
                View checkbox = findViewById(R.id.show_all_waiting_box);
                if (checkbox.getVisibility() == View.INVISIBLE)
                    checkbox.setVisibility(View.VISIBLE);
                View listView = findViewById(R.id.app_item_list);
                if (listView.getVisibility() == View.VISIBLE)
                    listView.setVisibility(View.INVISIBLE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.INVISIBLE)
                    button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);

                button_bar_item = findViewById(R.id.checkin_button);
                if (m_sess.getDisplayPatientId() != -1 || (m_sess.isWaiting() && m_sess.getWaitingPatientItem() != null)) {
                    if (button_bar_item.getVisibility() == View.INVISIBLE) {
                        button_bar_item.setVisibility(View.VISIBLE);
                    }
                } else {
                    button_bar_item.setVisibility(View.INVISIBLE);
                }
                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);

                button_bar_item = findViewById(R.id.sendtostation_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.INVISIBLE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.VISIBLE)
                    app.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
        }
    }

    private void hideButtonBarButtons()
    {
        View button_bar_item;
        button_bar_item = findViewById(R.id.away_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
        button_bar_item = findViewById(R.id.back_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
        button_bar_item = findViewById(R.id.checkin_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
        button_bar_item = findViewById(R.id.checkout_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
        button_bar_item = findViewById(R.id.sendtostation_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
    }

    private void updateStationDetail()
    {
        // {"name":"Dental3","name_es":"Dental3","activepatient":18,"away":false,"level":1,"nextpatient":null,"awaytime":30,"clinic":1,"station":1,"active":true,"willreturn":"2017-09-13T20:47:12","id":3}
        TextView label = (TextView) findViewById(R.id.station_name_state);
        JSONObject activeObject = m_sess.getClinicStationData();
        String stationLabel = String.format(getApplicationContext().getString(R.string.label_station) + ": %s", m_sess.getClinicStationName());
        try {
            m_isActive = activeObject.getBoolean("active");
            m_isAway = activeObject.getBoolean("away");
            if (m_isActive) {
                stationLabel += "\n" + getApplicationContext().getString(R.string.label_state) + ": " + getApplicationContext().getString(R.string.label_active);
            } else if (m_isAway == true ) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                String willret = activeObject.getString("willreturn");

                Date d;
                try {
                    d = df.parse(willret);
                    SimpleDateFormat dflocal = new SimpleDateFormat("hh:mm:ss a");
                    dflocal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    willret = dflocal.format(d);
                } catch (ParseException e) {
                    willret = activeObject.getString("willreturn");
                }

                stationLabel += String.format("\n" + getApplicationContext().getString(R.string.label_state) + ": " + getApplicationContext().getString(R.string.label_away_will_return) + ": %s", willret);
            } else {
                stationLabel += "\n" + getApplicationContext().getString(R.string.label_state) + ": " + getApplicationContext().getString(R.string.label_waiting);
            }
        } catch (JSONException e) {
        }
        label.setText(stationLabel);
        ImageView icon = (ImageView) findViewById(R.id.station_icon);
        int activeStationId = m_sess.getStationStationId();
        icon.setImageResource(m_sess.getStationIconResource(activeStationId));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        instance = null;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final View root = getWindow().getDecorView().getRootView();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goImmersive();
            }
        });
        instance = this;
        m_sess.clearPatientData();

        CheckBox box;
        box = (CheckBox) findViewById(R.id.show_all_waiting);
        if (box != null) {
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    m_sess.setShowAll(isChecked);
                    m_waitingUpdateCount = 0;
                }
            });
        }
        if (m_task == null) {
            m_task = new UpdatePatientLists();
            m_task.execute((Object) null);
        }

        setButtonBarCallbacks();
    }

    private void setupRecyclerViews() {
        View recycler = findViewById(R.id.waiting_item_list);
        assert recycler != null;
        setupWaitingRecyclerView((RecyclerView) recycler);

        recycler = findViewById(R.id.active_item_list);
        assert recycler != null;
        setupActiveRecyclerView((RecyclerView) recycler);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_are_you_sure_you_want_to_exit)));
        alertDialogBuilder.setPositiveButton(R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if(m_task!=null){
                            m_task.cancel(true);
                            m_task = null;
                        }
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

        alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(StationSelectorActivity.this,"Please select another station.",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setupWaitingRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter((m_waitingAdapter = new PatientItemRecyclerViewAdapter(true)));
    }

    private void setupActiveRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter((m_activeAdapter = new PatientItemRecyclerViewAdapter(false)));
    }

    public class PatientItemRecyclerViewAdapter
            extends RecyclerView.Adapter<PatientItemRecyclerViewAdapter.ViewHolder> {

        private List<PatientItem> mValues = new ArrayList<PatientItem>();
        private boolean m_isWaiting;

        public PatientItemRecyclerViewAdapter(boolean isWaiting) {
            m_isWaiting = isWaiting;
        }

        public void swap(List<PatientItem> items)
        {
            if (items != null) {
                mValues.clear();
                mValues.addAll(items);
                if (m_isWaiting && m_sess.isWaiting()) {
                    WaitingPatientList.clearItems();
                    View recycler = findViewById(R.id.waiting_item_list_box);
                    if (items.size() == 0) {
                        recycler.setVisibility(View.GONE);
                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        for (int i = 0; i < items.size(); i++) {
                            WaitingPatientList.addItem(mValues.get(i));
                        }
                    }
                } else if (m_sess.isWaiting()){
                    View recycler = findViewById(R.id.active_item_list_box);
                    ActivePatientList.clearItems();
                    if (items.size() == 0) {
                        recycler.setVisibility(View.GONE);
                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        for (int i = 0; i < items.size(); i++) {
                            ActivePatientList.addItem(mValues.get(i));
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            JSONObject pObj = mValues.get(position).pObject;
            boolean isNext = mValues.get(position).isNext;
            //boolean isReturnToClinic = mValues.get(position).isReturnToClinic;
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            if (isNext) {
                holder.mView.setBackgroundColor(getResources().getColor(R.color.colorYellow));
            } else {
                holder.mView.setBackgroundColor(Color.TRANSPARENT);
            }

            String gender = "";
            String last = "";
            String first = "";
            int patientId = -1;

            try {
                gender = pObj.getString("gender");
                last = pObj.getString("paternal_last");
                first = pObj.getString("first");
                patientId = pObj.getInt("id");

            } catch (JSONException e) {
            }

            holder.mContentView.setText(String.format("%s, %s", last, first));

            if (gender.equals("Male")) {
                holder.mHeadshotView.setImageResource(R.drawable.boyfront);
            } else {
                holder.mHeadshotView.setImageResource(R.drawable.girlfront);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
            holder.mHeadshotView.setLayoutParams(layoutParams);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateViewVisibilities();
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                    arguments.putBoolean("isWaiting", m_isWaiting);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                }
            });


            /* if we have a return to clinic patient that has been seen and returned to us, highlight.
             * For example, we are ENT and sent a patient to audiology, and they were seen by
             * audiology and are back in our line. */

            if (isReturnToClinicStation() == true) {
                SearchReturnToClinicStationHelper searchHelper = new SearchReturnToClinicStationHelper();
                searchHelper.setState("scheduled_return");
                searchHelper.setContext(getApplicationContext());
                searchHelper.setRequestingStation(m_sess.getClinicStationId());
                searchHelper.setClinic(m_sess.getClinicId());
                searchHelper.setPatient(patientId);
                SearchReturnToClinicStation rtc = new SearchReturnToClinicStation();
                rtc.setView(holder.mView);
                searchHelper.addListener(rtc);
                AsyncTask task = searchHelper;
                try {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
                } catch (Exception ex) {
                    // ignore, try again on the next pass. Might see this if server is not responding in a timely manner
                }

                /* if we have a return to clinic patient that was sent to us, highlight. For example,
                 * we are audiology and ENT just sent us a patient that needs to be seen immediately
                 * and then returned by to ENT once we are done. */

                SearchReturnToClinicStationHelper searchHelper2 = new SearchReturnToClinicStationHelper();
                searchHelper2.setState("scheduled_dest");
                searchHelper2.setContext(getApplicationContext());
                searchHelper2.setStation(m_sess.getStationStationId());
                searchHelper2.setClinic(m_sess.getClinicId());
                searchHelper2.setPatient(patientId);
                SearchReturnToClinicStation rtc2 = new SearchReturnToClinicStation();
                rtc2.setView(holder.mView);
                searchHelper2.addListener(rtc2);
                AsyncTask task2 = searchHelper2;
                try {
                    task2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
                } catch (Exception ex) {
                    // ignore, try again on the next pass. Might see this if server is not responding in a timely manner
                }
            } // true
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public final ImageView mHeadshotView;
            public PatientItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mHeadshotView = (ImageView) view.findViewById(R.id.imageContent);
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    private void createAppList() {
        String station = m_sess.getActiveStationName();

        final ArrayList<String> names = m_appListItems.getNames(station);
        final ArrayList<Integer> imageIds = m_appListItems.getImageIds(station);
        final ArrayList<Integer> selectors = m_appListItems.getSelectors(station);

        AppsList adapter = new AppsList(StationActivity.this, names, imageIds, selectors);

        ListView list;

        list = (ListView) findViewById(R.id.app_item_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(StationActivity.this, "You Clicked on " + names.get(+position), Toast.LENGTH_SHORT).show();
                Bundle arguments = new Bundle();
                //arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                //arguments.putBoolean("isWaiting", m_isWaiting);

                // XXX select based on name

                String selectedName = names.get(position);

                if (!m_showingAppFragment || names.get(position).equals(getApplicationContext().getString(R.string.xray_name)) ||
                        names.get(position).equals(getApplicationContext().getString(R.string.exam_name)) ||
                        selectedName.equals(m_fragmentName) == false) {
                    if (names.get(position).equals(getApplicationContext().getString(R.string.routing_slip_name))) {
                        showRoutingSlip();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.medical_history_name))) {
                        showMedicalHistory();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.xray_name))) {
                        showXRaySearchResults();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.ent_history_name))) {
                        showENTHistorySearchResults();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.exam_name))) {
                        showENTExamSearchResults();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.audiogram_name))) {
                        //showXRaySearchResults();
                        //m_showingAppFragment = true;
                        //m_fragmentName = names.get(position);
                        Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.diagnosis_name))) {
                        //showXRaySearchResults();
                        //m_showingAppFragment = true;
                        //m_fragmentName = names.get(position);
                        Toast.makeText(StationActivity.this, R.string.msg_feature_not_implemented, Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.treatment_plan_name))) {
                        //showXRaySearchResults();
                        //m_showingAppFragment = true;
                        //m_fragmentName = names.get(position);
                        Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();

                    }
                }
            }
        });
    }

    private void showRoutingSlip()
    {
        Bundle arguments = new Bundle();
        AppRoutingSlipFragment fragment = new AppRoutingSlipFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void showXRaySearchResults()
    {
        Bundle arguments = new Bundle();
        AppPatientXRayListFragment fragment = new AppPatientXRayListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void showENTHistorySearchResults()
    {
        Bundle arguments = new Bundle();
        AppPatientENTHistoryListFragment fragment = new AppPatientENTHistoryListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void showENTExamSearchResults()
    {
        Bundle arguments = new Bundle();
        AppPatientENTExamListFragment fragment = new AppPatientENTExamListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void showMedicalHistory()
    {
        Bundle arguments = new Bundle();
        AppMedicalHistoryFragment fragment = new AppMedicalHistoryFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            goImmersive();
        }
    }

    public void goImmersive() {
        View v1 = getWindow().getDecorView().getRootView();
        v1.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}