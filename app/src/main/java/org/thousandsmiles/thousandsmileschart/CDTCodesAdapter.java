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

import java.util.ArrayList;
import java.util.List;

public class CDTCodesAdapter extends ArrayAdapter<CDTCodesModel> {

    private class CDTCodesModelCheckboxState {
        private CDTCodesModel m_model;
        private boolean m_isSelected;
        private boolean m_isCompleted;
    }

    private ArrayList<CDTCodesModelCheckboxState> m_statelist = new ArrayList<CDTCodesModelCheckboxState>();
    private List<CDTCodesModel> m_list;
    private final Activity m_context;
    boolean m_checkAll_flag = false;
    boolean m_checkItem_flag = false;

    public CDTCodesAdapter(Activity context, List<CDTCodesModel> list) {
        super(context, R.layout.cdt_codes_list_row, list);
        this.m_context = context;
        m_list = list;
        for (int i = 0; i < list.size(); i++) {
            CDTCodesModelCheckboxState state  = new CDTCodesModelCheckboxState();
            state.m_model = list.get(i);
            state.m_isSelected = false;
            state.m_isCompleted = false; // XXX
            this.m_statelist.add(state);
        }
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
        protected CheckBox completed;
    }

    public ArrayList<CDTCodesModel> getCheckedItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();
/*
        for (int i = 0 ; i < m_list.size() ; i++){
            if (m_list.get(i).m_isSelected) {
                ret.add(m_list.get(i).m_model);
            }
        }
        */
        return ret;
    }

    public ArrayList<CDTCodesModel> getCompletedCheckedItems()
    {
        ArrayList<CDTCodesModel> ret = new ArrayList<CDTCodesModel>();
/*
        for (int i = 0 ; i < m_list.size() ; i++){
            if (m_list.get(i).m_isCompleted) {
                ret.add(m_list.get(i).m_model);
            }
        }

 */
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
                    //m_list.get(getPosition).m_isSelected = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            viewHolder.completed = (CheckBox) convertView.findViewById(R.id.completed);
            viewHolder.completed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int getPosition = (Integer) buttonView.getTag();  // Here we get the position that we have set for the checkbox using setTag.
                    //m_list.get(getPosition).m_isCompleted = buttonView.isChecked(); // Set the value of checkbox to maintain its state.
                }
            });
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.label, viewHolder.text);
            convertView.setTag(R.id.check, viewHolder.checkbox);
            convertView.setTag(R.id.completed, viewHolder.completed);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkbox.setTag(position);
        viewHolder.completed.setTag(position);// This line is important.

        viewHolder.text.setText(m_list.get(position).repr());
        /*
        viewHolder.checkbox.setChecked(m_list.get(position).m_isSelected);
        viewHolder.completed.setChecked(m_list.get(position).m_isCompleted);

         */
        viewHolder.checkbox.setChecked(false);
        viewHolder.completed.setChecked(false);
        return convertView;
    }
}