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
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.ENTDiagnosis;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisExtra;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisExtraREST;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisREST;
import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;

import java.util.ArrayList;

public class AppENTDiagnosisFragment extends Fragment implements FormSaveListener, PatientCheckoutListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private ENTDiagnosis m_entDiagnosis = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private AppENTDiagnosisFragment m_this;
    private AppFragmentContext m_ctx = new AppFragmentContext();

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

            builder.setTitle(m_activity.getString(R.string.title_unsaved_ent_diagnosis));
            builder.setMessage(m_activity.getString(R.string.msg_save_ent_diagnosis));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateENTDiagnosis();
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

    public void setAppFragmentContext(AppFragmentContext ctx) {
        m_ctx = ctx;
    }

    public static AppENTDiagnosisFragment newInstance() {
        return new AppENTDiagnosisFragment();
    }

    void showAddDialog()
    {
        ENTDiagnosisExtraDialogFragment rtc = new ENTDiagnosisExtraDialogFragment();
        rtc.setParentActivity(this.getActivity());
        rtc.setAppENTDiagnosisFragment(this);
        rtc.show(getFragmentManager(), m_activity.getString(R.string.msg_add_extra_diagnosis_item));
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
        ArrayList<ENTDiagnosisExtra> extra = m_sess.getENTDiagnosisExtraList();

        ((ViewGroup) tableLayout).removeAllViews();

        for (int i = 0; i < extra.size(); i++) {

            ENTDiagnosisExtra ex = extra.get(i);

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
            cb.setChecked(m_sess.isInENTDiagnosisExtraDeleteList(ex));

            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }

            cb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ENTDiagnosisExtra extr = (ENTDiagnosisExtra) v.getTag();
                    if (((CheckBox)v).isChecked()) {
                        m_sess.addENTDiagnosisExtraToDeleteList(extr);
                        enableRemoveButton();
                    } else {
                        m_sess.removeENTDiagnosisExtraFromDeleteList(extr);
                        if (m_sess.getENTDiagnosisExtraDeleteList().size() == 0) {
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

            String value = ex.getValue();
            tv.setText(value);
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

    private void deleteRemovalObject(final ENTDiagnosisExtra extra) {
        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTDiagnosisExtraREST rest = new ENTDiagnosisExtraREST(m_sess.getContext());
                Object lock;
                int status;

                lock = rest.getEntDiagnosisExtraById(extra.getId());

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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_read_ent_diagnosis_extra_data), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    lock = rest.deleteENTDiagnosisExtra(extra.getId());

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
                                Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_delete_ent_diagnosis_extra), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_deleted_ent_diagnosis_extra), Toast.LENGTH_LONG).show();
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
        // from the ent diagnosis extra list maintained in session singleton,
        // refresh the list, and then clear the removal list

        ArrayList<ENTDiagnosisExtra> extras = m_sess.getENTDiagnosisExtraList();
        ArrayList<ENTDiagnosisExtra> removals = m_sess.getENTDiagnosisExtraDeleteList();

        for (int i = 0; i < removals.size(); i++) {
            ENTDiagnosisExtra extra = removals.get(i);
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
        ((StationActivity)m_activity).subscribeSave(this);
        ((StationActivity)m_activity).subscribeCheckout(this);
    }

    private void copyENTDiagnosisDataToUI()
    {
        CheckBox cb1, cb2;
        TextView tx;
        Switch sw;
        boolean boolVal;

        if (m_entDiagnosis != null) {

            ENTHistory.EarSide side;

            side = m_entDiagnosis.getHlConductive();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_right);

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

            side = m_entDiagnosis.getHl();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_right);

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

            side = m_entDiagnosis.getHlMixed();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_right);

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

            side = m_entDiagnosis.getHlSensory();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_right);

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

            side = m_entDiagnosis.getExternalCerumenImpaction();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_right);

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

            side = m_entDiagnosis.getExternalEarCanalFB();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_right);

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

            side = m_entDiagnosis.getExternalMicrotia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_right);

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

            side = m_entDiagnosis.getTympanicAtelectasis();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_right);

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

            side = m_entDiagnosis.getTympanicGranuloma();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_right);

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

            side = m_entDiagnosis.getTympanicMonomer();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_right);

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

            side = m_entDiagnosis.getTympanicTube();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tube_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tube_right);

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

            side = m_entDiagnosis.getTympanicPerf();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_right);

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

            side = m_entDiagnosis.getMiddleEarCholesteatoma();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_right);

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

            side = m_entDiagnosis.getMiddleEarEustTubeDysTMRetraction();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_right);

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

            side = m_entDiagnosis.getMiddleEarOtitisMedia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_right);

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

            side = m_entDiagnosis.getMiddleEarSerousOtitisMedia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_right);

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

            boolVal = m_entDiagnosis.getOralAnkyloglossia();

            sw = (Switch) m_view.findViewById(R.id.oral_ankyloglossia);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralTonsilEnlarge();

            sw = (Switch) m_view.findViewById(R.id.oral_tonsil_enlarge);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftLipRepairDeformity();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repair_deformity);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftLipUnilateral();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unilateral);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftLipBilateral();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_bilateral);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftLipUnrepaired();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unrepaired);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftLipRepaired();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repaired);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            // palate


            boolVal = m_entDiagnosis.getOralCleftPalateUnilateral();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unilateral);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftPalateBilateral();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_bilateral);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftPalateUnrepaired();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unrepaired);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftPalateUnrepaired();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unrepaired);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralCleftPalateRepaired();

            sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_repaired);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getOralSpeechProblem();

            sw = (Switch) m_view.findViewById(R.id.oral_speech_problem);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getNoseDeviatedSeptum();

            sw = (Switch) m_view.findViewById(R.id.nose_deviated_septum);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getNoseTurbinateHypertrophy();

            sw = (Switch) m_view.findViewById(R.id.nose_turbinate_hypertrophy);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            boolVal = m_entDiagnosis.getNoseDeformitySecondaryToCleftPalate();

            sw = (Switch) m_view.findViewById(R.id.nose_deformity_secondary_to_cleft_palate);

            if (boolVal) {
                sw.setChecked(true);
            }
            else {
                sw.setChecked(false);
            }

            side = m_entDiagnosis.getSyndromeHemifacialMicrosomia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_right);

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

            side = m_entDiagnosis.getSyndromePierreRobin();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_right);

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

            String notes = m_entDiagnosis.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

            t.setText(notes);
        }
    }

    public void setDirty()
    {
        if (m_ctx.getReadOnly() == true) {
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
        Switch sw;

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tube_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tube_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_right);
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

        sw = (Switch) m_view.findViewById(R.id.oral_ankyloglossia);
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

        sw = (Switch) m_view.findViewById(R.id.oral_tonsil_enlarge);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repair_deformity);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unilateral);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_bilateral);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unrepaired);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repaired);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unilateral);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_bilateral);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unrepaired);
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

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_repaired);
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

        sw = (Switch) m_view.findViewById(R.id.oral_speech_problem);
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

        sw = (Switch) m_view.findViewById(R.id.nose_deviated_septum);
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

        sw = (Switch) m_view.findViewById(R.id.nose_deformity_secondary_to_cleft_palate);
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

        sw = (Switch) m_view.findViewById(R.id.nose_deviated_septum);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_right);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_left);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_right);
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

        Button bt = (Button) m_view.findViewById(R.id.add_button);
        if (bt != null) {
            bt.setEnabled(!m_ctx.getReadOnly());
        }
    }

    private ENTDiagnosis copyENTDiagnosisDataFromUI()
    {
        CheckBox cb1, cb2;
        TextView tx;
        Switch sw;

        ENTDiagnosis diag = null;

        if (m_entDiagnosis == null) {
            diag = new ENTDiagnosis();
        } else {
            diag = m_entDiagnosis;      // copies over clinic, patient ID, etc..
        }

        diag.setPatient(m_sess.getDisplayPatientId());
        diag.setClinic(m_sess.getClinicId());
        diag.setUsername("nobody");

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_conductive_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setHlConductive(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setHlConductive(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setHlConductive(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setHlConductive(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_hearing_loss_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setHl(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setHl(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setHl(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setHl(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_mixed_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setHlMixed(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setHlMixed(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setHlMixed(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setHlMixed(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_sensory_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setHlSensory(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setHlSensory(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setHlSensory(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setHlSensory(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_cerumen_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setExternalCerumenImpaction(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setExternalCerumenImpaction(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setExternalCerumenImpaction(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setExternalCerumenImpaction(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ear_canal_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setExternalEarCanalFB(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setExternalEarCanalFB(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setExternalEarCanalFB(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setExternalEarCanalFB(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_microtia_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setExternalMicrotia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setExternalMicrotia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setExternalMicrotia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setExternalMicrotia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_atelectasis_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setTympanicAtelectasis(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setTympanicAtelectasis(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setTympanicAtelectasis(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setTympanicAtelectasis(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_granuloma_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setTympanicGranuloma(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setTympanicGranuloma(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setTympanicGranuloma(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setTympanicGranuloma(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_monomer_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setTympanicMonomer(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setTympanicMonomer(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setTympanicMonomer(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setTympanicMonomer(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tube_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tube_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setTympanicTube(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setTympanicTube(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setTympanicTube(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setTympanicTube(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_tympanic_perf_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setTympanicPerf(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setTympanicPerf(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setTympanicPerf(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setTympanicPerf(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_cholesteatoma_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setMiddleEarCholesteatoma(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setMiddleEarCholesteatoma(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setMiddleEarCholesteatoma(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setMiddleEarCholesteatoma(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_eustatian_tube_dysfunction_with_tm_retraction_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setMiddleEarEustTubeDysTMRetraction(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setMiddleEarEustTubeDysTMRetraction(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setMiddleEarEustTubeDysTMRetraction(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setMiddleEarEustTubeDysTMRetraction(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_otitis_media_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setMiddleEarOtitisMedia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setMiddleEarOtitisMedia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setMiddleEarOtitisMedia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setMiddleEarOtitisMedia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_serous_otitis_media_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setMiddleEarSerousOtitisMedia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setMiddleEarSerousOtitisMedia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setMiddleEarSerousOtitisMedia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setMiddleEarSerousOtitisMedia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_ankyloglossia);

        if (sw.isChecked()) {
            diag.setOralAnkyloglossia(true);
        } else {
            diag.setOralAnkyloglossia(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_tonsil_enlarge);

        if (sw.isChecked()) {
            diag.setOralTonsilEnlarge(true);
        } else {
            diag.setOralTonsilEnlarge(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repair_deformity);

        if (sw.isChecked()) {
            diag.setOralCleftLipRepairDeformity(true);
        } else {
            diag.setOralCleftLipRepairDeformity(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unilateral);

        if (sw.isChecked()) {
            diag.setOralCleftLipUnilateral(true);
        } else {
            diag.setOralCleftLipUnilateral(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_bilateral);

        if (sw.isChecked()) {
            diag.setOralCleftLipBilateral(true);
        } else {
            diag.setOralCleftLipBilateral(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_unrepaired);

        if (sw.isChecked()) {
            diag.setOralCleftLipUnrepaired(true);
        } else {
            diag.setOralCleftLipUnrepaired(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_lip_repaired);

        if (sw.isChecked()) {
            diag.setOralCleftLipRepaired(true);
        } else {
            diag.setOralCleftLipRepaired(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unilateral);

        if (sw.isChecked()) {
            diag.setOralCleftPalateUnilateral(true);
        } else {
            diag.setOralCleftPalateUnilateral(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_bilateral);

        if (sw.isChecked()) {
            diag.setOralCleftPalateBilateral(true);
        } else {
            diag.setOralCleftPalateBilateral(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_unrepaired);

        if (sw.isChecked()) {
            diag.setOralCleftPalateUnrepaired(true);
        } else {
            diag.setOralCleftPalateUnrepaired(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_cleft_palate_repaired);

        if (sw.isChecked()) {
            diag.setOralCleftPalateRepaired(true);
        } else {
            diag.setOralCleftPalateRepaired(false);
        }

        sw = (Switch) m_view.findViewById(R.id.oral_speech_problem);

        if (sw.isChecked()) {
            diag.setOralSpeechProblem(true);
        } else {
            diag.setOralSpeechProblem(false);
        }

        sw = (Switch) m_view.findViewById(R.id.nose_deviated_septum);

        if (sw.isChecked()) {
            diag.setNoseDeviatedSeptum(true);
        } else {
            diag.setNoseDeviatedSeptum(false);
        }

        sw = (Switch) m_view.findViewById(R.id.nose_turbinate_hypertrophy);

        if (sw.isChecked()) {
            diag.setNoseTurbinateHypertrophy(true);
        } else {
            diag.setNoseTurbinateHypertrophy(false);
        }

        sw = (Switch) m_view.findViewById(R.id.nose_deformity_secondary_to_cleft_palate);

        if (sw.isChecked()) {
            diag.setNoseDeformitySecondaryToCleftPalate(true);
        } else {
            diag.setNoseDeformitySecondaryToCleftPalate(false);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_hemifacial_microsomia_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setSyndromeHemifacialMicrosomia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setSyndromeHemifacialMicrosomia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setSyndromeHemifacialMicrosomia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setSyndromeHemifacialMicrosomia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_pierre_robin_syndrome_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            diag.setSyndromePierreRobin(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            diag.setSyndromePierreRobin(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            diag.setSyndromePierreRobin(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            diag.setSyndromePierreRobin(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

        Editable text = t.getText();

        diag.setComment(text.toString());

        return diag;
    }

    private boolean validateFields()
    {
        return true;
    }

    private void getENTDiagnosisExtraDataFromREST(final ENTDiagnosis diagnosis)
    {
        m_sess.clearENTExtraDiagnosisList();
        m_sess.clearENTDiagnosisExtraDeleteList();

        Thread thread = new Thread(){
            public void run() {
                if (m_sess.getENTExtraDiagnoses(diagnosis.getId()) == true) {
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

    class UpdateENTDiagnosisListener implements RESTCompletionListener {

        private ENTDiagnosis m_diagnosis = null;

        public void setDiagnosis(ENTDiagnosis diagnosis) {
            m_diagnosis = diagnosis;

        }
        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = m_diagnosis.getId();
                m_sess.updateENTDiagnosisExtra(id);
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

    class CreateENTDiagnosisListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = a.getInt("id");
                m_sess.updateENTDiagnosisExtra(id);
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

    void updateENTDiagnosis()
    {
        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTDiagnosisREST rest = new ENTDiagnosisREST(m_sess.getContext());

                Object lock;
                int status;

                if (m_sess.getNewENTDiagnosis() == true) {
                    CreateENTDiagnosisListener listener = new CreateENTDiagnosisListener();
                    rest.addListener(listener);
                    lock = rest.createENTDiagnosis(copyENTDiagnosisDataFromUI());
                } else {
                    ENTDiagnosis hist = copyENTDiagnosisDataFromUI();
                    UpdateENTDiagnosisListener listener = new UpdateENTDiagnosisListener();
                    listener.setDiagnosis(m_entDiagnosis);
                    rest.addListener(listener);
                    lock = rest.updateENTDiagnosis(hist);
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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_ent_diagnosis), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_entDiagnosis = copyENTDiagnosisDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_ent_diagnosis), Toast.LENGTH_LONG).show();
                            m_sess.setNewENTDiagnosis(false);
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
            m_entDiagnosis = (ENTDiagnosis) bundle.getSerializable("diagnosis");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_ent_diagnosis_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
        m_this = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        copyENTDiagnosisDataToUI();
        setViewDirtyListeners();
        final View addButton = m_activity.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                handleAddButtonPress(addButton);
            }
        });
        if (m_sess.getNewENTDiagnosis() == true) {
            setDirty();
        } else {
            clearDirty();
            getENTDiagnosisExtraDataFromREST(m_entDiagnosis);
        }
        final View removeButton = (Button) m_activity.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                handleRemoveButtonPress(removeButton);
            }
        });
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
        View view = inflater.inflate(R.layout.app_ent_diagnosis_layout, container, false);
        m_view  = view;
        setButtonBarCallbacks();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}