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

package org.thousandsmiles.thousandsmileschart;

public class CheckoutParams {
    private int m_returnMonths;
    private String m_msg;
    private boolean m_returnToClinicStation = false;

    // following are valid if m_returnToClinicStation is true

    private int m_requestingClinicStationId;    // requesting clinic station (one that is checking out
    private int m_stationId;                    // the station we want patient to go to, e.g., dental


    public void setReturnMonths(int n)
    {
        m_returnMonths = n;
    }
    public int getReturnMonths()
    {
        return m_returnMonths;
    }
    public void setReturnToClinicStation(boolean val) {m_returnToClinicStation = val;}
    public boolean isReturnToClinicStation() {return m_returnToClinicStation;}
    public void setRequestingClinicStationId(int id) {m_requestingClinicStationId = id;}
    public int getRequestingClinicStationId() {return m_requestingClinicStationId;}
    public void setMessage(String msg)
    {
        m_msg = msg;
    }
    public String getMessage() {return m_msg;}
    public void setStationId(int id) {m_stationId = id;}
    public int getStationId() {return m_stationId;}
}