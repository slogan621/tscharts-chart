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

package org.thousandsmiles.thousandsmilesstation;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.ReturnToClinicStationREST;

public class MarkReturnToClinicStationCheckedOutDestHelper extends AsyncTask<Object, Object, Object> {
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private boolean m_success = false;
    private int m_rtcsId = -1;

    private class GetReturnToClinicStationHandler implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
            if (a.length() > 1) {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, R.string.msg_more_than_one_matching_returntoclinicstation_returned, Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (a.length() == 1) {
                try {
                    JSONObject o = (JSONObject) a.get(0);
                    m_rtcsId = o.getInt("id");
                    m_success = true;
                } catch (JSONException e) {
                }
            }
        }
    }

    @Override
    protected String doInBackground(Object... params) {
        run();
        return "";
    }

    private void run()
    {
        int patientId = m_sess.getDisplayPatientId();
        int clinicId = m_sess.getClinicId();
        int stationId = m_sess.getStationStationId();

        Object lock;
        int status;

        final ReturnToClinicStationREST rtcsREST = new ReturnToClinicStationREST(m_sess.getContext());
        rtcsREST.addListener(new GetReturnToClinicStationHandler());
        lock = rtcsREST.getReturnToClinicStation(clinicId, patientId, stationId, "scheduled_dest");

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

        if (m_success == true && m_rtcsId != -1) {
            lock = rtcsREST.setState(m_rtcsId, "checked_out_dest");

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
            status = rtcsREST.getStatus();
            if (status != 200) {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, R.string.unable_to_set_return_to_clinic_state_to_checked_out_dest, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, R.string.msg_successfully_updated_return_to_clinic_state_to_checked_out_dest, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        m_sess.setListWasClicked(false);
        m_sess.setDisplayPatientId(-1);

        /* ignore return, either we found it or we didn't and the rest completion handler will deal with it */
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