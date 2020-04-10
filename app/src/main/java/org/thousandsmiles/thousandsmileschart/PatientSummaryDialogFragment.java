/*
 * (C) Copyright Syd Logan 2019-2020
 * (C) Copyright Thousand Smiles Foundation 2019-2020
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.HideyHelper;
import org.thousandsmiles.tscharts_lib.PatientData;

import static android.view.View.GONE;

public class PatientSummaryDialogFragment extends DialogFragment {

    private View m_view;
    PatientData m_rd;
    Activity m_activity;
    PatientData m_patientData = new PatientData();
    AlertDialog.Builder m_builder;

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //updatePatientData();
    }

    @Override
    public void onActivityCreated(Bundle b) {
        super.onActivityCreated(b);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
    }

    public void updatePatientData()
    {
        TextView text;

        text = m_view.findViewById(R.id.value_id);
        text.setText(String.format("%d", m_patientData.getId()));
        text = m_view.findViewById(R.id.value_oldid);
        text.setText(String.format("%d", m_patientData.getOldId()));
        text = m_view.findViewById(R.id.value_curp);
        text.setText(String.format("%s", m_patientData.getCURP()));
        text = m_view.findViewById(R.id.value_dob);
        text.setText(String.format("%s", m_patientData.getDob()));
        text = m_view.findViewById(R.id.value_first);
        text.setText(String.format("%s", m_patientData.getFirst()));
        text = m_view.findViewById(R.id.value_middle);
        text.setText(String.format("%s", m_patientData.getMiddle()));
        text = m_view.findViewById(R.id.value_fatherlast);
        text.setText(String.format("%s", m_patientData.getFatherLast()));
        text = m_view.findViewById(R.id.value_motherlast);
        text.setText(String.format("%s", m_patientData.getMotherLast()));
        text = m_view.findViewById(R.id.value_gender);
        text.setText(String.format("%s", m_patientData.getGender()));
        text = m_view.findViewById(R.id.value_phone1);
        text.setText(String.format("%s", m_patientData.getPhone1()));
        text = m_view.findViewById(R.id.value_phone2);
        text.setText(String.format("%s", m_patientData.getPhone2()));
        text = m_view.findViewById(R.id.value_street1);
        text.setText(String.format("%s", m_patientData.getStreet1()));
        text = m_view.findViewById(R.id.value_street2);
        text.setText(String.format("%s", m_patientData.getStreet2()));
        text = m_view.findViewById(R.id.value_colonia);
        text.setText(String.format("%s", m_patientData.getColonia()));
        text = m_view.findViewById(R.id.value_state);
        text.setText(String.format("%s", m_patientData.getState()));
        text = m_view.findViewById(R.id.value_email);
        text.setText(String.format("%s", m_patientData.getEmail()));
        text = m_view.findViewById(R.id.value_recent_xrays);
        ImageView v = m_view.findViewById(R.id.img_recent_xrays);
        if (m_patientData.getIsCurrentXray() == false) {
            text.setText(String.format("%s", getString(R.string.no)));
            v.setVisibility(View.GONE);
        } else {
            text.setText(String.format("%s", getString(R.string.yes)));
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_rd = getArguments().getParcelable(null);
        m_activity = getActivity();
        m_patientData.fromJSONObject(SessionSingleton.getInstance().getPatientData(m_rd.getId()));

        // xray data should already be cached and not require REST call to backend

        m_patientData.setIsCurrentXray(CommonSessionSingleton.getInstance().hasCurrentXRay(m_rd.getId(), 365));
        m_builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater

        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.patient_summary_dialog, null);

        updatePatientData();

        m_builder.setView(m_view)
                // Add action buttons

                .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(m_activity);
                    }
                });
        Dialog ret = m_builder.create();
        ret.setTitle(R.string.title_patient_summary);
        return ret;
    }
}