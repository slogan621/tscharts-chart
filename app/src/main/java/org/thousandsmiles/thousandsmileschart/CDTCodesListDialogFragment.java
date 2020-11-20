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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.CDTCodesModel;
import org.thousandsmiles.tscharts_lib.CDTCodesModelList;
import org.thousandsmiles.tscharts_lib.CDTCodesREST;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.MedicationsModel;
import org.thousandsmiles.tscharts_lib.MedicationsModelList;
import org.thousandsmiles.tscharts_lib.MedicationsREST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CDTCodesListDialogFragment extends DialogFragment implements CDTCodeEditorCompletionPublisher {

    private int m_patientId;
    private View m_view;

    // maintain some lists as tooth codes are modified. These will be passed to listeners
    // once the user submits the dialog.

    private ArrayList<CDTCodesModel> m_initial = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel> m_uncompleted = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel> m_completed = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel> m_added = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel>m_removed = new ArrayList<CDTCodesModel>();

    private CDTCodesModelList m_list = CDTCodesModelList.getInstance();
    private CDTCodesAdapter m_adapter;
    private CDTCodesAdapter m_listAdapter;
    private TextView m_textView;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private String m_tooth;
    private Dialog m_dialog;
    private ArrayList<CDTCodeEditorCompletionListener> m_listeners = new ArrayList<CDTCodeEditorCompletionListener>();

    public void setPatientId(int id) {
        m_patientId = id;
    }

    private String getTextField()
    {
        String ret = new String();

        if (m_textView != null) {
            ret = m_textView.getText().toString();
        }
        return ret;
    }

    private ArrayList<CDTCodesModel> getCheckedCDTCodesFromUI()
    {
        return m_listAdapter.getCheckedItems();
    }

    private ArrayList<CDTCodesModel> getCompletedCDTCodesFromUI()
    {
        return m_listAdapter.getCompletedItems();
    }

    private ArrayList<CDTCodesModel> getUncompletedCDTCodesFromUI()
    {
        return m_listAdapter.getUncompletedItems();
    }

    private void removeCDTCodesFromUI(ArrayList<CDTCodesModel> codes)
    {
        ListView listView = (ListView) m_view.findViewById(R.id.cdt_codes_list);
        for (int i = 0; i < codes.size(); i++) {
            for (int j = 0; j < listView.getCount(); j++) {
                View v = listView.getChildAt(j);
                if (v != null) {
                    TextView t = (TextView) v.findViewById(R.id.label);
                    if (t.getText().toString().equals(codes.get(i).repr())) {
                        listView.removeViewInLayout(v);
                        if (m_initial.indexOf(codes.get(i)) != -1) {
                            m_removed.add(codes.get(i));
                        }
                        m_added.remove(codes.get(i));
                    }
                }
            }
        }
        m_listAdapter.removeCDTCodes(codes);

        if (listView.getCount() == 0) {
            View v = m_view.findViewById(R.id.cdt_codes_list);
            v.setVisibility(View.GONE);
            v = m_view.findViewById(R.id.remove_cdt_code_button);
            v.setVisibility(View.GONE);
        }
    }

    private void addCDTCodeToUI(CDTCodesModel cdtCode)
    {
        String repr = cdtCode.repr();
        if (repr.length() > 0) {
            try {
                m_listAdapter.add(cdtCode);
                m_listAdapter.notifyDataSetChanged();
                View v = m_view.findViewById(R.id.cdt_codes_list);
                v.setVisibility(View.VISIBLE);
                v = m_view.findViewById(R.id.remove_cdt_code_button);
                v.setVisibility(View.VISIBLE);
                if (m_added.indexOf(cdtCode) == -1) {
                    m_added.add(cdtCode);
                    m_listAdapter.stateListAdd(cdtCode);
                }
                m_removed.remove(cdtCode);
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.msg_failed_to_set_cdt_code, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), R.string.msg_enter_a_cdt_code, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void subscribe(CDTCodeEditorCompletionListener instance) {
        m_listeners.add(instance);
    }

    @Override
    public void unsubscribe(CDTCodeEditorCompletionListener instance) {
        m_listeners.remove(instance);
    }

    private void onCancel() {
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).onCancel();
        }
    }

    private void onCompletion() {
        ArrayList completedItems = getCompletedCDTCodesFromUI();
        ArrayList uncompletedItems = getUncompletedCDTCodesFromUI();
        boolean isMissing;

        CheckBox cb = m_view.findViewById(R.id.tooth_missing);
        isMissing = cb.isChecked();
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).onCompletion(m_tooth, isMissing, m_added, m_removed, completedItems, uncompletedItems);
        }
    }

    public class GetCDTCodesList extends AsyncTask<Object, Object, Object> {
        @Override
        protected String doInBackground(Object... params) {
            getCDTCodesList();
            return "";
        }

        private void getCDTCodesList() {
            final CDTCodesREST cdtCodesREST = new CDTCodesREST(m_sess.getContext());
            Object lock = cdtCodesREST.getCDTCodesList();

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = cdtCodesREST.getStatus();
            if (status == 200) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        AutoCompleteTextView textView = (AutoCompleteTextView) m_view.findViewById(R.id.cdtcodesautocomplete);
                        String[] MultipleTextStringValue = CommonSessionSingleton.getInstance().getCDTCodesListStringArray();
                        ArrayAdapter<String> cdtCodeNames = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, MultipleTextStringValue);
                        textView.setAdapter(cdtCodeNames);
                        textView.setThreshold(2);
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), R.string.msg_unable_to_get_cdt_codes_list, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void configCDTCodesAutocomplete()
    {
        // get CDT list from backend

        AsyncTask task = new GetCDTCodesList();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
    }

    public void setToothNumber(String tooth)
    {
        m_tooth = tooth;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.cdt_codes_list_dialog, null);

        ListView listView = (ListView) m_view.findViewById(R.id.cdt_codes_list);

        String str = getTextField();

        // get cdt codes for tooth and populate list

        ////CSVToItems(str);

        m_adapter = new CDTCodesAdapter(getActivity(), m_list.getModels());
        m_initial = m_adapter.getAllItems(); // initial list of items
        m_listAdapter = new CDTCodesAdapter(getActivity(), new ArrayList<CDTCodesModel>());
        listView.setAdapter(m_listAdapter);

        View button_item = m_view.findViewById(R.id.add_cdt_code_button);
        button_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                AutoCompleteTextView textView = (AutoCompleteTextView) m_view.findViewById(R.id.cdtcodesautocomplete);
                String cdt = textView.getText().toString();
                CDTCodesModel m = m_list.getModel(cdt);
                if (m != null) {
                    addCDTCodeToUI(m);
                }
                textView.setText("");

                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_view.getWindowToken(), 0);
            }
        });
        button_item = m_view.findViewById(R.id.remove_cdt_code_button);
        button_item.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                /* iterate the list for checked items */

                ArrayList checkedItems = getCheckedCDTCodesFromUI();

                /* remove from adapter */

                if (checkedItems.size() != 0) {
                    removeCDTCodesFromUI(checkedItems);
                }
            }
        });

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.select_medications_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        /* get all cdt codes in the list */

                        onCompletion();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.select_medications_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onCancel();
                        dialog.dismiss();
                    }
                });
        m_dialog = builder.create();
        m_dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        String title = String.format("%s - Tooth %s", getContext().getString(R.string.title_edit_cdt_codes_dialog), m_tooth);
        m_dialog.setTitle(title);
        configCDTCodesAutocomplete();
        if (listView.getCount() == 0) {
            View v = m_view.findViewById(R.id.cdt_codes_list);
            v.setVisibility(View.GONE);
            v = m_view.findViewById(R.id.remove_cdt_code_button);
            v.setVisibility(View.GONE);
        }
        return m_dialog;
    }
}