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
import android.widget.RadioButton;

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
                        // read dialog contents, and then poke them into the parent dialog.
                        View v = m_activity.findViewById(R.id.extra_container);
                        /*
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

                         */
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