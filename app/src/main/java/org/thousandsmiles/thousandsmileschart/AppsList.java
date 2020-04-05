/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AppsList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] appNames;
    private final Integer[] imageIds;
    private final Integer[] selectors;
    public AppsList(Activity context, ArrayList<String> appNames,
                    ArrayList<Integer> imageIds, ArrayList<Integer> selectors) {
        super(context, R.layout.app_list, appNames);
        this.context = context;
        this.appNames = appNames.toArray(new String[appNames.size()]);
        this.imageIds = imageIds.toArray(new Integer[appNames.size()]);
        this.selectors = selectors.toArray(new Integer[appNames.size()]);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.app_list, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(appNames[position]);

        imageView.setImageDrawable(context.getResources().getDrawable(selectors[position]));

        return rowView;
    }
}