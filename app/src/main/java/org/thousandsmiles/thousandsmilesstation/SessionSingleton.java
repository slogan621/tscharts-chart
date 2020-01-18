/*
 * (C) Copyright Syd Logan 2017-2020
 * (C) Copyright Thousand Smiles Foundation 2017-2020
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
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ENTExam;
import org.thousandsmiles.tscharts_lib.ENTExamREST;
import org.thousandsmiles.tscharts_lib.ENTHistory;
import org.thousandsmiles.tscharts_lib.ENTHistoryREST;
import org.thousandsmiles.tscharts_lib.MedicalHistory;
import org.thousandsmiles.tscharts_lib.MedicalHistoryREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;
import org.thousandsmiles.tscharts_lib.StationREST;
import org.thousandsmiles.tscharts_lib.XRay;
import org.thousandsmiles.tscharts_lib.XRayREST;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private static String m_token = "";
    private static Context m_ctx;
    private static ArrayList<JSONObject> m_clinicStationData = new ArrayList<JSONObject>();
    private static String m_clinicStationName = "";
    private static int m_stationStationId = 0;
    private static int m_clinicStationId = 0;
    private int m_selectorNumColumns;
    private int m_width = -1;
    private int m_height = -1;
    private boolean m_listWasClicked = false;
    private boolean m_waitingIsFromActive = false; // waiting patient from active list or not
    private static JSONObject m_queueStatusJSON = null;
    private JSONObject m_displayPatientRoutingSlip = null;
    private JSONObject m_routingSlipEntryResponse = null;
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<String, Integer> m_clinicStationNameToId = new HashMap<String, Integer>();
    private static HashMap<Integer, Integer> m_clinicStationToStation = new HashMap<Integer, Integer>();
    private static HashMap<Integer, JSONObject> m_clinicStationToData = new HashMap<Integer, JSONObject>();
    private static HashMap<String, Integer> m_stationToSelector = new HashMap<String, Integer>();
    private static HashMap<String, Integer> m_stationToUnvisitedSelector = new HashMap<String, Integer>();
    private static HashMap<String, String> m_stationToSpanish = new HashMap<String, String>();
    private ArrayList<Integer> m_activePatients = new ArrayList<Integer>();
    private ArrayList<Integer> m_waitingPatients = new ArrayList<Integer>();
    private int m_displayPatientId = -1; // id of the patient that will get checked in/checked out when the corresponding button is pressed
    private int m_displayRoutingSlipEntryId = -1; // id of the routingslip entry for m_displayPatientId
    // XXX Consider moving these station class names to the API
    private CommonSessionSingleton m_commonSessionSingleton = null;
    private boolean m_showAll = false;
    private boolean m_newMedHistory = false;
    private boolean m_newXRay = false;
    private boolean m_newENTExam = false;
    private boolean m_newENTHistory = false;

    public void setNewXRay(boolean val) {
        m_newXRay = val;
    }

    public boolean getNewXRay() {
        return m_newXRay;
    }

    public void setNewENTHistory(boolean val) {
        m_newENTHistory = val;
    }

    public boolean getNewENTHistory() {
        return m_newENTHistory;
    }

    public void setNewENTExam(boolean val) {
        m_newENTExam = val;
    }

    public boolean getNewENTExam() {
        return m_newENTExam;
    }

    public void setNewMedHistory(boolean val) {
        m_newMedHistory = val;
    }

    public boolean getNewMedHistory() {
        return m_newMedHistory;
    }

    public void setShowAll(boolean flag)
    {
        m_showAll = flag;
    }

    public CommonSessionSingleton getCommonSessionSingleton()
    {
        if (m_commonSessionSingleton == null) {
            m_commonSessionSingleton = CommonSessionSingleton.getInstance();
        }
        return m_commonSessionSingleton;
    }

    public void setRoutingSlipEntryResponse(JSONObject o)
    {
        m_routingSlipEntryResponse = o;
    }

    public JSONObject getRoutingSlipEntryResponse()
    {
        return m_routingSlipEntryResponse;
    }

    void initStationToSpanish() {
        m_stationToSpanish.put("Audiology", "Audiología");
        m_stationToSpanish.put("Dental", "Dental");
        m_stationToSpanish.put("ENT", "ENT");
        m_stationToSpanish.put("Ortho", "Orto");
        m_stationToSpanish.put("X-Ray", "Rayos X");
        m_stationToSpanish.put("Surgery Screening", "Cribado cirúrgico");
        m_stationToSpanish.put("Speech", "El habla");
        m_stationToSpanish.put("Hygiene", "Higiene");
    }

    public String getStationNameTranslated(String en)
    {
        String name;
        Locale current = m_ctx.getResources().getConfiguration().locale;

        if (m_stationToSpanish.size() == 0) {
            initStationToSpanish();
        }
        if (current.getLanguage().equals("es")) {
            name = m_stationToSpanish.get(en);
        } else {
            name = en;
        }
        return name;
    }

    public void setDisplayPatientRoutingSlip(JSONObject o)
    {
        m_displayPatientRoutingSlip = o;
    }

    public JSONObject getDisplayPatientRoutingSlip()
    {
        return m_displayPatientRoutingSlip;
    }

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
        m_stationToSelector.put("Hygiene", R.drawable.hygiene_selector);
    }

    public void initStationNameToUnvisitedSelectorMap()
    {
        m_stationToUnvisitedSelector.clear();
        m_stationToUnvisitedSelector.put("Audiology", R.drawable.audiology_unvisited_selector);
        m_stationToUnvisitedSelector.put("Dental", R.drawable.dental_unvisited_selector);
        m_stationToUnvisitedSelector.put("ENT", R.drawable.ent_unvisited_selector);
        m_stationToUnvisitedSelector.put("Ortho", R.drawable.ortho_unvisited_selector);
        m_stationToUnvisitedSelector.put("Speech", R.drawable.speech_unvisited_selector);
        m_stationToUnvisitedSelector.put("Surgery Screening", R.drawable.surgery_unvisited_selector);
        m_stationToUnvisitedSelector.put("X-Ray", R.drawable.xray_unvisited_selector);
        m_stationToUnvisitedSelector.put("Hygiene", R.drawable.hygiene_unvisited_selector);
    }

    public void setListWasClicked(boolean val)
    {
        m_listWasClicked = val;
    }

    public boolean getListWasClicked()
    {
        return m_listWasClicked;
    }

    public void setWaitingIsFromActiveList(boolean val)
    {
        m_waitingIsFromActive = val;
    }

    public boolean getWaitingIsFromActiveList()
    {
        return m_waitingIsFromActive;
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

    public int getQueueEntryId(int patientId)
    {
        JSONArray queues;
        int ret = -1;

        try {
            queues = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < queues.length(); i++) {
                JSONObject o = queues.getJSONObject(i);
                JSONArray entries = o.getJSONArray("entries");
                for (int j = 0; j < entries.length(); j++) {
                    JSONObject entry = entries.getJSONObject(j);
                    int patient = entry.getInt("patient");
                    if (patient == patientId) {
                        ret = entry.getInt("id");
                        break;
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

    public String getStationNameFromId(int id)
    {
        String ret = "";

        ret = m_stationIdToName.get(id);
        return ret;
    }

    public boolean isDentalStation() {
        boolean ret = false;
        int id = m_stationStationId;

        String name = getStationNameFromId(id);
        if (name.equals("Dental")) {
            ret = true;
        }
        return ret;
    }

    public boolean isENTStation() {
        boolean ret = false;
        int id = m_stationStationId;

        String name = getStationNameFromId(id);
        if (name.equals("ENT")) {
            ret = true;
        }
        return ret;
    }

    public boolean isXRayStation() {
        boolean ret = false;
        int id = m_stationStationId;

        String name = getStationNameFromId(id);
        if (name.equals("X-Ray")) {
            ret = true;
        }
        return ret;
    }

    public boolean isAudiologyStation() {
        boolean ret = false;
        int id = m_stationStationId;

        String name = getStationNameFromId(id);
        if (name.equals("Audiology")) {
            ret = true;
        }
        return ret;
    }

    public int getStationIdFromName(String name) {
        int ret = -1;
        Iterator it = m_stationIdToName.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            if (pair.getValue().equals(name)) {
                ret = (int) pair.getKey();
                break;
            }
        }
        return ret;
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

    private class GetDataListener implements RESTCompletionListener
    {
        private int m_patientId = 0;
        private JSONArray m_resultArray = null;

        public void setPatientId(int id)
        {
            m_patientId = id;
        }

        public JSONArray getResultArray() {
            return m_resultArray;
        }

        public void onSuccess(int code, String message, JSONArray a)
        {
            if (code == 200) {
                m_resultArray = a;
            }
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
                    item = new PatientItem(String.format("%d", id), "", "", o, false);
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
                            item = new PatientItem(String.format("%d", id), "", "", o, false);
                            break;
                        }
                    }
                }
            }
        }
        return item;
    }

    public void setClinicId(int id) {
        getCommonSessionSingleton().setClinicId(id);
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

    ArrayList<Station> getStationList()
    {
        ArrayList<Station> ret = new ArrayList<Station>();

        Iterator it = m_stationIdToName.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Station m = new Station();
            m.setName(pair.getValue().toString());
            m.setStation((int) pair.getKey());
            m.setSelector(m_stationToSelector.get(m.getName()));
            m.setUnvisitedSelector(m_stationToUnvisitedSelector.get(m.getName()));
            ret.add(m);
        }
        return ret;
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
            Object lock = clinicStationData.getClinicStationData(getClinicId());

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

    public int getDisplayRoutingSlipEntryId()
    {
        return m_displayRoutingSlipEntryId;
    }

    public void setDisplayRoutingSlipEntryId(int id) {
        m_displayRoutingSlipEntryId = id;
    }

    public int setDisplayRoutingSlipEntryIdForPatient(int patientId) {
        boolean found = false;
        if (m_queueStatusJSON != null) {
            try {
                JSONArray r = m_queueStatusJSON.getJSONArray("queues");
                int i = 0;
                while (found == false && i < r.length()) {
                    try {
                        JSONObject o = r.getJSONObject(i);

                        JSONArray entries = o.getJSONArray("entries");

                        for (int j = 0; j < entries.length(); j++) {
                            JSONObject entry = entries.getJSONObject(j);
                            int id = entry.getInt("patient");
                            if (id == patientId) {
                                m_displayRoutingSlipEntryId = entry.getInt("routingslipentry");
                                found = true;
                                break;
                            }
                        }

                    } catch (JSONException e) {
                    }
                    i++;
                }
            } catch (JSONException e) {
            }
        }

        return m_displayRoutingSlipEntryId;
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
                        if (m_showAll == true || stationId == m_stationStationId) {
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

    public class GetRoutingSlipListener implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
            SessionSingleton sess = SessionSingleton.getInstance();
            sess.setRoutingSlipEntryResponse(o);
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
        }
    }

    public RoutingSlipEntry getRoutingSlipEntry(int id) {
        boolean ret = false;

        RoutingSlipEntry entry = new RoutingSlipEntry();

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final RoutingSlipEntryREST rsData = new RoutingSlipEntryREST(getContext());
            rsData.addListener(new GetRoutingSlipListener());
            Object lock = rsData.getRoutingSlipEntry(id);

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

            int status = rsData.getStatus();
            if (status == 200) {
                JSONObject o = getRoutingSlipEntryResponse();
                try {
                    String state = o.getString("state");
                    int routingslip = o.getInt("routingslip");
                    int station = o.getInt("station");
                    int rsEntryid = o.getInt("id");
                    String name = m_stationIdToName.get(station);
                    entry.setName(name);
                    if (state.equals("Checked In") || state.equals("Checked Out")) {
                        entry.setVisited(true);
                    } else {
                        entry.setVisited(false);
                    }
                    entry.setStation(station);
                    entry.setId(rsEntryid);
                    entry.setSelector(m_stationToSelector.get(name));
                    entry.setSelector(m_stationToSelector.get(name));

                } catch (JSONException e) {
                    entry = null;
                }

            } else {
                entry = null;
            }
        }
        return entry;
    }

    public ArrayList<RoutingSlipEntry> getRoutingSlipEntries(int clinic, int patient) {
        boolean ret = false;

        ArrayList<RoutingSlipEntry> entries = new ArrayList<RoutingSlipEntry>();

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final RoutingSlipREST rsData = new RoutingSlipREST(getContext());
            Object lock = rsData.getRoutingSlip(clinic, patient);

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

            int status = rsData.getStatus();
            if (status == 200) {
                // iterate the routingslip entries and add them to the result
                JSONObject o = getDisplayPatientRoutingSlip();
                JSONArray r;
                try {
                    r = o.getJSONArray("routing");
                    for (int i = 0; i < r.length(); i++) {
                        RoutingSlipEntry m = getRoutingSlipEntry(r.getInt(i));
                        entries.add(m);
                    }
                } catch (JSONException e) {

                }

            } else {
                entries = null;
            }
        }
        return entries;
    }

    private List<PatientItem> getPatientListData(ArrayList<Integer> list)
    {
        ArrayList<PatientItem> items = new ArrayList<PatientItem>();

        String id;
        String content;
        String details;
        boolean isNext;

        for (int i = 0; i < list.size(); i++) {
            int val = list.get(i);
            id = String.format("%d", val);
            JSONObject pData = m_patientData.get(val);
            content = String.format("Content %d", val);
            details = String.format("Details %d", val);
            PatientItem nextPatient = getWaitingPatientItem();
            boolean isWaiting;

            isWaiting = false;
            if (nextPatient != null && nextPatient.id.equals(id)) {
                isWaiting = true;
            }

            PatientItem item = new PatientItem(id, content, details, pData, isWaiting);
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
        return getCommonSessionSingleton().getClinicId();
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

    JSONArray getXRays(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final XRayREST xrayREST = new XRayREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            xrayREST.addListener(listener);

            Object lock = xrayREST.getAllXRays(clinicId, patientId);

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

            int status = xrayREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
    }

    JSONArray getENTExams(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTExamREST entExamREST = new ENTExamREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            entExamREST.addListener(listener);

            Object lock = entExamREST.getAllENTExams(clinicId, patientId);

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

            int status = entExamREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
    }

    JSONArray getENTHistories(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTHistoryREST entHistoryREST = new ENTHistoryREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            entHistoryREST.addListener(listener);

            Object lock = entHistoryREST.getAllENTHistories(clinicId, patientId);

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

            int status = entHistoryREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
    }

    void updateMedicalHistory(/*final RESTCompletionListener listener */)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                MedicalHistoryREST rest = new MedicalHistoryREST(getContext());
                //rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateMedicalHistory(m_commonSessionSingleton.getPatientMedicalHistory());

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
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public MedicalHistory getMedicalHistory(int clinicid, int patientid)
    {
        boolean ret = false;
        MedicalHistory mh = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final MedicalHistoryREST mhData = new MedicalHistoryREST(getContext());
            Object lock = mhData.getMedicalHistoryData(clinicid, patientid);

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

            int status = mhData.getStatus();
            if (status == 200) {
                mh = getCommonSessionSingleton().getPatientMedicalHistory();
            }
        }
        return mh;
    }

    void updateENTExam(/*final RESTCompletionListener listener */)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTExamREST rest = new ENTExamREST(getContext());
                //rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateENTExam(m_commonSessionSingleton.getPatientENTExam());

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
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_exam), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_exam), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public ENTExam getENTExam(int clinicid, int patientid)
    {
        boolean ret = false;
        ENTExam exam = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTExamREST examData = new ENTExamREST(getContext());
            Object lock = examData.getEntExam(clinicid, patientid);

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

            int status = examData.getStatus();
            if (status == 200) {
                exam = getCommonSessionSingleton().getPatientENTExam();
            }
        }
        return exam;
    }

    void updateENTHistory(/*final RESTCompletionListener listener */)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTHistoryREST rest = new ENTHistoryREST(getContext());
                //rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateENTHistory(m_commonSessionSingleton.getPatientENTHistory());

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
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_history), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public ENTHistory getENTHistory(int clinicid, int patientid)
    {
        boolean ret = false;
        ENTHistory history = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTHistoryREST historyData = new ENTHistoryREST(getContext());
            Object lock = historyData.getEntHistory(clinicid, patientid);

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

            int status = historyData.getStatus();
            if (status == 200) {
                history = getCommonSessionSingleton().getPatientENTHistory();
            }
        }
        return history;
    }

    public XRay getXRay(int clinicid, int patientid)
    {
        boolean ret = false;
        XRay xray = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final XRayREST xrayData = new XRayREST(getContext());
            Object lock = xrayData.getXRay(clinicid, patientid);

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

            int status = xrayData.getStatus();
            if (status == 200) {
                xray = getCommonSessionSingleton().getPatientXray();
            }
        }
        return xray;
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


