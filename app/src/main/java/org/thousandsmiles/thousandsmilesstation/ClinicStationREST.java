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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ClinicStationREST extends RESTful {
    private final Object m_lock = new Object();

    private class UpdateClinicStationResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                //sess.addClinicStationData(response);
                m_lock.notify();
            }
        }
    }

    private class GetClinicStationDataResponseListener implements Response.Listener<JSONArray> {

        @Override
        public void onResponse(JSONArray response) {
            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                sess.addClinicStationData(response);
                m_lock.notify();
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {

            synchronized (m_lock) {
                if (error.networkResponse == null) {
                    if (error.getCause() instanceof java.net.ConnectException || error.getCause() instanceof  java.net.UnknownHostException) {
                        setStatus(101);
                    } else {
                        setStatus(-1);
                    }
                } else {
                   setStatus(error.networkResponse.statusCode);
                }
                m_lock.notify();
            }
        }
    }

    public class AuthJSONObjectRequest extends JsonObjectRequest
    {
        public AuthJSONObjectRequest(int method, String url, JSONObject jsonRequest,Response.Listener listener, ErrorListener errorListener)
        {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map getHeaders() throws AuthFailureError {
            Map headers = new HashMap();
            headers.put("Authorization", SessionSingleton.getInstance().getToken());
            return headers;
        }
    }

    public class AuthJSONArrayRequest extends JsonArrayRequest{

        public AuthJSONArrayRequest(String url, JSONArray jsonRequest,
                              Response.Listener<JSONArray> listener, ErrorListener errorListener) {
            super(url, listener, errorListener);
        }

        public AuthJSONArrayRequest(String url, Response.Listener<JSONArray> listener,
                              Response.ErrorListener errorListener, String username, String password) {
            super(url, listener, errorListener);

        }

        private Map<String, String> headers = new HashMap<String, String>();
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            //return headers;
            Map headers = new HashMap();
            headers.put("Authorization", SessionSingleton.getInstance().getToken());
            return headers;
        }

    }

    public ClinicStationREST(Context context) {
        setContext(context);
    }

    public Object getClinicStationData(int clinicid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/clinicstation?clinic=%d", getIP(), getPort(), clinicid);

        AuthJSONArrayRequest request = new AuthJSONArrayRequest(url, null, new GetClinicStationDataResponseListener(), new ErrorListener());

        queue.add((JsonArrayRequest) request);

        return m_lock;
    }

    public Object updateActiveClinicStationPatient(int clinicstationid, int patientId) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/clinicstation/%s/", getIP(), getPort(), clinicstationid);

        JSONObject data = new JSONObject();

        try {
            data.put("active", true);
            data.put("activepatient", patientId);
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
        }

        ClinicStationREST.AuthJSONObjectRequest request = new ClinicStationREST.AuthJSONObjectRequest(Request.Method.PUT, url, data,  new ClinicStationREST.UpdateClinicStationResponseListener(), new ClinicStationREST.ErrorListener());

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object putStationIntoWaitingState(int clinicstationid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/clinicstation/%s/", getIP(), getPort(), clinicstationid);

        JSONObject data = new JSONObject();

        try {
            data.put("active", false);
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
        }

        ClinicStationREST.AuthJSONObjectRequest request = new ClinicStationREST.AuthJSONObjectRequest(Request.Method.PUT, url, data,  new ClinicStationREST.UpdateClinicStationResponseListener(), new ClinicStationREST.ErrorListener());

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object putStationIntoAwayState(int clinicstationid, int numMinutes) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/clinicstation/%s/", getIP(), getPort(), clinicstationid);

        JSONObject data = new JSONObject();

        try {
            data.put("away", true);
            data.put("awaytime", numMinutes);
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
        }

        ClinicStationREST.AuthJSONObjectRequest request = new ClinicStationREST.AuthJSONObjectRequest(Request.Method.PUT, url, data,  new ClinicStationREST.UpdateClinicStationResponseListener(), new ClinicStationREST.ErrorListener());

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object returnStationFromAwayState(int clinicstationid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/clinicstation/%s/", getIP(), getPort(), clinicstationid);

        JSONObject data = new JSONObject();

        try {
            data.put("away", false);
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
        }

        ClinicStationREST.AuthJSONObjectRequest request = new ClinicStationREST.AuthJSONObjectRequest(Request.Method.PUT, url, data,  new ClinicStationREST.UpdateClinicStationResponseListener(), new ClinicStationREST.ErrorListener());

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }
}
