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

import org.thousandsmiles.tscharts_lib.ENTExam;
import org.thousandsmiles.tscharts_lib.ENTExamREST;
import org.thousandsmiles.tscharts_lib.ENTHistory;

public class AppENTExamFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTExam m_entExam = null;
    private boolean m_dirty = false;
    private View m_view = null;

    public static AppENTExamFragment newInstance() {
        return new AppENTExamFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
        }
    }

    private void copyENTExamDataToUI()
    {
        CheckBox cb1, cb2, cb3;
        TextView tx;
        RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8;

        if (m_entExam != null) {

            // Ears

            ENTHistory.EarSide side;

            side = m_entExam.getNormal();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);

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

            side = m_entExam.getMicrotia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);

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

            side = m_entExam.getWax();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);

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

            side = m_entExam.getDrainage();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);

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

            side = m_entExam.getExternalOtitis();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);

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

            side = m_entExam.getFb();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);

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

            // tubes

            ENTExam.ENTTube tube;

            tube = m_entExam.getTubeLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (tube) {
                case ENT_TUBE_IN_PLACE:
                    rb1.setChecked(true);
                    break;
                case ENT_TUBE_EXTRUDING:
                    rb2.setChecked(true);
                    break;
                case ENT_TUBE_IN_CANAL:
                    rb3.setChecked(true);
                    break;
                case ENT_TUBE_NONE:
                    rb4.setChecked(true);
                    break;
            }

            tube = m_entExam.getTubeRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (tube) {
                case ENT_TUBE_IN_PLACE:
                    rb1.setChecked(true);
                    break;
                case ENT_TUBE_EXTRUDING:
                    rb2.setChecked(true);
                    break;
                case ENT_TUBE_IN_CANAL:
                    rb3.setChecked(true);
                    break;
                case ENT_TUBE_NONE:
                    rb4.setChecked(true);
                    break;
            }

            // Tympanosclerosis

            ENTExam.ENTTympano tympano;

            tympano = m_entExam.getTympanoLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);

            switch (tympano) {
                case ENT_TYMPANOSCLEROSIS_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_25:
                    rb3.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_50:
                    rb4.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_75:
                    rb5.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_TOTAL:
                    rb6.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_NONE:
                    rb7.setChecked(true);
                    break;
            }

            tympano = m_entExam.getTympanoRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);

            switch (tympano) {
                case ENT_TYMPANOSCLEROSIS_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_25:
                    rb3.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_50:
                    rb4.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_75:
                    rb5.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_TOTAL:
                    rb6.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_NONE:
                    rb7.setChecked(true);
                    break;
            }

            // TM

            side = m_entExam.getTmGranulations();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);

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

            side = m_entExam.getTmRetraction();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);

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

            side = m_entExam.getTmAtelectasis();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);

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

            // Perforations

            ENTExam.ENTPerf perf;

            perf = m_entExam.getPerfLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);
            rb8 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);
            rb8.setChecked(false);

            switch (perf) {
                case ENT_PERF_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_PERF_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_PERF_MARGINAL:
                    rb3.setChecked(true);
                    break;
                case ENT_PERF_25:
                    rb4.setChecked(true);
                    break;
                case ENT_PERF_50:
                    rb5.setChecked(true);
                    break;
                case ENT_PERF_75:
                    rb6.setChecked(true);
                    break;
                case ENT_PERF_TOTAL:
                    rb7.setChecked(true);
                    break;
                case ENT_PERF_NONE:
                    rb8.setChecked(true);
                    break;
            }

            perf = m_entExam.getPerfRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);
            rb8 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);
            rb8.setChecked(false);

            switch (perf) {
                case ENT_PERF_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_PERF_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_PERF_MARGINAL:
                    rb3.setChecked(true);
                    break;
                case ENT_PERF_25:
                    rb4.setChecked(true);
                    break;
                case ENT_PERF_50:
                    rb5.setChecked(true);
                    break;
                case ENT_PERF_75:
                    rb6.setChecked(true);
                    break;
                case ENT_PERF_TOTAL:
                    rb7.setChecked(true);
                    break;
                case ENT_PERF_NONE:
                    rb8.setChecked(true);
                    break;
            }

            // Hearing loss

            ENTExam.ENTVoiceTest voice;

            voice = m_entExam.getVoiceTest();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (voice) {
                case ENT_VOICE_TEST_NORMAL:
                    rb1.setChecked(true);
                    break;
                case ENT_VOICE_TEST_ABNORMAL:
                    rb2.setChecked(true);
                    break;
                case ENT_VOICE_TEST_NONE:
                    rb3.setChecked(true);
                    break;
            }

            ENTExam.ENTForkTest forkTest;

            forkTest = m_entExam.getForkAD();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (forkTest) {
                case ENT_FORK_TEST_A_GREATER_B:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_TEST_B_GREATER_A:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_TEST_EQUAL:
                    rb3.setChecked(true);
                    break;
                case ENT_FORK_TEST_NONE:
                    rb4.setChecked(true);
                    break;
            }

            forkTest = m_entExam.getForkAS();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (forkTest) {
                case ENT_FORK_TEST_A_GREATER_B:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_TEST_B_GREATER_A:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_TEST_EQUAL:
                    rb3.setChecked(true);
                    break;
                case ENT_FORK_TEST_NONE:
                    rb4.setChecked(true);
                    break;
            }

            ENTExam.ENTBC bc = m_entExam.getBc();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);

            switch (bc) {
                case ENT_BC_AD_LAT_TO_AD:
                    rb1.setChecked(true);
                    break;
                case ENT_BC_AD_LAT_TO_AS:
                    rb2.setChecked(true);
                    break;
                case ENT_BC_AS_LAT_TO_AD:
                    rb3.setChecked(true);
                    break;
                case ENT_BC_AS_LAT_TO_AS:
                    rb4.setChecked(true);
                    break;
                case ENT_BC_NONE:
                    rb5.setChecked(true);
                    break;
            }

            ENTExam.ENTFork fork;

            fork = m_entExam.getFork();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (fork) {
                case ENT_FORK_256:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_512:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_NONE:
                    rb3.setChecked(true);
                    break;
            }

            String notes = m_entExam.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

            t.setText(notes);
        }
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        m_entExam = copyENTExamDataFromUI();
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
                    updateENTExam();
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);
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

    private ENTExam copyENTExamDataFromUI()
    {
        CheckBox cb1, cb2;
        TextView tx;
        RadioButton rb;

        ENTExam mh;

        if (m_entExam == null) {
            mh = new ENTExam();
        } else {
            mh = m_entExam;      // copies over clinic, patient ID, etc..
        }

        mh.setPatient(m_sess.getActivePatientId());
        mh.setClinic(m_sess.getClinicId());
        mh.setUsername("nobody");

        // normal

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // microtia

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // wax

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // drainage

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // otitis

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // fb

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // tubes

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_IN_PLACE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_EXTRUDING);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_IN_CANAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_IN_PLACE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_EXTRUDING);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_IN_CANAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_NONE);
        }

        // tympano

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_NONE);
        }

        // tm

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // perforations

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_MARGINAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_MARGINAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_NONE);
        }

        // voice test

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_NORMAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_ABNORMAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_NONE);
        }

        // tuning fork test

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_A_GREATER_B);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_B_GREATER_A);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_EQUAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_A_GREATER_B);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_B_GREATER_A);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_EQUAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_NONE);
        }

        // bc

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AD_LAT_TO_AD);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AD_LAT_TO_AS);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AS_LAT_TO_AD);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AS_LAT_TO_AS);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_NONE);
        }

        // fork

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_256);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_512);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_NONE);
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

    private void getENTExamDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        m_sess.setNewENTExam(false);
        new Thread(new Runnable() {
            public void run() {
            Thread thread = new Thread(){
                public void run() {
                ENTExam exam;
                exam = m_sess.getENTExam(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                if (exam == null) {
                    m_entExam = new ENTExam(); // null ??
                    m_entExam.setPatient(m_sess.getActivePatientId());
                    m_entExam.setClinic(m_sess.getClinicId());
                    m_entExam.setUsername("nobody");
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_exam_data), Toast.LENGTH_SHORT).show();
                            copyENTExamDataToUI(); // remove if null
                            setViewDirtyListeners();      // remove if null
                        }
                    });

                } else {
                    m_entExam = exam;
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_got_ent_exam_data), Toast.LENGTH_SHORT).show();
                            copyENTExamDataToUI();
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

    void updateENTExam()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTExamREST rest = new ENTExamREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewENTExam() == true) {
                    lock = rest.createENTExam(copyENTExamDataFromUI());
                } else {
                    lock = rest.updateENTExam(copyENTExamDataFromUI());
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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_ent_exam), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_entExam = copyENTExamDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_ent_exam), Toast.LENGTH_LONG).show();
                            m_sess.setNewENTExam(false);
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
            m_entExam = (ENTExam) bundle.getSerializable("exam");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_exam_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        copyENTExamDataToUI();
        setViewDirtyListeners();
        if (m_sess.getNewENTExam() == true) {
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

        final ENTExam mh = this.copyENTExamDataFromUI();

        if (m_dirty || mh.equals(m_entExam) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_ent_exam));
            builder.setMessage(m_activity.getString(R.string.msg_save_ent_exam));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    m_sess.getCommonSessionSingleton().updatePatientENTExam(mh);
                    m_sess.updateENTExam();
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
        View view = inflater.inflate(R.layout.app_ent_exam_layout, container, false);
        m_view  = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}