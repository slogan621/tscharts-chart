package org.thousandsmiles.thousandsmilesstation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class AwayDialogFragment extends DialogFragment {

    private View m_view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.away_dialog, null);
        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.away_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int numMinutes = 0;
                        RadioButton v = (RadioButton) m_view.findViewById(R.id.away_return5);
                        if (v.isChecked()) {
                            numMinutes = 5;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.away_return15);
                        if (v.isChecked()) {
                            numMinutes = 15;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.away_return30);
                        if (v.isChecked()) {
                            numMinutes = 30;
                        }
                        v = (RadioButton) m_view.findViewById(R.id.away_return60);
                        if (v.isChecked()) {
                            numMinutes = 60;
                        }

                        AwayParams params = new AwayParams();

                        params.setReturnMinutes(numMinutes);
                        AsyncTask task = new StationAway();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) params);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.checkout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_away_dialog);
        return ret;
    }
}