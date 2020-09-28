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

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 10;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(position, String.valueOf(1453), "02-16 " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static Drawable makeImageDetails(Context context, int position) {
        Drawable bitmap;
        if (position == 1) {
            bitmap = context.getResources().getDrawable(R.drawable.image1);
        } else if (position == 2) {
            bitmap = context.getResources().getDrawable(R.drawable.image2);
        } else if (position == 3) {
            bitmap = context.getResources().getDrawable(R.drawable.image3);
        } else {
            bitmap = context.getResources().getDrawable(R.drawable.image10);
        }

        return bitmap;
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final int index;
        public final String content;
        public final String details;
        public Drawable imageDetails;

        public void setImageDetails(Context context, int position) {
            Drawable bitmap;
            if (position == 0) {
                bitmap = context.getResources().getDrawable(R.drawable.image1);
            } else if (position == 1) {
                bitmap = context.getResources().getDrawable(R.drawable.image2);
            } else if (position == 2) {
                bitmap = context.getResources().getDrawable(R.drawable.image3);
            } else {
                bitmap = context.getResources().getDrawable(R.drawable.image10);
            }
            imageDetails = bitmap;
        }

        public DummyItem(int index, String id, String content, String details) {
            this.index = index;
            this.id = id;
            this.content = content;
            this.details = details;
            this.imageDetails = null;
        }
        @Override
        public String toString() {
            return content;
        }
    }
}
