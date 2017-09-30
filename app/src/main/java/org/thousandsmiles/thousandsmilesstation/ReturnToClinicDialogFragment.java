package org.thousandsmiles.thousandsmilesstation;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

public class ReturnToClinicDialogFragment extends DialogFragment {

    private int m_patientId;
    private View m_view;

    public void setPatientId(int id)
    {
        m_patientId = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.return_to_clinic_dialog, null);
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
                    }
                })
                .setNegativeButton(R.string.checkout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_checkout_dialog);
        return ret;
    }
}