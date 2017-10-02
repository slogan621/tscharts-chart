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

import android.os.AsyncTask;
import android.widget.Toast;

public class StationAway extends AsyncTask<Object, Object, Object> {

    private AwayParams m_params;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            m_params = (AwayParams) params[0];
            goAway();
        }
        return "";
    }

    private void goAway()
    {
        int clinicStationId = m_sess.getClinicStationId();

        int status;
        Object lock;

        final ClinicStationREST clinicStationREST = new ClinicStationREST(m_sess.getContext());
        lock = clinicStationREST.putStationIntoAwayState(clinicStationId, m_params.getReturnMinutes());

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
            StationActivity.instance.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(StationActivity.instance, "Successfully placed station in away state", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            StationActivity.instance.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(StationActivity.instance, "Unable to place station in away state", Toast.LENGTH_SHORT).show();
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