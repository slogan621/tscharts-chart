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
 *
 *  Code derived from https://stackoverflow.com/questions/16968412/how-to-use-flood-fill-algorithm-in-android
 */

package org.thousandsmiles.thousandsmileschart;

import android.graphics.Bitmap;
import android.graphics.Color;

public class HistogramEqualize {

    private Bitmap m_in = null;
    private final int m_levels = 256;
    private Bitmap m_equalized = null;

    public HistogramEqualize(Bitmap in) {
        m_in = in;
    }

    public Bitmap equalize() {
        int width = m_in.getWidth();
        int height = m_in.getHeight();
        double scale = 1.0 / (width * height);
        double [] hist = new double[m_levels];
        double [] LUT = new double[m_levels];

        for (int i = 0; i < hist.length; i++) {
            hist[i] = 0;
            LUT[i] = 0;
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = m_in.getPixel(i, j) & 0xff;
                hist[pixel] = hist[pixel] + scale;
            }
        }

        double sum = 0.0;
        for (int i = 0; i < m_levels; i++) {
            sum = sum + hist[i];
            LUT[i] = sum * (m_levels - 1);
        }

        m_equalized = Bitmap.createBitmap(width, height, m_in.getConfig());

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = (int) LUT[m_in.getPixel(i, j) & 0xff];
                pixel = Color.argb(0xff, pixel, pixel, pixel);
                m_equalized.setPixel(i, j, pixel);
            }
        }
        return m_equalized;
    }

    public Bitmap undo() {
        return m_in;
    }
}
