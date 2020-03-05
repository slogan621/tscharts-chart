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

package org.thousandsmiles.thousandsmilesstation;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.thousandsmiles.tscharts_lib.ENTDiagnosis;
import org.thousandsmiles.tscharts_lib.ENTDiagnosisExtra;
import org.thousandsmiles.tscharts_lib.ENTHistory;

import java.util.ArrayList;

public class ENTDiagnosisExtraDialogFragment extends DialogFragment {

    private View m_view;
    private Activity m_parentActivity;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private AppENTDiagnosisFragment m_appENTDiagnosisFragment;

    public void setAppENTDiagnosisFragment(AppENTDiagnosisFragment frag) {
        m_appENTDiagnosisFragment = frag;
    }

    private void disableRemoveButton()
    {
        m_appENTDiagnosisFragment.disableRemoveButton();
    }

    private void enableRemoveButton()
    {
        m_appENTDiagnosisFragment.enableRemoveButton();
    }

    public void setParentActivity(Activity activity) {
        m_parentActivity = activity;
    }

    private void AppendExtraToView() {
        TableLayout tableLayout = m_parentActivity.findViewById(R.id.extra_container);
        Context context = m_parentActivity.getApplicationContext();
        ArrayList<ENTDiagnosisExtra> extra = m_sess.getENTDiagnosisExtraList();

        ((ViewGroup) tableLayout).removeAllViews();

        for (int i = 0; i < extra.size(); i++) {

            ENTDiagnosisExtra ex = extra.get(i);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            layout.setLayoutParams(llParams);
            CheckBox cb = new CheckBox(context);


            int states[][] = {{android.R.attr.state_checked}, {}};
            int color1 = getResources().getColor(R.color.lightGray);
            int color2 = getResources().getColor(R.color.colorRed);

            int colors[] = {color2, color1};
            CompoundButtonCompat.setButtonTintList(cb, new ColorStateList(states, colors));

            layout.addView(cb);
            cb.setTag((Object) ex);
            cb.setChecked(m_sess.isInENTDiagnosisExtraDeleteList(ex));

            cb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ENTDiagnosisExtra extr = (ENTDiagnosisExtra) v.getTag();
                    if (((CheckBox)v).isChecked()) {
                        m_sess.addENTDiagnosisExtraToDeleteList(extr);
                        enableRemoveButton();
                    } else {
                        m_sess.removeENTDiagnosisExtraFromDeleteList(extr);
                        if (m_sess.getENTDiagnosisExtraDeleteList().size() == 0) {
                            disableRemoveButton();
                        }
                    }
                }
            });

            TextView tv = new TextView(context);
            tv.setText(ex.getName());
            tv.setTextColor(getResources().getColor(R.color.colorBlack));
            layout.addView(tv, llParams);
            tv = new TextView(context);
            String value = ex.getValue();
            tv.setText(value);
            tv.setTextColor(getResources().getColor(R.color.colorBlack));
            layout.addView(tv, llParams);

            TableRow tr = new TableRow(context);
            /* Create a Button to be the row-content. */

            TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tableRowParams.setMargins(8, 8, 8, 8);

            tr.addView(layout, tableRowParams);

            tableLayout.addView(tr, tableRowParams);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        m_sess.clearENTDiagnosisExtraDeleteList();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.ent_diagnosis_extra_dialog, null);
        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ENTDiagnosisExtra extra = new ENTDiagnosisExtra();

                        // read dialog contents, and then poke them into the parent dialog.

                        EditText t = (EditText) m_view.findViewById(R.id.condition_name);
                        extra.setName(t.getText().toString());
                        t = (EditText) m_view.findViewById(R.id.condition_value);
                        extra.setValue(t.getText().toString());

                        SessionSingleton.getInstance().addENTExtraDiagnosis(extra);
                        m_appENTDiagnosisFragment.updateExtrasView();
                        m_appENTDiagnosisFragment.setDirty();
                    }
                })
                .setNegativeButton(R.string.checkout_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.setTitle(R.string.title_ent_diagnosis_extra_dialog);
        return ret;
    }
}