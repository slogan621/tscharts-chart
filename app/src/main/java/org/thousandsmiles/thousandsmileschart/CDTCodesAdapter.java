/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.thousandsmiles.tscharts_lib.CDTCodesModel;
import org.thousandsmiles.tscharts_lib.DentalState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CDTCodesAdapter extends ArrayAdapter<CDTCodesModel> {

    private class CDTCodesModelCheckboxState {
        private CDTCodesModel m_model;
        private boolean m_isSelected;
        private boolean m_isCompleted;
        private boolean m_buccal;
        private boolean m_lingual;
        private boolean m_mesial;
        private boolean m_occlusal;
        private boolean m_labial;
        private boolean m_incisal;
    }

    private HashMap<String, CDTCodesModelCheckboxState> m_stateList = new HashMap<String, CDTCodesModelCheckboxState>();
    private List<CDTCodesModel> m_list;
    private final Activity m_context;

    public CDTCodesAdapter(Activity context, List<CDTCodesModel> list) {
        super(context, R.layout.cdt_codes_list_row, list);
        this.m_context = context;
        m_list = list;
        for (int i = 0; i < list.size(); i++) {
            stateListAdd(list.get(i), null);
        }
    }

    public void stateListAdd(CDTCodesModel m, PatientDentalToothState s)
    {
        CDTCodesModelCheckboxState state  = new CDTCodesModelCheckboxState();
        state.m_model = m;
        if (s != null) {
            state.m_isSelected = false;
            state.m_isCompleted = s.getCompleted(); // XXX
            state.m_buccal = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_BUCCAL);
            state.m_lingual = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_LINGUAL);
            state.m_mesial = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_MESIAL);
            state.m_occlusal = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_OCCLUSAL);
            state.m_labial = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_LABIAL);
            state.m_incisal = s.getSurfacesList().contains(DentalState.Surface.DENTAL_SURFACE_INCISAL);
        } else {
            state.m_isSelected = false;
            state.m_isCompleted = false;
            state.m_buccal = false;
            state.m_lingual = false;
            state.m_mesial = false;
            state.m_occlusal = false;
            state.m_labial = false;
            state.m_incisal = false;
        }
        this.m_stateList.put(state.m_model.repr(), state);
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
        protected CheckBox completed;
        protected CheckBox buccal;
        protected CheckBox lingual;
        protected CheckBox mesial;
        protected CheckBox occlusal;
        protected CheckBox labial;
        protected CheckBox incisal;
    }

    public ArrayList<CDTCodesModel> getCheckedItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();
        Iterator it = m_stateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair != null) {
                CDTCodesModelCheckboxState st = (CDTCodesModelCheckboxState) pair.getValue();
                if (st.m_isSelected) {
                    ret.add(st.m_model);
                }
            }
        }

        return ret;
    }

    public ArrayList<CDTCodesModel> getCompletedItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();
        Iterator it = m_stateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair != null) {
                CDTCodesModelCheckboxState st = (CDTCodesModelCheckboxState) pair.getValue();
                if (st.m_isCompleted) {
                    ret.add(st.m_model);
                }
            }
        }

        return ret;
    }

    public ArrayList<CDTCodesModel> getUncompletedItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();
        Iterator it = m_stateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair != null) {
                CDTCodesModelCheckboxState st = (CDTCodesModelCheckboxState) pair.getValue();
                if (st.m_isCompleted == false) {
                    ret.add(st.m_model);
                }
            }
        }

        return ret;
    }

    public ArrayList<DentalState.Surface>  getItemSurfaces(CDTCodesModel model)
    {
        ArrayList<DentalState.Surface> ret = new ArrayList<DentalState.Surface>();
        Iterator it = m_stateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair != null) {
                CDTCodesModelCheckboxState st = (CDTCodesModelCheckboxState) pair.getValue();
                if (st.m_model.getId() != model.getId()) {
                    continue;
                }
                if (st.m_buccal == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_BUCCAL);
                }
                if (st.m_lingual == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_LINGUAL);
                }
                if (st.m_incisal == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_INCISAL);
                }
                if (st.m_labial == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_LABIAL);
                }
                if (st.m_mesial == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_MESIAL);
                }
                if (st.m_occlusal == true) {
                    ret.add(DentalState.Surface.DENTAL_SURFACE_OCCLUSAL);
                }
                break;
            }
        }
        return ret;
    }

    public boolean getItemCompleted(CDTCodesModel model)
    {
        boolean ret = false;
        Iterator it = m_stateList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair != null) {
                CDTCodesModelCheckboxState st = (CDTCodesModelCheckboxState) pair.getValue();
                if (st.m_model.getId() != model.getId()) {
                    continue;
                }
                ret = st.m_isCompleted;
                break;
            }
        }
        return ret;
    }

    public ArrayList<CDTCodesModel> getAllStateItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();

        for (Map.Entry<String, CDTCodesModelCheckboxState> entry : m_stateList.entrySet()) {
            ret.add(entry.getValue().m_model);
        }
        return ret;
    }

    public ArrayList<CDTCodesModel> getAllItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();

        for (int i = 0 ; i < m_list.size() ; i++){
            ret.add(m_list.get(i));
        }
        return ret;
    }

    public void removeCDTCodes(ArrayList<CDTCodesModel> codes)
    {
        for (int i = 0; i < codes.size(); i++) {
            for (int j = 0; j < m_list.size(); j++) {
                if (m_list.get(j) == codes.get(i)) {
                    m_list.remove(j);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflator = m_context.getLayoutInflater();
            convertView = inflator.inflate(R.layout.cdt_codes_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.label);
            viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.check);
            viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_isSelected = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.completed = (CheckBox) convertView.findViewById(R.id.completed);
            viewHolder.completed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_isCompleted = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.buccal = (CheckBox) convertView.findViewById(R.id.buccal);
            viewHolder.buccal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_buccal = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.lingual = (CheckBox) convertView.findViewById(R.id.lingual);
            viewHolder.lingual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_lingual = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.mesial = (CheckBox) convertView.findViewById(R.id.mesial);
            viewHolder.mesial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_mesial = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.occlusal = (CheckBox) convertView.findViewById(R.id.occlusal);
            viewHolder.occlusal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_occlusal = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.labial = (CheckBox) convertView.findViewById(R.id.labial);
            viewHolder.labial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_labial = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.incisal = (CheckBox) convertView.findViewById(R.id.incisal);
            viewHolder.incisal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    m_stateList.get(m_list.get(getPosition).repr()).m_incisal = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.label, viewHolder.text);
            convertView.setTag(R.id.check, viewHolder.checkbox);
            convertView.setTag(R.id.completed, viewHolder.completed);
            convertView.setTag(R.id.buccal, viewHolder.buccal);
            convertView.setTag(R.id.lingual, viewHolder.lingual);
            convertView.setTag(R.id.mesial, viewHolder.mesial);
            convertView.setTag(R.id.occlusal, viewHolder.occlusal);
            convertView.setTag(R.id.labial, viewHolder.labial);
            convertView.setTag(R.id.incisal, viewHolder.incisal);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkbox.setTag(position);
        viewHolder.completed.setTag(position);// This line is important.
        viewHolder.buccal.setTag(position);
        viewHolder.lingual.setTag(position);
        viewHolder.mesial.setTag(position);
        viewHolder.occlusal.setTag(position);
        viewHolder.labial.setTag(position);
        viewHolder.incisal.setTag(position);

        viewHolder.text.setText(m_list.get(position).repr());
        CDTCodesModelCheckboxState state = m_stateList.get(m_list.get(position).repr());
        if (state != null) {
            viewHolder.completed.setChecked(m_stateList.get(m_list.get(position).repr()).m_isCompleted);
            viewHolder.buccal.setChecked(m_stateList.get(m_list.get(position).repr()).m_buccal);
            viewHolder.labial.setChecked(m_stateList.get(m_list.get(position).repr()).m_labial);
            viewHolder.lingual.setChecked(m_stateList.get(m_list.get(position).repr()).m_lingual);
            viewHolder.occlusal.setChecked(m_stateList.get(m_list.get(position).repr()).m_occlusal);
            viewHolder.mesial.setChecked(m_stateList.get(m_list.get(position).repr()).m_mesial);
            viewHolder.incisal.setChecked(m_stateList.get(m_list.get(position).repr()).m_incisal);
        }

        viewHolder.checkbox.setChecked(false);
        return convertView;
    }
}