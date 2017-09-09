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

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private static String m_token = "";
    private static int m_clinicId;
    private static Context m_ctx;
    private static ArrayList<JSONObject> m_clinicStationData = new ArrayList<JSONObject>();
    private static String m_activeStationName = "";
    private static int m_activeStationStationId = 0;
    private static int m_activeStationId = 0;
    private static JSONObject m_queueStatusJSON = null;
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<Integer, Integer> m_clinicStationToStation = new HashMap<Integer, Integer>();
    ArrayList<Integer> m_activePatients = new ArrayList<Integer>();
    ArrayList<Integer> m_waitingPatients = new ArrayList<Integer>();

    public void setToken(String token) {
        m_token = String.format("Token %s", token);
    }

    public String getToken() {
        return m_token;
    }

    public void setActiveStationName(String name) {
        m_activeStationName = name;
    }

    public void setActiveStationStationId(int id) {
        m_activeStationStationId = id;
    }

    public void setActiveStationId(int id) {
        m_activeStationId = id;
    }

    public void setQueueStatusJSON(JSONObject obj)
    {
        m_queueStatusJSON = obj;
    }

    public void setClinicId(int id) {
        m_clinicId = id;
    }

    public String getActiveStationName() {
        return m_stationIdToName.get(m_activeStationStationId);
    }

    public void addStationData(JSONArray data) {
        //int id;
        int i;
        JSONObject stationdata;

        for (i = 0; i < data.length(); i++)  {
            try {
                stationdata = data.getJSONObject(i);
                m_stationIdToName.put(stationdata.getInt("id"), stationdata.getString("name"));
            } catch (JSONException e) {
                return;
            }
        }
    }

    public void addClinicStationData(JSONArray data) {
        //int id;
        int i;
        JSONObject stationdata;

        for (i = 0; i < data.length(); i++)  {
            try {
                stationdata = data.getJSONObject(i);
                //id = stationdata.getInt("id");
                m_clinicStationData.add(stationdata);
                m_clinicStationToStation.put(stationdata.getInt("id"), stationdata.getInt("station"));
            } catch (JSONException e) {
                return;
            }
        }
    }

    public void clearClinicStationData() {
        m_clinicStationData.clear();
    }

    public JSONObject getClinicStationData(int id) {
        JSONObject o = null;
        if (m_clinicStationData != null) {
            o = m_clinicStationData.get(id);
        }
        return o;
    }

    public int getClinicStationCount() {
        return m_clinicStationData.size();
    }

    public boolean updateClinicStationData() {
        boolean ret = false;

        clearClinicStationData();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ClinicStationREST clinicStationData = new ClinicStationREST(getContext());
            Object lock = clinicStationData.getClinicStationData(m_clinicId);

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

            int status = clinicStationData.getStatus();
            if (status == 200) {
                ret = true;
            }
        }
        return ret;
    }

    public boolean updateStationData() {
        boolean ret = false;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final StationREST stationData = new StationREST(getContext());
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

    public JSONObject getPatientData(final int id) {

        JSONObject o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
            final PatientREST patientData = new PatientREST(getContext());
            Object lock = patientData.getPatientData(id);

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

            int status = patientData.getStatus();
            if (status == 200) {
                o = m_patientData.get(id);
            }
        }
        if (o == null) {
            return o;
        }
        return o;
    }

    public boolean updateActivePatientList()
    {
        boolean ret = true;
        int patient = 0;

        /*
           get a list of patients that are active for the
           class of this clinic station, e.g., "Dental".
         */

        m_activePatients.clear();

        for (int i = 0; i < m_clinicStationData.size(); i++) {
            JSONObject o;
            int stationId; // e.g., Dental, Ortho
            int id;        // clinicstation id
            boolean isActive;

            o = m_clinicStationData.get(i);
            try {

                id = o.getInt("id");
                stationId = m_clinicStationToStation.get(id); // this defines the class
                isActive = o.getBoolean("active");
                if (isActive) {
                    patient = o.getInt("activepatient");
                }

            } catch (JSONException e) {
                continue;   // skip it
            }

            if (!isActive) {
                // station is not active, skip
                continue;
            }

            if (id == m_activeStationId) {
                // this station, so skip
                continue;
            }

            if (stationId == m_activeStationStationId) {
                // same class as this station, so add to list

                JSONObject p = getPatientData(patient);
                if (p == null) {
                    ret = false;
                    break;
                }
                m_activePatients.add(patient);
            }
        }

        return ret;
    }

    public boolean updateQueues() {
        Object lock;
        int status;
        boolean ret = false;

        QueueREST queueData = new QueueREST(getContext());
        lock = queueData.getQueueData(getClinicId());
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

        status = queueData.getStatus();
        if (status == 200) {
            ret = true;
        }
        return ret;
    }

    public boolean updateWaitingPatientList() {
        boolean ret = true;

        m_waitingPatients.clear();
        /*
           get a list of all patients that are waiting in queues for
           stations that match the class of this station, e.g., "Dental"
         */

        if (m_queueStatusJSON == null) {
            ret = false;
        } else {

            try {
                JSONArray r = m_queueStatusJSON.getJSONArray("queues");
                for (int i = 0; i < r.length(); i++) {
                    try {
                        JSONObject o = r.getJSONObject(i);
                        int stationId = m_clinicStationToStation.get(o.getInt("clinicstation"));
                        JSONArray entries = o.getJSONArray("entries");
                        if (stationId == m_activeStationStationId) {
                            for (int j = 0; j < entries.length(); j++) {
                                JSONObject entry = entries.getJSONObject(j);
                                int patient = entry.getInt("patient");

                                JSONObject p = getPatientData(patient);
                                if (p == null) {
                                    continue;
                                }
                                m_waitingPatients.add(patient);
                            }
                        }
                    } catch (JSONException e) {
                        ret = false;
                    }
                }
            } catch (JSONException e) {
                ret = false;
            }
        }

        return ret;
    }

    private List<PatientItem> getPatientListData(ArrayList<Integer> list)
    {
        ArrayList<PatientItem> items = new ArrayList<PatientItem>();

        String id;
        String content;
        String details;

        for (int i = 0; i < list.size(); i++) {
            int val = list.get(i);
            id = String.format("%d", val);
            content = String.format("Content %d", val);
            details = String.format("Details %d", val);

            PatientItem item = new PatientItem(id, content, details);
            items.add(item);
        }
        return items;
    }

    public List<PatientItem> getActivePatientListData()
    {
        return getPatientListData(m_activePatients);
    }

    public List<PatientItem> getWaitingPatientListData()
    {
        return getPatientListData(m_waitingPatients);
    }

    public int getClinicId() {
        return m_clinicId;
    }

    public void addPatientData(JSONObject data) {
        int id;

        try {
            id = data.getInt("id");
        } catch (JSONException e) {
            return;
        }
        m_patientData.put(id, data);
    }

    public static SessionSingleton getInstance() {
        if (m_instance == null) {
            m_instance = new SessionSingleton();
        }
        return m_instance;
    }

    public void setContext(Context ctx) {
        m_ctx = ctx;
    }
    public Context getContext() {
        return m_ctx;
    }
}


