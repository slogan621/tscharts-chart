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

package org.thousandsmiles.thousandsmileschart;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.Audiogram;
import org.thousandsmiles.tscharts_lib.AudiogramREST;
import org.thousandsmiles.tscharts_lib.CategoryREST;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisExtra;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisExtraREST;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisREST;
import org.thousandsmiles.tscharts_lib.ENTExam;
import org.thousandsmiles.tscharts_lib.ENTExamREST;
import org.thousandsmiles.tscharts_lib.ENTHistoryExtra;
import org.thousandsmiles.tscharts_lib.ENTHistoryExtraREST;
import org.thousandsmiles.tscharts_lib.ENTHistoryREST;
import org.thousandsmiles.tscharts_lib.ENTTreatment;
import org.thousandsmiles.tscharts_lib.ENTTreatmentREST;
import org.thousandsmiles.tscharts_lib.ImageREST;
import org.thousandsmiles.tscharts_lib.MedicalHistory;
import org.thousandsmiles.tscharts_lib.MedicalHistoryREST;
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.PatientREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;
import org.thousandsmiles.tscharts_lib.RoutingSlipREST;
import org.thousandsmiles.tscharts_lib.StationREST;
import org.thousandsmiles.tscharts_lib.XRayREST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private static Context m_ctx;
    private static ArrayList<JSONObject> m_clinicStationData = new ArrayList<JSONObject>();
    private static ArrayList<JSONObject> m_stationData = new ArrayList<JSONObject>();
    private static int m_stationStationId = 0;
    private int m_selectorNumColumns;
    private int m_width = -1;
    private int m_height = -1;
    private JSONObject m_displayPatientRoutingSlip = null;
    private JSONObject m_routingSlipEntryResponse = null;
    private JSONArray m_patientSearchResults = null;

    // following are used to filter search results based on the station that was selected during patient search

    // cache of patient routing slip entries, indexed by patient ID.

    private static HashMap<Integer, ArrayList<RoutingSlipEntry>> m_patientRoutingSlipEntries = new HashMap<Integer, ArrayList<RoutingSlipEntry>>();

    // get routing slip cache entries

    ArrayList <RoutingSlipEntry> getRoutingSlipCacheEntries(int patient) {
        if (m_patientRoutingSlipEntries.containsKey(patient)) {
            // cache hit, return what we have

            return m_patientRoutingSlipEntries.get(patient);
        }
        ArrayList<RoutingSlipEntry> ret = getRoutingSlipEntries(getClinicId(), patient);
        m_patientRoutingSlipEntries.put(patient, ret);
        return ret;
    }

    // end of data used to filter search results based on station

    private static HashMap<Integer, ArrayList<Integer>> m_stationToCategory = new HashMap<Integer, ArrayList<Integer>>();
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<String, Integer> m_clinicStationNameToId = new HashMap<String, Integer>();
    private static HashMap<Integer, Integer> m_clinicStationToStation = new HashMap<Integer, Integer>();
    private static HashMap<Integer, JSONObject> m_clinicStationToData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, JSONObject> m_stationToData = new HashMap<Integer, JSONObject>();
    private static HashMap<String, Integer> m_stationToSelector = new HashMap<String, Integer>();
    private static HashMap<String, Integer> m_stationToUnvisitedSelector = new HashMap<String, Integer>();
    private static HashMap<String, String> m_stationToSpanish = new HashMap<String, String>();
    public HashMap<Integer, PatientData> getPatientHashMap()
    {
        return m_patientHashMap;
    }
    private ArrayList<ENTHistoryExtra> m_entHistoryExtraList = new ArrayList<ENTHistoryExtra>();
    private ArrayList<ENTHistoryExtra> m_entHistoryExtraDeleteList = new ArrayList<ENTHistoryExtra>();
    private ArrayList<ENTDiagnosisExtra> m_entDiagnosisExtraList = new ArrayList<ENTDiagnosisExtra>();
    private ArrayList<ENTDiagnosisExtra> m_entDiagnosisExtraDeleteList = new ArrayList<ENTDiagnosisExtra>();
    private static ArrayList<JSONObject> m_categoryData = new ArrayList<JSONObject>();
    private int m_displayPatientId = -1; // id of the patient that will get checked in/checked out when the corresponding button is pressed
    private int m_displayRoutingSlipEntryId = -1; // id of the routingslip entry for m_displayPatientId
    // XXX Consider moving these station class names to the API
    private CommonSessionSingleton m_commonSessionSingleton = null;
    private boolean m_newMedHistory = false;
    private boolean m_newXRay = false;
    private boolean m_newENTExam = false;
    private boolean m_newENTHistory = false;
    private boolean m_newENTDiagnosis = false;
    private boolean m_newENTTreatment = false;
    private boolean m_newAudiogram = false;
    private HashMap<Integer, PatientData> m_patientHashMap = new HashMap<Integer, PatientData>();

    public void replacePatientHashMap(HashMap<Integer, PatientData> map)
    {
        m_patientHashMap = (HashMap<Integer, PatientData>) map.clone();
    }

    private Integer getCategory(String name) {
        Integer ret = null;

        for (int i = 0; i < m_categoryData.size(); i++) {
            try {
                JSONObject o = m_categoryData.get(i);
                int id = o.getInt("id");
                String catName = o.getString("name");
                if (catName.equals(name)) {
                    ret = i;
                    break;
                }

            } catch (Exception e) {

            }
        }
        return ret;
    }

    public boolean isXRayStation() {
        boolean ret = false;
        int id = getStationStationId();
        String name = getStationNameFromId(id);
        if (name.equals("X-Ray")) {
            ret = true;
        }
        return ret;
    }

    public void initializeStationToCategoryMap() {
        for (int i = 0; i < m_stationData.size(); i++) {
            JSONObject o = m_stationData.get(i);
            try {
                String name = o.getString("name");
                int id = o.getInt("id");
                ArrayList l = new ArrayList<Integer>();

                if (name.equals("Dental")) {
                    Integer cat = getCategory("Dental");
                    if (cat != null) {
                        l.add(cat);
                    }
                } else if (name.equals("X-Ray")) {
                    Integer cat = getCategory("Dental");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Ortho");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("Speech")) {
                    Integer cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("Surgery Screening")) {
                    Integer cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("Audiology")) {
                    Integer cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("ENT")) {
                    Integer cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("Hygiene")) {
                    Integer cat = getCategory("Dental");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Ortho");
                    if (cat != null) {
                        l.add(cat);
                    }
                }  else if (name.equals("Ortho")) {
                    Integer cat = getCategory("Ortho");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("New Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                    cat = getCategory("Returning Cleft");
                    if (cat != null) {
                        l.add(cat);
                    }
                }
                m_stationToCategory.put(id, l);
            } catch (Exception e) {
            }
        }
    }

    public boolean setPatientOldId(int curId, int oldId) {
        PatientData data;
        boolean ret = false;

        JSONObject obj;

        try {
            obj = m_patientData.get(curId);
            obj.put("oldid", oldId);
            ret = true;
        } catch (Exception e) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getContext(), getContext().getString(R.string.msg_failed_to_record_old_patient_id), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (ret == true) {
            try {
                data = m_patientHashMap.get(curId);
                data.setOldId(oldId);

                ret = true;
            } catch (Exception e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_failed_to_record_old_patient_id), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        return ret;
    }

    public void setNewAudiogram(boolean val)
    {
        m_newAudiogram = val;
    }

    public boolean getNewAudiogram() {
        return m_newAudiogram;
    }

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

    public void setNewENTDiagnosis(boolean val) {
        m_newENTDiagnosis = val;
    }

    public boolean getNewENTDiagnosis() {
        return m_newENTDiagnosis;
    }

    public void setNewENTExam(boolean val) {
        m_newENTExam = val;
    }

    public boolean getNewENTExam() {
        return m_newENTExam;
    }

    public void setNewENTTreatment(boolean val) {
        m_newENTTreatment = val;
    }

    public boolean getNewENTTreatment() {
        return m_newENTTreatment;
    }


    public void setNewMedHistory(boolean val) {
        m_newMedHistory = val;
    }

    public boolean getNewMedHistory() {
        return m_newMedHistory;
    }

    public void clearENTExtraHistoryList() {
        m_entHistoryExtraList.clear();
    }

    public void addENTExtraHistory(ENTHistoryExtra item) {
        m_entHistoryExtraList.add(item);
    }

    public void clearENTExtraDiagnosisList() {
        m_entDiagnosisExtraList.clear();
    }

    public void addENTExtraDiagnosis(ENTDiagnosisExtra item) {
        m_entDiagnosisExtraList.add(item);
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
        m_stationToSpanish.put("Runner", "Corredor");
    }

    public void clearPatientSearchResultData()
    {
        m_patientSearchResults = null;
        m_patientData.clear();
        m_patientHashMap.clear();
    }

    public PatientItem getActivePatientItem()
    {
        PatientItem item = null;

        int id = getDisplayPatientId();

        if (id != -1) {
            JSONObject o = getPatientData(id);
            if (o != null) {
                item = new PatientItem(String.format("%d", id), "", "", o, false);
            }
        }

        return item;
    }

    public void setPatientSearchResults(JSONArray results)
    {
        m_patientSearchResults = results;
    }

    public void getPatientSearchResultData()
    {
        for (int i = 0; m_patientSearchResults != null && i < m_patientSearchResults.length(); i++) {
            try {
                getPatientData(m_patientSearchResults.getInt(i));
            } catch (JSONException e) {
            }
        }
    }

    public void getPatientSearchResultRoutingSlipEntries()
    {
        for (int i = 0; m_patientSearchResults != null && i < m_patientSearchResults.length(); i++) {
            try {
                getPatientRoutingSlipEntries(m_patientSearchResults.getInt(i));
            } catch (JSONException e) {
            }
        }
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

    public ArrayList<ENTHistoryExtra>  getENTHistoryExtraList() {
        return m_entHistoryExtraList;
    }

    public ArrayList<ENTHistoryExtra>  getENTHistoryExtraDeleteList() {
        return m_entHistoryExtraDeleteList;
    }

    public void clearENTHistoryExtraList() {
        m_entHistoryExtraList.clear();
    }

    public void clearENTHistoryExtraDeleteList() {
        m_entHistoryExtraDeleteList.clear();
    }

    public void addENTHistoryExtraToDeleteList(ENTHistoryExtra extr) {
        m_entHistoryExtraDeleteList.add(extr);
    }

    public void removeENTHistoryExtraFromDeleteList(ENTHistoryExtra extr) {
        m_entHistoryExtraDeleteList.remove(extr);
    }

    public boolean isInENTHistoryExtraDeleteList(ENTHistoryExtra extr) {
        return m_entHistoryExtraDeleteList.contains(extr);
    }

    /* ENT Diagnosis extra */

    public ArrayList<ENTDiagnosisExtra>  getENTDiagnosisExtraList() {
        return m_entDiagnosisExtraList;
    }

    public ArrayList<ENTDiagnosisExtra>  getENTDiagnosisExtraDeleteList() {
        return m_entDiagnosisExtraDeleteList;
    }

    public void clearENTDiagnosisExtraList() {
        m_entDiagnosisExtraList.clear();
    }

    public void clearENTDiagnosisExtraDeleteList() {
        m_entDiagnosisExtraDeleteList.clear();
    }

    public void addENTDiagnosisExtraToDeleteList(ENTDiagnosisExtra extr) {
        m_entDiagnosisExtraDeleteList.add(extr);
    }

    public void removeENTDiagnosisExtraFromDeleteList(ENTDiagnosisExtra extr) {
        m_entDiagnosisExtraDeleteList.remove(extr);
    }

    public boolean isInENTDiagnosisExtraDeleteList(ENTDiagnosisExtra extr) {
        return m_entDiagnosisExtraDeleteList.contains(extr);
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
        m_stationToSelector.put("Runner", R.drawable.runner_selector);
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
        m_stationToUnvisitedSelector.put("Runner", R.drawable.runner_unvisited_selector);
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

    public String getStationNameFromId(int id)
    {
        String ret = "";

        ret = m_stationIdToName.get(id);
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

        if (m_stationToSelector.containsKey(name)) {
            ret = m_stationToSelector.get(name);
        }
        return ret;
    }

    public void setStationStationId(int id) {
        m_stationStationId = id;
    }

    public int getStationStationId() { return m_stationStationId; }

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

    public void addCategoryData(JSONArray data) {
        int i;
        JSONObject categorydata;

        for (i = 0; i < data.length(); i++)  {
            try {
                categorydata = data.getJSONObject(i);
                m_categoryData.add(categorydata);
            } catch (JSONException e) {
                return;
            }
        }
    }

    public JSONObject getCategoryData(int i) {
        JSONObject ret = null;

        ret = m_categoryData.get(i);
        return ret;
    }

    public int getCategoryCount() {
        return m_categoryData.size();
    }

    public void updateCategoryDataTask() {
        UpdateCategoryData task = new UpdateCategoryData();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
    }

    private class UpdateCategoryData extends AsyncTask<Object, Object, Object> {

        @Override
        protected String doInBackground(Object... params) {
            updateCategoryData();
            return "";
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

    public boolean updateCategoryData() {
        boolean ret = false;

        m_categoryData.clear();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final CategoryREST categoryData = new CategoryREST(getContext());
            categoryData.addListener(new GetCategoryListener());
            Object lock = categoryData.getCategoryData();

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

            int status = categoryData.getStatus();
            if (status == 200) {
                ret = true;
            }
        }
        return ret;
    }

    class GetCategoryListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
            try {
                addCategoryData(a);
                initializeStationToCategoryMap();
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }


    public String getActiveStationName() {
        return m_stationIdToName.get(m_stationStationId);
    }

    public void addStationData(JSONArray data) {
        int i;
        JSONObject stationdata;
        int maxId = -999;

        for (i = 0; i < data.length(); i++)  {
            try {
                stationdata = data.getJSONObject(i);
                int id = stationdata.getInt("id");
                if (id > maxId) {
                    maxId = id;
                }
                m_stationIdToName.put(stationdata.getInt("id"), stationdata.getString("name"));
                m_stationData.add(stationdata);
            } catch (JSONException e) {
                return;
            }
        }
        m_stationIdToName.put(maxId + 1, m_ctx.getString(R.string.station_name_runner));
        JSONObject o = new JSONObject();
        try {
            o.put("id", maxId + 1);
            o.put("name", "Runner");
            m_stationData.add(o);
        } catch (Exception e) {
        }
    }

    ArrayList<Station> getStationList()
    {
        ArrayList<Station> ret = new ArrayList<Station>();

        initStationNameToSelectorMap();
        initStationNameToUnvisitedSelectorMap();
        Iterator it = m_stationIdToName.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            if (pair.getValue().toString().equals("Runner")) {
                continue;
            }
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

    public JSONObject getStationData(int id) {
        JSONObject o = null;
        if (m_stationData != null) {
            o = m_stationData.get(id);
        }
        return o;
    }

    public int getStationCount() {
        return m_stationData.size();
    }

    public class UpdateStationDataListener implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
            SessionSingleton sess = SessionSingleton.getInstance();
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
            addStationData(a);
        }
    }

    public class GetPatientDataListener implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
            SessionSingleton sess = SessionSingleton.getInstance();
            sess.addPatientData(o);
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
        }
    }

    public JSONObject getPatientRoutingSlipEntries(final int id)
    {
        JSONObject o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
            final PatientREST patientData = new PatientREST(getContext());
            patientData.addListener(new GetPatientDataListener());
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
                o = m_patientHashMap.get(id).toJSONObject();
                m_patientData.put(id, o);
                CommonSessionSingleton.getInstance().hasCurrentXRay(id, 365);
            }
        }
        if (o == null) {
            return o;
        }
        return o;
    }

    public JSONObject getPatientData(final int id) {

        JSONObject o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
            final PatientREST patientData = new PatientREST(getContext());
            patientData.addListener(new GetPatientDataListener());
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
                o = m_patientHashMap.get(id).toJSONObject();
                m_patientData.put(id, o);
                CommonSessionSingleton.getInstance().hasCurrentXRay(id, 365);
            }
        }
        if (o == null) {
            return o;
        }
        return o;
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

    public class GetRoutingSlipListener implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
            SessionSingleton.getInstance().setDisplayPatientRoutingSlip(o);
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
        }
    }

    public class GetRoutingSlipEntryListener implements RESTCompletionListener {
        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
            setRoutingSlipEntryResponse(o);
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
            rsData.addListener(new GetRoutingSlipEntryListener());
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
            rsData.addListener(new GetRoutingSlipListener());
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
        PatientData pd = new PatientData();
        pd.fromJSONObject(data);
        m_patientHashMap.put(id, pd);
    }

    JSONArray getXRayThumbnails(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ImageREST imageREST = new ImageREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            imageREST.addListener(listener);

            Object lock;
            if (clinicId == -1) {
                lock = imageREST.getTypedImagesForPatient(patientId, "Xray");
            } else {
                lock = imageREST.getTypedImagesForPatientAndClinic(clinicId, patientId, "Xray");
            }

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

            int status = imageREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
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

    JSONArray getAudiograms(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final AudiogramREST audiogramREST = new AudiogramREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            audiogramREST.addListener(listener);

            Object lock = audiogramREST.getAudiogram(clinicId, patientId);

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

            int status = audiogramREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
    }

    JSONArray getENTDiagnoses(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTDiagnosisREST entDiagnosisREST = new ENTDiagnosisREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            entDiagnosisREST.addListener(listener);

            Object lock = entDiagnosisREST.getAllENTDiagnoses(clinicId, patientId);

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

            int status = entDiagnosisREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();
            }
        }
        return ret;
    }

    boolean getENTExtraDiagnoses(final int diagnosisId)
    {
        JSONArray ret = null;
        boolean retval = false;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTDiagnosisExtraREST entDiagnosisExtraREST = new ENTDiagnosisExtraREST(getContext());
            GetDataListener listener = new GetDataListener();
            entDiagnosisExtraREST.addListener(listener);

            Object lock = entDiagnosisExtraREST.getAllENTDiagnosesExtra(diagnosisId);

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

            int status = entDiagnosisExtraREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();

                clearENTExtraDiagnosisList();
                // SYD iterate the result array and place it in the global ent history extra list

                for (int i = 0; i < ret.length(); i++) {
                    ENTDiagnosisExtra ex = new ENTDiagnosisExtra();
                    try {
                        ex.fromJSONObject(ret.getJSONObject(i));
                        addENTExtraDiagnosis(ex);
                    } catch (Exception e) {

                    }
                }
                retval = true;
            }
        }
        return retval;
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

    boolean getENTExtraHistories(final int historyId)
    {
        JSONArray ret = null;
        boolean retval = false;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTHistoryExtraREST entHistoryExtraREST = new ENTHistoryExtraREST(getContext());
            GetDataListener listener = new GetDataListener();
            entHistoryExtraREST.addListener(listener);

            Object lock = entHistoryExtraREST.getAllENTHistoriesExtra(historyId);

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

            int status = entHistoryExtraREST.getStatus();
            if (status == 200) {
                ret = listener.getResultArray();

                clearENTExtraHistoryList();
                // SYD iterate the result array and place it in the global ent history extra list

                for (int i = 0; i < ret.length(); i++) {
                    ENTHistoryExtra ex = new ENTHistoryExtra();
                    try {
                        ex.fromJSONObject(ret.getJSONObject(i));
                        addENTExtraHistory(ex);
                    } catch (Exception e) {

                    }
                }
                retval = true;
            }
        }
        return retval;
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

    JSONArray getENTTreatments(final int clinicId, final int patientId)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTTreatmentREST entTreatmentREST = new ENTTreatmentREST(getContext());
            GetDataListener listener = new GetDataListener();
            listener.setPatientId(patientId);
            entTreatmentREST.addListener(listener);

            Object lock = entTreatmentREST.getAllENTTreatments(clinicId, patientId);

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

            int status = entTreatmentREST.getStatus();
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

    void updateENTTreatment(/*final RESTCompletionListener listener */)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTTreatmentREST rest = new ENTTreatmentREST(getContext());
                //rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateENTTreatment(m_commonSessionSingleton.getPatientENTTreatment());

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
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_treatment), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_treatment), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public ENTTreatment getENTTreatment(int clinicid, int patientid)
    {
        boolean ret = false;
        ENTTreatment treatment = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final ENTTreatmentREST treatmentData = new ENTTreatmentREST(getContext());
            Object lock = treatmentData.getEntTreatment(clinicid, patientid);

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

            int status = treatmentData.getStatus();
            if (status == 200) {
                treatment = getCommonSessionSingleton().getPatientENTTreatment();
            }
        }
        return treatment;
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

    class CreateENTHistoryExtraListener implements RESTCompletionListener {

        ENTHistoryExtra m_ex = null;

        void setExtra(ENTHistoryExtra ex)
        {
          m_ex = ex;
        }

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                m_ex.setId(a.getInt("id"));
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    class UpdateENTHistoryExtraListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    void updateENTHistoryExtra(final int historyId)
    {
        Thread thread = new Thread(){
            public void run() {
                boolean error = false;
                // note we use session context because this may be called after onPause()

                Object lock;
                int status;

                ArrayList<ENTHistoryExtra> extra = getENTHistoryExtraList();

                for (int i = 0; i < extra.size(); i++) {
                    ENTHistoryExtra ex = extra.get(i);
                    ex.setHistory(historyId);
                    ENTHistoryExtraREST rest = new ENTHistoryExtraREST(getContext());

                    if (ex.getId() == 0) {
                        CreateENTHistoryExtraListener listener = new CreateENTHistoryExtraListener();
                        listener.setExtra(ex);
                        rest.addListener(listener);
                        lock = rest.createENTHistoryExtra(ex);
                    } else {
                        UpdateENTHistoryExtraListener listener = new UpdateENTHistoryExtraListener();
                        rest.addListener(listener);
                        lock = rest.updateENTHistoryExtra(ex);
                    }

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

                    // if a create, get returned ID from response listener and update in the
                    // global object

                    if (status == 200) {
                        //ex.setId();
                    }

                    if (status != 200) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_history_extra), Toast.LENGTH_LONG).show();
                            }
                        });
                        error = true;
                    }
                }
                if (error == false) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_history_extra), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    void updateENTDiagnosis(/*final RESTCompletionListener listener */)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                ENTDiagnosisREST rest = new ENTDiagnosisREST(getContext());
                //rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateENTDiagnosis(m_commonSessionSingleton.getPatientENTDiagnosis());

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
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_diagnosis), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_diagnosis), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    class CreateENTDiagnosisExtraListener implements RESTCompletionListener {

        ENTDiagnosisExtra m_ex = null;

        void setExtra(ENTDiagnosisExtra ex)
        {
            m_ex = ex;
        }

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                m_ex.setId(a.getInt("id"));
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    class UpdateENTDiagnosisExtraListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    void updateENTDiagnosisExtra(final int diagnosisId)
    {
        Thread thread = new Thread(){
            public void run() {
                boolean error = false;
                // note we use session context because this may be called after onPause()

                Object lock;
                int status;

                ArrayList<ENTDiagnosisExtra> extra = getENTDiagnosisExtraList();

                for (int i = 0; i < extra.size(); i++) {
                    ENTDiagnosisExtra ex = extra.get(i);
                    ex.setDiagnosis(diagnosisId);
                    ENTDiagnosisExtraREST rest = new ENTDiagnosisExtraREST(getContext());

                    if (ex.getId() == 0) {
                        CreateENTDiagnosisExtraListener listener = new CreateENTDiagnosisExtraListener();
                        listener.setExtra(ex);
                        rest.addListener(listener);
                        lock = rest.createENTDiagnosisExtra(ex);
                    } else {
                        UpdateENTDiagnosisExtraListener listener = new UpdateENTDiagnosisExtraListener();
                        rest.addListener(listener);
                        lock = rest.updateENTDiagnosisExtra(ex);
                    }

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

                    // if a create, get returned ID from response listener and update in the
                    // global object

                    if (status == 200) {
                        //ex.setId();
                    }

                    if (status != 200) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_ent_diagnosis_extra), Toast.LENGTH_LONG).show();
                            }
                        });
                        error = true;
                    }
                }
                if (extra.size() != 0 && error == false) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_ent_diagnosis_extra), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public Audiogram getAudiogram(int clinicid, int patientid, int imageId)
    {
        boolean ret = false;
        Audiogram audiogram = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final AudiogramREST audiogramData = new AudiogramREST(getContext());
            Object lock = audiogramData.getAudiogram(clinicid, patientid, imageId);

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

            int status = audiogramData.getStatus();
            if (status == 200) {
                audiogram = getCommonSessionSingleton().getPatientAudiogram();
            }
        }
        return audiogram;
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


