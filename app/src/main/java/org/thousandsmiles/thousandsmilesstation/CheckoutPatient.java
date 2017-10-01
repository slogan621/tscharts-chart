package org.thousandsmiles.thousandsmilesstation;

import android.os.AsyncTask;
import android.widget.Toast;

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
                            Toast.makeText(StationActivity.instance, "Patient successfully checked out", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    StationActivity.instance.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(StationActivity.instance, "Unable to update state change object", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, "Unable to update routing slip", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            StationActivity.instance.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(StationActivity.instance, "Unable to set clinic station state", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(StationActivity.instance, "Return to clinic successfully created", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                StationActivity.instance.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StationActivity.instance, "Unable to create return to clinic for patient", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        m_sess.setListWasClicked(false);
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