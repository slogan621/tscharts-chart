/*
 * (C) Copyright Syd Logan 2017-2021
 * (C) Copyright Thousand Smiles Foundation 2017-2021
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
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ReturnToClinicDialogFragment extends DialogFragment {

    private int m_patientId;
    private View m_view;
    StationActivity m_stationActivity;
    SessionSingleton m_sess = SessionSingleton.getInstance();

    public void setPatientId(int id)
    {
        m_patientId = id;
    }
    public void setStationActivity(StationActivity p)
    {
        m_stationActivity = p;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final RoutingSlipEntry routingSlipEntry = m_sess.currentStationInRoutingSlip();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.return_to_clinic_dialog, null);

        LinearLayout ll = m_view.findViewById(R.id.layout_remove_routing_slip);
        CheckBox cb = m_view.findViewById(R.id.checkbox_remove_routing_slip);

        if (routingSlipEntry == null) {
            ll.setVisibility(GONE);
        } else {
            ll.setVisibility(VISIBLE);
            cb.setChecked(true);
            String text = getString(R.string.label_checkbox_remove_from_routingslip_formatted, routingSlipEntry.getName());
            cb.setText(text);
        }

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.checkout_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int numMonths = 0;
                        String msg = "";
                        RadioButton v = (RadioButton) m_view.findViewById(R.id.checkout_returnNo);
                        if (v.isChecked()) {
                            numMonths = 0;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.checkout_return3);
                        if (v.isChecked()) {
                            numMonths = 3;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.checkout_return6);
                        if (v.isChecked()) {
                            numMonths = 6;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.checkout_return9);
                        if (v.isChecked()) {
                            numMonths = 9;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.checkout_return12);
                        if (v.isChecked()) {
                            numMonths = 12;
                        }
                        EditText t = (EditText) m_view.findViewById(R.id.checkout_msg);
                        msg = t.getText().toString();

                        CheckoutParams params = new CheckoutParams();

                        params.setMessage(msg);
                        params.setReturnMonths(numMonths);
                        CheckoutPatient task = new CheckoutPatient();
                        task.setStationActivity(m_stationActivity);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) params);
                        dialog.dismiss();

                        CheckBox cb = m_view.findViewById(R.id.checkbox_remove_routing_slip);
                        if (cb.isChecked() == true) {
                            new Thread(() -> {
                                int status = m_sess.deleteRoutingSlipEntry(m_stationActivity.getApplicationContext(), routingSlipEntry.getId());
                                if (status == 200) {
                                    m_stationActivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(m_stationActivity, m_stationActivity.getString(R.string.msg_routing_slip_entry_successfully_removed), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {

                                    m_stationActivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(m_stationActivity, m_stationActivity.getString(R.string.msg_routing_slip_entry_unsuccessfully_removed), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                })
                .setNegativeButton(R.string.checkout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_checkout_dialog);
        return ret;
    }
}