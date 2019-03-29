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
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.Iterator;

public class AppPatientXRayEditorFragment extends Fragment {
    private StationActivity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private XRay m_xray = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private boolean m_leaving = false;
    private ImageMap m_childImageMap;
    private ImageMap m_adultImageMap;
    private ToothMapState m_childToothMapState;
    private ToothMapState m_adultToothMapState;
    private XRay.XRayMouthType m_mouthType;

    private void initImageMaps() {
        m_childImageMap = new ImageMap();
        m_adultImageMap = new ImageMap();

        m_childToothMapState = new ToothMapState();
        m_adultToothMapState = new ToothMapState();

        m_childImageMap.setColoredPixel(getResources().getColor(R.color.colorThousandsmiles));
        m_childImageMap.setUncoloredPixel(getResources().getColor(R.color.xrayGray));
        BitmapDrawable b = (BitmapDrawable)(this.getResources().getDrawable(R.drawable.child_teeth_gray));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 577, 714, false);
        m_childImageMap.setWidth(577);
        m_childImageMap.setHeight(714);
        m_childImageMap.setBitmap(resizedBitmap);

        m_adultImageMap.setColoredPixel(getResources().getColor(R.color.colorThousandsmiles));
        m_adultImageMap.setUncoloredPixel(getResources().getColor(R.color.xrayGray));
        b = (BitmapDrawable) this.getResources().getDrawable(R.drawable.adult_teeth_gray);
        resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 587, 877, false);
        m_adultImageMap.setWidth(587);
        m_adultImageMap.setHeight(877);
        m_adultImageMap.setBitmap(resizedBitmap);

        Point p = new Point(237, 24);
        m_adultImageMap.addImageMapObject(p, 50, 40, 1);
        p = new Point(301, 21);
        m_adultImageMap.addImageMapObject(p, 50, 40, 2);
    }

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
            m_mouthType = m_xray.getMouthType();
            switch(m_mouthType) {
                case XRAY_MOUTH_TYPE_CHILD:
                    rb1.setChecked(true);
                    rb2.setChecked(false);
                    break;
                case XRAY_MOUTH_TYPE_ADULT:
                    rb1.setChecked(false);
                    rb2.setChecked(true);
                    break;
            }
            setMouthTypeImage();
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
                // kludge give some time for the update
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
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

    private void setMouthTypeImage()
    {
        ImageView img = (ImageView) m_view.findViewById(R.id.xray_mouth_image);

        if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
            img.setImageBitmap(m_childImageMap.getBitmap());
        } else {
            img.setImageBitmap(m_adultImageMap.getBitmap());
        }

        colorTeeth();
    }

    private void colorTeeth() {
        Iterator it;
        ToothMapState state;

        if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
            state = m_childToothMapState;
            state.set(m_xray.getTeeth());
            ArrayList<ImageMap.ImageMapObject> o = m_childImageMap.getImageMapObjects();
            it = o.iterator();
        } else {
            state = m_adultToothMapState;
            state.set(m_xray.getTeeth());
            ArrayList<ImageMap.ImageMapObject> o = m_adultImageMap.getImageMapObjects();
            it = o.iterator();
        }

        FloodFill ff = new FloodFill();
        if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
            ff.setBitmap(m_childImageMap.getBitmap());
            ff.setDrawableWidth(m_childImageMap.getWidth());
            ff.setDrawableHeight(m_childImageMap.getHeight());
        } else {
            ff.setBitmap(m_adultImageMap.getBitmap());
            ff.setDrawableWidth(m_adultImageMap.getWidth());
            ff.setDrawableHeight(m_adultImageMap.getHeight());
        }
        while (it.hasNext()) {
            ImageMap.ImageMapObject o = (ImageMap.ImageMapObject) it.next();
            Point p = o.getOrigin();
            Point q = new Point(p.x + 10, p.y + 10);

            ff.setPoint(q);
            if (state.isSelected((int) o.getTag())) {
                if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
                    ff.setTargetColor(m_childImageMap.getUncoloredPixel());
                    ff.setReplacementColor(m_childImageMap.getColoredPixel());
                } else {
                    ff.setTargetColor(m_adultImageMap.getUncoloredPixel());
                    ff.setReplacementColor(m_adultImageMap.getColoredPixel());
                }
            } else {
                if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
                    ff.setTargetColor(m_childImageMap.getColoredPixel());
                    ff.setReplacementColor(m_childImageMap.getUncoloredPixel());
                } else {
                    ff.setTargetColor(m_adultImageMap.getColoredPixel());
                    ff.setReplacementColor(m_adultImageMap.getUncoloredPixel());
                }
            }
            ff.fill();
        }

        Bitmap b = ff.getBitmap();
        ImageView img = (ImageView) m_view.findViewById(R.id.xray_mouth_image);
        img.setImageBitmap(b);
    }

    private void setViewDirtyListeners()
    {
        RadioButton rb;

        rb = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_child);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    m_mouthType = isChecked == true ? XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD : XRay.XRayMouthType.XRAY_MOUTH_TYPE_ADULT;
                    setDirty();
                    setMouthTypeImage();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_adult);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    m_mouthType = isChecked == true ? XRay.XRayMouthType.XRAY_MOUTH_TYPE_ADULT : XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD;
                    setDirty();
                    setMouthTypeImage();
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
        initImageMaps();
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
                boolean ret = false;

                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (view == mouthImage && ev.getAction() == MotionEvent.ACTION_DOWN) {
                    ImageMap.ImageMapObject im;
                    if (m_mouthType == XRay.XRayMouthType.XRAY_MOUTH_TYPE_CHILD) {
                        im = m_childImageMap.hitTest(x, y);
                        if (im != null) {
                            int tooth = (int) im.getTag();
                            if (m_childToothMapState.isSelected(tooth)) {
                                m_childToothMapState.clearSelected(tooth);
                            } else {
                                m_childToothMapState.addSelected(tooth);
                            }
                            m_xray.setTeeth(m_childToothMapState.getSelected());
                            setDirty();
                            colorTeeth();
                        }
                    } else {
                        im = m_adultImageMap.hitTest(x, y);
                        if (im != null) {
                            int tooth = (int) im.getTag();
                            if (m_adultToothMapState.isSelected(tooth)) {
                                m_adultToothMapState.clearSelected(tooth);
                            } else {
                                m_adultToothMapState.addSelected(tooth);
                            }
                            m_xray.setTeeth(m_adultToothMapState.getSelected());
                            setDirty();
                            colorTeeth();
                        }
                    }
                    if (im != null) {
                        ret = true;
                    }
                }
                return ret;
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