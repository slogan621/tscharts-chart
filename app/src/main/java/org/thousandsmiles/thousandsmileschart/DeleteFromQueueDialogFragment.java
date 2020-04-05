/*
 * (C) Copyright Syd Logan 2018-2019
 * (C) Copyright Thousand Smiles Foundation 2018-2019
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
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.thousandsmiles.tscharts_lib.HideyHelper;
import org.thousandsmiles.tscharts_lib.PatientData;

import java.util.ArrayList;

public class DeleteFromQueueDialogFragment extends DialogFragment {

    private View m_view;
    PatientData m_rd;
    Activity m_activity;
    int m_stationId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        m_rd = getArguments().getParcelable(null);
        m_activity = getActivity();
        m_stationId = SessionSingleton.getInstance().getStationIdFromName("X-Ray");
        AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.delete_queue_dialog, null);
        TextView text = (TextView) m_view.findViewById(R.id.title);

        String title = String.format(getString(R.string.msg_remove_from_queue),
                m_rd.getPatientFullName(true), m_rd.getId(),
                getResources().getString(R.string.xray_name));
        text.setText(title);

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AsyncTask task = new DeleteFromQueueTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) m_rd,
                                (Object) m_stationId, (Object) m_activity);
                        dialog.dismiss();
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(m_activity);
                    }
                })
                .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        HideyHelper h = new HideyHelper();
                        h.toggleHideyBar(m_activity);
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_delete_queue_dialog);
        return ret;
    }
}