/*
 * (C) Copyright Syd Logan 2019-2021
 * (C) Copyright Thousand Smiles Foundation 2019-2021
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
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.FormDirtyListener;
import org.thousandsmiles.tscharts_lib.FormDirtyNotifierFragment;
import org.thousandsmiles.tscharts_lib.FormDirtyPublisher;
import org.thousandsmiles.tscharts_lib.FormSaveAndPatientCheckoutNotifierActivity;
import org.thousandsmiles.tscharts_lib.FormSaveListener;
import org.thousandsmiles.tscharts_lib.HeadshotImage;
import org.thousandsmiles.tscharts_lib.ImageDisplayedListener;
import org.thousandsmiles.tscharts_lib.PatientCheckoutListener;
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.XRay;
import org.thousandsmiles.tscharts_lib.XRayREST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AppPatientXRayEditorFragment extends FormDirtyNotifierFragment implements FormSaveListener, PatientCheckoutListener {
    private FormSaveAndPatientCheckoutNotifierActivity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private XRay m_xray = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private ImageMap m_childImageMap;
    private ImageMap m_adultImageMap;
    private ToothMapState m_childToothMapState;
    private ToothMapState m_adultToothMapState;
    private XRay.XRayMouthType m_mouthType;
    private ArrayList<FormDirtyListener> m_listeners = new ArrayList<FormDirtyListener>();
    private XRayThumbnailTable m_currentXRayThumbnailTable = new XRayThumbnailTable(R.id.xray_current_image_table);
    private XRayThumbnailTable m_olderXRayThumbnailTable = new XRayThumbnailTable(R.id.xray_older_image_table);
    ;

    private void initImageMaps() {
        m_childImageMap = new ImageMap();
        m_adultImageMap = new ImageMap();

        m_childToothMapState = new ToothMapState();
        m_adultToothMapState = new ToothMapState();

        m_childImageMap.setColoredPixel(getResources().getColor(R.color.colorThousandsmiles));
        m_childImageMap.setUncoloredPixel(getResources().getColor(R.color.xrayGray));
        BitmapDrawable b = (BitmapDrawable) (this.getResources().getDrawable(R.drawable.child_teeth_gray));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 577, 714, false);
        m_childImageMap.setWidth(577);
        m_childImageMap.setHeight(714);
        m_childImageMap.setBitmap(resizedBitmap);

        createChildHitRegions();

        m_adultImageMap.setColoredPixel(getResources().getColor(R.color.colorThousandsmiles));
        m_adultImageMap.setUncoloredPixel(getResources().getColor(R.color.xrayGray));
        b = (BitmapDrawable) this.getResources().getDrawable(R.drawable.adult_teeth_gray);
        resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 587, 877, false);
        m_adultImageMap.setWidth(587);
        m_adultImageMap.setHeight(877);
        m_adultImageMap.setBitmap(resizedBitmap);

        createAdultHitRegions();
    }

    private boolean validate() {
        final XRay xray = this.copyXRayDataFromUI();
        if (xray == null) {
            return false;
        }
        return true;
    }

    void notifyReadyForCheckout(boolean success) {
        m_activity.fragmentReadyForCheckout(success);
    }

    void notifySaveDone(boolean success) {
        m_activity.fragmentSaveDone(success);
    }

    private boolean saveInternal(final boolean showReturnToClinic) {
        boolean ret = validate();
        if (ret == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

            builder.setTitle(m_activity.getString(R.string.title_unsaved_xray));
            builder.setMessage(m_activity.getString(R.string.msg_save_xray));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateXRay();
                    if (showReturnToClinic == true) {
                        notifyReadyForCheckout(true);
                    } else {
                        notifySaveDone(true);
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (showReturnToClinic == true) {
                        notifyReadyForCheckout(false);
                    } else {
                        notifySaveDone(true);
                    }
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
        return ret;
    }

    @Override
    public boolean save() {
        boolean ret = true;
        if (m_dirty) {
            ret = saveInternal(false);
        } else {
            notifySaveDone(true);
        }
        return ret;
    }

    @Override
    public boolean checkout() {
        if (m_dirty) {
            saveInternal(true);
        } else {
            notifyReadyForCheckout(true);
        }
        return true;
    }

    protected class HitRegion {
        public int m_x1;
        public int m_y1;
        public int m_x2;
        public int m_y2;
        public int m_width;
        public int m_height;

        HitRegion(int x1, int y1, int x2, int y2) {
            m_x1 = x1;
            m_y1 = y1;
            m_x2 = x2;
            m_y2 = y2;
            m_width = m_x2 - m_x1;
            m_height = m_y2 - m_y1;
        };
    }

    private void createAdultHitRegions() {
        HitRegion [] regions = {
            new HitRegion(61, 368, 101, 409),
            new HitRegion(70, 303, 119, 343),
            new HitRegion(85, 225, 147, 280),
            new HitRegion(109, 172, 146, 201),
            new HitRegion(129, 128, 171, 154),
            new HitRegion(154, 82, 188, 106),
            new HitRegion(193, 51, 228, 79),
            new HitRegion(238, 29, 280, 66),
            new HitRegion(304, 25, 346, 75),
            new HitRegion(365, 50, 402, 73),
            new HitRegion(405, 86, 438, 114),
            new HitRegion(413, 137, 461, 159),
            new HitRegion(433, 181, 485, 211),
            new HitRegion(440, 241, 507, 281),
            new HitRegion(458, 313, 519, 351),
            new HitRegion(464, 378, 527, 430),
            new HitRegion(479, 502, 526, 547),
            new HitRegion(446, 570, 505, 620),
            new HitRegion(430, 641, 489, 696),
            new HitRegion(418, 718, 469, 760),
            new HitRegion(395, 768, 444, 809),
            new HitRegion(360, 806, 404, 840),
            new HitRegion(329, 822, 356, 857),
            new HitRegion(293, 834, 320, 860),
            new HitRegion(258, 836, 281, 856),
            new HitRegion(220, 824, 243, 849),
            new HitRegion(174, 804, 209, 828),
            new HitRegion(142, 763, 177, 792),
            new HitRegion(121, 713, 159, 742),
            new HitRegion(99, 638, 151, 686),
            new HitRegion(79, 562, 136, 606),
            new HitRegion(67, 495, 115, 532)
        };

        for (int i = 0; i < regions.length; i++) {
            m_adultImageMap.addImageMapObject(new Point(regions[i].m_x1, regions[i].m_y1),
                    regions[i].m_width, regions[i].m_height, (Object) (i + 1));
        }
    }

    private void createChildHitRegions() {
        HitRegion [] regions = {
                new HitRegion(82, 262, 141, 310),
                new HitRegion(93, 201, 149, 241),
                new HitRegion(114, 152, 165, 185),
                new HitRegion(152, 103, 197, 139),
                new HitRegion(182, 59, 228, 95),
                new HitRegion(238, 42, 279, 74),
                new HitRegion(296, 40, 331, 80),
                new HitRegion(349, 56, 386, 86),
                new HitRegion(373, 96, 419, 125),
                new HitRegion(399, 143, 455, 184),
                new HitRegion(427, 203, 475, 238),
                new HitRegion(440, 260, 497, 299),
                new HitRegion(440, 400, 492, 442),
                new HitRegion(424, 465, 489, 503),
                new HitRegion(401, 516, 459, 562),
                new HitRegion(380, 572, 429, 610),
                new HitRegion(345, 612, 397, 648),
                new HitRegion(291, 625, 443, 669),
                new HitRegion(240, 622, 282, 664),
                new HitRegion(190, 615, 229, 650),
                new HitRegion(160, 578, 205, 605),
                new HitRegion(117, 520, 169, 561),
                new HitRegion(91, 468, 152, 507),
                new HitRegion(75, 407, 140, 448)
        };

        for (int i = 0; i < regions.length; i++) {
            m_childImageMap.addImageMapObject(new Point(regions[i].m_x1, regions[i].m_y1),
                    regions[i].m_width, regions[i].m_height, (Object) (i + 1));
        }
    }

    public static AppPatientXRayEditorFragment newInstance() {
        return new AppPatientXRayEditorFragment();
    }

    private class XRayThumbnailTable implements ImageDisplayedListener  {

        private int m_tableId;
        private ArrayList<XRayImage> m_thumbnails = new ArrayList<XRayImage>();
        private ArrayList<XRayImage> m_viewList = new ArrayList<XRayImage>();

        public XRayThumbnailTable(int tableId) {
            m_tableId = tableId;
        }

        private int searchThumbnails(int id) {
            int ret = -1;

            for (int i = 0; i < m_thumbnails.size(); i++) {
                if (m_thumbnails.get(i).getId() == id) {
                    ret = i;
                    break;
                }
            }
            return ret;
        }

        public void onImageDisplayed(int imageId, String path)
        {
            SessionSingleton sess = SessionSingleton.getInstance();
            sess.getCommonSessionSingleton().addHeadShotPath(imageId, path);
            sess.getCommonSessionSingleton().startNextHeadshotJob();
            int idx = searchThumbnails(imageId);
            if (idx != -1) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                m_thumbnails.get(idx).setBitmap(bitmap);
            }
        }

        public void onImageError(int imageId, String path, int errorCode)
        {
            if (errorCode != 404) {
                m_activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_xray_thumbnails_for_patient), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            SessionSingleton.getInstance().getCommonSessionSingleton().removeHeadShotPath(imageId);
            SessionSingleton.getInstance().getCommonSessionSingleton().startNextHeadshotJob();
        }

        private void clearXRayThumbnailList() {
            m_thumbnails.clear();
        }

        private void setTableId(int id) {
            m_tableId = id;
        }

        public void add(XRayImage item) {
            m_thumbnails.add(item);
        }

        private void ClearXRayThumbnailTable ()
        {
            TableLayout layout = (TableLayout) m_activity.findViewById(m_tableId);

            if (layout != null) {
                int count = layout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = layout.getChildAt(i);
                    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                }
            }
        }

        private void HideXRayThumbnailTable ()
        {
            View v = (View) m_activity.findViewById(m_tableId);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }

        private void ShowXRayThumbnailTable()
        {
            View v = (View) m_activity.findViewById(m_tableId);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
        }

        private void setViewButtonEnabled(boolean enabled) {
            Button view = m_activity.findViewById(R.id.button_xray_view_selected);
            if (view != null) {
                view.setEnabled(enabled);
            }
        }

        private void LayoutXRayThumbnailTable () {

            TableLayout tableLayout = (TableLayout) m_activity.findViewById(m_tableId);
            // debug tableLayout.setBackgroundColor(getResources().getColor(R.color.colorYellow));
            TableRow row = null;

            ClearXRayThumbnailTable();
            ShowXRayThumbnailTable();

            TextView txt = new TextView(m_activity.getApplicationContext());

            HashMap<Integer, PatientData> map = m_sess.getPatientHashMap();

            for (int i = 0; i < m_thumbnails.size(); i++) {

                XRayImage img = m_thumbnails.get(i);


                row = new TableRow(m_activity.getApplicationContext());
                TableRow.LayoutParams tableRowLOParams = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float) 1.0);
                // debug row.setBackgroundColor(getResources().getColor(R.color.colorThousandsmiles));

                LinearLayout imageAndLabelsLO = new LinearLayout(m_activity.getApplicationContext());
                LinearLayout.LayoutParams imageAndLabelsLOParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float) 1.0);

                imageAndLabelsLO.setOrientation(LinearLayout.VERTICAL);
                // debug imageAndLabelsLO.setBackgroundColor(getResources().getColor(R.color.colorRed));
                imageAndLabelsLO.setLayoutParams(imageAndLabelsLOParams);

                ImageView imgView = new ImageView(m_activity.getApplicationContext());

                imgView.setTag(img);

                ActivityManager.MemoryInfo memoryInfo = m_sess.getCommonSessionSingleton().getAvailableMemory();

                if (!memoryInfo.lowMemory) {
                    HeadshotImage headshot = new HeadshotImage();
                    headshot.setImageType("Xray");
                    m_sess.getCommonSessionSingleton().addHeadshotImage(headshot);
                    headshot.setActivity(m_activity);
                    headshot.setImageView(imgView);
                    headshot.registerListener(this);
                    Thread t = headshot.getImage(img.getId());
                    m_sess.getCommonSessionSingleton().addHeadshotJob(headshot);
                } else {
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity.getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                imageAndLabelsLO.addView(imgView);

                txt = new TextView(m_activity.getApplicationContext());
                try {
                    String start = m_sess.getCommonSessionSingleton().getClinicById(img.getClinic()).getString("start");
                    String end = m_sess.getCommonSessionSingleton().getClinicById(img.getClinic()).getString("end");
                    String val;
                    if (start.equals(end)) {
                        val = start;
                    } else {
                        val = String.format("%s - %s", start, end);
                    }
                    txt.setText(String.format("Clinic Date %s", val));
                } catch (Exception e) {
                    txt.setText(String.format("Error getting clinic date"));
                }

                txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                imageAndLabelsLO.addView(txt);

                txt = new TextView(m_activity.getApplicationContext());
                txt.setText(String.format("C%03d-P%05d-%06d", img.getClinic(), img.getPatient(), img.getId()));
                txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                imageAndLabelsLO.addView(txt);

                LinearLayout chkboxAndImageLO = new LinearLayout(m_activity.getApplicationContext());
                chkboxAndImageLO.setOrientation(LinearLayout.HORIZONTAL);
                chkboxAndImageLO.setGravity(Gravity.CENTER_VERTICAL);
                // debug  chkboxAndImageLO.setBackgroundColor(getResources().getColor(R.color.colorGreen));

                CheckBox ch = new CheckBox(m_activity.getApplicationContext());
                ch.setChecked(false);
                ch.setTag(img);
                ColorStateList darkStateList = ContextCompat.getColorStateList(getContext(), R.color.colorBlack);
                CompoundButtonCompat.setButtonTintList(ch, darkStateList);
                ch.setGravity(Gravity.CENTER_VERTICAL);
                ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        XRayImage xray = (XRayImage) buttonView.getTag();
                        if (isChecked) {
                            m_viewList.add(xray);
                        } else {
                            m_viewList.remove(xray);
                        }
                        setViewButtonEnabled(m_viewList.size() == 0 ? false : true);
                    }
                });

                chkboxAndImageLO.addView(ch);
                chkboxAndImageLO.addView(imageAndLabelsLO);

                if (row != null) {
                    row.addView(chkboxAndImageLO, tableRowLOParams);
                }

                 tableLayout.addView(row, tableRowLOParams);
            }

            m_sess.getCommonSessionSingleton().startNextHeadshotJob();
        }
    }

    private void initializeCurrentXRayThumbnailData() {
        m_sess = SessionSingleton.getInstance();
        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        JSONArray xrays;
                        m_currentXRayThumbnailTable.clearXRayThumbnailList();
                        xrays = m_sess.getXRayThumbnails(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                        if (xrays == null) {
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_activity, R.string.msg_unable_to_get_xray_thumbnails_for_patient, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            for (int i = 0; i < xrays.length(); i++) {
                                try {
                                    XRayImage xray = new XRayImage();
                                    xray.setId(xrays.getInt(i));
                                    xray.setClinic(m_sess.getClinicId());
                                    xray.setPatient(m_sess.getDisplayPatientId());
                                    m_currentXRayThumbnailTable.add(xray);
                                    CommonSessionSingleton sess = CommonSessionSingleton.getInstance();
                                    sess.setContext(getContext());
                                    JSONObject co = sess.getClinicById(xray.getClinic());
                                    if (co == null) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (Exception e) {
                                        }
                                    }
                                } catch (JSONException e) {
                                }
                            }
                        }
                        m_activity.runOnUiThread(new Runnable() {
                            public void run() {
                                m_currentXRayThumbnailTable.LayoutXRayThumbnailTable();
                                /* now process older thumbnails, using current list to detect duplicates */
                                initializePastXRayThumbnailData();
                            }
                        });
                    }
                };
                thread.start();
            }
        }).start();
    }

    private void initializePastXRayThumbnailData() {

        m_sess = SessionSingleton.getInstance();
        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        JSONArray xrays;
                        m_olderXRayThumbnailTable.clearXRayThumbnailList();
                        xrays = m_sess.getXRayThumbnails(-1, m_sess.getDisplayPatientId());
                        if (xrays == null) {
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_activity, R.string.msg_unable_to_get_xray_thumbnails_for_patient, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            for (int i = 0; i < xrays.length(); i++) {
                                try {
                                    int id = xrays.getInt(i);
                                    if (m_currentXRayThumbnailTable.searchThumbnails(id) != -1) {
                                        // current clinic XRay, skip
                                        continue;
                                    }
                                    XRayImage xray = new XRayImage();
                                    xray.setId(xrays.getInt(i));
                                    xray.setClinic(m_sess.getClinicId());
                                    xray.setPatient(m_sess.getDisplayPatientId());
                                    m_olderXRayThumbnailTable.add(xray);
                                    CommonSessionSingleton sess = CommonSessionSingleton.getInstance();
                                    sess.setContext(getContext());
                                    JSONObject co = sess.getClinicById(xray.getClinic());
                                    if (co == null) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (Exception e) {
                                        }
                                    }
                                } catch (JSONException e) {
                                }
                            }
                        }
                        m_activity.runOnUiThread(new Runnable() {
                            public void run() {
                                m_olderXRayThumbnailTable.LayoutXRayThumbnailTable();
                            }
                        });
                    }
                };
                thread.start();
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(FormSaveAndPatientCheckoutNotifierActivity) context;
            initializeCurrentXRayThumbnailData();
            m_activity.subscribeSave(this);
            m_activity.subscribeCheckout(this);
        }
    }

    private void copyXRayDataToUI()
    {
        RadioButton rb1, rb2;
        CheckBox cb1, cb2, cb3, cb4;
        ArrayList<String> typeList;
        boolean enableUI = m_sess.isXRayStation() || m_sess.isDentalStation();

        if (m_xray != null) {
            cb1 = (CheckBox) m_view.findViewById(R.id.xray_type_full);
            cb2 = (CheckBox) m_view.findViewById(R.id.xray_type_anteriors_bitewings);
            cb3 = (CheckBox) m_view.findViewById(R.id.xray_type_panoramic_view);
            cb4 = (CheckBox) m_view.findViewById(R.id.xray_type_cephalometric);

            cb1.setEnabled(enableUI);
            cb2.setEnabled(enableUI);
            cb3.setEnabled(enableUI);
            cb4.setEnabled(enableUI);

            typeList = m_xray.getXrayTypeList();
            cb1.setChecked(false);
            cb2.setChecked(false);
            cb3.setChecked(false);
            cb4.setChecked(false);
            for (int i = 0; i < typeList.size(); i++) {
                String type = typeList.get(i);
                if (type.equals(XRay.XRAY_TYPE_FULL)) {
                    cb1.setChecked(true);
                } else if (type.equals(XRay.XRAY_TYPE_ANTERIORS_BITEWINGS)) {
                    cb2.setChecked(true);
                } else if (type.equals(XRay.XRAY_TYPE_PANORAMIC_VIEW)) {
                    cb3.setChecked(true);
                } else if (type.equals(XRay.XRAY_TYPE_CEPHALOMETRIC)) {
                    cb4.setChecked(true);
                }
            }

            rb1 = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_child);
            rb2 = (RadioButton) m_view.findViewById(R.id.xray_mouth_type_adult);
            rb1.setEnabled(enableUI);
            rb2.setEnabled(enableUI);
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
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).dirty(true);
        }
        m_dirty = true;
    }

    private void clearDirty() {
        for (int i = 0; i < m_listeners.size(); i++) {
            m_listeners.get(i).dirty(false);
        }
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
        CheckBox cb;

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

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_full);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_anteriors_bitewings);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_cephalometric);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_panoramic_view);
        if (cb != null) {
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        CheckBox cb;
        boolean checked;
        boolean xRayTypeSet = false;

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

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_full);
        if (cb != null) {
            if (cb.isChecked()) {
                xray.addType(XRay.XRAY_TYPE_FULL);
                xRayTypeSet = true;
            } else {
                xray.removeType(XRay.XRAY_TYPE_FULL);
            }
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_anteriors_bitewings);
        if (cb != null) {
            if (cb.isChecked()) {
                xray.addType(XRay.XRAY_TYPE_ANTERIORS_BITEWINGS);
                xRayTypeSet = true;
            } else {
                xray.removeType(XRay.XRAY_TYPE_ANTERIORS_BITEWINGS);
            }
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_panoramic_view);
        if (cb != null) {
            if (cb.isChecked()) {
                xray.addType(XRay.XRAY_TYPE_PANORAMIC_VIEW);
                xRayTypeSet = true;
            } else {
                xray.removeType(XRay.XRAY_TYPE_PANORAMIC_VIEW);
            }
        }

        cb = (CheckBox) m_view.findViewById(R.id.xray_type_cephalometric);
        if (cb != null) {
            if (cb.isChecked()) {
                xray.addType(XRay.XRAY_TYPE_CEPHALOMETRIC);
                xRayTypeSet = true;
            } else {
                xray.removeType(XRay.XRAY_TYPE_CEPHALOMETRIC);
            }
        }

        if (xRayTypeSet == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_missing_patient_data));
            builder.setMessage(m_activity.getString(R.string.msg_xray_type_missing));

            builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return null;
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
                    lock = rest.createXRay(m_sess.getDisplayPatientId(), m_sess.getClinicId(),
                            m_xray.getTeeth(), m_xray.convertFromDBXrayTypeToCSV(m_xray.getType()), m_xray.getMouthTypeAsString());
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

        Button button = (Button) m_view.findViewById(R.id.button_xray_set);
        button.setEnabled(m_sess.isXRayStation() || m_sess.isDentalStation());
        if ((m_sess.isXRayStation() || m_sess.isDentalStation()) && button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    m_xray.setTeeth(0xffffffff);
                    setDirty();
                    colorTeeth();
                }
            });
        }

        button = (Button) m_view.findViewById(R.id.button_xray_clear);
        button.setEnabled(m_sess.isXRayStation() || m_sess.isDentalStation());
        if ((m_sess.isXRayStation() || m_sess.isDentalStation()) && button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    m_xray.setTeeth(0);
                    setDirty();
                    colorTeeth();
                }
            });
        }

        button = (Button) m_view.findViewById(R.id.button_xray_view_selected);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                  showXRayViewer();
                }
            });
        }

        final View mouthImage = (View)getActivity().findViewById(R.id.xray_mouth_image);
        mouthImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (m_sess.isXRayStation() == false && m_sess.isDentalStation() == false) {
                    return false;
                }
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

    private void showXRayViewer()
    {
        XraysDetailFragment fragment = new XraysDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("xrays", m_currentXRayThumbnailTable.m_viewList);
        fragment.setArguments(arguments);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity.unsubscribeSave(this);
        m_activity.unsubscribeCheckout(this);
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

    @Override
    public void subscribeDirty(FormDirtyListener instance) {
        m_listeners.add(instance);
    }

    @Override
    public void unsubscribeDirty(FormDirtyListener instance) {
        m_listeners.remove(instance);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_activity != null) {
            m_activity.unsubscribeSave(this);
            m_activity.unsubscribeCheckout(this);
        }
    }
}