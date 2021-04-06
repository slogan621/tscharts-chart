/*
 * (C) Copyright Syd Logan 2021
 * (C) Copyright Thousand Smiles Foundation 2021
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.thousandsmiles.tscharts_lib.PatientData;

public class ResetPasswordDialogFragment extends DialogFragment {

    private View m_view;
    private int m_patientId;
    PatientData m_patientData;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    public void setPatientId(int id)
    {
        m_patientId = id;
    }

    public void setPatientData(PatientData data)
    {
        m_patientData = data;
    }

    /*
    private void getReturnToClinicData() {
        new Thread(new Runnable() {
            public void run() {
                m_sess.getReturnToClinics();
            };
        }).start();
    }
    */

    @Override
    public void onResume()
    {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog ret = null;
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        m_view = inflater.inflate(R.layout.reset_password_dialog, null);

        builder.setView(m_view)
                    // Add action buttons
                    .setPositiveButton(R.string.action_reset_password, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            m_sess.getCommonSessionSingleton().cancelHeadshotImages();
                            m_sess.setDisplayPatientId(m_patientId);
                            //getReturnToClinicData();
                            m_sess.getCommonSessionSingleton().setPhotoPath(m_sess.getCommonSessionSingleton().getHeadShotPath(m_patientId));
                            Intent i = new Intent(getContext(), StationActivity.class);
                            startActivity(i);
                            getActivity().finish();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        ret = builder.create();
        ret.setTitle(R.string.action_reset_password);

        return ret;
    }
}