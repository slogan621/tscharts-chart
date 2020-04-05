package org.thousandsmiles.thousandsmileschart;

/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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
import android.os.Looper;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.PatientREST;

public class SetPatientOldIDTask extends AsyncTask<Object, Object, Object> {
    private Activity m_activity;
    private PatientData m_patientData;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            m_patientData = (PatientData) params[0];
            m_activity = (Activity) params[1];
            updatePatientOldId(m_patientData.getId(), m_patientData.getOldId());
        }
        return "";
    }

    private boolean updatePatientOldId(int patient, int oldId)
    {
        boolean ret = true;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final PatientREST patientREST = new PatientREST(m_activity);

            try {
              Object lock =  patientREST.updatePatientOldId(patient, oldId);

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

                int status = patientREST.getStatus();
                if (status != 200) {
                    ret = false;
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_failed_to_update_patient_old_id), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch (Exception e) {
            }
        }

        if (ret == true) {
            ret = m_sess.setPatientOldId(m_patientData.getId(), m_patientData.getOldId());
        }
        if (ret == true) {
            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_successful_update_old_id), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_unsuccessful_update_old_id), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return ret;
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
