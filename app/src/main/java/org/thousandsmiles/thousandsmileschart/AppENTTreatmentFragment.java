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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.ENTTreatment;
import org.thousandsmiles.tscharts_lib.ENTTreatmentREST;

public class AppENTTreatmentFragment extends Fragment implements FormSaveListener, PatientCheckoutListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTTreatment m_entTreatment = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private AppFragmentContext m_ctx = new AppFragmentContext();

    public void setAppFragmentContext(AppFragmentContext ctx) {
        m_ctx = ctx;
    }

    public static AppENTTreatmentFragment newInstance() {
        return new AppENTTreatmentFragment();
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

            builder.setTitle(m_activity.getString(R.string.title_unsaved_ent_treatment));
            builder.setMessage(m_activity.getString(R.string.msg_save_ent_treatment));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateENTTreatment();
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
        }
        ((StationActivity)m_activity).subscribeSave(this);
        ((StationActivity)m_activity).subscribeCheckout(this);
    }

    private void copyENTTreatmentDataToUI()
    {
        CheckBox cb1, cb2, cb3;
        RadioButton rb1, rb2;
        EditText tx;

        ENTHistory.EarSide side;
        String textVal;
        boolean boolVal;

        if (m_entTreatment != null) {
            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getEarCleanedSide();

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

            tx = (EditText) m_view.findViewById(R.id.ent_ears_cleaned_comment);
            textVal = m_entTreatment.getEarCleanedComment();

            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getAudiogramSide();

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

            tx = (EditText) m_view.findViewById(R.id.ent_audiogram_comment);
            textVal = m_entTreatment.getAudiogramComment();

            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_false);
            rb1.setChecked(false);
            rb2.setChecked(false);

            boolVal = m_entTreatment.getAudiogramRightAway();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_audiogram_right_away_comment);
            textVal = m_entTreatment.getAudiogramRightAwayComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getTympanogramSide();

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

            tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_comment);
            textVal = m_entTreatment.getTympanogramComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_false);
            rb1.setChecked(false);
            rb2.setChecked(false);

            boolVal = m_entTreatment.getTympanogramRightAway();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_right_away_comment);
            textVal = m_entTreatment.getTympanogramRightAwayComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getMastoidDebridedSide();

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

            tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_comment);
            textVal = m_entTreatment.getMastoidDebridedComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getMastoidDebridedHearingAidEval();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_comment);
            textVal = m_entTreatment.getMastoidDebridedHearingAidEvalComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getAntibioticDrops();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_comment);
            textVal = m_entTreatment.getAntibioticDropsComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getAntibioticOrally();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_orally_comment);
            textVal = m_entTreatment.getAntibioticOrallyComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getAntibioticAcuteInfection();

            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }
            tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_comment);
            textVal = m_entTreatment.getAntibioticAcuteInfectionComment();

            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getAntibioticAfterWaterExposureInfectionPrevention();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_comment);
            textVal = m_entTreatment.getAntibioticAfterWaterExposureInfectionPreventionComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getBoricAcidSide();

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

            tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_comment);
            textVal = m_entTreatment.getBoricAcidSideComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getBoricAcidToday();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_today_comment);
            textVal = m_entTreatment.getBoricAcidTodayComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getBoricAcidForHomeUse();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }
            tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_for_home_use_comment);
            textVal = m_entTreatment.getBoricAcidForHomeUseComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_fb_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getForeignBodyRemoved();
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

            tx = (EditText) m_view.findViewById(R.id.ent_fb_comment);
            textVal = m_entTreatment.getForeignBodyRemovedComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_3_months);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_6_months);
            cb3 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_prn);

            cb1.setChecked(false);
            cb2.setChecked(false);
            cb3.setChecked(false);

            boolVal = m_entTreatment.getReturn3Months();
            if (boolVal == true) {
                cb1.setChecked(true);
            }
            boolVal = m_entTreatment.getReturn6Months();
            if (boolVal == true) {
                cb2.setChecked(true);
            }
            boolVal = m_entTreatment.getReturnPrn();
            if (boolVal == true) {
                cb3.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_return_to_clinic_comment);
            textVal = m_entTreatment.getReturnComment();
            tx.setText(textVal);

            boolVal = m_entTreatment.getReferredPvtENTEnsenada();
            if (boolVal == true) {
                cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_pvt_ent_ensenada);
            }
            tx = (EditText) m_view.findViewById(R.id.ent_referred_pvt_ensenada_comment);
            textVal = m_entTreatment.getReferredPvtENTEnsenadaComment();
            tx.setText(textVal);

            boolVal = m_entTreatment.getReferredChildrensHospitalTJ();
            if (boolVal == true) {
                cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_childrens_hospital_tj);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_referred_childrens_hospital_tj_comment);
            textVal = m_entTreatment.getReferredChildrensHospitalTJComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getTubesTomorrow();
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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tubes_comment);
            textVal = m_entTreatment.getTubesTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_right);
            side = m_entTreatment.getTPlastyTomorrow();
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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tplasty_comment);
            textVal = m_entTreatment.getTPlastyTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getEuaTomorrow();

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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_eua_comment);
            textVal = m_entTreatment.getEuaTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getFbRemovalTomorrow();
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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_fb_comment);
            textVal = m_entTreatment.getFbRemovalTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getMiddleEarExploreMyringotomyTomorrow();
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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_myringotomy_comment);
            textVal = m_entTreatment.getMiddleEarExploreMyringotomyTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getCerumenTomorrow();
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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_cerumen_comment);
            textVal = m_entTreatment.getCerumenTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getGranulomaTomorrow();

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

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_granuloma_comment);
            textVal = m_entTreatment.getGranulomaTomorrowComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_false);
            rb1.setChecked(false);
            rb2.setChecked(false);

            boolVal = m_entTreatment.getSeptorhinoplastyTomorrow();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }
            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_comment);
            textVal = m_entTreatment.getSeptorhinoplastyTomorrowComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getScarRevisionCleftLipTomorrow();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_scar_revision_comment);
            textVal = m_entTreatment.getScarRevisionCleftLipTomorrowComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getFrenulectomyTomorrow();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }
            tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_frenulectomy_comment);
            textVal = m_entTreatment.getFrenulectomyTomorrowComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getTubesFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_tubes_comment);
            textVal = m_entTreatment.getTubesFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getTPlastyFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_tplasty_comment);
            textVal = m_entTreatment.getTPlastyFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_right);
            cb1.setChecked(false);
            cb2.setChecked(false);
            side = m_entTreatment.getEuaFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_eua_comment);
            textVal = m_entTreatment.getEuaFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getFbRemovalFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_fb_comment);
            textVal = m_entTreatment.getFbRemovalFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getMiddleEarExploreMyringotomyFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_myringotomy_comment);
            textVal = m_entTreatment.getMiddleEarExploreMyringotomyFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getCerumenFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_cerumen_comment);
            textVal = m_entTreatment.getCerumenFutureComment();
            tx.setText(textVal);

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_right);
            cb1.setChecked(false);
            cb2.setChecked(false);

            side = m_entTreatment.getGranulomaFuture();
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

            tx = (EditText) m_view.findViewById(R.id.ent_future_granuloma_comment);
            textVal = m_entTreatment.getGranulomaFutureComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_false);
            rb1.setChecked(false);
            rb2.setChecked(false);

            boolVal = m_entTreatment.getSeptorhinoplastyFuture();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_future_septorhinoplasty_comment);
            textVal = m_entTreatment.getSeptorhinoplastyFutureComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_false);
            rb1.setChecked(false);
            rb2.setChecked(false);
            boolVal = m_entTreatment.getScarRevisionCleftLipFuture();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_future_scar_revision_comment);
            textVal = m_entTreatment.getScarRevisionCleftLipFutureComment();
            tx.setText(textVal);

            rb1 = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_true);
            rb2 = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_false);
            rb1.setChecked(false);
            rb2.setChecked(false);

            boolVal = m_entTreatment.getFrenulectomyFuture();
            if (boolVal == true) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }

            tx = (EditText) m_view.findViewById(R.id.ent_future_frenulectomy_comment);
            textVal = m_entTreatment.getFrenulectomyFutureComment();
            tx.setText(textVal);

            tx = (EditText) m_view.findViewById(R.id.ent_notes);
            textVal = m_entTreatment.getComment();
            tx.setText(textVal);
        }
    }

    private void setDirty()
    {
        if (m_ctx.getReadOnly()) {
            return;
        }
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
        CheckBox cb;
        RadioButton rb;
        EditText tx;

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_ears_cleaned_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_audiogram_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_audiogram_right_away_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_right_away_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_orally_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_today_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_for_home_use_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_fb_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_fb_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_fb_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_3_months);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_6_months);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_prn);
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
        tx = (EditText) m_view.findViewById(R.id.ent_return_to_clinic_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_pvt_ent_ensenada);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_childrens_hospital_tj);
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
        tx = (EditText) m_view.findViewById(R.id.ent_referred_pvt_ensenada_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (EditText) m_view.findViewById(R.id.ent_referred_childrens_hospital_tj_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tubes_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tplasty_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_eua_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_fb_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_myringotomy_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_cerumen_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_granuloma_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_scar_revision_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_frenulectomy_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_tubes_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_tplasty_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_eua_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_fb_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_myringotomy_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_cerumen_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_left);
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
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_right);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_granuloma_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_septorhinoplasty_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_scar_revision_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_true);
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
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_false);
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
        tx = (EditText) m_view.findViewById(R.id.ent_future_frenulectomy_comment);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
        tx = (EditText) m_view.findViewById(R.id.ent_notes);
        if (tx != null) {
            if (m_ctx.getReadOnly() == true) {
                tx.setEnabled(false);
            }
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setDirty();
                }
            });
        }
    }

    private ENTTreatment copyENTTreatmentDataFromUI()
    {
        CheckBox cb1, cb2, cb3;
        EditText tx;
        RadioButton rb;

        ENTTreatment treatment;

        if (m_entTreatment == null) {
            treatment = new ENTTreatment();
        } else {
            treatment = m_entTreatment;     
        }

        treatment.setPatient(m_sess.getDisplayPatientId());
        treatment.setClinic(m_sess.getClinicId());
        treatment.setUsername("nobody");

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_cleaned_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setEarCleanedSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setEarCleanedSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setEarCleanedSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setEarCleanedSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        tx = (EditText) m_view.findViewById(R.id.ent_ears_cleaned_comment);
        Editable text = tx.getText();
        treatment.setEarCleanedComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_audiogram_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setAudiogramSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setAudiogramSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setAudiogramSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setAudiogramSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        tx = (EditText) m_view.findViewById(R.id.ent_audiogram_comment);
        text = tx.getText();
        treatment.setAudiogramComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_true);
        if (rb.isChecked()) {
            treatment.setAudiogramRightAway(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_audiogram_right_away_false);
        if (rb.isChecked()) {
            treatment.setAudiogramRightAway(false);
        }

        tx = (EditText) m_view.findViewById(R.id.ent_audiogram_right_away_comment);
        text = tx.getText();
        treatment.setAudiogramRightAwayComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tympanogram_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setTympanogramSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setTympanogramSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setTympanogramSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setTympanogramSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_comment);
        text = tx.getText();
        treatment.setTympanogramComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_true);
        if (rb.isChecked()) {
            treatment.setTympanogramRightAway(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tympanogram_right_away_false);
        if (rb.isChecked()) {
            treatment.setTympanogramRightAway(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tympanogram_right_away_comment);
        text = tx.getText();
        treatment.setTympanogramRightAwayComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_mastoid_debrided_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setMastoidDebridedSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setMastoidDebridedSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setMastoidDebridedSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setMastoidDebridedSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_comment);
        text = tx.getText();
        treatment.setMastoidDebridedComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_true);
        if (rb.isChecked()) {
            treatment.setMastoidDebridedHearingAidEval(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_false);
        if (rb.isChecked()) {
            treatment.setMastoidDebridedHearingAidEval(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_mastoid_debrided_hearing_aid_eval_comment);
        text = tx.getText();
        treatment.setMastoidDebridedHearingAidEvalComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_true);
        if (rb.isChecked()) {
            treatment.setAntibioticDrops(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_false);
        if (rb.isChecked()) {
            treatment.setAntibioticDrops(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_comment);
        text = tx.getText();
        treatment.setAntibioticDropsComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_true);
        if (rb.isChecked()) {
            treatment.setAntibioticOrally(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_orally_false);
        if (rb.isChecked()) {
            treatment.setAntibioticOrally(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_orally_comment);
        text = tx.getText();
        treatment.setAntibioticOrallyComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_true);
        if (rb.isChecked()) {
            treatment.setAntibioticAcuteInfection(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_false);
        if (rb.isChecked()) {
            treatment.setAntibioticAcuteInfection(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_acute_infection_comment);
        text = tx.getText();
        treatment.setAntibioticAcuteInfectionComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_true);
        if (rb.isChecked()) {
            treatment.setAntibioticAfterWaterExposureInfectionPrevention(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_false);
        if (rb.isChecked()) {
            treatment.setAntibioticAfterWaterExposureInfectionPrevention(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_antibiotic_drops_after_water_exposure_comment);
        text = tx.getText();
        treatment.setAntibioticAfterWaterExposureInfectionPreventionComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_boric_acid_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setBoricAcidSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setBoricAcidSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setBoricAcidSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setBoricAcidSide(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_comment);
        text = tx.getText();
        treatment.setBoricAcidSideComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_true);
        if (rb.isChecked()) {
            treatment.setBoricAcidToday(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_today_false);
        if (rb.isChecked()) {
            treatment.setBoricAcidToday(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_today_comment);
        text = tx.getText();
        treatment.setBoricAcidTodayComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_true);
        if (rb.isChecked()) {
            treatment.setBoricAcidForHomeUse(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_boric_acid_for_home_use_false);
        if (rb.isChecked()) {
            treatment.setBoricAcidForHomeUse(true);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_boric_acid_for_home_use_comment);
        text = tx.getText();
        treatment.setBoricAcidForHomeUseComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setForeignBodyRemoved(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setForeignBodyRemoved(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setForeignBodyRemoved(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setForeignBodyRemoved(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_fb_comment);
        text = tx.getText();
        treatment.setForeignBodyRemovedComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_3_months);
        if (cb1.isChecked()) {
            treatment.setReturn3Months(true);
        } else {
            treatment.setReturn3Months(false);
        }
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_6_months);
        if (cb1.isChecked()) {
            treatment.setReturn6Months(true);
        }
        else {
            treatment.setReturn6Months(false);
        }
        cb3 = (CheckBox) m_view.findViewById(R.id.checkbox_return_to_clinic_prn);
        if (cb1.isChecked()) {
            treatment.setReturnPrn(true);
        } else {
            treatment.setReturnPrn(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_return_to_clinic_comment);
        text = tx.getText();
        treatment.setReturnComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_pvt_ent_ensenada);
        if (cb1.isChecked()) {
            treatment.setReferredPvtENTEnsenada(true);
        } else {
            treatment.setReferredPvtENTEnsenada(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_referred_pvt_ensenada_comment);
        text = tx.getText();
        treatment.setReferredPvtENTEnsenadaComment(text.toString());

        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_referrals_childrens_hospital_tj);
        if (cb1.isChecked()) {
            treatment.setReferredChildrensHospitalTJ(true);
        } else {
            treatment.setReferredChildrensHospitalTJ(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_referred_childrens_hospital_tj_comment);
        text = tx.getText();
        treatment.setReferredChildrensHospitalTJComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tubes_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setTubesTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setTubesTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setTubesTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setTubesTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tubes_comment);
        text = tx.getText();
        treatment.setTubesTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_tplasty_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setTPlastyTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setTPlastyTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setTPlastyTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setTPlastyTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_tplasty_comment);
        text = tx.getText();
        treatment.setTPlastyTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_eua_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setEuaTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setEuaTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setEuaTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setEuaTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_eua_comment);
        text = tx.getText();
        treatment.setEuaTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setFbRemovalTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setFbRemovalTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setFbRemovalTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setFbRemovalTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_fb_comment);
        text = tx.getText();
        treatment.setFbRemovalTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_myringotomy_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setMiddleEarExploreMyringotomyTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_myringotomy_comment);
        text = tx.getText();
        treatment.setMiddleEarExploreMyringotomyTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_cerumen_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setCerumenTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setCerumenTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setCerumenTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setCerumenTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_cerumen_comment);
        text = tx.getText();
        treatment.setCerumenTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tomorrow_granuloma_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setGranulomaTomorrow(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setGranulomaTomorrow(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setGranulomaTomorrow(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setGranulomaTomorrow(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_granuloma_comment);
        text = tx.getText();
        treatment.setGranulomaTomorrowComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_true);
        if (rb.isChecked()) {
            treatment.setSeptorhinoplastyTomorrow(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_false);
        if (rb.isChecked()) {
            treatment.setSeptorhinoplastyTomorrow(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_septorhinoplasty_comment);
        text = tx.getText();
        treatment.setSeptorhinoplastyTomorrowComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_true);
        if (rb.isChecked()) {
            treatment.setScarRevisionCleftLipTomorrow(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_scar_revision_false);
        if (rb.isChecked()) {
            treatment.setScarRevisionCleftLipTomorrow(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_scar_revision_comment);
        text = tx.getText();
        treatment.setScarRevisionCleftLipTomorrowComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_true);
        if (rb.isChecked()) {
            treatment.setFrenulectomyTomorrow(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_tomorrow_frenulectory_false);
        if (rb.isChecked()) {
            treatment.setFrenulectomyTomorrow(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_tomorrow_frenulectomy_comment);
        text = tx.getText();
        treatment.setFrenulectomyTomorrowComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tubes_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setTubesFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setTubesFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setTubesFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setTubesFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_tubes_comment);
        text = tx.getText();
        treatment.setTubesFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_tplasty_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setTPlastyFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setTPlastyFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setTPlastyFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setTPlastyFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_tplasty_comment);
        text = tx.getText();
        treatment.setTPlastyFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_eua_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setEuaFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setEuaFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setEuaFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setEuaFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_eua_comment);
        text = tx.getText();
        treatment.setEuaFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setFbRemovalFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setFbRemovalFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setFbRemovalFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setFbRemovalFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_fb_comment);
        text = tx.getText();
        treatment.setFbRemovalFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_myringotomy_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setMiddleEarExploreMyringotomyFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setMiddleEarExploreMyringotomyFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_myringotomy_comment);
        text = tx.getText();
        treatment.setMiddleEarExploreMyringotomyFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_cerumen_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setCerumenFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setCerumenFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setCerumenFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setCerumenFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_cerumen_comment);
        text = tx.getText();
        treatment.setCerumenFutureComment(text.toString());

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_future_granuloma_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            treatment.setGranulomaFuture(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            treatment.setGranulomaFuture(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            treatment.setGranulomaFuture(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            treatment.setGranulomaFuture(ENTHistory.EarSide.EAR_SIDE_NONE);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_granuloma_comment);
        text = tx.getText();
        treatment.setGranulomaFutureComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_true);
        if (rb.isChecked()) {
            treatment.setSeptorhinoplastyFuture(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_septorhinoplasty_false);
        if (rb.isChecked()) {
            treatment.setSeptorhinoplastyFuture(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_septorhinoplasty_comment);
        text = tx.getText();
        treatment.setSeptorhinoplastyFutureComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_true);
        if (rb.isChecked()) {
            treatment.setScarRevisionCleftLipFuture(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_scar_revision_false);
        if (rb.isChecked()) {
            treatment.setScarRevisionCleftLipFuture(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_scar_revision_comment);
        text = tx.getText();
        treatment.setScarRevisionCleftLipFutureComment(text.toString());

        rb = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_true);
        if (rb.isChecked()) {
            treatment.setFrenulectomyFuture(true);
        }
        rb = (RadioButton) m_view.findViewById(R.id.ent_future_frenulectory_false);
        if (rb.isChecked()) {
            treatment.setFrenulectomyFuture(false);
        }
        tx = (EditText) m_view.findViewById(R.id.ent_future_frenulectomy_comment);
        text = tx.getText();
        treatment.setFrenulectomyFutureComment(text.toString());

        tx = (EditText) m_view.findViewById(R.id.ent_notes);
        text = tx.getText();
        treatment.setComment(text.toString());

        return treatment;
    }

    private boolean validateFields()
    {
        return true;
    }

    private void getENTTreatmentDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        m_sess.setNewENTTreatment(false);
        new Thread(new Runnable() {
            public void run() {
            Thread thread = new Thread(){
                public void run() {
                ENTTreatment treatment;
                treatment = m_sess.getENTTreatment(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                if (treatment == null) {
                    m_entTreatment = new ENTTreatment(); // null ??
                    m_entTreatment.setPatient(m_sess.getDisplayPatientId());
                    m_entTreatment.setClinic(m_sess.getClinicId());
                    m_entTreatment.setUsername("nobody");
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_treatment_data), Toast.LENGTH_SHORT).show();
                            copyENTTreatmentDataToUI(); // remove if null
                            setViewDirtyListeners();      // remove if null
                        }
                    });

                } else {
                    m_entTreatment = treatment;
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_got_ent_treatment_data), Toast.LENGTH_SHORT).show();
                            copyENTTreatmentDataToUI();
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

    void updateENTTreatment()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTTreatmentREST rest = new ENTTreatmentREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewENTTreatment() == true) {
                    lock = rest.createENTTreatment(copyENTTreatmentDataFromUI());
                } else {
                    lock = rest.updateENTTreatment(copyENTTreatmentDataFromUI());
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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_ent_treatment), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_entTreatment = copyENTTreatmentDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_ent_treatment), Toast.LENGTH_LONG).show();
                            m_sess.setNewENTTreatment(false);
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
            m_entTreatment = (ENTTreatment) bundle.getSerializable("treatment");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_treatment_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        copyENTTreatmentDataToUI();
        setViewDirtyListeners();
        if (m_sess.getNewENTTreatment() == true) {
            setDirty();
        } else {
            clearDirty();
        }
        setFrameToggleClickListeners();
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
        View view = inflater.inflate(R.layout.app_ent_treatment_layout, container, false);
        m_view  = view;
        setButtonBarCallbacks();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // section toggles

    private void setFrameToggleClickListeners()
    {
        Button b;

        b = (Button) getActivity().findViewById(R.id.button_ears_cleaned_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_ears_cleaned_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_audiogram_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_audiogram_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_tympanogram_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_tympanogram_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_mastoid_debrided_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_mastoid_debrided_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_antibiotic_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_antibiotic_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_boric_acid_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_boric_acid_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_fb_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_fb_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_return_to_clinic_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_return_to_clinic_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_referrals_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_referrals_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_tomorrow_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_tomorrow_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });

        b = (Button) getActivity().findViewById(R.id.button_future_toggle);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LinearLayout layout = getActivity().findViewById(R.id.ent_future_frame);
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    ((Button) v).setText(R.string.button_label_hide);
                } else {
                    layout.setVisibility(View.GONE);
                    ((Button) v).setText(R.string.button_label_show);
                }
            }
        });
    }
}
