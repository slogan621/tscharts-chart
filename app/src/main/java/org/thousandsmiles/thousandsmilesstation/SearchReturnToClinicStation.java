/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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

import android.view.View;

import org.json.JSONArray;
import org.thousandsmiles.tscharts_lib.SearchReturnToClinicStationListener;

public class SearchReturnToClinicStation implements SearchReturnToClinicStationListener {
    private View m_view = null;
    private View m_title = null;
    private boolean m_updateTitleOnly = false;

    public void setUpdateTitleOnly(boolean val) {
        m_updateTitleOnly = true;
    }

    public void setView(View v) {
        m_view = v;
    }

    public void setTitle(View v) {
        m_title = v;
    }

    public void onCompletion (JSONArray response, boolean success) {
        if (success == true) {
            if (response.length() > 0 /*&& m_view != null */) {
                //m_item.isReturnToClinic = true;
                if (m_updateTitleOnly == false) {
                    m_view.setBackgroundColor(m_view.getResources().getColor(R.color.colorGreen));
                } else {
                    m_title.setBackgroundColor(m_title.getResources().getColor(R.color.colorGreen));
                }
            }
        } else {
            //m_item.isReturnToClinic = false;
        }
    }
}
