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
import java.lang.Math;

// implement the matlab 'jet' colormap algorithm

public class FalseColor {

    private Bitmap m_in = null;
    private final int m_levels = 256;
    private Bitmap m_colorized = null;

    public FalseColor(Bitmap in) {
        m_in = in;
    }

    double interpolate( double val, double y0, double x0, double y1, double x1 ) {
        return (val-x0)*(y1-y0)/(x1-x0) + y0;
    }

    double base( double val ) {
        if ( val <= -0.75 ) return 0;
        else if ( val <= -0.25 ) return interpolate( val, 0.0, -0.75, 1.0, -0.25 );
        else if ( val <= 0.25 ) return 1.0;
        else if ( val <= 0.75 ) return interpolate( val, 1.0, 0.25, 0.0, 0.75 );
        else return 0.0;
    }

    double red( double gray ) {
        return base( gray - 0.5 );
    }

    double green( double gray ) {
        return base( gray );
    }

    double blue( double gray ) {
        return base( gray + 0.5 );
    }

    public Bitmap process() {
        int width = m_in.getWidth();
        int height = m_in.getHeight();

        m_colorized = Bitmap.createBitmap(width, height, m_in.getConfig());

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                final int clr = m_in.getPixel(i, j);
                final int red = (int) Math.ceil((255.0 * red((clr & 0x00ff0000 >> 16)/255.0)));
                final int green = (int)  Math.ceil((255.0 * green((clr & 0x0000ff00 >> 8)/255.0)));
                final int blue = (int) Math.ceil(255.0 * blue((clr & 0x000000ff)/255.0));
                int pixel = Color.argb(0xff, red, green, blue);
                m_colorized.setPixel(i, j, pixel);
            }
        }
        return m_colorized;
    }

    public Bitmap undo() {
        return m_in;
    }
}
