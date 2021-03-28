/*
 * (C) Copyright Syd Logan 2020-2021
 * (C) Copyright Thousand Smiles Foundation 2020-2021
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
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
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
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.DentalState;

import java.util.ArrayList;

import static android.view.View.GONE;

public class CDTCodesListDialogFragment extends DialogFragment implements CDTCodeEditorCompletionPublisher {

    private int m_patientId;
    private View m_view;

    // maintain some lists as tooth codes are modified. These will be passed to listeners
    // once the user submits the dialog.

    private ArrayList<CDTCodesModel> m_supportedCodes = new ArrayList<CDTCodesModel>(); // the static set of codes supported by the app
    private ArrayList<CDTCodesModel> m_uncompleted = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel> m_completed = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel> m_added = new ArrayList<CDTCodesModel>();
    private ArrayList<CDTCodesModel>m_removed = new ArrayList<CDTCodesModel>();

    private ArrayList<PatientDentalToothState> m_initialState = new ArrayList<PatientDentalToothState>(); // initial state of tooth on launch of dialog
    private ArrayList<PatientDentalToothState> m_endState = new ArrayList<PatientDentalToothState>(); // state of tooth on non-cancelling dismissal of dialog

    private CDTCodesModelList m_list = CDTCodesModelList.getInstance();
    private CDTCodesAdapter m_adapter;
    private CDTCodesAdapter m_listAdapter;
    private TextView m_textView;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private String m_toothString;
    private int m_toothNumber;
    private Dialog m_dialog;
    private boolean m_isFullMouth = false;
    private boolean m_isTop = false;
    private ArrayList<CDTCodeEditorCompletionListener> m_listeners = new ArrayList<CDTCodeEditorCompletionListener>();

    public void setPatientId(int id) {
        m_patientId = id;
    }

    public void setTop(boolean val) {
        m_isTop = val;
    }

    private String getTextField()
    {
        String ret = new String();

        if (m_textView != null) {
            ret = m_textView.getText().toString();
        }
        return ret;
    }

    private PatientDentalToothState getEndStateItem(int id) {
        PatientDentalToothState ret = null;

        for (int i = 0; i < m_endState.size(); i++) {
           if (m_endState.get(i).getCDTCodesModel().getId() == id) {
               ret = m_endState.get(i);
           }
        }
        return ret;
    }

    private ArrayList<CDTCodesModel> getCheckedCDTCodesFromUI()
    {
        ArrayList<CDTCodesModel> l = m_listAdapter.getCheckedItems();
        return l;
    }

    private ArrayList<CDTCodesModel> getCompletedCDTCodesFromUI()
    {
        ArrayList<CDTCodesModel> l = m_listAdapter.getCompletedItems();

        for (int i = 0; i < l.size(); i++) {
            CDTCodesModel model = l.get(i);
            PatientDentalToothState state = getEndStateItem(model.getId());
            if (state != null) {
                state.setCompleted(true);
            }
        }
        return l;
    }

    private CDTCodesModel getMissingToothCode() {
        CDTCodesModel ret = null;
        for (int i = 0; i < m_supportedCodes.size(); i++) {
            CDTCodesModel p = m_supportedCodes.get(i);
            if (p.getCode().equals("D1001")) {
                ret = p;
                break;
            }
        }
        return ret;
    }

    private void updateEndState() {
        boolean isMissing;

        CheckBox cb = m_view.findViewById(R.id.tooth_missing);
        isMissing = cb.isChecked();

        if (isMissing == true && m_endState.size() == 0) {
            PatientDentalToothState s = new PatientDentalToothState();
            s.setMissing(isMissing);
            s.setToothNumber(m_toothNumber);
            s.setCDTCodesModel(getMissingToothCode());
            s.setUpper(m_isTop);
            m_endState.add(s);
        }

        for (int i = 0; i < m_endState.size(); i++) {
            PatientDentalToothState state = m_endState.get(i);
            CDTCodesModel model = state.getCDTCodesModel();
            ArrayList<DentalState.Surface> surfaces = m_listAdapter.getItemSurfaces(model);
            state.setSurfaces(surfaces);
            state.setMissing(isMissing);
            state.setCompleted(m_listAdapter.getItemCompleted(model));
            state.setToothNumber(m_toothNumber);
            state.setUpper(m_isTop);
        }
    }

    public void addInitialToothState(PatientDentalToothState toothState)
    {
        m_initialState.add(toothState);
        m_endState.add(toothState);
    }

    private ArrayList<CDTCodesModel> getUncompletedCDTCodesFromUI()
    {
        return m_listAdapter.getUncompletedItems();
    }

    private ArrayList<PatientDentalToothState> mergeInitAndEndStates()
    {
        ArrayList<PatientDentalToothState> ret = new ArrayList<PatientDentalToothState>();
        ret = (ArrayList<PatientDentalToothState>) m_endState.clone();

        for (Object x : m_initialState){
            if (!ret.contains(x))
                ret.add((PatientDentalToothState) x);
        }
        return ret;
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

                        if (m_supportedCodes.indexOf(codes.get(i)) != -1) {
                            m_removed.add(codes.get(i));
                        }
                        m_added.remove(codes.get(i));

                        // XXX removing this code is needed for correct handling at dismissal (tooth coloring)
                        // XXX keeping this code is needed for correct display in dialog once item removed.

                        PatientDentalToothState state = new PatientDentalToothState();
                        state.setCDTCodesModel(codes.get(i));
                        state.setToothNumber(m_toothNumber);
                        state.setUpper(m_isTop);
                        m_endState.remove(state);
                    }
                }
            }
        }
        m_listAdapter.removeCDTCodes(codes);

        if (listView.getCount() == 0) {
            View v = m_view.findViewById(R.id.cdt_codes_list);
            v.setVisibility(GONE);
            v = m_view.findViewById(R.id.remove_cdt_code_button);
            v.setVisibility(GONE);
        }
    }

    private void addCDTCodeToUI(CDTCodesModel cdtCode, PatientDentalToothState state, boolean addToEndState)
    {
        String repr = cdtCode.repr();
        if (repr.length() > 0 && cdtCode.getCode().equals("D1001") == false) {
            try {
                m_listAdapter.add(cdtCode);
                m_listAdapter.notifyDataSetChanged();
                View v = m_view.findViewById(R.id.cdt_codes_list);
                v.setVisibility(View.VISIBLE);
                v = m_view.findViewById(R.id.remove_cdt_code_button);
                v.setVisibility(View.VISIBLE);
                if (m_added.indexOf(cdtCode) == -1) {
                    m_added.add(cdtCode);
                    m_listAdapter.stateListAdd(cdtCode, state);
                }
                m_removed.remove(cdtCode);

                if (state != null) {
                    if (addToEndState == true) {
                        m_endState.add(state);
                    }
                }

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
        ArrayList<CDTCodesModel> completedItems = getCompletedCDTCodesFromUI();
        ArrayList<CDTCodesModel> uncompletedItems = getUncompletedCDTCodesFromUI();
        updateEndState();
        boolean isMissing;

        CheckBox cb = m_view.findViewById(R.id.tooth_missing);
        isMissing = cb.isChecked();
        ArrayList<PatientDentalToothState> mergedStates = mergeInitAndEndStates();
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).onCompletion(m_toothString, isMissing, m_added, m_removed, completedItems, uncompletedItems, mergedStates);
        }
    }

    /*
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

    */

    private void configCDTCodesAutocomplete()
    {
        AutoCompleteTextView textView = (AutoCompleteTextView) m_view.findViewById(R.id.cdtcodesautocomplete);
        String[] MultipleTextStringValue = CommonSessionSingleton.getInstance().getCDTCodesListStringArray();
        ArrayAdapter<String> cdtCodeNames = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, MultipleTextStringValue);
        textView.setAdapter(cdtCodeNames);
        textView.setThreshold(2);

        // get CDT list from backend

        //AsyncTask task = new GetCDTCodesList();
        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Object) null);
    }

    public void setToothNumber(int tooth)
    {
        m_toothNumber = tooth;
    }

    public void setToothString(String tooth)
    {
        m_toothString = tooth;
    }

    public void isFullMouth(boolean val)
    {
        m_isFullMouth = val;
    }

    private
    void initDialog() {

        for (int i = 0; i < m_endState.size(); i++) {
            PatientDentalToothState s = m_endState.get(i);
            if (s.getRemoved() == false && s.getCDTCodesModel() != null) {
                CDTCodesModel m = s.getCDTCodesModel();
                addCDTCodeToUI(m, s, false);
                if (m.getCode().equals("D1001")) {
                    // tooth missing code
                    CheckBox cb = m_view.findViewById(R.id.tooth_missing);
                    cb.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        m_supportedCodes = m_adapter.getAllItems();
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
                    PatientDentalToothState state = new PatientDentalToothState();
                    state.setCDTCodesModel(m);
                    addCDTCodeToUI(m, state,true);
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

        if (m_isFullMouth) {
            CheckBox cb = m_view.findViewById(R.id.tooth_missing);
            if (cb != null) {
                cb.setVisibility(GONE);
            }
        }

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
        String title;
        if (m_isFullMouth == false) {
            title = String.format("%s - Tooth %s", getContext().getString(R.string.title_edit_cdt_codes_dialog), m_toothString);
        } else {
            title = getContext().getString(R.string.title_edit_cdt_codes_full_mouth_dialog);
        }
        m_dialog.setTitle(title);
        configCDTCodesAutocomplete();
        if (listView.getCount() == 0) {
            View v = m_view.findViewById(R.id.cdt_codes_list);
            v.setVisibility(GONE);
            v = m_view.findViewById(R.id.remove_cdt_code_button);
            v.setVisibility(GONE);
        }
        initDialog();
        return m_dialog;
    }
}