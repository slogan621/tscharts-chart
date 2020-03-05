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

package org.thousandsmiles.thousandsmilesstation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ENTDiagnosis;

import java.util.ArrayList;

public class AppPatientENTDiagnosisListFragment extends Fragment {
    private int mColumns;
    private boolean m_goingDown = false;
    private SessionSingleton m_sess;
    private CommonSessionSingleton m_commonSess;
    private ArrayList<ENTDiagnosis> m_entDiagnoses = new ArrayList<ENTDiagnosis>();
    private Activity m_activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            initializeENTDiagnosisData();
        }
    }

    private void clearENTDiagnosisList() {
        m_entDiagnoses.clear();
    }

    private void ClearSearchResultTable()
    {
        TableLayout layout = (TableLayout) m_activity.findViewById(R.id.namestablelayout);

        if (layout != null) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
            }
        }
    }

    private void HideSearchResultTable()
    {
        View v = (View) m_activity.findViewById(R.id.namestablelayout);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    private void ShowSearchResultTable()
    {
        View v = (View) m_activity.findViewById(R.id.namestablelayout);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void showENTDiagnosis(ENTDiagnosis exam)
    {
        Bundle arguments = new Bundle();
        arguments.putSerializable("diagnosis", exam);
        AppENTDiagnosisFragment fragment = new AppENTDiagnosisFragment();
        fragment.setArguments(arguments);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    private void LayoutENTDiagnosisTable() {
        TableLayout layout = (TableLayout) m_activity.findViewById(R.id.namestablelayout);
        TableRow row = null;
        int count;

        ClearSearchResultTable();
        ShowSearchResultTable();

        LinearLayout btnLO = new LinearLayout(m_activity);

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLO.setOrientation(LinearLayout.VERTICAL);

        TableRow.LayoutParams parms = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);

        int leftMargin=10;
        int topMargin=2;
        int rightMargin=10;
        int bottomMargin=2;
        parms.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        parms.gravity = (Gravity.CENTER_VERTICAL);

        btnLO.setLayoutParams(parms);
        ImageButton button = new ImageButton(m_activity);

        btnLO.setBackgroundColor(getResources().getColor(R.color.lightGray));

        button.setBackgroundColor(getResources().getColor(R.color.lightGray));
        button.setImageDrawable(getResources().getDrawable(R.drawable.headshot_plus));

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_activity);
                alertDialogBuilder.setMessage(m_activity.getString(R.string.question_create_new_ent_diagnosis_record));
                alertDialogBuilder.setPositiveButton(R.string.button_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                m_sess.setNewENTDiagnosis(true);
                                showENTDiagnosis(new ENTDiagnosis());
                            }
                        });

                alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        btnLO.addView(button);

        boolean newRow = true;
        row = new TableRow(m_activity);
        row.setWeightSum((float)1.0);

        TextView txt = new TextView(m_activity);
        txt.setText(R.string.button_label_add_new_ent_diagnosis);
        txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        txt.setBackgroundColor(getResources().getColor(R.color.lightGray));
        txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        btnLO.addView(txt);

        row.setLayoutParams(parms);

        if (row != null) {
            row.addView(btnLO);
        }

        if (newRow == true) {
            layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        count = 1;

        int extraCells = (m_entDiagnoses.size() + 1) % 3;
        if (extraCells != 0) {
            extraCells = 3 - extraCells;
        }

        for (int i = 0; i < m_entDiagnoses.size(); i++) {
            newRow = false;
            if ((count % 3) == 0) {
                newRow = true;
                row = new TableRow(m_activity);
                row.setWeightSum((float)1.0);
                row.setLayoutParams(parms);
            }

            btnLO = new LinearLayout(m_activity);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);

            button = new ImageButton(m_activity);

            if (count == 0) {

                btnLO.setBackgroundColor(getResources().getColor(R.color.lightGray));
                button.setBackgroundColor(getResources().getColor(R.color.lightGray));
                button.setImageDrawable(getResources().getDrawable(R.drawable.headshot_plus));

            } else {
                button.setImageDrawable(getResources().getDrawable(R.drawable.xray));
            }

            ENTDiagnosis ENTDiagnosis = m_entDiagnoses.get(i);
            button.setTag(ENTDiagnosis);


            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    m_sess.setNewENTDiagnosis(false);
                    showENTDiagnosis((ENTDiagnosis) v.getTag());
                }
            });

            btnLO.addView(button);

            txt = new TextView(m_activity);
            CommonSessionSingleton sess = CommonSessionSingleton.getInstance();
            sess.setContext(getContext());
            JSONObject o = sess.getClinicById(ENTDiagnosis.getClinic());
            String clinicStr = String.format("Clinic ID %d",ENTDiagnosis.getClinic());

            if (o != null) {
                try {
                    clinicStr = String.format("Clinic ID %d %s %s", ENTDiagnosis.getClinic(),
                            o.getString("location"), o.getString("start"));
                } catch (JSONException e) {
                    clinicStr = String.format("Clinic ID %d",ENTDiagnosis.getClinic());
                }
            }

            txt.setText(clinicStr);
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
            btnLO = new LinearLayout(m_activity);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);
            if (row != null) {
                row.addView(btnLO);
            }
        }
    }

    public static AppPatientENTDiagnosisListFragment newInstance()
    {
        return new AppPatientENTDiagnosisListFragment();
    }

    private void initializeENTDiagnosisData() {

        m_commonSess = CommonSessionSingleton.getInstance();
        m_sess = SessionSingleton.getInstance();

        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        JSONArray ENTDiagnoses;
                        clearENTDiagnosisList();
                        ENTDiagnoses = m_sess.getENTDiagnoses(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                        if (ENTDiagnoses == null) {
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_activity, R.string.msg_unable_to_get_ent_diagnoses_for_patient, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            for (int i = 0; i < ENTDiagnoses.length(); i++) {
                                try {
                                    ENTDiagnosis ENTDiagnosis = new ENTDiagnosis();
                                    JSONObject o = (JSONObject) ENTDiagnoses.get(i);
                                    ENTDiagnosis.fromJSONObject(o);
                                    m_entDiagnoses.add(ENTDiagnosis);
                                    CommonSessionSingleton sess = CommonSessionSingleton.getInstance();
                                    sess.setContext(getContext());
                                    JSONObject co = sess.getClinicById(ENTDiagnosis.getClinic());
                                    if (co == null) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (Exception e) {
                                        }
                                    }
                                } catch (JSONException e) {
                                }
                            }
                        }
                        m_activity.runOnUiThread(new Runnable() {
                            public void run() {
                                LayoutENTDiagnosisTable();
                            }
                        });
                    }
                };
                thread.start();
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }

        super.onPause();

        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_list_fragment_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
