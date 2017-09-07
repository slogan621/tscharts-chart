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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivePatientList extends PatientList {
    public static Map<String, PatientItem> ITEM_MAP = new HashMap<String, PatientItem>();
    public static List<PatientItem> ITEMS = new ArrayList<PatientItem>();

    public static void clearItems()
    {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public static void addItem(PatientItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
