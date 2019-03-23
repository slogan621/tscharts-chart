/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.XRay;
import org.thousandsmiles.tscharts_lib.XRayREST;

public class AppPatientXRayEditorFragment extends Fragment {
    private StationActivity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private XRay m_xray = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private boolean m_leaving = false;

    public static AppPatientXRayEditorFragment newInstance() {
        return new AppPatientXRayEditorFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(StationActivity) context;
        }
    }

    private void copyXRayDataToUI()
    {
        RadioButton rb1, rb2;
        XRay.XRayType type;
        XRay.XRayMouthType mouthType;

        if (m_xray != null) {
            rb1 = (RadioButton) m_view.findViewById(R.id.xray_type_full);
            rb2 = (RadioButton) m_view.findViewById(R.id.xray_type_anteriors_bitewings);
            type = m_xray.getType();
            switch(type) {
                case XRAY_TYPE_FULL:
                    rb1.setChecked(true);
                    rb2.setChecked(false);
                    break;
                case XRAY_TYPE_ANTERIORS_BITEWINGS:
                    rb1.setChecked(false);
                    rb2.setChecked(true);
                    break;
            }

            rb1 = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_child);
            rb2 = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_adult);
            mouthType = m_xray.getMouthType();
            switch(mouthType) {
                case XRAY_MOUTH_TYPE_CHILD:
                    rb1.setChecked(true);
                    rb2.setChecked(false);
                    break;
                case XRAY_MOUTH_TYPE_ADULT:
                    rb1.setChecked(false);
                    rb2.setChecked(true);
                    break;
            }
            setMouthTypeImage(mouthType);
        }
    }

    private void setDirty()
    {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        m_xray = copyXRayDataFromUI();
        button_bar_item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                m_leaving = true;
                updateXRay();
                m_activity.showXRaySearchResults();
            }

        });
        m_dirty = true;
    }

    private void clearDirty() {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        m_dirty = false;
    }

    private void setMouthTypeImage(XRay.XRayMouthType mouthType)
    {
        ImageView img = (ImageView) m_view.findViewById(R.id.xray_mouth_image);
        if (mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
            img.setImageResource(R.drawable.child_teeth);
        } else {
            img.setImageResource(R.drawable.adult_teeth);
        }
    }

    private void setViewDirtyListeners()
    {
        RadioButton rb;

        rb = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_child);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setMouthTypeImage(isChecked == true ? XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD : XRay.XRayMouthType.XRAY_MOUTH_TYPE_ADULT);
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_adult);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setMouthTypeImage(isChecked == true ? XRay.XRayMouthType.XRAY_MOUTH_TYPE_ADULT : XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD);
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_type_full);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_type_anteriors_bitewings);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.birth_weight_lb);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
    }

    private XRay copyXRayDataFromUI()
    {
        RadioButton rb;
        boolean checked;

        XRay xray;

        if (m_xray == null) {
            xray = new XRay();
        } else {
            xray = m_xray;      // copy constructor copies over clinic, patient ID, etc..
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_child);
        if (rb != null) {
            if (rb.isChecked()) {
                xray.setMouthType(XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD);
            } else {
                xray.setMouthType(XRay.XRayMouthType.XRAY_MOUTH_TYPE_ADULT);
            }
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_type_full);
        if (rb != null) {
            if (rb.isChecked()) {
                xray.setType(XRay.XRayType.XRAY_TYPE_FULL);
            } else {
                xray.setType(XRay.XRayType.XRAY_TYPE_ANTERIORS_BITEWINGS);
            }
        }

        return xray;
    }

    void updateXRay()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                XRayREST rest = new XRayREST(m_sess.getContext());
                Object lock = null;
                int status;

                if (m_sess.getNewXRay() == true) {
                    lock = rest.createXRay(m_sess.getActivePatientId(), m_sess.getClinicId(),
                            m_xray.getTeeth(), m_xray.getTypeAsString(), m_xray.getMouthTypeAsString());
                    m_sess.setNewXRay(false);
                } else {
                    lock = rest.updateXRay(m_xray);
                }

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
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_xray), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_xray), Toast.LENGTH_LONG).show();
                        }
                    });
                }
           }
        };
        thread.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        Bundle bundle = this.getArguments();
        try {
            m_xray = (XRay) bundle.getSerializable("xray");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_xray_data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        copyXRayDataToUI();
        setViewDirtyListeners();
        if (m_sess.getNewXRay() == true) {
            setDirty();
        } else {
            clearDirty();
        }

        final View mouthImage = (View)getActivity().findViewById(R.id.xray_mouth_image);
        mouthImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (view == mouthImage) {
                    // check to see if it intersects a tooth and respond if so.
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }

        super.onPause();

        final XRay xray = this.copyXRayDataFromUI();

        if ((m_dirty || xray.equals(m_xray) == false) && m_leaving == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_xray));
            builder.setMessage(m_activity.getString(R.string.msg_save_xray));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateXRay();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_xray_editor_layout, container, false);
        m_view  = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}