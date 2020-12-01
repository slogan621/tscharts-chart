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
        CheckBox cb1, cb2, cb3, cb4;
        TextView tx;
        EditText editTx;
        RadioButton rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8;

        if (m_dentalTreatment != null) {

            // exam

            Boolean bVal = m_dentalTreatment.getExam();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_exam);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_exam_notes);
            editTx.setText(m_dentalTreatment.getExamComment());

            // prophy

            bVal = m_dentalTreatment.getProphy();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_prophy);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_prophy_notes);
            editTx.setText(m_dentalTreatment.getProphyComment());

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ur);
            cb2 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_lr);
            cb3 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ul);
            cb4 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ll);

            cb1.setChecked(m_dentalTreatment.getSrpUR());
            cb2.setChecked(m_dentalTreatment.getSrpLR());
            cb3.setChecked(m_dentalTreatment.getSrpUL());
            cb4.setChecked(m_dentalTreatment.getSrpLL());

            editTx = (EditText) m_view.findViewById(R.id.dental_srp_notes);
            editTx.setText(m_dentalTreatment.getSrpComment());

            bVal = m_dentalTreatment.getXraysViewed();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_xrays_viewed);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_xrays_notes);
            editTx.setText(m_dentalTreatment.getXraysViewedComment());

            bVal = m_dentalTreatment.getHeadNeckOralCancerExam();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_head_and_neck_oral_cancer_exam);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_head_and_neck_oral_cancer_exam_notes);
            editTx.setText(m_dentalTreatment.getHeadNeckOralCancerExamComment());

            bVal = m_dentalTreatment.getOralHygieneInstruction();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_oral_hygiene_instruction);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_oral_hygiene_instruction_notes);
            editTx.setText(m_dentalTreatment.getOralHygieneInstructionComment());

            bVal = m_dentalTreatment.getFlourideTxVarnish();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_flouride_tx_varnish);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_flouride_tx_varnish_notes);
            editTx.setText(m_dentalTreatment.getFlourideTxVarnishComment());

            bVal = m_dentalTreatment.getNutritionalCounseling();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_dental_nutritional_counseling);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_nutritional_counseling_notes);
            editTx.setText(m_dentalTreatment.getNutritionalCounselingComment());

            bVal = m_dentalTreatment.getOrthoEvaluation();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_evaluation);
            cb1.setChecked(bVal);

            bVal = m_dentalTreatment.getOrthoTx();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_tx);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_orthodontic_notes);
            editTx.setText(m_dentalTreatment.getOrthoEvaluationComment());  // tx notes unused

            // oral surgery

            bVal = m_dentalTreatment.getOralSurgeryEvaluation();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_evaluation);
            cb1.setChecked(bVal);

            bVal = m_dentalTreatment.getOralSurgeryTx();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_tx);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_oral_surgery_notes);
            editTx.setText(m_dentalTreatment.getOralSurgeryEvaluationComment());  // tx notes unused

            bVal = m_dentalTreatment.getLocalAnestheticBenzocaine();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_benzocaine);
            cb1.setChecked(bVal);

            bVal = m_dentalTreatment.getLocalAnestheticLidocaine();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_lidocaine);
            cb1.setChecked(bVal);

            bVal = m_dentalTreatment.getLocalAnestheticSeptocaine();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_septocaine);
            cb1.setChecked(bVal);

            bVal = m_dentalTreatment.getLocalAnestheticOther();

            cb1 = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_other);
            cb1.setChecked(bVal);

            editTx = (EditText) m_view.findViewById(R.id.dental_anesthetic_notes);
            editTx.setText(m_dentalTreatment.getLocalAnestheticComment());

            int numberOfCarps = m_dentalTreatment.getLocalAnestheticNumberCarps();
            editTx = (EditText) m_view.findViewById(R.id.text_number_of_carps);
            String numCarps = String.format("%d", numberOfCarps);
            editTx.setText(numCarps);

            String notes = m_dentalTreatment.getComment();

            EditText t = (EditText) m_view.findViewById(R.id.dental_notes);

            t.setText(notes);
        }
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
        CheckBox cb;

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_exam);
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

        EditText t = (EditText) m_view.findViewById(R.id.dental_exam_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_prophy);
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

        t = (EditText) m_view.findViewById(R.id.dental_prophy_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_lr);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ll);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ur);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ul);
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

        t = (EditText) m_view.findViewById(R.id.dental_srp_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_xrays_viewed);
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

        t = (EditText) m_view.findViewById(R.id.dental_xrays_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_head_and_neck_oral_cancer_exam);
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

        t = (EditText) m_view.findViewById(R.id.dental_head_and_neck_oral_cancer_exam_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_oral_hygiene_instruction);
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

        t = (EditText) m_view.findViewById(R.id.dental_oral_hygiene_instruction_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_flouride_tx_varnish);
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

        t = (EditText) m_view.findViewById(R.id.dental_flouride_tx_varnish_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_nutritional_counseling);
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

        t = (EditText) m_view.findViewById(R.id.dental_nutritional_counseling_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_evaluation);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_tx);
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

        t = (EditText) m_view.findViewById(R.id.dental_orthodontic_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_evaluation);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_tx);
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

        t = (EditText) m_view.findViewById(R.id.dental_oral_surgery_notes);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_benzocaine);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_lidocaine);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_septocaine);
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

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_other);
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

        t = (EditText) m_view.findViewById(R.id.text_number_of_carps);
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

        t = (EditText) m_view.findViewById(R.id.dental_anesthetic_notes);
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

        ////

        t = (EditText) m_view.findViewById(R.id.dental_notes);
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
    }

    private DentalTreatment copyDentalTreatmentDataFromUI()
    {
        CheckBox cb;
        TextView tx;

        DentalTreatment dt;

        if (m_dentalTreatment == null) {
            dt = new DentalTreatment();
        } else {
            dt = m_dentalTreatment;      // copies over clinic, patient ID, etc..
        }

        dt.setPatient(m_sess.getDisplayPatientId());
        dt.setClinic(m_sess.getClinicId());
        dt.setUsername("nobody");

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_exam);
        if (cb != null) {
            dt.setExam(cb.isChecked());
        }

        EditText t = (EditText) m_view.findViewById(R.id.dental_exam_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setExamComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_prophy);
        if (cb != null) {
            dt.setProphy(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_prophy_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setProphyComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_lr);
        if (cb != null) {
            dt.setSrpLR(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ll);
        if (cb != null) {
            dt.setSrpLL(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ur);
        if (cb != null) {
            dt.setSrpUR(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_srp_ul);
        if (cb != null) {
            dt.setSrpUL(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_srp_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setSrpComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_xrays_viewed);
        if (cb != null) {
            dt.setXraysViewed(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_xrays_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setXraysViewedComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_head_and_neck_oral_cancer_exam);
        if (cb != null) {
            dt.setHeadNeckOralCancerExam(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_head_and_neck_oral_cancer_exam_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setHeadNeckOralCancerExamComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_oral_hygiene_instruction);
        if (cb != null) {
            dt.setOralHygieneInstruction(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_oral_hygiene_instruction_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setOralHygieneInstructionComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_flouride_tx_varnish);
        if (cb != null) {
            dt.setFlourideTxVarnish(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_flouride_tx_varnish_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setFlourideTxVarnishComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_dental_nutritional_counseling);
        if (cb != null) {
            dt.setNutritionalCounseling(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_nutritional_counseling_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setNutritionalCounselingComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_evaluation);
        if (cb != null) {
            dt.setOrthoEvaluation(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_orthodontic_tx);
        if (cb != null) {
            dt.setOrthoTx(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_orthodontic_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setOrthoEvaluationComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_evaluation);
        if (cb != null) {
            dt.setOralSurgeryEvaluation(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_oral_surgery_tx);
        if (cb != null) {
            dt.setOralSurgeryTx(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.dental_oral_surgery_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setOralSurgeryEvaluationComment(text.toString());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_benzocaine);
        if (cb != null) {
            dt.setLocalAnestheticBenzocaine(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_lidocaine);
        if (cb != null) {
            dt.setLocalAnestheticLidocaine(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_septocaine);
        if (cb != null) {
            dt.setLocalAnestheticSeptocaine(cb.isChecked());
        }

        cb = (CheckBox) m_view.findViewById(R.id.checkbox_local_anesthetic_other);
        if (cb != null) {
            dt.setLocalAnestheticOther(cb.isChecked());
        }

        t = (EditText) m_view.findViewById(R.id.text_number_of_carps);
        if (t != null) {
            Editable text = t.getText();
            dt.setLocalAnestheticNumberCarps(Integer.parseInt(text.toString()));
        }

        t = (EditText) m_view.findViewById(R.id.dental_anesthetic_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setLocalAnestheticComment(text.toString());
        }

        ////

        t = (EditText) m_view.findViewById(R.id.dental_notes);
        if (t != null) {
            Editable text = t.getText();
            dt.setComment(text.toString());
        }

        return dt;
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
