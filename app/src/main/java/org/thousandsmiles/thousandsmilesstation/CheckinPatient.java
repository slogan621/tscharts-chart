/*
 * (C) Copyright Syd Logan 2018
 * (C) Copyright Thousand Smiles Foundation 2018
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

/*
 * This class is a helper class that is coupled tightly to the StationActivity class
 */

package org.thousandsmiles.thousandsmilesstation;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;

public class CheckinPatient extends AsyncTask<Object, Object, Object> implements RESTCompletionListener {
    private StationActivity m_stationActivity;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        checkinPatient();
        return "";
    }

    public void setStationActivity(StationActivity p)
    {
        m_stationActivity = p;
    }

    public void onFail(int code, String msg)
    {
        m_stationActivity.runOnUiThread(new Runnable() {
            public void run() {
                m_stationActivity.setButtonEnabled(true);
            }
        });
    }

    public void onSuccess(int code, String msg)
    {
    }

    public void onSuccess(int code, String msg, JSONObject o)
    {
    }

    public void onSuccess(int code, String msg, JSONArray a)
    {
    }

    private void checkinPatient()
    {
        int patientId = m_sess.getDisplayPatientId();
        int clinicStationId = m_sess.getClinicStationId();
        int queueEntryId = m_sess.getQueueEntryId(patientId);
        int routingSlipEntryId = m_sess.setDisplayRoutingSlipEntryId(patientId);

        m_stationActivity.runOnUiThread(new Runnable() {
            public void run() {
                m_stationActivity.setButtonEnabled(false);
            }
        });

        final QueueREST queueREST = new QueueREST(m_sess.getContext());
        queueREST.addListener(this);
        Object lock = queueREST.deleteQueueEntry(queueEntryId);

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

        int status = queueREST.getStatus();
        if (status == 200) {
            final ClinicStationREST clinicStationREST = new ClinicStationREST(m_sess.getContext());
            clinicStationREST.addListener(this);
            lock = clinicStationREST.updateActiveClinicStationPatient(clinicStationId, patientId);

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
            status = clinicStationREST.getStatus();
            if (status == 200) {
                final RoutingSlipEntryREST rseREST = new RoutingSlipEntryREST(m_sess.getContext());
                rseREST.addListener(this);
                lock = rseREST.markRoutingSlipStateCheckedIn(routingSlipEntryId);

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
                status = rseREST.getStatus();
                if (status == 200 ) {
                    final StateChangeREST stateChangeREST = new StateChangeREST(m_sess.getContext());
                    stateChangeREST.addListener(this);
                    lock = stateChangeREST.stateChangeCheckin(clinicStationId, patientId);

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
                    status = stateChangeREST.getStatus();
                    if (status == 200) {
                        m_stationActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                m_stationActivity.setButtonEnabled(true);
                                m_stationActivity.showMedicalHistory();
                                Toast.makeText(m_stationActivity, R.string.msg_patient_signed_in, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        m_stationActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(m_stationActivity, R.string.msg_unable_to_update_state_change, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    m_stationActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_stationActivity, R.string.msg_unable_to_update_routing_slip, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                m_stationActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(m_stationActivity, R.string.msg_unable_to_set_clinic_station_state, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            m_stationActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_stationActivity, R.string.msg_unable_to_delete_queue_entry, Toast.LENGTH_SHORT).show();
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