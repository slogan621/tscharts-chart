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
 *
 *  Code derived from https://stackoverflow.com/questions/16968412/how-to-use-flood-fill-algorithm-in-android
 */

package org.thousandsmiles.thousandsmilesstation;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class FloodFill {

    public interface FloodFillCompletionListener {
        public void onFillDone(Bitmap bm);
    }

    public class FloodFillTask extends AsyncTask<Object, Integer, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected Void doInBackground(Object... params) {
            if (params.length > 0) {
                FloodFill floodFill = (FloodFill) params[0];
                floodFill.fill();                              // do the fill
                floodFill.notifyListeners();                   // call listeners, passing them the resulting bitmap
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    private int m_replacementColor;
    private int m_targetColor;
    private Bitmap m_bitmap;
    private Point m_point;
    private int m_drawableWidth;
    private int m_drawableHeight;
    private ArrayList<FloodFillCompletionListener> m_listeners = new ArrayList<FloodFillCompletionListener>();

    public void registerListener(FloodFillCompletionListener o) {
        m_listeners.add(o);
    }

    private void notifyListeners() {
        Iterator it;

        it = m_listeners.iterator();
        while (it.hasNext()) {
            FloodFillCompletionListener o = (FloodFillCompletionListener) it.next();
            o.onFillDone(m_bitmap);
        }
    }

    public void setPoint(Point val) {
        m_point = val;
    }

    public Point getPoint() {
        return m_point;
    }

    public void setBitmap(Bitmap val) {
        m_bitmap = val.copy( Bitmap.Config.ARGB_8888 , true);
    }

    public Bitmap getBitmap() {
        return m_bitmap;
    }

    public void setReplacementColor(int val) {
        m_replacementColor = val;
    }

    public int getReplacementColor() {
        return m_replacementColor;
    }

    public void setTargetColor(int val) {
        m_targetColor = val;
    }

    public int getTargetColor() {
        return m_targetColor;
    }

    public void setDrawableWidth(int width) {
        m_drawableWidth = width;
    }

    public void setDrawableHeight(int height) {
        m_drawableHeight = height;
    }

    public void fill() {
        int width = m_bitmap.getWidth();
        int height = m_bitmap.getHeight();
        int density = m_bitmap.getDensity();
        int target = m_targetColor;
        int replacement = m_replacementColor;
        Point node = m_point;

        //node.x = (int) (node.x * (double) width / m_drawableWidth);
        //node.y = (int) (node.y * (double) height / m_drawableHeight);

        if (target != replacement) {
            Queue<Point> queue = new LinkedList<Point>();
            do {
                int x = node.x;
                int y = node.y;
                while (x > 0 && m_bitmap.getPixel(x - 1, y) == target) {
                    x--;
                }

                boolean spanUp = false;
                boolean spanDown = false;
                while (x < width) {
                  int p = m_bitmap.getPixel(x, y);
                  if (p != target) {
                      break;
                  }
                  else {
                      m_bitmap.setPixel(x, y, replacement);

                      if (!spanUp && y > 0 && m_bitmap.getPixel(x, y - 1) == target) {
                          queue.add(new Point(x, y - 1));
                          spanUp = true;
                      } else if (spanUp && y > 0 && m_bitmap.getPixel(x, y - 1) != target) {
                          spanUp = false;
                      }
                      if (!spanDown && y < height - 1 && m_bitmap.getPixel(x, y + 1) == target) {
                          queue.add(new Point(x, y + 1));
                          spanDown = true;
                      } else if (spanDown && y < (height - 1) && m_bitmap.getPixel(x, y + 1) != target) {
                          spanDown = false;
                      }
                      x++;
                  }
                }
            } while ((node = queue.poll()) != null);
        }
    }
}
