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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
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
    private static String m_clinicStationName = "";
    private static int m_stationStationId = 0;
    private static int m_clinicStationId = 0;
    private int m_selectorNumColumns;
    private int m_width = -1;
    private int m_height = -1;
    private static JSONObject m_queueStatusJSON = null;
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<String, Integer> m_clinicStationNameToId = new HashMap<String, Integer>();
    private static HashMap<Integer, Integer> m_clinicStationToStation = new HashMap<Integer, Integer>();
    private static HashMap<Integer, JSONObject> m_clinicStationToData = new HashMap<Integer, JSONObject>();
    private static HashMap<String, Integer> m_stationToSelector = new HashMap<String, Integer>();
    private ArrayList<Integer> m_activePatients = new ArrayList<Integer>();
    private ArrayList<Integer> m_waitingPatients = new ArrayList<Integer>();
    private int m_displayPatientId; // id of the patient that will get checked in/checked out when the corresponding button is pressed
    private int m_displayRoutingSlipEntryId; // id of the routingslip for m_displayPatientId
    // XXX Consider moving these station class names to the API

    public void initStationNameToSelectorMap()
    {
        m_stationToSelector.clear();
        m_stationToSelector.put("Audiology", R.drawable.audiology_selector);
        m_stationToSelector.put("Dental", R.drawable.dental_selector);
        m_stationToSelector.put("ENT", R.drawable.ent_selector);
        m_stationToSelector.put("Ortho", R.drawable.ortho_selector);
        m_stationToSelector.put("Speech", R.drawable.speech_selector);
        m_stationToSelector.put("Surgery Screening", R.drawable.surgery_selector);
        m_stationToSelector.put("X-Ray", R.drawable.xray_selector);
    }

    public int getStationIconResource(int id) {
        String name = m_stationIdToName.get(id);
        return m_stationToSelector.get(name);
    }

    private void getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        m_width = (int) (metrics.widthPixels / metrics.density);
        m_height = (int) (metrics.heightPixels / metrics.density);
    }

    public int getSelectorNumColumns()
    {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(m_ctx);
        }
        m_selectorNumColumns = m_width / 250;
        return m_selectorNumColumns;
    }

    public int getQueueEntryId(int clinicStationId, int patientId)
    {
        JSONArray queues;
        int ret = -1;

        try {
            queues = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < queues.length(); i++) {
                JSONObject o = queues.getJSONObject(i);
                int clinicstation = o.getInt("clinicstation");
                if (clinicstation == clinicStationId) {
                    JSONArray entries = o.getJSONArray("entries");
                    for (int j = 0; j < entries.length(); j++) {
                        JSONObject entry = entries.getJSONObject(j);
                        int patient = entry.getInt("patient");
                        if (patient == patientId) {
                            ret = entry.getInt("id");
                            break;
                        }
                    }
                }
                if (ret != -1) {
                    break;
                }
            }
        } catch (JSONException e) {
        }
        return ret;
    }

    public int getScreenWidth()
    {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(m_ctx);
        }
        return m_width;
    }

    public int getScreenHeight()
    {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(m_ctx);
        }
        return m_height;
    }

    public int getSelector(String name) {
        int ret = R.drawable.medical_selector;

        if (m_clinicStationNameToId.containsKey(name)) {
            int id = m_clinicStationNameToId.get(name);
            if (m_clinicStationToStation.containsKey(id)) {
                id = m_clinicStationToStation.get(id);
                if (m_stationIdToName.containsKey(id)) {
                    name = m_stationIdToName.get(id);
                    if (m_stationToSelector.containsKey(name)) {
                        ret = m_stationToSelector.get(name);
                    }
                }
            }
        }
        return ret;
    }

    public void setToken(String token) {
        m_token = String.format("Token %s", token);
    }

    public String getToken() {
        return m_token;
    }

    public void setClinicStationName(String name) {
        m_clinicStationName = name;
    }

    public String getClinicStationName() {
        return m_clinicStationName;
    }

    public void setStationStationId(int id) {
        m_stationStationId = id;
    }

    public int getStationStationId() { return m_stationStationId; }

    public JSONObject getClinicStationData() { return m_clinicStationToData.get(m_clinicStationId);}

    public void setClinicStationId(int id) {
        m_clinicStationId = id;
    }

    public int getClinicStationId() {
        return m_clinicStationId;
    }

    public void setQueueStatusJSON(JSONObject obj)
    {
        m_queueStatusJSON = obj;
    }

    public boolean isAway() {
        JSONObject o = m_clinicStationToData.get(m_clinicStationId);
        boolean isAway = false;
        try {
            isAway = o.getBoolean("away");
        } catch (JSONException e) {
        }
        return isAway;
    }

    public void clearPatientData()
    {
        m_patientData.clear();
    }

    public boolean isActive() {
        JSONObject o = m_clinicStationToData.get(m_clinicStationId);
        boolean isActive = false;
        try {
            isActive = o.getBoolean("active");
        } catch (JSONException e) {
        }
        return isActive;
    }

    public boolean isWaiting() {
        return (isActive() == false && isAway() == false);
    }

    public int getActivePatientId()
    {
        JSONObject o = m_clinicStationToData.get(m_clinicStationId);
        int id = -1;
        if (isActive()) {
            try {
                id = o.getInt("activepatient");
            } catch (JSONException e) {
            }
        }
        return id;
    }

    public PatientItem getActivePatientItem()
    {
        PatientItem item = null;

        if (isActive()) {
            int id = getActivePatientId();

            if (id != -1) {
                JSONObject o = getPatientData(id);
                if (o != null) {
                    item = new PatientItem(String.format("%d", id), "", "", o);
                }
            }
        }
        return item;
    }

    public boolean isWaitingForThisClinicStation(int patient) {
        boolean ret = false;
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    int clinicstation = o.getInt("clinicstation");
                    if (clinicstation == getClinicStationId()) {
                        JSONArray entries = o.getJSONArray("entries");

                        for (int j = 0; j < entries.length(); j++) {
                            JSONObject entry = entries.getJSONObject(j);
                            int patientid = entry.getInt("patient");
                            if (patientid == patient && clinicstation == getClinicStationId()) {
                                ret = true;
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                }
            }
        } catch (JSONException e) {
        }
        return ret;
    }

    public PatientItem getWaitingPatientItem()
    {
        PatientItem item = null;

        if (isWaiting()) {
            if (m_waitingPatients.isEmpty() == false)
            {
                for (int i = 0; i < m_waitingPatients.size(); i++) {
                    int id = m_waitingPatients.get(i);    // first item is next in list
                    if (id != -1 && isWaitingForThisClinicStation(id)) {
                        JSONObject o = getPatientData(id);
                        if (o != null) {
                            item = new PatientItem(String.format("%d", id), "", "", o);
                            break;
                        }
                    }
                }
            }
        }
        return item;
    }

    public void setClinicId(int id) {
        m_clinicId = id;
    }

    public String getActiveStationName() {
        return m_stationIdToName.get(m_stationStationId);
    }

    public void addStationData(JSONArray data) {
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
                m_clinicStationData.add(stationdata);
                m_clinicStationToStation.put(stationdata.getInt("id"), stationdata.getInt("station"));
                m_clinicStationNameToId.put(stationdata.getString("name"), stationdata.getInt("id"));
                m_clinicStationToData.put(stationdata.getInt("id"), stationdata);
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

            if (id == m_clinicStationId) {
                // this station, so skip
                continue;
            }

            if (stationId == m_stationStationId) {
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

    public void setDisplayPatientId(int id)
    {
        m_displayPatientId = id;
    }

    public int getDisplayPatientId()
    {
        return m_displayPatientId;
    }

    public int getDisplayRoutingSlipEntryId(int clinicstationId, int patientId)
    {
        int routingslipEntryId = -1;
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    int clinicstation = o.getInt("clinicstation");
                    if (clinicstation == clinicstationId) {
                        JSONArray entries = o.getJSONArray("entries");

                        for (int j = 0; j < entries.length(); j++) {
                            JSONObject entry = entries.getJSONObject(j);
                            int patientid = entry.getInt("patient");
                            if (patientid == patientId) {
                                routingslipEntryId = entry.getInt("routingslipentry");
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                }
            }
        } catch (JSONException e) {
        }
        return routingslipEntryId;
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
                        int clinicstation = o.getInt("clinicstation");
                        int stationId = m_clinicStationToStation.get(clinicstation);
                        JSONArray entries = o.getJSONArray("entries");
                        if (stationId == m_stationStationId) {
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
            JSONObject pData = m_patientData.get(val);
            content = String.format("Content %d", val);
            details = String.format("Details %d", val);

            PatientItem item = new PatientItem(id, content, details, pData);
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


