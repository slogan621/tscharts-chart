/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StationActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private enum StationState {
        ACTIVE,
        WAITING,
        AWAY,
    }

    private boolean m_twoPane;
    private StationState m_state = StationState.WAITING; // the status of this station
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    AsyncTask m_task = null;
    private PatientItemRecyclerViewAdapter m_waitingAdapter = null;
    private PatientItemRecyclerViewAdapter m_activeAdapter = null;
    private AppListItems m_appListItems = new AppListItems();
    private boolean m_isActive = false;
    private boolean m_isAway = false;
    private int m_activePatient = 0;
    public static StationActivity instance = null;  // hack to let me get at the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        View button_bar_item = findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    private class UpdatePatientLists extends AsyncTask<Object, Object, Object> {
        @Override
        protected String doInBackground(Object... params) {
            boolean first = true;

            while (true) {
                m_sess.updateClinicStationData();
                m_sess.updateQueues();
                m_sess.updateActivePatientList();
                m_sess.updateWaitingPatientList();
                if (first) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            setupRecyclerViews();
                            createAppList();
                        }
                    });
                    first = false;
                }

                m_sess.getActivePatientItem();
                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updatePatientDetail();   // only if not in an application
                        updateStationDetail();
                        updateViewVisibilities();
                    }
                });

                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        setWaitingPatientListData();
                        setActivePatientListData();
                    }
                });
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                }
            }
            //return "";
        }

        private void setWaitingPatientListData()
        {
            List<PatientItem> items;

            items = m_sess.getWaitingPatientListData();
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

    void showReturnToClinic()
    {
        ReturnToClinicDialogFragment rtc = new ReturnToClinicDialogFragment();
        rtc.setPatientId(m_sess.getActivePatientId());
        rtc.show(getSupportFragmentManager(), "Return To Clinic");
    }

    void showAway()
    {
        AwayDialogFragment rtc = new AwayDialogFragment();
        //rtc.setPatientId(m_sess.getActivePatientId());
        rtc.show(getSupportFragmentManager(), "Away");
    }

    public class CheckinPatient extends AsyncTask<Object, Object, Object> {
        @Override
        protected String doInBackground(Object... params) {
            checkinPatient();
            return "";
        }

        private void checkinPatient()
        {
            int patientId = m_sess.getDisplayPatientId();
            int clinicStationId = m_sess.getClinicStationId();
            int queueEntryId = m_sess.getQueueEntryId(patientId);
            int routingSlipEntryId = m_sess.setDisplayRoutingSlipEntryId(patientId);

            final QueueREST queueREST = new QueueREST(m_sess.getContext());
            Object lock = queueREST.deleteQueueEntry(queueEntryId);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = queueREST.getStatus();
            if (status == 200) {
                final ClinicStationREST clinicStationREST = new ClinicStationREST(m_sess.getContext());
                lock = clinicStationREST.updateActiveClinicStationPatient(clinicStationId, patientId);

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = clinicStationREST.getStatus();
                if (status == 200) {
                    final RoutingSlipEntryREST rseREST = new RoutingSlipEntryREST(m_sess.getContext());
                    lock = rseREST.markRoutingSlipStateCheckedIn(routingSlipEntryId);

                    synchronized (lock) {
                        // we loop here in case of race conditions or spurious interrupts
                        while (true) {
                            try {
                                lock.wait();
                                break;
                            } catch (InterruptedException e) {
                                continue;
                            }
                        }
                    }
                    status = rseREST.getStatus();
                    if (status == 200 ) {
                        final StateChangeREST stateChangeREST = new StateChangeREST(m_sess.getContext());
                        lock = stateChangeREST.stateChangeCheckin(clinicStationId, patientId);

                        synchronized (lock) {
                            // we loop here in case of race conditions or spurious interrupts
                            while (true) {
                                try {
                                    lock.wait();
                                    break;
                                } catch (InterruptedException e) {
                                    continue;
                                }
                            }
                        }
                        status = stateChangeREST.getStatus();
                        if (status == 200) {
                            StationActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(StationActivity.this, "Patient successfully signed in", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            StationActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(StationActivity.this, "Unable to update state change object", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(StationActivity.this, "Unable to update routing slip", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(StationActivity.this, "Unable to set clinic station state", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.this, "Unable to delete queue entry", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
                    Toast.makeText(StationActivity.this, "Stealing an active patient is not yet supported", Toast.LENGTH_SHORT).show();
                } else {
                    AsyncTask task = new CheckinPatient();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
                }
            }
        });
        button_bar_item = findViewById(R.id.checkout_button);
        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showReturnToClinic();
            }
        });
    }

    private void updatePatientDetail()
    {
        Bundle arguments = new Bundle();
        //arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
        ItemDetailFragment fragment = new ItemDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    private void updateViewVisibilities()
    {
        JSONObject activeObject = m_sess.getClinicStationData();
        try {
            m_isActive = activeObject.getBoolean("active");
            m_isAway = activeObject.getBoolean("away");

            View button_bar_item;

            if (m_isActive) {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.GONE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.GONE);
                View listView = findViewById(R.id.app_item_list);
                if (recycler.getVisibility() == View.GONE)
                    listView.setVisibility(View.VISIBLE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.GONE)
                    app.setVisibility(View.VISIBLE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.checkin_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.GONE)
                    button_bar_item.setVisibility(View.VISIBLE);
            } else if (m_isAway == true ) {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.INVISIBLE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.VISIBLE)
                    recycler.setVisibility(View.INVISIBLE);
                View listView = findViewById(R.id.app_item_list);
                if (listView.getVisibility() == View.VISIBLE)
                    listView.setVisibility(View.INVISIBLE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.VISIBLE)
                    app.setVisibility(View.GONE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.GONE)
                    button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item = findViewById(R.id.checkin_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
            } else {
                View recycler = findViewById(R.id.waiting_item_list_box);
                if (recycler.getVisibility() == View.INVISIBLE)
                    recycler.setVisibility(View.VISIBLE);
                recycler = findViewById(R.id.active_item_list_box);
                if (recycler.getVisibility() == View.INVISIBLE)
                    recycler.setVisibility(View.VISIBLE);
                View listView = findViewById(R.id.app_item_list);
                if (listView.getVisibility() == View.VISIBLE)
                    listView.setVisibility(View.INVISIBLE);

                button_bar_item = findViewById(R.id.away_button);
                if (button_bar_item.getVisibility() == View.GONE)
                    button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item = findViewById(R.id.back_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);
                button_bar_item = findViewById(R.id.checkin_button);
                if (button_bar_item.getVisibility() == View.GONE)
                    button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item = findViewById(R.id.checkout_button);
                if (button_bar_item.getVisibility() == View.VISIBLE)
                    button_bar_item.setVisibility(View.GONE);

                View app = findViewById(R.id.app_panel);
                if (app.getVisibility() == View.VISIBLE)
                    app.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
        }
    }

    private void updateStationDetail()
    {
        // {"name":"Dental3","name_es":"Dental3","activepatient":18,"away":false,"level":1,"nextpatient":null,"awaytime":30,"clinic":1,"station":1,"active":true,"willreturn":"2017-09-13T20:47:12","id":3}
        TextView label = (TextView) findViewById(R.id.station_name_state);
        JSONObject activeObject = m_sess.getClinicStationData();
        String stationLabel = String.format("Station: %s", m_sess.getClinicStationName());
        try {
            m_isActive = activeObject.getBoolean("active");
            m_isAway = activeObject.getBoolean("away");
            if (m_isActive) {
                stationLabel += "\nState: Active";
            } else if (m_isAway == true ) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                String willret = activeObject.getString("willreturn");

                Date d;
                try {
                    d = df.parse(willret);
                    SimpleDateFormat dflocal = new SimpleDateFormat("hh:mm:ss a");
                    dflocal.setTimeZone(TimeZone.getDefault());
                    willret = dflocal.format(d);
                } catch (ParseException e) {
                    willret = activeObject.getString("willreturn");
                }

                stationLabel += String.format("\nState: Away, Will Return: %s", willret);
            } else {
                stationLabel += "\nState: Waiting";
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
        instance = this;
        m_sess.clearPatientData();
        if (m_task == null) {
            m_task = new UpdatePatientLists();
            m_task.execute((Object) null);
        }

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            m_twoPane = true;
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
        alertDialogBuilder.setMessage(String.format("Are you sure you want to exit?"));
        alertDialogBuilder.setPositiveButton("yes",
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

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
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
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);

            String gender = "";
            String last = "";
            String first = "";

            try {
                gender = pObj.getString("gender");
                last = pObj.getString("paternal_last");
                first = pObj.getString("first");

            } catch (JSONException e) {

            }

            holder.mContentView.setText(String.format("%s, %s", last, first));

            if (gender.equals("Male")) {
                holder.mHeadshotView.setImageResource(R.drawable.boy_silhouette);
            } else {
                holder.mHeadshotView.setImageResource(R.drawable.girl_silhouette);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_twoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        arguments.putBoolean("isWaiting", m_isWaiting);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    }
                }
            });
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

        AppListItems appListItems = new AppListItems();

        final ArrayList<String> names = appListItems.getNames(station);
        final ArrayList<Integer> imageIds = appListItems.getImageIds(station);
        final ArrayList<Integer> selectors = appListItems.getSelectors(station);

        AppsList adapter = new AppsList(StationActivity.this, names, imageIds, selectors);

        ListView list;

        list = (ListView) findViewById(R.id.app_item_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(StationActivity.this, "You Clicked on " + names.get(+position), Toast.LENGTH_SHORT).show();
                Bundle arguments = new Bundle();
                //arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                //arguments.putBoolean("isWaiting", m_isWaiting);

                // XXX select based on name

                if (names.get(position).equals("Routing Slip")) {
                    AppRoutingSlipFragment fragment = new AppRoutingSlipFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.app_panel, fragment)
                            .commit();
                } else {
                    AppMedicalHistoryFragment fragment = new AppMedicalHistoryFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.app_panel, fragment)
                            .commit();
                }
            }
        });
    }
}
