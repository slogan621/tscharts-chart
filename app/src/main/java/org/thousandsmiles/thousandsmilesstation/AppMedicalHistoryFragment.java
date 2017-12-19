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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AppMedicalHistoryFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private MedicalHistory m_medicalHistory;
    private boolean m_dirty = false;

    public static AppMedicalHistoryFragment newInstance() {
        return new AppMedicalHistoryFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            getMedicalHistoryDataFromREST();
        }
    }

    private void copyMedicalHistoryDataToUI()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;

        // Pregnancy

        sw = (Switch) m_activity.findViewById(R.id.mother_alcohol);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isMotherAlcohol());
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_smoke);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isPregnancySmoke());
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_complications);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isPregnancyComplications());
        }

        tx = (TextView) m_activity.findViewById(R.id.pregnancy_duration);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getPregnancyDuration()));
        }

        // Birth

        sw = (Switch) m_activity.findViewById(R.id.birth_complications);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isBirthComplications());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isCongenitalHeartDefect());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_workup);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isCongenitalHeartDefectWorkup());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_planforcare);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isCongenitalHeartDefectPlanForCare());
        }

        tx = (TextView) m_activity.findViewById(R.id.birth_weight);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getBirthWeight()));
        }

        bv = m_medicalHistory.isBirthWeightMetric();
        rb = (RadioButton) m_activity.findViewById(R.id.birth_weight_kg);
        rb.setChecked(bv);
        rb = (RadioButton) m_activity.findViewById(R.id.birth_weight_lb);
        rb.setChecked(!bv);

        // Growth Stages

        tx = (TextView) m_activity.findViewById(R.id.first_crawl);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getFirstCrawl()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_sit);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getFirstSit()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_words);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getFirstWords()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_walk);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getFirstWalk()));
        }

        // Family History

        sw = (Switch) m_activity.findViewById(R.id.parents_cleft);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isParentsCleft());
        }

        sw = (Switch) m_activity.findViewById(R.id.siblings_cleft);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isSiblingsCleft());
        }

        sw = (Switch) m_activity.findViewById(R.id.relative_cleft);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isRelativeCleft());
        }

        // Current Health

        tx = (TextView) m_activity.findViewById(R.id.height);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getHeight()));
        }

        bv = m_medicalHistory.isHeightMetric();
        rb = (RadioButton) m_activity.findViewById(R.id.height_cm);
        rb.setChecked(bv);
        rb = (RadioButton) m_activity.findViewById(R.id.height_in);
        rb.setChecked(!bv);

        tx = (TextView) m_activity.findViewById(R.id.weight);
        if (tx != null) {
            tx.setText(String.format("%d", m_medicalHistory.getWeight()));
        }

        bv = m_medicalHistory.isWeightMetric();
        rb = (RadioButton) m_activity.findViewById(R.id.weight_kg);
        rb.setChecked(bv);
        rb = (RadioButton) m_activity.findViewById(R.id.weight_lb);
        rb.setChecked(!bv);

        sw = (Switch) m_activity.findViewById(R.id.cold_cough_fever);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isColdCoughFever());
        }

        sw = (Switch) m_activity.findViewById(R.id.hivaids);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isHivaids());
        }

        sw = (Switch) m_activity.findViewById(R.id.anemia);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isAnemia());
        }

        sw = (Switch) m_activity.findViewById(R.id.athsma);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isAthsma());
        }

        sw = (Switch) m_activity.findViewById(R.id.cancer);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isCancer());
        }

        sw = (Switch) m_activity.findViewById(R.id.diabetes);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isDiabetes());
        }

        sw = (Switch) m_activity.findViewById(R.id.epilepsy);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isEpilepsy());
        }

        sw = (Switch) m_activity.findViewById(R.id.bleeding_problems);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isBleedingProblems());
        }

        sw = (Switch) m_activity.findViewById(R.id.hepititis);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isHepititis());
        }

        sw = (Switch) m_activity.findViewById(R.id.tuberculosis);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isTuberculosis());
        }

        sw = (Switch) m_activity.findViewById(R.id.troubleeating);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isTroubleEating());
        }

        sw = (Switch) m_activity.findViewById(R.id.troublehearing);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isTroubleHearing());
        }

        sw = (Switch) m_activity.findViewById(R.id.troublespeaking);
        if (sw != null) {
            sw.setChecked(m_medicalHistory.isTroubleSpeaking());
        }

        // Medications

        tx = (TextView) m_activity.findViewById(R.id.meds);
        if (tx != null) {
            tx.setText(m_medicalHistory.getMeds());
        }

        tx = (TextView) m_activity.findViewById(R.id.allergymeds);
        if (tx != null) {
            tx.setText(m_medicalHistory.getAllergyMeds());
        }
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        button_bar_item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            updateMedicalHistory();
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
        Switch sw;
        TextView tx;
        RadioButton rb;

        sw = (Switch) m_activity.findViewById(R.id.mother_alcohol);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_smoke);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_complications);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        tx = (TextView) m_activity.findViewById(R.id.pregnancy_duration);
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

        sw = (Switch) m_activity.findViewById(R.id.birth_complications);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_workup);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_planforcare);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.birth_weight);
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

        rb = (RadioButton) m_activity.findViewById(R.id.birth_weight_kg);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_activity.findViewById(R.id.birth_weight_lb);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Growth Stages

        tx = (TextView) m_activity.findViewById(R.id.first_crawl);
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

        tx = (TextView) m_activity.findViewById(R.id.first_sit);
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

        tx = (TextView) m_activity.findViewById(R.id.first_words);
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

        tx = (TextView) m_activity.findViewById(R.id.first_walk);
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

        sw = (Switch) m_activity.findViewById(R.id.parents_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.siblings_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.relative_cleft);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Current Health

        tx = (TextView) m_activity.findViewById(R.id.height);
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

        rb = (RadioButton) m_activity.findViewById(R.id.height_cm);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_activity.findViewById(R.id.height_in);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.weight);
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

        rb = (RadioButton) m_activity.findViewById(R.id.weight_kg);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_activity.findViewById(R.id.weight_lb);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.cold_cough_fever);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.hivaids);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.anemia);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.athsma);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.cancer);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.diabetes);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.epilepsy);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.bleeding_problems);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.hepititis);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.tuberculosis);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.troubleeating);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.troublehearing);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_activity.findViewById(R.id.troublespeaking);
        if (sw != null) {
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Medications

        tx = (TextView) m_activity.findViewById(R.id.meds);
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

        tx = (TextView) m_activity.findViewById(R.id.allergymeds);
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
    }

    private MedicalHistory copyMedicalHistoryDataFromUI()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;
        boolean checked;

        MedicalHistory mh = new MedicalHistory();

        mh = m_medicalHistory;

        // Pregnancy

        sw = (Switch) m_activity.findViewById(R.id.mother_alcohol);
        if (sw != null) {
            mh.setMotherAlcohol(sw.isChecked());
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_smoke);
        if (sw != null) {
            mh.setPregnancySmoke(sw.isChecked());
        }
        sw = (Switch) m_activity.findViewById(R.id.pregnancy_complications);
        if (sw != null) {
            mh.setPregnancyComplications(sw.isChecked());
        }
        tx = (TextView) m_activity.findViewById(R.id.pregnancy_duration);
        if (tx != null) {
            mh.setPregnancyDuration(Integer.parseInt(tx.getText().toString()));
        }

        // Birth

        sw = (Switch) m_activity.findViewById(R.id.birth_complications);
        if (sw != null) {
            mh.setBirthComplications(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect);
        if (sw != null) {
            mh.setCongenitalHeartDefect(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_workup);
        if (sw != null) {
            mh.setCongenitalHeartDefectWorkup(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.congenitalheartdefect_planforcare);
        if (sw != null) {
            mh.setCongenitalHeartDefectPlanForCare(sw.isChecked());
        }

        tx = (TextView) m_activity.findViewById(R.id.birth_weight);
        if (tx != null) {
            mh.setBirthWeight(Integer.parseInt(tx.getText().toString()));
        }

        rb = (RadioButton) m_activity.findViewById(R.id.birth_weight_kg);
        if (rb != null) {
            mh.setBirthWeightMetric(rb.isChecked());
        }

        // Growth Stages

        tx = (TextView) m_activity.findViewById(R.id.first_crawl);
        if (tx != null) {
            mh.setFirstCrawl(Integer.parseInt(tx.getText().toString()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_sit);
        if (tx != null) {
            mh.setFirstSit(Integer.parseInt(tx.getText().toString()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_words);
        if (tx != null) {
            mh.setFirstWords(Integer.parseInt(tx.getText().toString()));
        }

        tx = (TextView) m_activity.findViewById(R.id.first_walk);
        if (tx != null) {
            mh.setFirstWalk(Integer.parseInt(tx.getText().toString()));
        }

        // Family History

        sw = (Switch) m_activity.findViewById(R.id.parents_cleft);
        if (sw != null) {
            mh.setParentsCleft(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.siblings_cleft);
        if (sw != null) {
            mh.setSiblingsCleft(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.relative_cleft);
        if (sw != null) {
            mh.setRelativeCleft(sw.isChecked());
        }

        // Current Health

        tx = (TextView) m_activity.findViewById(R.id.height);
        if (tx != null) {
            mh.setHeight(Integer.parseInt(tx.getText().toString()));
        }

        rb = (RadioButton) m_activity.findViewById(R.id.height_cm);
        if (rb != null) {
            mh.setHeightMetric(rb.isChecked());
        }

        rb = (RadioButton) m_activity.findViewById(R.id.height_in);
        if (rb != null) {
            mh.setHeightMetric(rb.isChecked());
        }

        tx = (TextView) m_activity.findViewById(R.id.weight);
        if (tx != null) {
            mh.setWeight(Integer.parseInt(tx.getText().toString()));
        }

        rb = (RadioButton) m_activity.findViewById(R.id.weight_kg);
        if (rb != null) {
            mh.setWeightMetric(rb.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.cold_cough_fever);
        if (sw != null) {
            mh.setColdCoughFever(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.hivaids);
        if (sw != null) {
            mh.setHivaids(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.anemia);
        if (sw != null) {
            mh.setAnemia(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.athsma);
        if (sw != null) {
            mh.setAthsma(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.cancer);
        if (sw != null) {
            mh.setCancer(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.diabetes);
        if (sw != null) {
            mh.setDiabetes(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.epilepsy);
        if (sw != null) {
            mh.setEpilepsy(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.bleeding_problems);
        if (sw != null) {
            mh.setBleedingProblems(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.hepititis);
        if (sw != null) {
            mh.setHepititis(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.tuberculosis);
        if (sw != null) {
            mh.setTuberculosis(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.troubleeating);
        if (sw != null) {
            mh.setTroubleEating(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.troublehearing);
        if (sw != null) {
            mh.setTroubleHearing(sw.isChecked());
        }

        sw = (Switch) m_activity.findViewById(R.id.troublespeaking);
        if (sw != null) {
            mh.setTroubleSpeaking(sw.isChecked());
        }

        // Medications

        tx = (TextView) m_activity.findViewById(R.id.meds);
        if (tx != null) {
            mh.setMeds(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.allergymeds);
        if (tx != null) {
            mh.setAllergyMeds(tx.getText().toString());
        }
        return mh;
    }

    private void getMedicalHistoryDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        new Thread(new Runnable() {
            public void run() {
            Thread thread = new Thread(){
                public void run() {
                m_medicalHistory = m_sess.getMedicalHistory(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                if (m_medicalHistory == null) {
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, "Unable to get medical history data", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, "Successfully got medical history data", Toast.LENGTH_SHORT).show();
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

                lock = rest.updateMedicalHistory(copyMedicalHistoryDataFromUI());

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
                            Toast.makeText(m_activity, "Unable to save medical history", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_medicalHistory = copyMedicalHistoryDataFromUI();
                            Toast.makeText(m_activity, "Successfully saved medical history", Toast.LENGTH_LONG).show();
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
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }

        super.onPause();

        MedicalHistory mh = this.copyMedicalHistoryDataFromUI();

        if (m_dirty || mh.equals(m_medicalHistory) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Unsaved Changes to Medical History");
            builder.setMessage("Save medical history changes?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
        View view = inflater.inflate(R.layout.app_medical_history_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}