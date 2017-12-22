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

public class MedicationsModelList {
    static private MedicationsModelList m_instance = null;
    private List<MedicationsModel> list = new ArrayList<MedicationsModel>();

    public List<MedicationsModel> getModel() {
        list.add(new MedicationsModel("Linux", false));
        list.add(new MedicationsModel("Windows7", false));
        list.add(new MedicationsModel("Suse", false));
        list.add(new MedicationsModel("Eclipse", false));
        list.add(new MedicationsModel("Ubuntu", false));
        list.add(new MedicationsModel("Solaris", false));
        list.add(new MedicationsModel("Android", false));
        list.add(new MedicationsModel("iPhone", false));
        list.add(new MedicationsModel("Java", false));
        list.add(new MedicationsModel(".Net", false));
        list.add(new MedicationsModel("PHP", false));
        return list;
    }

    private MedicationsModelList(){}

    synchronized static public MedicationsModelList getInstance()
    {
        if (m_instance == null) {
            m_instance = new MedicationsModelList();
        }
        return m_instance;
    }
}
