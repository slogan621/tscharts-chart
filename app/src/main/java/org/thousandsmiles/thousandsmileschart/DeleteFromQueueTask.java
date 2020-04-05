package org.thousandsmiles.thousandsmileschart;

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

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;

import java.util.ArrayList;

public class DeleteFromQueueTask extends AsyncTask<Object, Object, Object> {

    private PatientData m_params;
    private Activity m_activity;
    private int m_stationId;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            m_params = (PatientData) params[0];
            m_stationId = (int) params[1];
            m_activity = (Activity) params[2];
            deleteRoutingSlipEntry();
        }
        return "";
    }

    private void deleteRoutingSlipEntry()
    {
        int status;
        Object lock;

        ArrayList<RoutingSlipEntry> entries  = m_sess.getRoutingSlipEntries(m_sess.getClinicId(), m_params.getId());

        boolean found = false;
        int routingSlipEntry = 0;

        for (int i = 0; i < entries.size(); i++) {
           RoutingSlipEntry x = entries.get(i);
           if (x.getStation() == m_stationId) {
               routingSlipEntry = x.getId();
               found = true;
               break;
           }
        }

        if (found == false) {
            return;
        }

        final RoutingSlipEntryREST rse = new RoutingSlipEntryREST(m_sess.getContext());
        lock = rse.deleteRoutingSlipEntry(routingSlipEntry);

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
        status = rse.getStatus();
        if (status == 200) {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_successfully_removed), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_unsuccessfully_removed), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // This is called from background thread but runs in UI
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        // Do things like update the progress bar
    }

    // This runs in UI when background thread finishes
    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);

        // Do things like hide the progress bar or change a TextView
    }
}