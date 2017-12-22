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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MedicationsListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private int m_patientId;
    private View m_view;
    private MedicationsModelList m_list = MedicationsModelList.getInstance();

    public void setPatientId(int id)
    {
        m_patientId = id;
    }

    private String isCheckedOrNot(CheckBox checkbox) {
        if(checkbox.isChecked())
            return "is checked";
        else
            return "is not checked";
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
        TextView label = (TextView) v.getTag(R.id.label);
        CheckBox checkbox = (CheckBox) v.getTag(R.id.check);
        Toast.makeText(v.getContext(), label.getText().toString()+" "+isCheckedOrNot(checkbox), Toast.LENGTH_LONG).show();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.medications_list_dialog, null);

        ListView listView = (ListView) m_view.findViewById(R.id.medications_list);
        MedicationsAdapter adapter = new MedicationsAdapter(getActivity(), m_list.getModel());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.select_medications_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /*
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
                        AsyncTask task = new CheckoutPatient();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) params);
                        */
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.select_medications_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();

        ret.setTitle(R.string.title_select_medications_dialog);
        return ret;
    }
}