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

public class AppListItems {

    // List of apps a station has access to
    private static HashMap<String, ArrayList<AppListItem>> m_stationToAppList =
            new HashMap<String, ArrayList<AppListItem>>();

    private class AppListItem {
        private String m_name;
        private int m_imageId;
        private int m_selector;

        public void setName(String name) {
            m_name = name;
        }

        public String getName() {
            return m_name;
        }

        public void setImageId(int id) {
            m_imageId = id;
        }

        public int getImageId() {
            return m_imageId;
        }

        public void setSelector(int id) {
            m_selector = id;
        }

        public int getSelector() {
            return m_selector;
        }
    }

    private void initDental() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("Dental", items);
    }

    private void initXRay() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("XRay", items);
    }

    private void initAudiology() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("Audiology", items);
    }

    private void initSpeech() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("Speech", items);
    }

    private void initENT() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("ENT", items);
    }

    private void initSurgeryScreening() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("SurgeryScreening", items);
    }

    private void initOrtho() {
        ArrayList<AppListItem> items = new ArrayList<AppListItem>();
        AppListItem item;

        item = new AppListItem();
        item.setName("Routing Slip");
        item.setImageId(R.drawable.app_routing_slip);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);

        item = new AppListItem();
        item.setName("Medical History");
        item.setImageId(R.drawable.app_medical_history);
        item.setSelector(R.drawable.app_medical_history_selector);
        items.add(item);
        m_stationToAppList.put("Ortho", items);
    }

    // constructor

    public AppListItems() {
        initDental();
        initXRay();
        initAudiology();
        initSpeech();
        initENT();
        initSurgeryScreening();
        initOrtho();
    }

    public ArrayList<String> getNames(String station) {
        ArrayList<AppListItem> items = m_stationToAppList.get(station);
        ArrayList<String> ret = new ArrayList<String>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ret.add(items.get(i).getName());
            }
        }
        return ret;
    }

    public ArrayList<Integer> getImageIds(String station) {
        ArrayList<AppListItem> items = m_stationToAppList.get(station);
        ArrayList<Integer> ret = new ArrayList<Integer>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ret.add(items.get(i).getImageId());
            }
        }
        return ret;
    }

    public ArrayList<Integer> getSelectors(String station) {
        ArrayList<AppListItem> items = m_stationToAppList.get(station);
        ArrayList<Integer> ret = new ArrayList<Integer>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ret.add(items.get(i).getSelector());
            }
        }
        return ret;
    }
}