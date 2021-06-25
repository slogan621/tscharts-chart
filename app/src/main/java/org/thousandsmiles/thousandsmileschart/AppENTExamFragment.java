/*
 * (C) Copyright Syd Logan 2020-2021
 * (C) Copyright Thousand Smiles Foundation 2020-2021
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
import org.thousandsmiles.tscharts_lib.FormDirtyListener;
import org.thousandsmiles.tscharts_lib.FormDirtyNotifierFragment;
import org.thousandsmiles.tscharts_lib.FormDirtyPublisher;
import org.thousandsmiles.tscharts_lib.FormSaveAndPatientCheckoutNotifierActivity;
import org.thousandsmiles.tscharts_lib.FormSaveListener;
import org.thousandsmiles.tscharts_lib.PatientCheckoutListener;

import java.util.ArrayList;

public class AppENTExamFragment extends FormDirtyNotifierFragment implements FormSaveListener, PatientCheckoutListener {
    private FormSaveAndPatientCheckoutNotifierActivity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTExam m_entExam = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private AppFragmentContext m_ctx = new AppFragmentContext();
    private ArrayList<FormDirtyListener> m_listeners = new ArrayList<FormDirtyListener>();

    public void setAppFragmentContext(AppFragmentContext ctx) {
        m_ctx = ctx;
    }

    public static AppENTExamFragment newInstance() {
        return new AppENTExamFragment();
    }

    private boolean validate() {
        return validateFields();
    }

    void notifyReadyForCheckout(boolean success) {
        m_activity.fragmentReadyForCheckout(success);
    }

    void notifySaveDone(boolean success) {
        m_activity.fragmentSaveDone(success);
    }

    private boolean saveInternal(final boolean showReturnToClinic) {
        boolean ret = validate();
        if (ret == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

            builder.setTitle(m_activity.getString(R.string.title_unsaved_ent_exam));
            builder.setMessage(m_activity.getString(R.string.msg_save_ent_exam));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateENTExam();
                    if (showReturnToClinic == true) {
                        notifyReadyForCheckout(true);
                    } else {
                        notifySaveDone(true);
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (showReturnToClinic == true) {
                        notifyReadyForCheckout(false);
                    } else {
                        notifySaveDone(true);
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
        } else {
            notifySaveDone(true);
        }
        return ret;
    }

    @Override
    public boolean checkout() {
        if (m_dirty) {
            saveInternal(true);
        } else {
            notifyReadyForCheckout(true);
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(FormSaveAndPatientCheckoutNotifierActivity) context;
            m_activity.subscribeSave(this);
            m_activity.subscribeCheckout(this);
        }
    }

    private void copyENTExamDataToUI()
    {
        CheckBox cb1, cb2, cb3;
        TextView tx;
        Switch sw;
        RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8;

        if (m_entExam != null) {

            // Mouth

            sw = (Switch) m_view.findViewById(R.id.cleft_lip);
            if (sw != null) {
                sw.setChecked(m_entExam.getCleft_lip());
            }
            sw = (Switch) m_view.findViewById(R.id.cleft_palate);
            if (sw != null) {
                sw.setChecked(m_entExam.getCleft_palate());
            }

            ENTExam.TristateBoolean value;

            value = m_entExam.getRepaired_lip();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_yes);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_no);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_na);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (value) {
                case EAR_TRI_STATE_BOOLEAN_YES:
                    rb1.setChecked(true);
                    break;
                case EAR_TRI_STATE_BOOLEAN_NO:
                    rb2.setChecked(true);
                    break;
                case EAR_TRI_STATE_BOOLEAN_NA:
                    rb3.setChecked(true);
                    break;
            }

            value = m_entExam.getRepaired_palate();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_yes);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_no);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_na);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (value) {
                case EAR_TRI_STATE_BOOLEAN_YES:
                    rb1.setChecked(true);
                    break;
                case EAR_TRI_STATE_BOOLEAN_NO:
                    rb2.setChecked(true);
                    break;
                case EAR_TRI_STATE_BOOLEAN_NA:
                    rb3.setChecked(true);
                    break;
            }

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

            side = m_entExam.getEffusion();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_right);

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

            side = m_entExam.getMiddleEarInfection();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_right);

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
        if (m_ctx.getReadOnly() == true) {
            return;
        }

        m_dirty = true;

        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).dirty(true);
        }
    }

    private void clearDirty() {
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).dirty(false);
        }
        m_dirty = false;
    }

    private void setViewDirtyListeners()
    {
        CheckBox cb;
        RadioButton rb;
        Switch sw;

        // Mouth

        sw = (Switch) m_view.findViewById(R.id.cleft_lip);
        if (sw != null) {
            if (m_ctx.getReadOnly() == true) {
                sw.setEnabled(false);
            }
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        sw = (Switch) m_view.findViewById(R.id.cleft_palate);
        if (sw != null) {
            if (m_ctx.getReadOnly() == true) {
                sw.setEnabled(false);
            }
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_yes);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_no);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_na);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_yes);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_no);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_na);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        // Ears

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);
        if (t != null) {
            if (m_ctx.getReadOnly() == true) {
                t.setEnabled(false);
            }
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
        Switch sw;

        ENTExam mh;

        if (m_entExam == null) {
            mh = new ENTExam();
        } else {
            mh = m_entExam;      // copies over clinic, patient ID, etc..
        }

        mh.setPatient(m_sess.getDisplayPatientId());
        mh.setClinic(m_sess.getClinicId());
        mh.setUsername("nobody");

        // mouth

        sw = (Switch) m_view.findViewById(R.id.cleft_lip);
        if (sw != null) {
            mh.setCleft_lip(sw.isChecked());
        }

        sw = (Switch) m_view.findViewById(R.id.cleft_palate);
        if (sw != null) {
            mh.setCleft_palate(sw.isChecked());
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_yes);

        if (rb.isChecked()) {
            mh.setRepaired_lip(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_YES);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_no);

        if (rb.isChecked()) {
            mh.setRepaired_lip(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_NO);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_lip_na);

        if (rb.isChecked()) {
            mh.setRepaired_lip(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_NA);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_yes);

        if (rb.isChecked()) {
            mh.setRepaired_palate(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_YES);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_no);

        if (rb.isChecked()) {
            mh.setRepaired_palate(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_NO);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_repaired_palate_na);

        if (rb.isChecked()) {
            mh.setRepaired_palate(ENTExam.TristateBoolean.EAR_TRI_STATE_BOOLEAN_NA);
        }

        // ears

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

        // effusion

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_effusion_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setEffusion(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setEffusion(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setEffusion(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setEffusion(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // middle ear infection

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_middle_ear_infection_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setMiddleEarInfection(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setMiddleEarInfection(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setMiddleEarInfection(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setMiddleEarInfection(ENTHistory.EarSide.EAR_SIDE_NONE);
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
       return true;
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
                    m_entExam.setPatient(m_sess.getDisplayPatientId());
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
        ((StationActivity) m_activity).unsubscribeSave(this);
        ((StationActivity) m_activity).unsubscribeCheckout(this);

        super.onPause();
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

    @Override
    public void subscribeDirty(FormDirtyListener instance) {
        m_listeners.add(instance);
    }

    @Override
    public void unsubscribeDirty(FormDirtyListener instance) {
        m_listeners.remove(instance);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_activity != null) {
            m_activity.unsubscribeSave(this);
            m_activity.unsubscribeCheckout(this);
        }
    }
}