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
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.ENTHistoryExtra;
import org.thousandsmiles.tscharts_lib.ENTHistoryExtraREST;
import org.thousandsmiles.tscharts_lib.ENTHistoryREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;

import java.util.ArrayList;

public class AppENTHistoryFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTHistory m_entHistory = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private AppENTHistoryFragment m_this;

    public static AppENTHistoryFragment newInstance() {
        return new AppENTHistoryFragment();
    }

    void showAddDialog()
    {
        ENTHistoryExtraDialogFragment rtc = new ENTHistoryExtraDialogFragment();
        rtc.setParentActivity(this.getActivity());
        rtc.setAppENTHistoryFragment(this);
        rtc.show(getFragmentManager(), m_activity.getString(R.string.msg_add_extra_exam_item));
    }

    public void disableRemoveButton()
    {
        Button b = (Button) m_activity.findViewById(R.id.remove_button);
        b.setEnabled(false);
    }

    public void enableRemoveButton()
    {
        Button b = (Button) m_activity.findViewById(R.id.remove_button);
        b.setEnabled(true);
    }

    public void updateExtrasView() {
        TableLayout tableLayout = m_activity.findViewById(R.id.extra_container);
        Context context = m_activity.getApplicationContext();
        ArrayList<ENTHistoryExtra> extra = m_sess.getENTHistoryExtraList();

        ((ViewGroup) tableLayout).removeAllViews();

        for (int i = 0; i < extra.size(); i++) {

            ENTHistoryExtra ex = extra.get(i);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            layout.setLayoutParams(llParams);
            CheckBox cb = new CheckBox(context);


            int states[][] = {{android.R.attr.state_checked}, {}};
            int color1 = getResources().getColor(R.color.lightGray);
            int color2 = getResources().getColor(R.color.colorRed);

            int colors[] = {color2, color1};
            CompoundButtonCompat.setButtonTintList(cb, new ColorStateList(states, colors));

            layout.addView(cb);
            cb.setTag((Object) ex);
            cb.setChecked(m_sess.isInENTHistoryExtraDeleteList(ex));

            cb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ENTHistoryExtra extr = (ENTHistoryExtra) v.getTag();
                    if (((CheckBox)v).isChecked()) {
                        m_sess.addENTHistoryExtraToDeleteList(extr);
                        enableRemoveButton();
                    } else {
                        m_sess.removeENTHistoryExtraFromDeleteList(extr);
                        if (m_sess.getENTHistoryExtraDeleteList().size() == 0) {
                            disableRemoveButton();
                        }
                    }
                }
            });

            TextView tv = new TextView(context);
            tv.setText(ex.getName());
            tv.setTextColor(getResources().getColor(R.color.colorBlack));
            layout.addView(tv, llParams);
            tv = new TextView(context);
            String side = ex.getSide();
            side = side.substring(0, 1).toUpperCase() + side.substring(1);
            tv.setText(side);
            tv.setTextColor(getResources().getColor(R.color.colorBlack));
            layout.addView(tv, llParams);
            tv = new TextView(context);
            String duration = ex.getDuration();
            duration = duration.substring(0, 1).toUpperCase() + duration.substring(1);
            tv.setText(duration);
            tv.setTextColor(getResources().getColor(R.color.colorBlack));
            layout.addView(tv, llParams);

            TableRow tr = new TableRow(context);
            /* Create a Button to be the row-content. */

            TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tableRowParams.setMargins(8, 8, 8, 8);

            tr.addView(layout, tableRowParams);

            tableLayout.addView(tr, tableRowParams);
        }
    }

    public void handleAddButtonPress(View v) {
        showAddDialog();
    }

    private void deleteRemovalObject(final ENTHistoryExtra extra) {
        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTHistoryExtraREST rest = new ENTHistoryExtraREST(m_sess.getContext());
                Object lock;
                int status;

                lock = rest.getEntHistoryExtraById(extra.getId());

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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_read_ent_history_extra_data), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    lock = rest.deleteENTHistoryExtra(extra.getId());

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
                                Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_delete_ent_history_extra), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_deleted_ent_history_extra), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        };
        thread.start();
    }

    public void handleRemoveButtonPress(View v)
    {
        // remove all items in removal list from database if present, remove them
        // from the ent history extra list maintained in session singleton,
        // refresh the list, and then clear the removal list

        ArrayList<ENTHistoryExtra> extras = m_sess.getENTHistoryExtraList();
        ArrayList<ENTHistoryExtra> removals = m_sess.getENTHistoryExtraDeleteList();

        for (int i = 0; i < removals.size(); i++) {
            ENTHistoryExtra extra = removals.get(i);
            deleteRemovalObject(extra);
            removals.remove(extra);
            extras.remove(extra);
        }
        updateExtrasView();
        disableRemoveButton();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            m_activity = (Activity) context;
        }
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

    public void setDirty()
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

    private void getENTHistoryExtraDataFromREST(final ENTHistory history)
    {
        m_sess.clearENTExtraHistoryList();
        m_sess.clearENTHistoryExtraDeleteList();

        Thread thread = new Thread(){
            public void run() {
                if (m_sess.getENTExtraHistories(history.getId()) == true) {
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            updateExtrasView();
                        }
                    });

                }
            }
        };
        thread.start();
    }

    class UpdateENTHistoryListener implements RESTCompletionListener {

        private ENTHistory m_hist = null;

        public void setHistory(ENTHistory hist) {
            m_hist = hist;

        }
        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = m_hist.getId();
                m_sess.updateENTHistoryExtra(id);
            } catch (Exception e) {
            }

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {

        }
    }

    class CreateENTHistoryListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = a.getInt("id");
                m_sess.updateENTHistoryExtra(id);
            } catch (Exception e) {
            }

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {

        }
    }

    void updateENTHistory()
    {
        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTHistoryREST rest = new ENTHistoryREST(m_sess.getContext());

                Object lock;
                int status;

                if (m_sess.getNewENTHistory() == true) {
                    CreateENTHistoryListener listener = new CreateENTHistoryListener();
                    rest.addListener(listener);
                    lock = rest.createENTHistory(copyENTHistoryDataFromUI());
                } else {
                    ENTHistory hist = copyENTHistoryDataFromUI();
                    UpdateENTHistoryListener listener = new UpdateENTHistoryListener();
                    listener.setHistory(m_entHistory);
                    rest.addListener(listener);
                    lock = rest.updateENTHistory(hist);
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
        m_this = this;
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
            getENTHistoryExtraDataFromREST(m_entHistory);
        }
        final View removeButton = (Button) m_activity.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                handleRemoveButtonPress(removeButton);
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