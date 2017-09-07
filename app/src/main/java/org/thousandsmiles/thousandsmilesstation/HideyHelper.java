package org.thousandsmiles.thousandsmilesstation;

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

import android.app.Activity;
import android.os.Handler;
import android.view.View;

/**
 * Created by slogan on 10/5/15.
 */


public class HideyHelper {
    public void toggleHideyBar(Activity activity) {
        final Handler handler = new Handler();
        final Activity myActivity = activity;

        handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               myActivity.getWindow().getDecorView().setSystemUiVisibility(
                       View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                               | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                               | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                               | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                               | View.SYSTEM_UI_FLAG_FULLSCREEN
                               | View.SYSTEM_UI_FLAG_IMMERSIVE);
           }
        }, 500);


        //activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

}
