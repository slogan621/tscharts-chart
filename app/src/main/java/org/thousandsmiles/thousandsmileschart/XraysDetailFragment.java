package org.thousandsmiles.thousandsmileschart;

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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

import static java.lang.Math.abs;

public class XraysDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    ArrayList<XRayImage> m_images = null;

    private View m_rootView;
    private int m_index;
    boolean m_swiped;
    boolean m_swipeBack;
    private GestureDetectorCompat m_detector;
    SessionSingleton m_sess = SessionSingleton.getInstance();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public XraysDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_images = (ArrayList<XRayImage>) getArguments().getSerializable("xrays");
    }

    public void doHistogramEqualization() {
        ImageView mImageView;

        mImageView = (ImageView) m_rootView.findViewById(R.id.xrays_image_detail);
        // http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap

        Bitmap bm = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        HistogramEqualize hist = new HistogramEqualize(bm);
        Bitmap newBm = hist.equalize();
        Drawable d = new BitmapDrawable(getResources(), newBm);
        mImageView.setImageDrawable(d);
    }

    public void doFalseColor() {
        ImageView mImageView;

        mImageView = (ImageView) m_rootView.findViewById(R.id.xrays_image_detail);
        // http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap

        Bitmap bm = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        FalseColor hist = new FalseColor(bm);
        Bitmap newBm = hist.process();
        Drawable d = new BitmapDrawable(getResources(), newBm);
        mImageView.setImageDrawable(d);
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doHistogramEqualization();
            }
        });

        FloatingActionButton colorize = (FloatingActionButton) getActivity().findViewById(R.id.colorize);
        colorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFalseColor();
            }
        });
    }

    // XXX following currently unused, flings are communicated with ImageViewer class

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            //Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            /*
             * XXX in some cases, velocityX is neg and in some, pos, could be due to
             * tablet orientation. Needs to be investigated. But for now, any swipe that
             * is along the x axis will trigger the swipe
             */
            if (abs((int)velocityX) > abs((int)velocityY)) {
                m_swiped = true;
                if (velocityX < 0) {
                    m_swipeBack = false;
                    if (m_index == m_images.size() - 1) {
                        m_index = 0;
                    } else {
                        m_index += 1;
                    }
                } else {
                    m_swipeBack = true;
                    if (m_index == 0) {
                        m_index = m_images.size() - 1;
                    } else {
                        m_index -= 1;
                    }
                }
                displayImage();
            }
            return true;
        }
    }

    private class SingleFlingListener implements PhotoViewAttacher.OnSingleFlingListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (abs((int)velocityX) > abs((int)velocityY)) {
                m_swiped = true;
                if (velocityX < 0) {
                    m_swipeBack = false;
                    if (m_index == m_images.size() - 1) {
                        m_index = 0;
                    } else {
                        m_index += 1;
                    }
                } else {
                    m_swipeBack = true;
                    if (m_index == 0) {
                        m_index = m_images.size() - 1;
                    } else {
                        m_index -= 1;
                    }
                }
                displayImage();
            }
            return true;
        }
    }

    private void displayImage() {
        ImageView imageView;
        TextView textView = null;
        PhotoViewAttacher attacher;
        XRayImage image = m_images.get(m_index);

        imageView = (ImageView) m_rootView.findViewById(R.id.xrays_image_detail);

        imageView.setImageBitmap(image.getBitmap());

        textView = (TextView) m_rootView.findViewById(R.id.xrays_detail);
        if (textView != null) {
            try {
                String start = m_sess.getCommonSessionSingleton().getClinicById(image.getClinic()).getString("start");
                String end = m_sess.getCommonSessionSingleton().getClinicById(image.getClinic()).getString("end");
                String val;
                if (start.equals(end)) {
                    val = start;
                } else {
                    val = String.format("%s - %s", start, end);
                }
                textView.setText(String.format("Clinic Date %s C%03d-P%05d-%06d", val, image.getClinic(), image.getPatient(), image.getId()));
            } catch (Exception e) {
                textView.setText(String.format("C%03d-P%05d-%06d", image.getClinic(), image.getPatient(), image.getId()));
            }
        }

        attacher = new PhotoViewAttacher(imageView);
        attacher.setOnSingleFlingListener(new SingleFlingListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.xrays_image_detail, container, false);
        m_rootView = rootView;
        m_index = 0;

        displayImage();
        return rootView;
    }
}
