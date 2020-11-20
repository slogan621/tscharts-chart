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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.CDTCodesModel;
import org.thousandsmiles.tscharts_lib.DentalTreatment;
import org.thousandsmiles.tscharts_lib.DentalTreatmentREST;
import org.thousandsmiles.tscharts_lib.XRay;

import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.abs;

public class AppDentalTreatmentFragment extends Fragment implements CDTCodeEditorCompletionListener, FormSaveListener, PatientCheckoutListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private DentalTreatment m_dentalTreatment = null;
    private boolean m_dirty = false;
    private View m_view = null;
    private AppFragmentContext m_ctx = new AppFragmentContext();
    private boolean m_swiped = false;
    private boolean m_topTooth = true;
    private GestureDetectorCompat m_detector;
    private boolean m_showingToothChart = false;
    private ImageMap m_topToothImageMap;
    private ImageMap m_bottomToothImageMap;
    private ToothMapState m_topToothMapState;
    private ToothMapState m_bottomToothMapState;

    @Override
    public void showReturnToClinic()
    {
        ((StationActivity)m_activity).showReturnToClinic();
    }

    private boolean saveInternal(final boolean showReturnToClinic) {
        boolean ret = validate();
        if (ret == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);

            builder.setTitle(m_activity.getString(R.string.title_unsaved_dental_treatment));
            builder.setMessage(m_activity.getString(R.string.msg_save_dental_treatment));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    updateDentalTreatment();
                    if (showReturnToClinic == true) {
                        showReturnToClinic();
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (showReturnToClinic == true) {
                        showReturnToClinic();
                    }
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        return ret;
    }

    @Override
    public boolean save() {
        boolean ret = true;
        if (m_dirty) {
            ret = saveInternal(false);
        }
        return ret;
    }

    @Override
    public boolean checkout() {
        if (m_dirty) {
            saveInternal(true);
        } else {
            showReturnToClinic();
        }
        return true;
    }

    private void initImageMaps() {
        m_topToothImageMap = new ImageMap();
        m_bottomToothImageMap = new ImageMap();

        m_topToothMapState = new ToothMapState();
        m_bottomToothMapState = new ToothMapState();

        m_topToothImageMap.setColoredPixel(getResources().getColor(R.color.colorRed));
        m_topToothImageMap.setUncoloredPixel(getResources().getColor(R.color.colorWhite));
        BitmapDrawable b = (BitmapDrawable)(this.getResources().getDrawable(R.drawable.tooth_chart_top));

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 1190, 1030, false);
        m_topToothImageMap.setWidth(resizedBitmap.getWidth());
        m_topToothImageMap.setHeight(resizedBitmap.getHeight());
        m_topToothImageMap.setBitmap(resizedBitmap);

        createTopHitRegions();

        m_bottomToothImageMap.setColoredPixel(getResources().getColor(R.color.colorRed));
        m_bottomToothImageMap.setUncoloredPixel(getResources().getColor(R.color.colorWhite));
        b = (BitmapDrawable) this.getResources().getDrawable(R.drawable.tooth_chart_bottom);

        resizedBitmap = Bitmap.createScaledBitmap(b.getBitmap(), 1190, 1030, false);
        m_bottomToothImageMap.setWidth(resizedBitmap.getWidth());
        m_bottomToothImageMap.setHeight(resizedBitmap.getHeight());
        m_bottomToothImageMap.setBitmap(resizedBitmap);

        createBottomHitRegions();
    }

    @Override
    public void onCompletion(String tooth, boolean isMissing, ArrayList<CDTCodesModel> addedItems, ArrayList<CDTCodesModel> removedItems, ArrayList<CDTCodesModel> completedItems,
                             ArrayList<CDTCodesModel> uncompletedItems) {
        int color;

        if (isMissing) {
            color = getResources().getColor(R.color.lightGray);
        } else {
            if (completedItems.size() > 0) {
                if (uncompletedItems.size() == 0) {
                    color = getResources().getColor(R.color.colorGreen);   // all work completed
                } else {
                    color = getResources().getColor(R.color.colorYellow);  // some work completed
                }
            } else {
                color = getResources().getColor(R.color.colorRed);         // no work completed
            }
        }

        int idx = stringToTooth(m_topTooth, tooth);
        if (m_topTooth == true) {
            m_topToothImageMap.setItemColor(idx, color);
        } else {
            m_bottomToothImageMap.setItemColor(idx, color);
        }
        setDirty();
        colorTeeth();
    }

    @Override
    public void onCancel() {
    }

    private boolean validate()
    {
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

    private void createBottomHitRegions() {
        AppDentalTreatmentFragment.HitRegion[] regions = {
            new AppDentalTreatmentFragment.HitRegion(959, 219, 1085, 319),
            new AppDentalTreatmentFragment.HitRegion(962, 345, 1088, 444),
            new AppDentalTreatmentFragment.HitRegion(942, 469, 1073, 584),
            new AppDentalTreatmentFragment.HitRegion(929, 612, 1043, 702),
            new AppDentalTreatmentFragment.HitRegion(871, 736, 966, 818),
            new AppDentalTreatmentFragment.HitRegion(793, 807, 890, 899),
            new AppDentalTreatmentFragment.HitRegion(702, 848, 776, 931),
            new AppDentalTreatmentFragment.HitRegion(608, 865, 682, 945),
            new AppDentalTreatmentFragment.HitRegion(527, 865, 596, 949),
            new AppDentalTreatmentFragment.HitRegion(445, 852, 507, 936),
            new AppDentalTreatmentFragment.HitRegion(351, 803, 422, 876),
            new AppDentalTreatmentFragment.HitRegion(251, 723, 367, 797),
            new AppDentalTreatmentFragment.HitRegion(201, 629, 320, 707),
            new AppDentalTreatmentFragment.HitRegion(142, 489, 283, 593),
            new AppDentalTreatmentFragment.HitRegion(119, 347, 245, 453),
            new AppDentalTreatmentFragment.HitRegion(106, 207, 233, 324),
            new AppDentalTreatmentFragment.HitRegion(797, 485, 875, 562),
            new AppDentalTreatmentFragment.HitRegion(775, 585, 850, 651),
            new AppDentalTreatmentFragment.HitRegion(733, 664, 816, 726),
            new AppDentalTreatmentFragment.HitRegion(679, 718, 749, 774),
            new AppDentalTreatmentFragment.HitRegion(610, 732, 675, 793),
            new AppDentalTreatmentFragment.HitRegion(549, 732, 600, 801),
            new AppDentalTreatmentFragment.HitRegion(481, 726, 527, 782),
            new AppDentalTreatmentFragment.HitRegion(425, 668, 499, 719),
            new AppDentalTreatmentFragment.HitRegion(381, 590, 470, 653),
            new AppDentalTreatmentFragment.HitRegion(342, 497, 442, 569)
        };

        Point fillPoints[] = {
                new Point(1006, 266),
                new Point(1039, 363),
                new Point(1028, 493),
                new Point(971, 639),
                new Point(932, 748),
                new Point(824, 837),
                new Point(733, 872),
                new Point(640, 881),
                new Point(554, 888),
                new Point(463, 869),
                new Point(397, 827),
                new Point(331, 757),
                new Point(263, 660),
                new Point(232, 543),
                new Point(205, 395),
                new Point(175, 265),
                new Point(841, 547),
                new Point(822, 616),
                new Point(774, 681),
                new Point(713, 731),
                new Point(628, 745),
                new Point(569, 750),
                new Point(499, 746),
                new Point(471, 686),
                new Point(436, 628),
                new Point(385, 510)
        };

        for (int i = 0; i < regions.length; i++) {
            m_bottomToothImageMap.addImageMapObject(new Point(regions[i].m_x1, regions[i].m_y1),
                    regions[i].m_width, regions[i].m_height, (Object) (i + 1), fillPoints[i]);
        }
    }

    private String toothToString(boolean top, int index) {
        String ret = "";
        if (top == true) {
            if (index > 16) {
                index -= 16;
                index -= 1;     // 'A' is 1
                ret = String.format("%c", index + 'A');
            } else {
                ret = String.format("%d", index);
            }
        } else {
            index += 16;
            if (index > 32) {
                index -= 32;
                index -= 1;     // 'A' is 1
                ret = String.format("%c", index + 'K');
            } else {
                ret = String.format("%d", index);
            }
        }
        return ret;
    }

    private int stringToTooth(boolean top, String str) {
        int ret;
        int ascii = (int) str.charAt(0);

        if (top == true) {
            if (ascii >= 'A') {
                ret = ascii - 'A' + 16;
            } else {
                ret = Integer.parseInt(str) - 1;
            }
        } else {
            if (ascii >= 'K') {
                ret = ascii - 'K' + 16;
            } else {
                ret = Integer.parseInt(str);
                ret = ret - 17;
            }
        }
        return ret;
    }

    private void createTopHitRegions() {
        AppDentalTreatmentFragment.HitRegion[] regions = {
            new AppDentalTreatmentFragment.HitRegion(130, 774, 261, 864),
            new AppDentalTreatmentFragment.HitRegion(160, 649, 276, 746),
            new AppDentalTreatmentFragment.HitRegion(180, 509, 294, 621),
            new AppDentalTreatmentFragment.HitRegion(219, 411, 320, 489),
            new AppDentalTreatmentFragment.HitRegion(273, 339, 363, 406),
            new AppDentalTreatmentFragment.HitRegion(343, 272, 417, 331),
            new AppDentalTreatmentFragment.HitRegion(403, 217, 487, 277),
            new AppDentalTreatmentFragment.HitRegion(500, 195, 570, 254),
            new AppDentalTreatmentFragment.HitRegion(607, 191, 696, 253),
            new AppDentalTreatmentFragment.HitRegion(726, 211, 780, 270),
            new AppDentalTreatmentFragment.HitRegion(795, 266, 871, 319),
            new AppDentalTreatmentFragment.HitRegion(850, 337, 939, 394),
            new AppDentalTreatmentFragment.HitRegion(884, 423, 988, 494),
            new AppDentalTreatmentFragment.HitRegion(902, 521, 1029, 637),
            new AppDentalTreatmentFragment.HitRegion(910, 671, 1057, 751),
            new AppDentalTreatmentFragment.HitRegion(929, 785, 1069, 885),
            new AppDentalTreatmentFragment.HitRegion(366, 526, 433, 589),
            new AppDentalTreatmentFragment.HitRegion(389, 450, 453, 510),
            new AppDentalTreatmentFragment.HitRegion(430, 391, 503, 438),
            new AppDentalTreatmentFragment.HitRegion(482, 341, 545, 392),
            new AppDentalTreatmentFragment.HitRegion(543, 309, 598, 359),
            new AppDentalTreatmentFragment.HitRegion(625, 307, 674, 363),
            new AppDentalTreatmentFragment.HitRegion(705, 340, 746, 386),
            new AppDentalTreatmentFragment.HitRegion(738, 392, 804, 434),
            new AppDentalTreatmentFragment.HitRegion(775, 451, 848, 495),
            new AppDentalTreatmentFragment.HitRegion(791, 517, 867, 576)
        };

        Point fillPoints[] = {
                new Point(189, 802),
                new Point(235, 709),
                new Point(252, 547),
                new Point(264, 466),
                new Point(330, 381),
                new Point(380, 308),
                new Point(451, 265),
                new Point(543, 223),
                new Point(642, 225),
                new Point(739, 245),
                new Point(814, 279),
                new Point(922, 344),
                new Point(949, 445),
                new Point(983, 543),
                new Point(995, 685),
                new Point(1025, 808),
                new Point(401, 540),
                new Point(428, 485),
                new Point(468, 418),
                new Point(518, 364),
                new Point(574, 328),
                new Point(640, 334),
                new Point(713, 358),
                new Point(776, 414),
                new Point(820, 480),
                new Point(839, 548)
        };

        for (int i = 0; i < regions.length; i++) {
            m_topToothImageMap.addImageMapObject(new Point(regions[i].m_x1, regions[i].m_y1),
                    regions[i].m_width, regions[i].m_height, (Object) (i + 1), fillPoints[i]);
        }
    }

    private boolean handleTouch(View view, MotionEvent ev) {
        final View mouthImage = (View)getActivity().findViewById(R.id.dental_treatment_toothchart);
        if (false && m_sess.isXRayStation() == false) {
            return false;
        }
        boolean ret = false;

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if (view == mouthImage && ev.getAction() == MotionEvent.ACTION_DOWN) {
            ImageMap.ImageMapObject im;
            String DEBUG_TAG = "Hit Points";
            if (m_topTooth == true) {
                Log.d(DEBUG_TAG, String.format("topTooth: %d, %d", x, y));
                im = m_topToothImageMap.hitTest(x, y);
                if (im != null) {
                    final int tooth = (int) im.getTag();
                    m_topToothMapState.addSelected(tooth);
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = String.format("top tooth %s", toothToString(true, tooth));
                            Toast.makeText(m_activity, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

                    CDTCodesListDialogFragment mld = new CDTCodesListDialogFragment();
                    mld.setPatientId(m_sess.getDisplayPatientId());
                    mld.subscribe(this);
                    mld.isFullMouth(false);
                    mld.setToothNumber(toothToString(true, tooth));
                    mld.show(getFragmentManager(), m_activity.getString(R.string.title_edit_cdt_codes_dialog));
                }
            } else {
                Log.d(DEBUG_TAG, String.format("bottomTooth: %d, %d", x, y));
                im = m_bottomToothImageMap.hitTest(x, y);
                if (im != null) {
                    final int tooth = (int) im.getTag();
                    m_bottomToothMapState.addSelected(tooth);
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = String.format("bottom tooth %s", toothToString(false, tooth));
                            Toast.makeText(m_activity, msg, Toast.LENGTH_SHORT).show();
                        }
                    });

                    CDTCodesListDialogFragment mld = new CDTCodesListDialogFragment();
                    mld.setPatientId(m_sess.getDisplayPatientId());
                    mld.subscribe(this);
                    mld.isFullMouth(false);
                    mld.setToothNumber(toothToString(false, tooth));
                    mld.show(getFragmentManager(), m_activity.getString(R.string.title_edit_cdt_codes_dialog));
                }
            }
            if (im != null) {
                ret = true;
            }
        }
        return ret;
    }

    public void colorTeeth() {
        Iterator it;
        ToothMapState state;

        if (m_topTooth == true) {
            state = m_topToothMapState;
            ArrayList<ImageMap.ImageMapObject> o = m_topToothImageMap.getImageMapObjects();
            it = o.iterator();
        } else {
            state = m_bottomToothMapState;
            ArrayList<ImageMap.ImageMapObject> o = m_bottomToothImageMap.getImageMapObjects();
            it = o.iterator();
        }

        FloodFill ff = new FloodFill();
        if (m_topTooth == true) {
            ff.setBitmap(m_topToothImageMap.getBitmap());
            ff.setDrawableWidth(m_topToothImageMap.getWidth());
            ff.setDrawableHeight(m_topToothImageMap.getHeight());
        } else {
            ff.setBitmap(m_bottomToothImageMap.getBitmap());
            ff.setDrawableWidth(m_bottomToothImageMap.getWidth());
            ff.setDrawableHeight(m_bottomToothImageMap.getHeight());
        }
        while (it.hasNext()) {
            ImageMap.ImageMapObject o = (ImageMap.ImageMapObject) it.next();

            if (state.isSelected((int) o.getTag())) {

                Point q = o.getFill();
                ff.setPoint(q);
                ff.setTargetColor(getResources().getColor(R.color.colorWhite));
                ff.setReplacementColor(o.getColor());
                ff.fill();
            }
        }

        Bitmap b = ff.getBitmap();
        ImageView img = (ImageView) m_view.findViewById(R.id.dental_treatment_toothchart);
        BitmapDrawable ob = new BitmapDrawable(getResources(), b);
        img.setBackground(ob);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            View v = m_view.findViewById(R.id.dental_treatment_toothchart);
            handleTouch(v, event);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            /*
             * XXX in some cases, velocityX is neg and in some, pos, could be due to
             * tablet orientation. Needs to be investigated. But for now, any swipe that
             * is along the x axis will trigger the swipe
             */
            if (abs((int)velocityX) > abs((int)velocityY)) {
                m_swiped = true;
                View v = m_view.findViewById(R.id.dental_treatment_toothchart);
                if (m_topTooth == false) {
                    m_topTooth = true;
                    v.setBackgroundResource(R.drawable.tooth_chart_top);
                } else {
                    m_topTooth = false;
                    v.setBackgroundResource(R.drawable.tooth_chart_bottom);
                }
            }
            FloatingActionButton fab = (FloatingActionButton) m_view.findViewById(R.id.fab);
            if (fab != null) {
                fab.setVisibility(View.VISIBLE);
            }
            colorTeeth();
            return true;
        }
    }

    public void setAppFragmentContext(AppFragmentContext ctx) {
        m_ctx = ctx;
    }

    public static AppDentalTreatmentFragment newInstance() {
        return new AppDentalTreatmentFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
        }
        ((StationActivity)m_activity).subscribeSave(this);
        ((StationActivity)m_activity).subscribeCheckout(this);
    }

    private void copyDentalTreatmentDataToUI()
    {
        /*
        CheckBox cb1, cb2, cb3;
        TextView tx;
        RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8;

        if (m_dentalTreatment != null) {

            // Ears

            ENTHistory.EarSide side;

            side = m_entExam.getNormal();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getMicrotia();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getWax();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getDrainage();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getExternalOtitis();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getFb();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            // tubes

            ENTExam.ENTTube tube;

            tube = m_entExam.getTubeLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (tube) {
                case ENT_TUBE_IN_PLACE:
                    rb1.setChecked(true);
                    break;
                case ENT_TUBE_EXTRUDING:
                    rb2.setChecked(true);
                    break;
                case ENT_TUBE_IN_CANAL:
                    rb3.setChecked(true);
                    break;
                case ENT_TUBE_NONE:
                    rb4.setChecked(true);
                    break;
            }

            tube = m_entExam.getTubeRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (tube) {
                case ENT_TUBE_IN_PLACE:
                    rb1.setChecked(true);
                    break;
                case ENT_TUBE_EXTRUDING:
                    rb2.setChecked(true);
                    break;
                case ENT_TUBE_IN_CANAL:
                    rb3.setChecked(true);
                    break;
                case ENT_TUBE_NONE:
                    rb4.setChecked(true);
                    break;
            }

            // Tympanosclerosis

            ENTExam.ENTTympano tympano;

            tympano = m_entExam.getTympanoLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);

            switch (tympano) {
                case ENT_TYMPANOSCLEROSIS_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_25:
                    rb3.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_50:
                    rb4.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_75:
                    rb5.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_TOTAL:
                    rb6.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_NONE:
                    rb7.setChecked(true);
                    break;
            }

            tympano = m_entExam.getTympanoRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);

            switch (tympano) {
                case ENT_TYMPANOSCLEROSIS_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_25:
                    rb3.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_50:
                    rb4.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_75:
                    rb5.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_TOTAL:
                    rb6.setChecked(true);
                    break;
                case ENT_TYMPANOSCLEROSIS_NONE:
                    rb7.setChecked(true);
                    break;
            }

            // TM

            side = m_entExam.getTmGranulations();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getTmRetraction();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            side = m_entExam.getTmAtelectasis();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);

            cb1.setChecked(false);
            cb2.setChecked(false);
            switch (side) {
                case EAR_SIDE_BOTH:
                    cb1.setChecked(true);
                    cb2.setChecked(true);
                    break;
                case EAR_SIDE_LEFT:
                    cb1.setChecked(true);
                    break;
                case EAR_SIDE_RIGHT:
                    cb2.setChecked(true);
                    break;
            }

            // Perforations

            ENTExam.ENTPerf perf;

            perf = m_entExam.getPerfLeft();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);
            rb8 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);
            rb8.setChecked(false);

            switch (perf) {
                case ENT_PERF_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_PERF_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_PERF_MARGINAL:
                    rb3.setChecked(true);
                    break;
                case ENT_PERF_25:
                    rb4.setChecked(true);
                    break;
                case ENT_PERF_50:
                    rb5.setChecked(true);
                    break;
                case ENT_PERF_75:
                    rb6.setChecked(true);
                    break;
                case ENT_PERF_TOTAL:
                    rb7.setChecked(true);
                    break;
                case ENT_PERF_NONE:
                    rb8.setChecked(true);
                    break;
            }

            perf = m_entExam.getPerfRight();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);
            rb6 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);
            rb7 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);
            rb8 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);
            rb6.setChecked(false);
            rb7.setChecked(false);
            rb8.setChecked(false);

            switch (perf) {
                case ENT_PERF_ANTERIOR:
                    rb1.setChecked(true);
                    break;
                case ENT_PERF_POSTERIOR:
                    rb2.setChecked(true);
                    break;
                case ENT_PERF_MARGINAL:
                    rb3.setChecked(true);
                    break;
                case ENT_PERF_25:
                    rb4.setChecked(true);
                    break;
                case ENT_PERF_50:
                    rb5.setChecked(true);
                    break;
                case ENT_PERF_75:
                    rb6.setChecked(true);
                    break;
                case ENT_PERF_TOTAL:
                    rb7.setChecked(true);
                    break;
                case ENT_PERF_NONE:
                    rb8.setChecked(true);
                    break;
            }

            // Hearing loss

            ENTExam.ENTVoiceTest voice;

            voice = m_entExam.getVoiceTest();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (voice) {
                case ENT_VOICE_TEST_NORMAL:
                    rb1.setChecked(true);
                    break;
                case ENT_VOICE_TEST_ABNORMAL:
                    rb2.setChecked(true);
                    break;
                case ENT_VOICE_TEST_NONE:
                    rb3.setChecked(true);
                    break;
            }

            ENTExam.ENTForkTest forkTest;

            forkTest = m_entExam.getForkAD();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (forkTest) {
                case ENT_FORK_TEST_A_GREATER_B:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_TEST_B_GREATER_A:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_TEST_EQUAL:
                    rb3.setChecked(true);
                    break;
                case ENT_FORK_TEST_NONE:
                    rb4.setChecked(true);
                    break;
            }

            forkTest = m_entExam.getForkAS();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);

            switch (forkTest) {
                case ENT_FORK_TEST_A_GREATER_B:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_TEST_B_GREATER_A:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_TEST_EQUAL:
                    rb3.setChecked(true);
                    break;
                case ENT_FORK_TEST_NONE:
                    rb4.setChecked(true);
                    break;
            }

            ENTExam.ENTBC bc = m_entExam.getBc();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);
            rb4 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);
            rb5 = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);
            rb4.setChecked(false);
            rb5.setChecked(false);

            switch (bc) {
                case ENT_BC_AD_LAT_TO_AD:
                    rb1.setChecked(true);
                    break;
                case ENT_BC_AD_LAT_TO_AS:
                    rb2.setChecked(true);
                    break;
                case ENT_BC_AS_LAT_TO_AD:
                    rb3.setChecked(true);
                    break;
                case ENT_BC_AS_LAT_TO_AS:
                    rb4.setChecked(true);
                    break;
                case ENT_BC_NONE:
                    rb5.setChecked(true);
                    break;
            }

            ENTExam.ENTFork fork;

            fork = m_entExam.getFork();

            rb1 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);
            rb2 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);
            rb3 = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);

            rb1.setChecked(false);
            rb2.setChecked(false);
            rb3.setChecked(false);

            switch (fork) {
                case ENT_FORK_256:
                    rb1.setChecked(true);
                    break;
                case ENT_FORK_512:
                    rb2.setChecked(true);
                    break;
                case ENT_FORK_NONE:
                    rb3.setChecked(true);
                    break;
            }

            String notes = m_entExam.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

            t.setText(notes);
        }

         */
    }

    private void setDirty()
    {
        if (m_ctx.getReadOnly() == true) {
            return;
        }

        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);

        m_dirty = true;
    }

    private void clearDirty() {
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);

        m_dirty = false;
    }

    private void setViewDirtyListeners()
    {
    /*
        CheckBox cb;
        RadioButton rb;

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }


        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }
        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);
        if (cb != null) {
            if (m_ctx.getReadOnly() == true) {
                cb.setEnabled(false);
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);
        if (rb != null) {
            if (m_ctx.getReadOnly() == true) {
                rb.setEnabled(false);
            }
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);
        if (t != null) {
            if (m_ctx.getReadOnly() == true) {
                t.setEnabled(false);
            }
            t.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

         */
    }

    private DentalTreatment copyDentalTreatmentDataFromUI()
    {
        CheckBox cb1, cb2;
        TextView tx;
        RadioButton rb;

        DentalTreatment mh;

        if (m_dentalTreatment == null) {
            mh = new DentalTreatment();
        } else {
            mh = m_dentalTreatment;      // copies over clinic, patient ID, etc..
        }

        mh.setPatient(m_sess.getDisplayPatientId());
        mh.setClinic(m_sess.getClinicId());
        mh.setUsername("nobody");

        /*
        // normal

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_normal_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setNormal(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // microtia

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_microtia_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setMicrotia(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // wax

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_wax_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setWax(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // drainage

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_drainage_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setDrainage(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // otitis

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_otitis_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setExternalOtitis(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // fb

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_ears_fb_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setFb(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // tubes

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_place);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_IN_PLACE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_extruding);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_EXTRUDING);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_in_canal);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_IN_CANAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_left_none);

        if (rb.isChecked()) {
            mh.setTubeLeft(ENTExam.ENTTube.ENT_TUBE_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_place);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_IN_PLACE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_extruding);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_EXTRUDING);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_in_canal);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_IN_CANAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tubes_right_none);

        if (rb.isChecked()) {
            mh.setTubeRight(ENTExam.ENTTube.ENT_TUBE_NONE);
        }

        // tympano

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_anterior);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_posterior);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_25);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_50);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_75);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_total);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_left_none);

        if (rb.isChecked()) {
            mh.setTympanoLeft(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_anterior);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_posterior);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_25);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_50);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_75);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_total);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tympano_right_none);

        if (rb.isChecked()) {
            mh.setTympanoRight(ENTExam.ENTTympano.ENT_TYMPANOSCLEROSIS_NONE);
        }

        // tm

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_granulation_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmGranulations(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_retraction_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmRetraction(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_left);
        cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_ent_tm_atelectasis_right);
        if (cb1.isChecked() && cb2.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_BOTH);
        } else if (cb1.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_LEFT);
        } else if (cb2.isChecked()) {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_RIGHT);
        } else {
            mh.setTmAtelectasis(ENTHistory.EarSide.EAR_SIDE_NONE);
        }

        // perforations

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_anterior);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_posterior);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_marginal);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_MARGINAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_25);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_50);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_75);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_total);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_left_none);

        if (rb.isChecked()) {
            mh.setPerfLeft(ENTExam.ENTPerf.ENT_PERF_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_anterior);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_ANTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_posterior);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_POSTERIOR);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_marginal);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_MARGINAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_25);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_25);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_50);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_50);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_75);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_75);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_total);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_TOTAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_perf_right_none);

        if (rb.isChecked()) {
            mh.setPerfRight(ENTExam.ENTPerf.ENT_PERF_NONE);
        }

        // voice test

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_normal);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_NORMAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_abnormal);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_ABNORMAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_voice_test_none);

        if (rb.isChecked()) {
            mh.setVoiceTest(ENTExam.ENTVoiceTest.ENT_VOICE_TEST_NONE);
        }

        // tuning fork test

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_greater_b);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_A_GREATER_B);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_b_greater_a);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_B_GREATER_A);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_a_equal_b);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_EQUAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_ad_none);

        if (rb.isChecked()) {
            mh.setForkAD(ENTExam.ENTForkTest.ENT_FORK_TEST_NONE);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_greater_b);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_A_GREATER_B);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_b_greater_a);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_B_GREATER_A);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_a_equal_b);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_EQUAL);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_ent_tuning_fork_as_none);

        if (rb.isChecked()) {
            mh.setForkAS(ENTExam.ENTForkTest.ENT_FORK_TEST_NONE);
        }

        // bc

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_ad);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AD_LAT_TO_AD);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_ad_lat_to_as);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AD_LAT_TO_AS);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_ad);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AS_LAT_TO_AD);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_as_lat_to_as);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_AS_LAT_TO_AS);
        }
        rb = (RadioButton) m_view.findViewById(R.id.radio_button_bc_none);

        if (rb.isChecked()) {
            mh.setBc(ENTExam.ENTBC.ENT_BC_NONE);
        }

        // fork

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_256);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_256);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_512);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_512);
        }

        rb = (RadioButton) m_view.findViewById(R.id.radio_button_fork_none);

        if (rb.isChecked()) {
            mh.setFork(ENTExam.ENTFork.ENT_FORK_NONE);
        }

        EditText t = (EditText) m_view.findViewById(R.id.ent_notes);

        Editable text = t.getText();

        mh.setComment(text.toString());

         */

        return mh;
    }

    private boolean validateFields()
    {
        boolean ret = true;
        return ret;
    }

    private void getDentalTreatmentDataFromREST()
    {
        m_sess = SessionSingleton.getInstance();

        m_sess.setNewDentalTreatment(false);
        new Thread(new Runnable() {
            public void run() {
            Thread thread = new Thread(){
                public void run() {
                DentalTreatment exam;
                exam = m_sess.getDentalTreatment(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                if (exam == null) {
                    m_dentalTreatment = new DentalTreatment(); // null ??
                    m_dentalTreatment.setPatient(m_sess.getDisplayPatientId());
                    m_dentalTreatment.setClinic(m_sess.getClinicId());
                    m_dentalTreatment.setUsername("nobody");
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_dental_treatment_data), Toast.LENGTH_SHORT).show();
                            copyDentalTreatmentDataToUI(); // remove if null
                            setViewDirtyListeners();      // remove if null
                        }
                    });

                } else {
                    m_dentalTreatment = exam;
                    m_activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_got_dental_treatment_data), Toast.LENGTH_SHORT).show();
                            copyDentalTreatmentDataToUI();
                            setViewDirtyListeners();

                        }
                    });
                }
                }
            };
            thread.start();
            }
        }).start();
    }

    void updateDentalTreatment()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                DentalTreatmentREST rest = new DentalTreatmentREST(m_sess.getContext());
                Object lock;
                int status;

                if (m_sess.getNewDentalTreatment() == true) {
                    lock = rest.createDentalTreatment(copyDentalTreatmentDataFromUI());
                } else {
                    lock = rest.updateDentalTreatment(copyDentalTreatmentDataFromUI());
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
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_dental_treatment), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            clearDirty();
                            m_dentalTreatment = copyDentalTreatmentDataFromUI();
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_dental_treatment), Toast.LENGTH_LONG).show();
                            m_sess.setNewDentalTreatment(false);
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
        Bundle bundle = this.getArguments();
        try {
            m_dentalTreatment = (DentalTreatment) bundle.getSerializable("treatment");
        } catch (Exception e ) {
            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_dental_treatment_data), Toast.LENGTH_SHORT).show();
        }
        setHasOptionsMenu(false);
        initImageMaps();
    }

    @Override
    public void onResume() {
        super.onResume();
        copyDentalTreatmentDataToUI();
        setViewDirtyListeners();
        if (m_sess.getNewDentalTreatment() == true) {
            setDirty();
        } else {
            clearDirty();
        }
    }

    @Override
    public void onPause() {

        ((StationActivity) m_activity).unsubscribeSave(this);
        ((StationActivity) m_activity).unsubscribeCheckout(this);

        super.onPause();
    }

    private void setButtonBarCallbacks() {
        View button_bar_item;

        button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveInternal(false);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_dental_treatment_layout, container, false);
        m_view  = view;
        View v = view.findViewById(R.id.dental_treatment_toothchart);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
        m_showingToothChart = false;
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View v1, v2;
                FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
                if (m_showingToothChart == false) {
                    v1 = m_view.findViewById(R.id.dental_treatment_scrollview);
                    v2 = m_view.findViewById(R.id.dental_treatment_toothchart);
                    m_detector = new GestureDetectorCompat(getContext(), new AppDentalTreatmentFragment.GestureListener());
                    v2.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return m_detector.onTouchEvent(event);
                        }
                    });
                    m_showingToothChart = true;
                    fab.setImageResource(R.drawable.back_216px_transparent);
                } else {
                    v2 = m_view.findViewById(R.id.dental_treatment_scrollview);
                    v1 = m_view.findViewById(R.id.dental_treatment_toothchart);
                    m_showingToothChart = false;
                    fab.setImageResource(R.drawable.tooth_216px_transparent);
                }

                v1.setVisibility(View.GONE);
                v2.setVisibility(View.VISIBLE);
            }
        });

        m_detector = new GestureDetectorCompat(getContext(), new AppDentalTreatmentFragment.GestureListener());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return m_detector.onTouchEvent(event);
            }
        });

        Button button = m_view.findViewById(R.id.button_per_visit_codes);

        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                CDTCodesListDialogFragment mld = new CDTCodesListDialogFragment();
                mld.setPatientId(m_sess.getDisplayPatientId());
                //mld.subscribe(this);
                mld.isFullMouth(true);
                mld.setToothNumber("");
                mld.show(getFragmentManager(), m_activity.getString(R.string.title_edit_cdt_codes_full_mouth_dialog));
            }
        });

        setButtonBarCallbacks();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}