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

package org.thousandsmiles.thousandsmilesstation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

public class AppRoutingSlipActivity extends AppCompatActivity {

    private SessionSingleton m_sess;
    private ArrayList<RoutingSlipEntry> m_routingSlipEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_sess = SessionSingleton.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_routing_slip);

        if (savedInstanceState == null) {
            showFragment(AppRoutingSlipFragment.newInstance());
        }

        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        m_routingSlipEntries = m_sess.getRoutingSlipEntries(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                    }
                };
                thread.start();
            }
        }).start();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "fragment").commit();
    }
}
