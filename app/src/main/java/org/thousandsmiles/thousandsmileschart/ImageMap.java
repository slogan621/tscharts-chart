/*
 * (C) Copyright Syd Logan 2019-2020
 * (C) Copyright Thousand Smiles Foundation 2019-2020
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

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Iterator;

public class ImageMap {
    public class ImageMapObject {
        Point m_origin;
        Point m_midPoint;
        Point m_fill;           // pre-calculated fill point
        int m_width;
        int m_height;
        Object m_tag = null;

        public void setOrigin(Point val) {
            m_origin = val;
        }

        public Point getOrigin()
        {
            return m_origin;
        }

        public void setMidPoint(Point val) {
            m_midPoint = val;
        }

        public Point getMidPoint()
        {
            return m_midPoint;
        }

        public void setFill(Point val) {
            m_fill = val;
        }

        public Point getFill()
        {
            return m_fill;
        }

        public void setWidth(int val) {
            m_width = val;
        }

        public int getWidth() {
            return m_width;
        }

        public void setHeight(int val) {
            m_height = val;
        }

        public int getHeight() {
            return m_height;
        }

        public void setTag(Object o) {
            m_tag = o;
        }

        public Object getTag() {
            return m_tag;            // for example tooth number
        }

        public boolean hitTest(int x, int y) {
            boolean ret  = false;
            if (x >= m_origin.x && x <= m_origin.x + m_width) {
                if (y >= m_origin.y && y <= m_origin.y + m_height) {
                    ret = true;
                }
            }
            return ret;
        }
    }

    private Bitmap m_bitmap;              // the original bitmap
    private int m_width;                  // its width
    private int m_height;                 // its height

    private int m_coloredPixel;
    private int m_uncoloredPixel;
    private ArrayList<ImageMapObject> m_objectList = new ArrayList<ImageMapObject>();

    public void setBitmap(Bitmap b) {
        m_bitmap = b;
    }

    public Bitmap getBitmap() {
        return m_bitmap;
    }

    /* set the size as it might be shown in GIMP or Photoshop, not as reported by Android */

     public void setWidth(int width) {
        m_width = width;
    }

    public void setHeight(int height) {
        m_height = height;
    }

    public int getWidth() {
        return m_width;
    }

    public int getHeight() {
        return m_height ;
    }

    public void setColoredPixel(int pixel)
    {
        m_coloredPixel = pixel;
    }

    public void setUncoloredPixel(int pixel)
    {
        m_uncoloredPixel = pixel;
    }

    public int getColoredPixel()
    {
        return m_coloredPixel;
    }

    public int getUncoloredPixel()
    {
        return m_uncoloredPixel;
    }

    private ImageMapObject findImageMapObjectAt(int x, int y) {
        ImageMapObject ret = null;
        Iterator i = m_objectList.iterator();

        while (i.hasNext()) {
            ImageMapObject t = (ImageMapObject) i.next();
            if (t.hitTest(x, y)) {
                ret = t;
                break;
            }
        }
        return ret;
    }

    public ImageMapObject hitTest(int x, int y) {
        ImageMapObject ret = null;

        Point retPoint = new Point(x, y);
        ret = findImageMapObjectAt(retPoint.x, retPoint.y);

        return ret;
    }

    public ArrayList<ImageMapObject> getImageMapObjects() {
        return m_objectList;
    }

    public void addImageMapObject(Point origin, int width, int height, Object tag) {
        ImageMapObject m = new ImageMapObject();
        m.setOrigin(origin);
        m.setWidth(width);
        m.setHeight(height);
        m.setMidPoint(new Point((origin.x + width) / 2, (origin.y + height) / 2));
        m.setTag(tag);
        m.setFill(new Point(origin.x + 10, origin.y + 10));
        m_objectList.add(m);
    }

    public void addImageMapObject(Point origin, int width, int height, Object tag, Point fill) {
        ImageMapObject m = new ImageMapObject();
        m.setOrigin(origin);
        m.setWidth(width);
        m.setHeight(height);
        m.setMidPoint(new Point((origin.x + width) / 2, (origin.y + height) / 2));
        m.setTag(tag);
        m.setFill(fill);
        m_objectList.add(m);
    }
}
