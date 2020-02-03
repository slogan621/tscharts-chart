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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.ENTHistoryREST;

public class AppENTHistoryFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTHistory m_entHistory = null;
    private boolean m_dirty = false;
    private View m_view = null;

    public static AppENTHistoryFragment newInstance() {
        return new AppENTHistoryFragment();
    }

    void showAddDialog()
    {
        ENTHistoryExtraDialogFragment rtc = new ENTHistoryExtraDialogFragment();
        rtc.setParentActivity(this.getActivity());
        rtc.show(getFragmentManager(), m_activity.getString(R.string.msg_add_extra_exam_item));
    }

    public void handleAddButtonPress(View v) {
        showAddDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
        }
        m_sess.clearENTExtraHistoryList();
    }

    private void copyENTHistoryDataToUI()
    {
        CheckBox cb1, cb2, cb3;
        TextView tx;
        RadioButton rb1, rb2, rb3, rb4, rb5;

        if (m_entHistory != null) {

            // Pain side

            ENTHistory.EarSide side;
            ENTHistory.ENTDuration duration;

            side = m_entHistory.getPainSide();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            // Drainage side

            side = m_entHistory.getDrainageSide();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            // Hearing Loss side

            side = m_entHistory.getHearingLossSide();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            // Pain duration

            duration = m_entHistory.getPainDuration();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_none);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_days);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_weeks);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_months);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_intermittent);

            rb1.setChecked(true);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);

            switch (duration) {
                case EAR_DURATION_NONE:
                    rb1.setChecked(true);
                    break;
                case EAR_DURATION_DAYS:
                    rb2.setChecked(true);
                    break;
                case EAR_DURATION_WEEKS:
                    rb3.setChecked(true);
                    break;
                case EAR_DURATION_MONTHS:
                    rb4.setChecked(true);
                    break;
                case EAR_DURATION_INTERMITTENT:
                    rb5.setChecked(true);
                    break;
            }

            // Drainage duration

            duration = m_entHistory.getDrainageDuration();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_none);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_days);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_weeks);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_months);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_intermittent);


            rb1.setChecked(true);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);

            switch (duration) {
                case EAR_DURATION_NONE:
                    rb1.setChecked(true);
                    break;
                case EAR_DURATION_DAYS:
                    rb2.setChecked(true);
                    break;
                case EAR_DURATION_WEEKS:
                    rb3.setChecked(true);
                    break;
                case EAR_DURATION_MONTHS:
                    rb4.setChecked(true);
                    break;
                case EAR_DURATION_INTERMITTENT:
                    rb5.setChecked(true);
                    break;
            }

            // Hearing loss duration

            duration = m_entHistory.getHearingLossDuration();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_none);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_days);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_weeks);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_months);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_intermittent);


            rb1.setChecked(true);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);

            switch (duration) {
                case EAR_DURATION_NONE:
                    rb1.setChecked(true);
                    break;
                case EAR_DURATION_DAYS:
                    rb2.setChecked(true);
                    break;
                case EAR_DURATION_WEEKS:
                    rb3.setChecked(true);
                    break;
                case EAR_DURATION_MONTHS:
                    rb4.setChecked(true);
                    break;
                case EAR_DURATION_INTERMITTENT:
                    rb5.setChecked(true);
                    break;
            }

            String notes = m_entHistory.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

            t.setText(notes);

        }
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        m_entHistory = copyENTHistoryDataFromUI();
        button_bar_item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean valid = validateFields();
                if (valid == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(m_activity.getString(R.string.title_missing_patient_data));
                    builder.setMessage(m_activity.getString(R.string.msg_please_enter_required_patient_data));

                    builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    updateENTHistory();
                }
            }

        });
        m_dirty = true;
    }

    private void clearDirty() {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        m_dirty = false;
    }

    private void setViewDirtyListeners()
    {
        CheckBox cb;
        RadioButton rb;

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_days);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_weeks);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_months);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_intermittent);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_days);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_weeks);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_months);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_intermittent);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_days);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_weeks);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_months);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_intermittent);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);
        if (t != null) {
            t.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }
    }

    private ENTHistory copyENTHistoryDataFromUI()
    {
        CheckBox cb1, cb2;
        TextView tx;
        RadioButton rb;

        ENTHistory mh = null;

        if (m_entHistory == null) {
            mh = new ENTHistory();
        } else {
            mh = m_entHistory;      // copies over clinic, patient ID, etc..
        }

        mh.setPatient(m_sess.getActivePatientId());
        mh.setClinic(m_sess.getClinicId());
        mh.setUsername("nobody");


        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_pain_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setPainSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setPainSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setPainSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setPainSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_drainage_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setDrainageSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setDrainageSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setDrainageSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setDrainageSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_hearing_loss_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setHearingLossSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setHearingLossSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setHearingLossSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setHearingLossSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_none);
        if (rb.isChecked()) {
            mh.setPainDuration(ENTHistory.ENTDuration.EAR_DURATION_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_days);
        if (rb.isChecked()) {
            mh.setPainDuration(ENTHistory.ENTDuration.EAR_DURATION_DAYS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_weeks);
        if (rb.isChecked()) {
            mh.setPainDuration(ENTHistory.ENTDuration.EAR_DURATION_WEEKS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_months);
        if (rb.isChecked()) {
            mh.setPainDuration(ENTHistory.ENTDuration.EAR_DURATION_MONTHS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_pain_duration_intermittent);
        if (rb.isChecked()) {
            mh.setPainDuration(ENTHistory.ENTDuration.EAR_DURATION_INTERMITTENT);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_none);
        if (rb.isChecked()) {
            mh.setDrainageDuration(ENTHistory.ENTDuration.EAR_DURATION_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_days);
        if (rb.isChecked()) {
            mh.setDrainageDuration(ENTHistory.ENTDuration.EAR_DURATION_DAYS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_weeks);
        if (rb.isChecked()) {
            mh.setDrainageDuration(ENTHistory.ENTDuration.EAR_DURATION_WEEKS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_months);
        if (rb.isChecked()) {
            mh.setDrainageDuration(ENTHistory.ENTDuration.EAR_DURATION_MONTHS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_drainage_duration_intermittent);
        if (rb.isChecked()) {
            mh.setDrainageDuration(ENTHistory.ENTDuration.EAR_DURATION_INTERMITTENT);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_none);
        if (rb.isChecked()) {
            mh.setHearingLossDuration(ENTHistory.ENTDuration.EAR_DURATION_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_days);
        if (rb.isChecked()) {
            mh.setHearingLossDuration(ENTHistory.ENTDuration.EAR_DURATION_DAYS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_weeks);
        if (rb.isChecked()) {
            mh.setHearingLossDuration(ENTHistory.ENTDuration.EAR_DURATION_WEEKS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_months);
        if (rb.isChecked()) {
            mh.setHearingLossDuration(ENTHistory.ENTDuration.EAR_DURATION_MONTHS);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_hearing_loss_duration_intermittent);
        if (rb.isChecked()) {
            mh.setHearingLossDuration(ENTHistory.ENTDuration.EAR_DURATION_INTERMITTENT);
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

        Editable text = t.getText();

        mh.setComment(text.toString());

        return mh;
    }

    private boolean validateFields()
    {
        boolean ret = true;
        return ret;
    }

    private void getENTHistoryDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        m_sess.setNewENTHistory(false);
        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        ENTHistory history;
                        history = m_sess.getENTHistory(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                        if (history == null) {
                            m_entHistory = new ENTHistory(); // null ??
                            m_entHistory.setPatient(m_sess.getActivePatientId());
                            m_entHistory.setClinic(m_sess.getClinicId());
                            m_entHistory.setUsername("nobody");
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_history_data), Toast.LENGTH_SHORT).show();
                                    copyENTHistoryDataToUI(); // remove if null
                                    setViewDirtyListeners();      // remove if null
                                }
                            });

                        } else {
                            m_entHistory = history;
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_got_ent_history_data), Toast.LENGTH_SHORT).show();
                                    copyENTHistoryDataToUI();
                                    setViewDirtyListeners();

                                }
                            });
                        }
                    }
                };
                thread.start();
            }
        }).start();
    }

    void updateENTHistory()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTHistoryREST rest = new ENTHistoryREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewENTHistory() == true) {
                    lock = rest.createENTHistory(copyENTHistoryDataFromUI());
                } else {
                    lock = rest.updateENTHistory(copyENTHistoryDataFromUI());
                }

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
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_ent_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_entHistory = copyENTHistoryDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_ent_history), Toast.LENGTH_LONG).show();
                            m_sess.setNewENTHistory(false);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        try {
            m_entHistory = (ENTHistory) bundle.getSerializable("history");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_history_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        copyENTHistoryDataToUI();
        setViewDirtyListeners();
        final View addButton = m_activity.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                handleAddButtonPress(addButton);
            }
        });
        if (m_sess.getNewENTHistory() == true) {
            setDirty();
        } else {
            clearDirty();
        }
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

        final ENTHistory mh = this.copyENTHistoryDataFromUI();

        if (m_dirty || mh.equals(m_entHistory) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_ent_history));
            builder.setMessage(m_activity.getString(R.string.msg_save_ent_history));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    m_sess.getCommonSessionSingleton().updatePatientENTHistory(mh);
                    m_sess.updateENTHistory();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_ent_history_layout, container, false);
        m_view  = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
