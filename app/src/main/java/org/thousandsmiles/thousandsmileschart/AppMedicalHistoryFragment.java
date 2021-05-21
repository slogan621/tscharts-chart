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

package org.thousandsmiles.thousandsmileschart;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.MedicalHistory;
import org.thousandsmiles.tscharts_lib.MedicalHistoryREST;

public class AppMedicalHistoryFragment extends Fragment implements FormSaveListener, PatientCheckoutListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private MedicalHistory m_medicalHistory = null;
    private boolean m_dirty = false;
    private View m_view = null;

    public static AppMedicalHistoryFragment newInstance() {
        return new AppMedicalHistoryFragment();
    }

    private boolean validate() {
        return validateFields();
    }

    @Override
    public void showReturnToClinic()
    {
        ((StationActivity)m_activity).showReturnToClinic();
    }

    private boolean saveInternal(final boolean showReturnToClinic) {
        boolean ret = validate();
        if (ret == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

            builder.setTitle(m_activity.getString(R.string.title_unsaved_medical_history));
            builder.setMessage(m_activity.getString(R.string.msg_save_medical_history));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateMedicalHistory();
                    if (showReturnToClinic == true) {
                        showReturnToClinic();
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (showReturnToClinic == true) {
                        showReturnToClinic();
                    }
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        return ret;
    }

    @Override
    public boolean save() {
        boolean ret = true;
        if (m_dirty) {
            ret = saveInternal(false);
        }
        return ret;
    }

    @Override
    public boolean checkout() {
        if (m_dirty) {
            saveInternal(true);
        } else {
            showReturnToClinic();
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            getMedicalHistoryDataFromREST();
        }
        ((StationActivity)m_activity).subscribeSave(this);
        ((StationActivity)m_activity).subscribeCheckout(this);
    }

    private void copyMedicalHistoryDataToUI()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;

        if (m_medicalHistory != null) {

            // Pregnancy

            sw = (Switch) m_view.findViewById(R.id.mother_alcohol);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isMotherAlcohol());
            }
            sw = (Switch) m_view.findViewById(R.id.pregnancy_smoke);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isPregnancySmoke());
            }
            sw = (Switch) m_view.findViewById(R.id.pregnancy_complications);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isPregnancyComplications());
            }

            tx = (TextView) m_view.findViewById(R.id.pregnancy_duration);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getPregnancyDuration()));
            }

            // Birth

            sw = (Switch) m_view.findViewById(R.id.birth_complications);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isBirthComplications());
            }

            sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isCongenitalHeartDefect());
            }

            sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_workup);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isCongenitalHeartDefectWorkup());
            }

            sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_planforcare);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isCongenitalHeartDefectPlanForCare());
            }

            tx = (TextView) m_view.findViewById(R.id.birth_weight);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getBirthWeight()));
            }

            bv = m_medicalHistory.isBirthWeightMetric();
            rb = (RadioButton) m_view.findViewById(R.id.birth_weight_kg);
            rb.setChecked(bv);
            rb = (RadioButton) m_view.findViewById(R.id.birth_weight_lb);
            rb.setChecked(!bv);

            // Growth Stages

            tx = (TextView) m_view.findViewById(R.id.first_crawl);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getFirstCrawl()));
            }

            tx = (TextView) m_view.findViewById(R.id.first_sit);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getFirstSit()));
            }

            tx = (TextView) m_view.findViewById(R.id.first_words);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getFirstWords()));
            }

            tx = (TextView) m_view.findViewById(R.id.first_walk);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getFirstWalk()));
            }

            // Family History

            sw = (Switch) m_view.findViewById(R.id.parents_cleft);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isParentsCleft());
            }

            sw = (Switch) m_view.findViewById(R.id.siblings_cleft);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isSiblingsCleft());
            }

            sw = (Switch) m_view.findViewById(R.id.relative_cleft);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isRelativeCleft());
            }

            // Current Health

            tx = (TextView) m_view.findViewById(R.id.height);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getHeight()));
            }

            bv = m_medicalHistory.isHeightMetric();
            rb = (RadioButton) m_view.findViewById(R.id.height_cm);
            rb.setChecked(bv);
            rb = (RadioButton) m_view.findViewById(R.id.height_in);
            rb.setChecked(!bv);

            tx = (TextView) m_view.findViewById(R.id.weight);
            if (tx != null) {
                tx.setText(String.format("%d", m_medicalHistory.getWeight()));
            }

            bv = m_medicalHistory.isWeightMetric();
            rb = (RadioButton) m_view.findViewById(R.id.weight_kg);
            rb.setChecked(bv);
            rb = (RadioButton) m_view.findViewById(R.id.weight_lb);
            rb.setChecked(!bv);

            sw = (Switch) m_view.findViewById(R.id.cold_cough_fever);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isColdCoughFever());
            }

            sw = (Switch) m_view.findViewById(R.id.hivaids);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isHivaids());
            }

            sw = (Switch) m_view.findViewById(R.id.anemia);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isAnemia());
            }

            sw = (Switch) m_view.findViewById(R.id.athsma);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isAthsma());
            }

            sw = (Switch) m_view.findViewById(R.id.cancer);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isCancer());
            }

            sw = (Switch) m_view.findViewById(R.id.diabetes);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isDiabetes());
            }

            sw = (Switch) m_view.findViewById(R.id.epilepsy);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isEpilepsy());
            }

            sw = (Switch) m_view.findViewById(R.id.bleeding_problems);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isBleedingProblems());
            }

            sw = (Switch) m_view.findViewById(R.id.hepatitis);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isHepatitis());
            }

            sw = (Switch) m_view.findViewById(R.id.tuberculosis);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isTuberculosis());
            }

            sw = (Switch) m_view.findViewById(R.id.troubleeating);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isTroubleEating());
            }

            sw = (Switch) m_view.findViewById(R.id.troublehearing);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isTroubleHearing());
            }

            sw = (Switch) m_view.findViewById(R.id.troublespeaking);
            if (sw != null) {
                sw.setChecked(m_medicalHistory.isTroubleSpeaking());
            }

            // Medications

            tx = (TextView) m_view.findViewById(R.id.meds);
            if (tx != null) {
                tx.setText(m_medicalHistory.getMeds());
            }

            tx = (TextView) m_view.findViewById(R.id.allergymeds);
            if (tx != null) {
                tx.setText(m_medicalHistory.getAllergyMeds());
            }
        }

        sw = (Switch) m_view.findViewById(R.id.born_with_cleft_lip);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isBornWithCleftLip());
        }

        sw = (Switch) m_view.findViewById(R.id.born_with_cleft_palate);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isBornWithCleftPalate());
        }

        clearDirty();
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        m_dirty = true;
    }

    private void clearDirty() {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        m_dirty = false;
    }

    private void setViewDirtyListeners()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;

        sw = (Switch) m_view.findViewById(R.id.mother_alcohol);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        sw = (Switch) m_view.findViewById(R.id.pregnancy_smoke);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        sw = (Switch) m_view.findViewById(R.id.pregnancy_complications);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        tx = (TextView) m_view.findViewById(R.id.pregnancy_duration);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        // Birth

        sw = (Switch) m_view.findViewById(R.id.birth_complications);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_workup);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_planforcare);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_view.findViewById(R.id.birth_weight);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        rb = (RadioButton) m_view.findViewById(R.id.birth_weight_kg);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.birth_weight_lb);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Growth Stages

        tx = (TextView) m_view.findViewById(R.id.first_crawl);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        tx = (TextView) m_view.findViewById(R.id.first_sit);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        tx = (TextView) m_view.findViewById(R.id.first_words);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        tx = (TextView) m_view.findViewById(R.id.first_walk);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        // Family History

        sw = (Switch) m_view.findViewById(R.id.parents_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.siblings_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.relative_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Current Health

        tx = (TextView) m_view.findViewById(R.id.height);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

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

        rb = (RadioButton) m_view.findViewById(R.id.height_cm);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.height_in);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_view.findViewById(R.id.weight);
        if (tx != null) {

            tx.addTextChangedListener(new TextWatcher() {

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

        rb = (RadioButton) m_view.findViewById(R.id.weight_kg);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.weight_lb);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.cold_cough_fever);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.hivaids);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.anemia);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.athsma);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.cancer);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.diabetes);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.epilepsy);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.bleeding_problems);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.hepatitis);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.tuberculosis);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.troubleeating);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.troublehearing);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.troublespeaking);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Medications

        final TextView tx1 = (TextView) m_view.findViewById(R.id.meds);
        if (tx1 != null) {
            tx1.setShowSoftInputOnFocus(false);
            tx1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        MedicationsListDialogFragment mld = new MedicationsListDialogFragment();
                        mld.setPatientId(m_sess.getDisplayPatientId());
                        mld.setTextField(tx1);
                        mld.show(getFragmentManager(), m_activity.getString(R.string.title_current_medications_dialog));
                    }
                }
            });
            tx1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MedicationsListDialogFragment mld = new MedicationsListDialogFragment();
                    mld.setPatientId(m_sess.getDisplayPatientId());
                    mld.setTextField(tx1);
                    mld.show(getFragmentManager(), m_activity.getString(R.string.title_current_medications_dialog));
                }
            });
            tx1.addTextChangedListener(new TextWatcher() {

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

        final TextView tx2 = (TextView) m_view.findViewById(R.id.allergymeds);
        if (tx2 != null) {
            tx2.setShowSoftInputOnFocus(false);
            tx2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        MedicationsListDialogFragment mld = new MedicationsListDialogFragment();
                        mld.setPatientId(m_sess.getDisplayPatientId());
                        mld.setTextField(tx2);
                        mld.show(getFragmentManager(), m_activity.getString(R.string.title_allergy_medications_dialog));
                    }
                }
            });
            tx2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MedicationsListDialogFragment mld = new MedicationsListDialogFragment();
                    mld.setPatientId(m_sess.getDisplayPatientId());
                    mld.setTextField(tx2);
                    mld.show(getFragmentManager(), m_activity.getString(R.string.title_allergy_medications_dialog));
                }
            });
            tx2.addTextChangedListener(new TextWatcher() {

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

            sw = (Switch) m_view.findViewById(R.id.born_with_cleft_lip);
            if (sw != null) {
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setDirty();
                    }
                });
            }

            sw = (Switch) m_view.findViewById(R.id.born_with_cleft_palate);
            if (sw != null) {
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setDirty();
                    }
                });
            }
        }
    }

    private MedicalHistory copyMedicalHistoryDataFromUI()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;
        boolean checked;

        MedicalHistory mh;

        if (m_medicalHistory == null) {
            mh = new MedicalHistory();
        } else {
            mh = m_medicalHistory;      // copies over clinic, patient ID, etc..
        }

        // Pregnancy

        sw = (Switch) m_view.findViewById(R.id.mother_alcohol);
        if (sw != null) {
            mh.setMotherAlcohol(sw.isChecked());
        }
        sw = (Switch) m_view.findViewById(R.id.pregnancy_smoke);
        if (sw != null) {
            mh.setPregnancySmoke(sw.isChecked());
        }
        sw = (Switch) m_view.findViewById(R.id.pregnancy_complications);
        if (sw != null) {
            mh.setPregnancyComplications(sw.isChecked());
        }
        tx = (TextView) m_view.findViewById(R.id.pregnancy_duration);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setPregnancyDuration(val);
            } catch (Exception e) {
            }
        }

        // Birth

        sw = (Switch) m_view.findViewById(R.id.birth_complications);
        if (sw != null) {
            mh.setBirthComplications(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect);
        if (sw != null) {
            mh.setCongenitalHeartDefect(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_workup);
        if (sw != null) {
            mh.setCongenitalHeartDefectWorkup(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.congenitalheartdefect_planforcare);
        if (sw != null) {
            mh.setCongenitalHeartDefectPlanForCare(sw.isChecked());
        }

        tx = (TextView) m_view.findViewById(R.id.birth_weight);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setBirthWeight(val);
            } catch (Exception e) {
            }
        }

        rb = (RadioButton) m_view.findViewById(R.id.birth_weight_kg);
        if (rb != null) {
            mh.setBirthWeightMetric(rb.isChecked());
        }

        // Growth Stages

        tx = (TextView) m_view.findViewById(R.id.first_crawl);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setFirstCrawl(val);
            } catch (Exception e) {
            }
        }

        tx = (TextView) m_view.findViewById(R.id.first_sit);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setFirstSit(val);
            } catch (Exception e) {
            }
        }

        tx = (TextView) m_view.findViewById(R.id.first_words);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setFirstWords(val);
            } catch (Exception e) {
            }
        }

        tx = (TextView) m_view.findViewById(R.id.first_walk);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setFirstWalk(val);
            } catch (Exception e) {
            }
        }

        // Family History

        sw = (Switch) m_view.findViewById(R.id.parents_cleft);
        if (sw != null) {
            mh.setParentsCleft(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.siblings_cleft);
        if (sw != null) {
            mh.setSiblingsCleft(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.relative_cleft);
        if (sw != null) {
            mh.setRelativeCleft(sw.isChecked());
        }

        // Current Health

        tx = (TextView) m_view.findViewById(R.id.height);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setHeight(val);
            } catch (Exception e) {
            }
        }

        rb = (RadioButton) m_view.findViewById(R.id.height_cm);
        if (rb != null) {
            mh.setHeightMetric(rb.isChecked());
        }

        rb = (RadioButton) m_view.findViewById(R.id.height_in);
        if (rb != null) {
            mh.setHeightMetric(!rb.isChecked());
        }

        tx = (TextView) m_view.findViewById(R.id.weight);
        if (tx != null) {
            int val;
            try {
                val = Integer.parseInt(tx.getText().toString());
                mh.setWeight(val);
            } catch (Exception e) {
            }
        }

        rb = (RadioButton) m_view.findViewById(R.id.weight_kg);
        if (rb != null) {
            mh.setWeightMetric(rb.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.cold_cough_fever);
        if (sw != null) {
            mh.setColdCoughFever(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.hivaids);
        if (sw != null) {
            mh.setHivaids(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.anemia);
        if (sw != null) {
            mh.setAnemia(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.athsma);
        if (sw != null) {
            mh.setAthsma(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.cancer);
        if (sw != null) {
            mh.setCancer(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.diabetes);
        if (sw != null) {
            mh.setDiabetes(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.epilepsy);
        if (sw != null) {
            mh.setEpilepsy(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.bleeding_problems);
        if (sw != null) {
            mh.setBleedingProblems(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.hepatitis);
        if (sw != null) {
            mh.setHepatitis(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.tuberculosis);
        if (sw != null) {
            mh.setTuberculosis(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.troubleeating);
        if (sw != null) {
            mh.setTroubleEating(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.troublehearing);
        if (sw != null) {
            mh.setTroubleHearing(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.troublespeaking);
        if (sw != null) {
            mh.setTroubleSpeaking(sw.isChecked());
        }

        // Medications

        tx = (TextView) m_view.findViewById(R.id.meds);
        if (tx != null) {
            mh.setMeds(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.allergymeds);
        if (tx != null) {
            mh.setAllergyMeds(tx.getText().toString());
        }

        sw = (Switch) m_view.findViewById(R.id.born_with_cleft_lip);
        if (sw != null) {
            mh.setBornWithCleftLip(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.born_with_cleft_palate);
        if (sw != null) {
            mh.setBornWithCleftPalate(sw.isChecked());
        }
        return mh;
    }

    private boolean validateFields()
    {
        boolean ret = true;
        TextView tx1;
        RadioButton rb;
        int minVal = 0;
        int maxVal = 9999;
        int val = 0;

        tx1 = (TextView)m_view.findViewById(R.id.pregnancy_duration);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            if (val < 8 || val > 10) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_pregnancy_range));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_pregnancy_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.birth_weight);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            rb = (RadioButton) m_view.findViewById(R.id.birth_weight_kg);
            if (rb != null) {
                if (rb.isChecked()) {
                    minVal = 2;
                    maxVal = 5;
                } else {
                    minVal = 5;
                    maxVal = 11;
                }
            }

            if (val < minVal || val > maxVal) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_birth_weight_range));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_birth_weight_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.first_crawl);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            if (val < 4 || val > 18) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_first_crawl_range));
            }
        } catch (NumberFormatException e) {
            tx1.setError(m_activity.getString(R.string.msg_invalid_first_crawl_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.first_sit);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            if (val < 4 || val > 18) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_first_sit_range));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_first_sit_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.first_walk);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            if (val < 8 || val > 18) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_first_walk_range));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_first_walk_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.first_words);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());
            if (val < 8 || val > 18)
            {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_first_words_range));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_first_words_range));
        }

        tx1 = (TextView)m_view.findViewById(R.id.height);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            rb = (RadioButton) m_view.findViewById(R.id.height_cm);
            if (rb != null) {
                if (rb.isChecked()) {
                    minVal = 0;
                    maxVal = 213;
                } else {
                    minVal = 0;
                    maxVal = 84;
                }
            }

            if (val < minVal || val > maxVal) {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_height));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_height));
        }

        rb = (RadioButton) m_view.findViewById(R.id.weight_kg);
        if (rb != null) {
            if (rb.isChecked()) {
                minVal = 0;
                maxVal = 136;
            } else {
                minVal = 0;
                maxVal = 300;
            }
        }

        tx1 = (TextView)m_view.findViewById(R.id.weight);
        tx1.setError(null);
        try {
            val = Integer.parseInt(tx1.getText().toString());

            if (val < minVal || val > maxVal)
            {
                ret = false;
                tx1.setError(m_activity.getString(R.string.msg_invalid_weight));
            }
        } catch (NumberFormatException e) {
            ret = false;
            tx1.setError(m_activity.getString(R.string.msg_invalid_weight));
        }

        if (ret == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_missing_patient_data));
            builder.setMessage(m_activity.getString(R.string.msg_correct_missing_or_invalid_data));

            builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        return ret;
    }

    private void getMedicalHistoryDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        m_sess.setNewMedHistory(false);
        new Thread(new Runnable() {
            public void run() {
            Thread thread = new Thread(){
                public void run() {
                MedicalHistory hist;
                hist = m_sess.getMedicalHistory(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                if (hist == null) {
                    m_medicalHistory = new MedicalHistory(); // null ??
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_medical_history), Toast.LENGTH_SHORT).show();
                            copyMedicalHistoryDataToUI(); // remove if null
                            setViewDirtyListeners();      // remove if null
                            m_sess.setNewMedHistory(true);
                        }
                    });

                } else {
                    m_medicalHistory = hist;
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_got_medical_history), Toast.LENGTH_SHORT).show();
                            copyMedicalHistoryDataToUI();
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

    void updateMedicalHistory()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                MedicalHistoryREST rest = new MedicalHistoryREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewMedHistory() == true) {
                    lock = rest.createMedicalHistory(copyMedicalHistoryDataFromUI());
                    m_sess.setNewMedHistory(false);
                } else {
                    lock = rest.updateMedicalHistory(copyMedicalHistoryDataFromUI());
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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_medicalHistory = copyMedicalHistoryDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
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
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        ((StationActivity) m_activity).unsubscribeSave(this);
        ((StationActivity) m_activity).unsubscribeCheckout(this);

        super.onPause();
    }

    private void setButtonBarCallbacks() {
        View button_bar_item;

        button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveInternal(false);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_medical_history_layout, container, false);
        m_view  = view;
        setButtonBarCallbacks();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}