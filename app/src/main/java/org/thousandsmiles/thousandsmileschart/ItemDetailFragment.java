/*
 * (C) Copyright Syd Logan 2017-2020
 * (C) Copyright Thousand Smiles Foundation 2017-2020
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

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.thousandsmiles.tscharts_lib.HeadshotImage;
import org.thousandsmiles.tscharts_lib.ImageDisplayedListener;
import org.thousandsmiles.tscharts_lib.PatientData;

import java.util.Locale;

public class ItemDetailFragment extends Fragment implements ImageDisplayedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private View m_rootView;

    /**
     * The dummy content this fragment is presenting.
     */
    private static PatientItem mItem;

    private SessionSingleton m_sess = SessionSingleton.getInstance();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    public void onImageDisplayed(int imageId, String path)
    {
        m_sess.getCommonSessionSingleton().addHeadShotPath(imageId, path);
        m_sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    public void onImageError(int imageId, String path, int errorCode)
    {
        if (errorCode != 404) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_unable_to_get_patient_headshot), Toast.LENGTH_SHORT).show();
                }
            });
        }
        m_sess.getCommonSessionSingleton().removeHeadShotPath(imageId);
        m_sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItem = m_sess.getActivePatientItem();
    }

    private void updatePatientView()
    {
        if (mItem != null) {
            try {
                ((TextView) m_rootView.findViewById(R.id.detail_row_name_id)).setText(getResources().getString(R.string.label_summary_id));
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_id)).setText(mItem.pObject.getString("id"));
                ((TextView) m_rootView.findViewById(R.id.detail_row_name_paternal_last)).setText(getResources().getString(R.string.paternal_last));
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_paternal_last)).setText(mItem.pObject.getString("paternal_last"));
                ((TextView) m_rootView.findViewById(R.id.detail_row_name_maternal_last)).setText(getResources().getString(R.string.maternal_last));
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_maternal_last)).setText(mItem.pObject.getString("maternal_last"));
                ((TextView) m_rootView.findViewById(R.id.detail_row_name_first)).setText(getResources().getString(R.string.first_name));
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_first)).setText(mItem.pObject.getString("first"));
                ((TextView) m_rootView.findViewById(R.id.detail_row_name_gender)).setText(getResources().getString(R.string.gender));
                String gender = mItem.pObject.getString("gender");
                TextView tx = (TextView) m_rootView.findViewById(R.id.detail_row_name_paternal_last);
                tx.setTypeface(null, Typeface.BOLD_ITALIC);
                tx.setBackgroundResource(R.color.pressed_color);
                tx = (TextView) m_rootView.findViewById(R.id.detail_row_value_paternal_last);
                tx.setTypeface(null, Typeface.BOLD_ITALIC);
                tx.setBackgroundResource(R.color.pressed_color);
                Locale current = getResources().getConfiguration().locale;
                if (current.getLanguage().equals("es")) {
                    if (gender.equals("Male") || gender.equals("Masculino")) {
                        gender = getResources().getString(R.string.male);
                    } else {
                        gender = getResources().getString(R.string.female);
                    }
                }
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_gender)).setText(gender);
                ImageView img = ((ImageView) m_rootView.findViewById(R.id.headshot));

                if (gender.equals("Male") || gender.equals("Masculino")) {
                    img.setImageResource(R.drawable.boyfront);
                } else {
                    img.setImageResource(R.drawable.girlfront);
                }

                HeadshotImage headshot  = new HeadshotImage();
                m_sess.getCommonSessionSingleton().addHeadshotImage(headshot);
                headshot.setActivity(getActivity());
                headshot.setImageView(img);
                headshot.registerListener(this);
                Thread t = headshot.getImage(mItem.pObject.getInt("id"));

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(350, 350);
                img.setLayoutParams(layoutParams);

                ((TextView) m_rootView.findViewById(R.id.detail_row_name_dob)).setText(getResources().getString(R.string.dob));
                PatientData d = new PatientData();
                String dob = mItem.pObject.getString("dob");
                d.setDob(dob);
                ((TextView) m_rootView.findViewById(R.id.detail_row_value_dob)).setText(d.getDobMilitary(m_sess.getContext()));
            } catch (JSONException e) {

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            updatePatientView();
        }

        return m_rootView;
    }
}
