/*
 * (C) Copyright Syd Logan 2017-2021
 * (C) Copyright Thousand Smiles Foundation 2017-2021
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

package org.thousandsmiles.thousandsmileschart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.AppVaccineFragment;
import org.thousandsmiles.tscharts_lib.FormDirtyListener;
import org.thousandsmiles.tscharts_lib.FormDirtyNotifierFragment;
import org.thousandsmiles.tscharts_lib.FormDirtyPublisher;
import org.thousandsmiles.tscharts_lib.FormSaveAndPatientCheckoutNotifierActivity;
import org.thousandsmiles.tscharts_lib.FormSaveListener;
import org.thousandsmiles.tscharts_lib.PatientCheckoutListener;

import java.util.ArrayList;

public class StationActivity extends FormSaveAndPatientCheckoutNotifierActivity {

    public void subscribeSave(FormSaveListener instance) {
        m_formSaveList.add(instance);
    }

    public void unsubscribeSave(FormSaveListener instance) {
        m_formSaveList.remove(instance);
    }

    @Override
    public void fragmentSaveDone(boolean success) {
    }

    private boolean saveForm() {
        boolean ret = true;
        for (int i = 0; i < m_formSaveList.size(); i++) {
            ret = m_formSaveList.get(i).save();
            if (ret == false) {
                break;
            }
        }
        return ret;
    }

    public void showPatientSearch() {
        Intent intent = new Intent(this, PatientSelectorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        this.finish();
    }

    public void showReturnToClinic()
    {
        if (m_sess.getStationSupportsRTC()) {
            ReturnToClinicDialogFragment rtc = new ReturnToClinicDialogFragment();
            rtc.setPatientId(m_sess.getDisplayPatientId());
            rtc.setStationActivity(this);
            rtc.show(getSupportFragmentManager(), this.getString(R.string.title_return_to_clinic));
        } else {
            final RoutingSlipEntry routingSlipEntry = m_sess.currentStationInRoutingSlip();

            if (routingSlipEntry != null) {
                String text = getString(R.string.msg_remove_from_routingslip_formatted, routingSlipEntry.getName());
                AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

                builder.setMessage(text).setTitle(R.string.title_remove_from_routing_slip);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                            new Thread(() -> {
                                int status = m_sess.deleteRoutingSlipEntry(getApplicationContext(), routingSlipEntry.getId());
                                if (status == 200) {

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_routing_slip_entry_successfully_removed), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_routing_slip_entry_unsuccessfully_removed), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).start();

                        showPatientSearch();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showPatientSearch();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                showPatientSearch();
            }
        }
    }

    public void subscribeCheckout(PatientCheckoutListener instance) {
        m_patientCheckoutList.add(instance);
    }

    public void unsubscribeCheckout(PatientCheckoutListener instance) {
        m_patientCheckoutList.remove(instance);
    }

    @Override
    public void fragmentReadyForCheckout(boolean success) {
        showReturnToClinic();
    }

    private boolean patientCheckout() {
        boolean ret = true;
        if (m_patientCheckoutList.size() == 0) {
            showReturnToClinic();
        } else {
            for (int i = 0; i < m_patientCheckoutList.size(); i++) {
                ret = m_patientCheckoutList.get(i).checkout();
                if (ret == false) {
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public void dirty(boolean dirty) {
        View button_bar_item = findViewById(R.id.save_button);
        if (button_bar_item != null) {
            if (dirty) {
                button_bar_item.setVisibility(View.VISIBLE);
            } else {
                button_bar_item.setVisibility(View.GONE);
            }
        }
    }

    private enum StationState {
        ACTIVE,
        WAITING,
        AWAY,
    }

    private ArrayList<FormSaveListener> m_formSaveList = new ArrayList<FormSaveListener>();
    private ArrayList<PatientCheckoutListener> m_patientCheckoutList = new ArrayList<PatientCheckoutListener>();
    private ItemDetailFragment m_fragment = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    AsyncTask m_task = null;
    private AppListItems m_appListItems = new AppListItems();
    private boolean m_isActive = false;
    private boolean m_isAway = false;
    private int m_currentPatient = -1;
    public static StationActivity instance = null;  // hack to let me get at the activity
    private boolean m_showingAppFragment = false;
    private String m_fragmentName;
    private Activity m_activity;
    private FormDirtyNotifierFragment m_currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        View button_bar_item = findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        hideButtonBarButtons();
        m_appListItems.setContext(getApplicationContext());
        m_appListItems.init();
        m_sess.updateCategoryDataTask();
        m_activity = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_task.isCancelled() == false) {
            m_task.cancel(true);
            m_task = null;
        }
    }

    private class UpdatePatientLists extends AsyncTask<Object, Object, Object>  {

        private int m_titleUpdateCountdown = 0;

        @Override
        protected String doInBackground(Object... params) {
            boolean first = true;

            while (true) {
                if (isCancelled()) {
                    break;
                }

                if (first) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            createAppList();
                        }
                    });
                    first = false;
                }

                if (m_currentPatient == -1 || m_sess.getDisplayPatientId() != m_currentPatient) {
                    m_currentPatient = m_sess.getDisplayPatientId();
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            updatePatientDetail();   // only if not in an application
                        }
                    });
                }

                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateViewVisibilities();
                    }
                });

                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                }
            }
            return "";
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
        View button_bar_item;

        button_bar_item = findViewById(R.id.checkout_button);

        button_bar_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                patientCheckout();
            }
        });

        button_bar_item = m_activity.findViewById(R.id.save_button);

        button_bar_item.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // notify fragment to save itself
                saveForm();
            }
        });

        TextView tx = (TextView) m_activity.findViewById(R.id.checkout_label);
        boolean isRunner = m_sess.getActiveStationName() == "Runner" ? true:false;
        if (isRunner == true) {
            tx.setText(R.string.return_to_search);
        } else {
            tx.setText(R.string.button_check_out);
        }
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

    private void setBackButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.back_button);
        button_bar_item.setEnabled(enable);
    }

    private void setCheckoutButtonEnabled(boolean enable) {
        View button_bar_item;
        button_bar_item = findViewById(R.id.checkout_button);
        button_bar_item.setEnabled(enable);
    }

    private void updateViewVisibilities()
    {
        m_isActive = true;
        m_isAway = false;

        View button_bar_item;

        if (m_isActive) {

            View listView = findViewById(R.id.app_item_list);

            listView.setVisibility(View.VISIBLE);

            View app = findViewById(R.id.app_panel);
            if (app.getVisibility() == View.GONE)
                app.setVisibility(View.VISIBLE);

            button_bar_item = findViewById(R.id.checkout_button);
            if (button_bar_item.getVisibility() == View.INVISIBLE) {
                button_bar_item.setVisibility(View.VISIBLE);
            }

        }
    }

    private void hideButtonBarButtons()
    {
        View button_bar_item;

        button_bar_item = findViewById(R.id.back_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
        button_bar_item = findViewById(R.id.checkout_button);
        if (button_bar_item.getVisibility() == View.VISIBLE)
            button_bar_item.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveForm();
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

        if (m_task == null) {
            m_task = new UpdatePatientLists();
            m_task.execute((Object) null);
        }

        setButtonBarCallbacks();
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

    private void createAppList() {
        String station = m_sess.getActiveStationNameTranslated();

        final ArrayList<String> names = m_appListItems.getNames(station);
        final ArrayList<Integer> imageIds = m_appListItems.getImageIds(station);
        final ArrayList<Integer> selectors = m_appListItems.getSelectors(station);
        final ArrayList<Boolean> readOnlyFlags = m_appListItems.getReadOnlyFlags(station);

        AppsList adapter = new AppsList(StationActivity.this, names, imageIds, selectors);

        ListView list;

        list = (ListView) findViewById(R.id.app_item_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle arguments = new Bundle();

                if (saveForm() == false) {
                    return;
                }

                // XXX select based on name

                String selectedName = names.get(position);

                if (!m_showingAppFragment || names.get(position).equals(getApplicationContext().getString(R.string.xray_name)) ||
                        names.get(position).equals(getApplicationContext().getString(R.string.exam_name)) ||
                        selectedName.equals(m_fragmentName) == false) {
                    if (m_showingAppFragment == true && m_currentFragment != null) {
                        ((FormDirtyPublisher) m_currentFragment).unsubscribeDirty((FormDirtyListener) m_activity);
                    }
                    view.setSelected(true);
                    if (names.get(position).equals(getApplicationContext().getString(R.string.routing_slip_name))) {
                        showRoutingSlip();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.medical_history_name))) {
                        showMedicalHistory();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.vaccinations_name))) {
                        showVaccinations();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.xray_name))) {
                        showXRaySearchResults();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.ent_history_name))) {
                        showENTHistorySearchResults(readOnlyFlags.get(position));
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.exam_name))) {
                        showENTExamSearchResults(readOnlyFlags.get(position));
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.audiogram_name))) {
                        showAudiogramSearchResults();
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.diagnosis_name))) {
                        showENTDiagnosisSearchResults(readOnlyFlags.get(position));
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this, R.string.msg_feature_not_implemented, Toast.LENGTH_LONG).show();
                    } else if (names.get(position).equals(getApplicationContext().getString(R.string.treatment_plan_name))) {
                        showENTTreatmentSearchResults(readOnlyFlags.get(position));
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();
                    }
                    else if (names.get(position).equals(getApplicationContext().getString(R.string.dental_chart_name))) {
                        showDentalChartSearchResults(readOnlyFlags.get(position));
                        m_showingAppFragment = true;
                        m_fragmentName = names.get(position);
                        //Toast.makeText(StationActivity.this,R.string.msg_feature_not_implemented,Toast.LENGTH_LONG).show();

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
        setActiveFragment(fragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.routing_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.routing_slip_name);
    }

    public void showXRaySearchResults()
    {
        Bundle arguments = new Bundle();
        AppPatientXRayListFragment fragment = new AppPatientXRayListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.xray_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.xray_name);
    }

    public void showAudiogramSearchResults()
    {
        Bundle arguments = new Bundle();
        AppPatientAudiogramListFragment fragment = new AppPatientAudiogramListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.audiology_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.audiogram_name);
    }

    public void showENTHistorySearchResults(Boolean readOnly)
    {
        Bundle arguments = new Bundle();
        AppPatientENTHistoryListFragment fragment = new AppPatientENTHistoryListFragment();
        AppFragmentContext ctx = new AppFragmentContext();
        ctx.setReadOnly(readOnly);
        fragment.setAppFragmentContext(ctx);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.ent_history_name);
    }

    public void showENTDiagnosisSearchResults(Boolean readOnly)
    {
        Bundle arguments = new Bundle();
        AppPatientENTDiagnosisListFragment fragment = new AppPatientENTDiagnosisListFragment();
        AppFragmentContext ctx = new AppFragmentContext();
        ctx.setReadOnly(readOnly);
        fragment.setAppFragmentContext(ctx);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.diagnosis_name);
    }

    public void showENTTreatmentSearchResults(Boolean readOnly)
    {
        Bundle arguments = new Bundle();
        AppPatientENTTreatmentListFragment fragment = new AppPatientENTTreatmentListFragment();
        AppFragmentContext ctx = new AppFragmentContext();
        ctx.setReadOnly(readOnly);
        fragment.setAppFragmentContext(ctx);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.treatment_plan_name);
    }

    public void showENTExamSearchResults(Boolean readOnly)
    {
        Bundle arguments = new Bundle();
        AppPatientENTExamListFragment fragment = new AppPatientENTExamListFragment();
        AppFragmentContext ctx = new AppFragmentContext();
        ctx.setReadOnly(readOnly);
        fragment.setAppFragmentContext(ctx);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.exam_name);
    }

    public void showDentalChartSearchResults(Boolean readOnly)
    {
        Bundle arguments = new Bundle();
        AppPatientDentalTreatmentListFragment fragment = new AppPatientDentalTreatmentListFragment();
        AppFragmentContext ctx = new AppFragmentContext();
        ctx.setReadOnly(readOnly);
        fragment.setAppFragmentContext(ctx);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.dental_chart_name);
    }

    public void showMedicalHistory()
    {
        Bundle arguments = new Bundle();
        AppMedicalHistoryFragment fragment = new AppMedicalHistoryFragment();
        fragment.setArguments(arguments);
        setActiveFragment(fragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.medhist_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.medical_history_name);
    }

    public void showVaccinations()
    {
        Bundle arguments = new Bundle();
        AppVaccineFragment fragment = new AppVaccineFragment();
        fragment.setArguments(arguments);
        setActiveFragment(fragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
        ImageView v = findViewById(R.id.chart_icon);
        Drawable res = getResources().getDrawable(R.drawable.vax_pressed);
        v.setImageDrawable(res);
        TextView t = findViewById(R.id.chart_name);
        t.setText(R.string.vaccinations_name);
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

    public void setActiveFragment(FormDirtyNotifierFragment fragment) {
        m_currentFragment = fragment;
        fragment.subscribeDirty(this);
    }
}