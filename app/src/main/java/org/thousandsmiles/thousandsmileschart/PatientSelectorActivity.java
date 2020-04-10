/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.ClinicREST;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.HeadshotImage;
import org.thousandsmiles.tscharts_lib.HideyHelper;
import org.thousandsmiles.tscharts_lib.ImageDisplayedListener;
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.PatientREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PatientSelectorActivity extends AppCompatActivity implements ImageDisplayedListener {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;
    private ActionDialogAdapter m_actionAdapter;
    private View m_progressView;
    private View m_searchBar;

    public void handleButtonPress(View v)
    {
        this.m_activity.finish();
    }

    public void onImageDisplayed(int imageId, String path)
    {
        SessionSingleton sess = SessionSingleton.getInstance();
        sess.getCommonSessionSingleton().addHeadShotPath(imageId, path);
        sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    public void onImageError(int imageId, String path, int errorCode)
    {
        if (errorCode != 404) {
            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_patient_headshot), Toast.LENGTH_SHORT).show();
                }
            });
        }
        SessionSingleton.getInstance().getCommonSessionSingleton().removeHeadShotPath(imageId);
        SessionSingleton.getInstance().getCommonSessionSingleton().startNextHeadshotJob();
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_are_you_sure_you_want_to_exit)));
        alertDialogBuilder.setPositiveButton(R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
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

    @Override
    protected void onResume() {
        super.onResume();

        final View root = getWindow().getDecorView().getRootView();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goImmersive();
            }
        });

        m_progressView = findViewById(R.id.search_progress);
        m_searchBar = findViewById(R.id.patient_search_bar);
    }

    /**
     * Shows the progress UI and hides the search bar.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_searchBar.setVisibility(show ? View.GONE : View.VISIBLE);
            m_progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    m_searchBar.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_searchBar.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void ClearSearchResultTable()
    {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);

        layout.removeAllViews();
    }

    private void HideSearchResultTable()
    {
       View v = (View) findViewById(R.id.namestablelayout);
       if (v != null) {
           v.setVisibility(View.GONE);
       }
    }

    private void ShowSearchResultTable()
    {
        View v = (View) findViewById(R.id.namestablelayout);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void LayoutSearchResults() {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);
        TableRow row = null;
        int count;

        ClearSearchResultTable();
        ShowSearchResultTable();

        LinearLayout btnLO = new LinearLayout(this);

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLO.setOrientation(LinearLayout.VERTICAL);

        TableRow.LayoutParams parms = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

        int leftMargin=10;
        int topMargin=2;
        int rightMargin=10;
        int bottomMargin=2;
        parms.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        parms.gravity = (Gravity.CENTER_VERTICAL);

        btnLO.setLayoutParams(parms);


        ImageButton button = new ImageButton(getApplicationContext());

        /*
        btnLO.setBackgroundColor(getResources().getColor(R.color.lightGray));

        button.setBackgroundColor(getResources().getColor(R.color.lightGray));
        button.setImageDrawable(getResources().getDrawable(R.drawable.headshot_plus));

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_context);
            alertDialogBuilder.setMessage(m_activity.getString(R.string.question_select_this_patient));
            alertDialogBuilder.setPositiveButton(R.string.button_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //m_sess.setIsNewPatient(true);
                            //m_sess.resetNewPatientObjects();
                            Intent intent = new Intent(m_activity, StationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

            alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(PatientSearchActivity.this, R.string.msg_select_another_category,Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            }
        });

        btnLO.addView(button);

         */

        boolean newRow = true;
        row = new TableRow(getApplicationContext());
        row.setWeightSum((float)1.0);


        TextView txt = new TextView(getApplicationContext());

        row.setLayoutParams(parms);

        if (row != null) {
            row.addView(btnLO);
        }

        if (newRow == true) {
            layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        HashMap<Integer, PatientData> map = m_sess.getPatientHashMap();

        count = 0;
        int extraCells = (map.size() + 1) % 3;
        if (extraCells != 0) {
            extraCells = 3 - extraCells;
        }

        for (Map.Entry<Integer, PatientData> entry : map.entrySet()) {
            Integer key = entry.getKey();
            PatientData value = entry.getValue();

            newRow = false;
            if ((count % 3) == 0) {
                newRow = true;
                row = new TableRow(getApplicationContext());
                row.setWeightSum((float)1.0);
                row.setLayoutParams(parms);
            }

            btnLO = new LinearLayout(this);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);

            button = new ImageButton(getApplicationContext());

            Boolean girl = false;
            int id;
            String paternalLast;
            String first;

            girl = value.getGender().equals("Female");
            id = value.getId();
            paternalLast = value.getFatherLast();
            first = value.getFirst();

            if (girl == true) {
                button.setImageDrawable(getResources().getDrawable(R.drawable.girlfront));
                button.setBackgroundColor(getResources().getColor(R.color.girlPink));
            } else {
                button.setImageDrawable(getResources().getDrawable(R.drawable.boyfront));
                button.setBackgroundColor(getResources().getColor(R.color.boyBlue));
            }

            button.setTag(value);

            ActivityManager.MemoryInfo memoryInfo = m_sess.getCommonSessionSingleton().getAvailableMemory();

            if (!memoryInfo.lowMemory) {
                HeadshotImage headshot = new HeadshotImage();
                m_sess.getCommonSessionSingleton().addHeadshotImage(headshot);
                headshot.setActivity(this);
                headshot.setImageView(button);
                headshot.registerListener(this);
                Thread t = headshot.getImage(id);
                m_sess.getCommonSessionSingleton().addHeadshotJob(headshot);
            } else {
                PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                    }
                });
            }

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    PatientData o = (PatientData) v.getTag();
                    showActionDialog(o);
                    //confirmPatientSelection(o);
                }
            });

            btnLO.addView(button);

            txt = new TextView(getApplicationContext());
            txt.setText(String.format("%d %s, %s", id, paternalLast, first));
            txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            btnLO.addView(txt);

            if (row != null) {
                row.addView(btnLO);
            }

            if (newRow == true) {
                layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
            }
            count++;
        }

        for (int i = 0; i < extraCells; i++) {
            btnLO = new LinearLayout(this);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);
            if (row != null) {
                row.addView(btnLO);
            }
        }
        m_sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    private Date isDateString(String s) {
        // supports variations where year is yyyy or yy day is dd and month is MM

        Date ret = null;

        String[] formats = {
                "MM-dd-yy",
                "MM/dd/yy",
                "MM dd yy",
                "MM-dd-yyyy",
                "MM/dd/yyyy",
                "MM dd yyyy"
        };

        for (int i = 0; i < formats.length; i++){
            try {
                DateFormat df = new SimpleDateFormat(formats[i], Locale.ENGLISH);
                Date date = df.parse(s);
                ret = date;
                break;
            } catch (ParseException pe) {
            }
        }
        return ret;
    }

    class GetMatchingPatientsListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
            try {
                m_sess.setPatientSearchResults(a);
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }


    private void getMatchingPatients(final String searchTerm)
    {
        // analyze search term, looking for DOB string, gender, or name. Then, search.

        ArrayList<Integer> ret = new ArrayList<Integer>();

        showProgress(true);

        m_sess.clearPatientSearchResultData();

        /*
        m_sess.setIsNewPatient(false);
        m_sess.setIsNewMedicalHistory(false);

         */

        final Date d = isDateString(searchTerm);
        new Thread(new Runnable() {
            public void run() {

            final PatientREST x = new PatientREST(getApplicationContext());
            x.addListener(new GetMatchingPatientsListener());

            PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    showProgress(true);
                }
            });

            final Object lock;

            if (d != null) {
                lock = x.findPatientsByDOB(d);
            } else if (searchTerm.length() < 2) {
                lock = x.findPatientsByName("impossible_patient_name");
            } else {
                lock = x.findPatientsByNameAndClinicId(searchTerm, m_sess.getClinicId());
            }

            Thread thread = new Thread(){
                public void run() {
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

                PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showProgress(false);
                        }
                });

                if (x.getStatus() == 200) {
                    m_sess.getPatientSearchResultData();
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        LayoutSearchResults();
                        Button button = (Button) findViewById(R.id.patient_search_button);
                        button.setEnabled(true);
                        }
                    });
                    return;
                } else if (x.getStatus() == 101) {
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 400) {
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 500) {
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 404) {
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        LayoutSearchResults();
                        Toast.makeText(getApplicationContext(), R.string.error_no_matching_patients_found, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                    PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Button button = (Button) findViewById(R.id.patient_search_button);
                        button.setEnabled(true);
                    }
                });
                }
            };
            thread.start();
            }
        }).start();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_patient_search);

        final Button button = (Button) findViewById(R.id.patient_search_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText t = (EditText) findViewById(R.id.patient_search);
                String searchTerm = t.getText().toString();
                button.setEnabled(false);
                m_sess.getCommonSessionSingleton().cancelHeadshotImages();
                HideSearchResultTable();
                getMatchingPatients(searchTerm);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                goImmersive();
            }

        });
        m_context = this;
        m_activity = this;
        m_sess.getCommonSessionSingleton().clearHeadShotCache();
        m_sess.getCommonSessionSingleton().setPhotoPath("");
        m_sess.getCommonSessionSingleton().setContext(this);

        if (m_sess.getCommonSessionSingleton().getClinicId() == -1) {
            final ClinicREST clinicREST = new ClinicREST(m_context);
            final Object lock;

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            lock = clinicREST.getClinicData(year, month, day);

            final Thread thread = new Thread() {
                public void run() {
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

                    SessionSingleton data = SessionSingleton.getInstance();
                    int status = clinicREST.getStatus();
                    if (status == 101) {
                        PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                            }
                        });

                    } else if (status == 400) {
                        PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status == 404) {
                        PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_clinic_not_found_date, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status == 500) {
                        PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status != 200) {
                        PatientSelectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        /*
                        getMexicanStates();
                        getStations();
                        if (m_sess.updateCategoryData() == false) {
                            PatientSearchActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.error_unable_to_get_category_data, Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            m_sess.initCategoryNameToSelectorMap();
                            m_sess.initCategoryNameToSpanishMap();
                        }

                         */
                    }
                }
            };
            thread.start();
        }
    }

    private class GetDisplayPatientRoutingSlips extends AsyncTask<Object, Object, Object> {

        @Override
        protected String doInBackground(Object... params) {
            SessionSingleton sess = SessionSingleton.getInstance();
            sess.getRoutingSlipEntries(sess.getClinicId(), sess.getDisplayPatientId());
            return "";
        }
    }

    android.app.AlertDialog showActionDialog(final PatientData rd)
    {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PatientSelectorActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = PatientSelectorActivity.this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.action_dialog, null);
        builder.setTitle(R.string.title_action_dialog);
        String msgString = getResources().getString(R.string.msg_action_dialog);
        String msg = String.format(msgString, rd.getPatientFullName(true), rd.getId());
        builder.setMessage(msg);
        builder.setView(v)
                // Add action buttons

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(PatientSelectorActivity.this);
                    }
                });
        GridView grid = v.findViewById(R.id.myGrid);
        grid.setClickable(true);
        m_actionAdapter = new ActionDialogAdapter();
        m_actionAdapter.initialize(rd.getId());

        SessionSingleton sess = SessionSingleton.getInstance();

        int displayId = sess.getDisplayPatientId();

        if (displayId != rd.getId()) {
            sess.setDisplayPatientId(rd.getId());

            GetDisplayPatientRoutingSlips task = new GetDisplayPatientRoutingSlips();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
            //try {
                // give it a second to finish
                synchronized(task) {
                    ;
                }
            //} catch (InterruptedException ex2) {
            //    Toast.makeText(getApplicationContext(), R.string.error_interrupted_getting_patient_routing_slip, Toast.LENGTH_LONG).show();
            //}
        }

        final android.app.AlertDialog testDialog = builder.create();

        grid.setAdapter(m_actionAdapter);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                testDialog.dismiss();
                if (m_actionAdapter.getPosition(ActionDialogAdapter.PatientOp.RemoveFromXRay) == position) {
                    DeleteFromQueueDialogFragment rtc = new DeleteFromQueueDialogFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(null, rd);
                    rtc.setArguments(args);
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_delete));
                } else if (m_actionAdapter.getPosition(ActionDialogAdapter.PatientOp.DeletePatientFromClinic) == position) {
                    MarkPatientRemovedDialogFragment rtc = new MarkPatientRemovedDialogFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(null, rd);
                    rtc.setArguments(args);
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_delete));
                } else if (m_actionAdapter.getPosition(ActionDialogAdapter.PatientOp.ViewPatientData) == position){
                    PatientSummaryDialogFragment rtc = new PatientSummaryDialogFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(null, rd);
                    rtc.setArguments(args);
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_delete));
                } else if (m_actionAdapter.getPosition(ActionDialogAdapter.PatientOp.EditOldChartId) == position){
                    SetPatientOldIDDialogFragment rtc = new SetPatientOldIDDialogFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(null, rd);
                    rtc.setArguments(args);
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_button_edit_old_id));
                } else if (m_actionAdapter.getPosition(ActionDialogAdapter.PatientOp.SignPatientIn) == position){
                    CheckinDialogFragment rtc = new CheckinDialogFragment();
                    rtc.setPatientId(rd.getId());
                    rtc.setPatientData(rd);
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.question_select_this_patient));
                }
                HideyHelper h = new HideyHelper();
                h.toggleHideyBar(PatientSelectorActivity.this);
            }
        });

        testDialog.show();
        return testDialog;
    }

    /* see also  https://stackoverflow.com/questions/24187728/sticky-immersive-mode-disabled-after-soft-keyboard-shown */
    @Override
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

