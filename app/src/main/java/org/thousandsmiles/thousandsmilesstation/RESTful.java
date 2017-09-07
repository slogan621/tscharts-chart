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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class RESTful {
    private Context m_context;
    private int m_status;

    protected int getStatus() {
        return m_status;
    }

    protected void setStatus(int status) {
        m_status = status;
    }

    protected void setContext(Context context) {
        m_context = context;
    }

    protected Context getContext() {
        return m_context;
    }

    public String getPort() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String val = sharedPref.getString("port", "80");
        return val;
    }

    public String getIP() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String val = sharedPref.getString("ipAddress", "");
        return val;
    }
}
