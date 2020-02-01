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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.ENTHistoryExtra;

public class ENTHistoryExtraDialogFragment extends DialogFragment {

    private View m_view;
    private Activity m_activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.ent_history_extra_dialog, null);
        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ENTHistoryExtra extra = new ENTHistoryExtra();

                        // read dialog contents, and then poke them into the parent dialog.
                        View v = m_activity.findViewById(R.id.extra_container);
                        EditText t = (EditText) m_view.findViewById(R.id.condition_name);
                        extra.setName(t.getText().toString());
                        Boolean cb1, cb2;

                        cb1 = ((CheckBox) m_view.findViewById(R.id.checkbox_ent_extra_left)).isChecked();
                        cb2 = ((CheckBox) m_view.findViewById(R.id.checkbox_ent_extra_right)).isChecked();

                        if (cb1 && cb2) {
                            extra.setSide(ENTHistory.EarSide.EAR_SIDE_BOTH);
                        } else if (cb1) {
                            extra.setSide(ENTHistory.EarSide.EAR_SIDE_LEFT);
                        } else if (cb2) {
                            extra.setSide(ENTHistory.EarSide.EAR_SIDE_RIGHT);
                        } else {
                            extra.setSide(ENTHistory.EarSide.EAR_SIDE_NONE);
                        }



                        RadioButton rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_extra_duration_none);
                        if (rb.isChecked()) {
                            extra.setDuration(ENTHistory.ENTDuration.EAR_DURATION_NONE);
                        }
                        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_extra_duration_days);
                        if (rb.isChecked()) {
                            extra.setDuration(ENTHistory.ENTDuration.EAR_DURATION_DAYS);
                        }
                        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_extra_duration_weeks);
                        if (rb.isChecked()) {
                            extra.setDuration(ENTHistory.ENTDuration.EAR_DURATION_WEEKS);
                        }
                        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_extra_duration_months);
                        if (rb.isChecked()) {
                            extra.setDuration(ENTHistory.ENTDuration.EAR_DURATION_MONTHS);
                        }
                        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_extra_duration_intermittent);
                        if (rb.isChecked()) {
                            extra.setDuration(ENTHistory.ENTDuration.EAR_DURATION_INTERMITTENT);
                        }

                        SessionSingleton.getInstance().addENTExtraHistory(extra);
                    }
                })
                .setNegativeButton(R.string.checkout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_ent_history_extra_dialog);
        return ret;
    }
}