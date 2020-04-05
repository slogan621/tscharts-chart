package org.thousandsmiles.thousandsmileschart;

import android.content.Context;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.StationREST;

public class StationData implements RESTCompletionListener {

    private Context m_context;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    public void setContext(Context context)
    {
        m_context = context;
    }

    private Context getContext()
    {
        return m_context;
    }
    public void onSuccess(int code, String message, JSONArray a)
    {
        m_sess.addStationData(a);
    }

    public void onSuccess(int code, String message, JSONObject a)
    {
    }

    public void onSuccess(int code, String message)
    {
    }

    public void onFail(int code, String message)
    {
    }

    public boolean updateStationData() {
        boolean ret = false;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final StationREST stationData = new StationREST(getContext());
            stationData.addListener(this);
            Object lock = stationData.getStationData();

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

            int status = stationData.getStatus();
            if (status == 200) {
                ret = true;
            }
        }
        return ret;
    }
}

