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

import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;

/**
 * Created by slogan on 9/30/17.
 */

public class CheckoutPatient extends AsyncTask<Object, Object, Object> {

    private CheckoutParams m_params;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            m_params = (CheckoutParams) params[0];
            checkoutPatient();
        }
        return "";
    }

    private void checkoutPatient()
    {
        int patientId = m_sess.getDisplayPatientId();
        int clinicStationId = m_sess.getClinicStationId();
        int routingSlipEntryId = m_sess.getDisplayRoutingSlipEntryId();

        int status;
        Object lock;

        final ClinicStationREST clinicStationREST = new ClinicStationREST(m_sess.getContext());
        lock = clinicStationREST.putStationIntoWaitingState(clinicStationId);

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
            lock = rseREST.markRoutingSlipStateCheckedOut(routingSlipEntryId);

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
                lock = stateChangeREST.stateChangeCheckout(clinicStationId, patientId);

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
                    StationActivity.instance.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(StationActivity.instance, R.string.msg_patient_successfully_checked_out, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    StationActivity.instance.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(StationActivity.instance, R.string.msg_unable_to_update_state_change_object, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, R.string.unable_to_update_routing_slip, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            StationActivity.instance.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(StationActivity.instance, R.string.unable_to_set_clinic_station_state, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (m_params.getReturnMonths() != 0) {
            final ReturnToClinicREST returnToClinicREST = new ReturnToClinicREST(m_sess.getContext());
            lock = returnToClinicREST.returnToClinic(m_sess.getClinicId(), m_sess.getStationStationId(), patientId, m_params.getReturnMonths(), m_params.getMessage());

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
                        Toast.makeText(StationActivity.instance, R.string.msg_return_to_clinic_successfully_created, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, R.string.msg_unable_to_create_return_to_clinic, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        m_sess.setListWasClicked(false);
        m_sess.setDisplayPatientId(-1);
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