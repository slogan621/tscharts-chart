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

import java.util.ArrayList;
import java.util.List;

/*
    List of medications
 */

public class MedicationsModelList {
    static private MedicationsModelList m_instance = null;
    private List<MedicationsModel> m_list = new ArrayList<MedicationsModel>();
    private ArrayList<String> m_strs = new ArrayList<String>();

    public List<MedicationsModel> getModel() {
        return m_list;
    }

    public String[] getModelStringArray() {
        String [] ret;

        ret = m_strs.toArray(new String[0]);
        return ret;
    }

    private MedicationsModelList(){}

    synchronized static public MedicationsModelList getInstance()
    {
        if (m_instance == null) {
            m_instance = new MedicationsModelList();
        }
        return m_instance;
    }

    public void setModelData(ArrayList<String> items) {
        m_strs = items;
        m_list.clear();
        for (int i = 0; i < m_strs.size(); i++) {
            m_list.add(new MedicationsModel(m_strs.get(i), false));
        }
    }
}
