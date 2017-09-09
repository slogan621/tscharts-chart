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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StationActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private enum StationState {
        ACTIVE,
        WAITING,
        AWAY,
    }

    private boolean m_twoPane;
    private StationState m_state = StationState.WAITING; // the status of this station
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    AsyncTask m_task = null;
    private PatientItemRecyclerViewAdapter m_waitingAdapter = null;
    private PatientItemRecyclerViewAdapter m_activeAdapter = null;
    private AppListItems m_appListItems = new AppListItems();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
    }

    private class UpdatePatientLists extends AsyncTask<Object, Object, Object> {
        @Override
        protected String doInBackground(Object... params) {
            boolean first = true;

            m_sess.updateStationData(); // get the list of stations

            while (true) {
                m_sess.updateClinicStationData();
                m_sess.updateQueues();
                m_sess.updateActivePatientList();
                m_sess.updateWaitingPatientList();
                if (first) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            setupRecyclerViews();
                            createAppList();
                        }
                    });
                    first = false;
                }
                StationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        setWaitingPatientListData();
                        setActivePatientListData();
                    }
                });
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e) {
                }
            }
            //return "";
        }

        private void   setWaitingPatientListData()
        {
            List<PatientItem> items;

            items = m_sess.getWaitingPatientListData();
            m_waitingAdapter.swap(items);
        }
        private void setActivePatientListData()
        {
            List<PatientItem> items;

            items = m_sess.getActivePatientListData();
            m_activeAdapter.swap(items);
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (m_task == null) {
            m_task = new UpdatePatientLists();
            m_task.execute((Object) null);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */


        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            m_twoPane = true;
        }
    }

    private void setupRecyclerViews() {
        View recycler = findViewById(R.id.waiting_item_list);
        assert recycler != null;
        setupWaitingRecyclerView((RecyclerView) recycler);

        recycler = findViewById(R.id.active_item_list);
        assert recycler != null;
        setupActiveRecyclerView((RecyclerView) recycler);
    }

    @Override
    public void onBackPressed() {
        if(m_task!=null){
            m_task.cancel(true);
            m_task = null;
        }
        super.onBackPressed();
    }

    private void setupWaitingRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter((m_waitingAdapter = new PatientItemRecyclerViewAdapter(true)));
    }

    private void setupActiveRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter((m_activeAdapter = new PatientItemRecyclerViewAdapter(false)));
    }

    public class PatientItemRecyclerViewAdapter
            extends RecyclerView.Adapter<PatientItemRecyclerViewAdapter.ViewHolder> {

        private List<PatientItem> mValues = new ArrayList<PatientItem>();

        private boolean m_isWaiting;

        public PatientItemRecyclerViewAdapter(boolean isWaiting) {
            m_isWaiting = isWaiting;
        }

        public void swap(List<PatientItem> items)
        {
            if (items != null) {
                mValues.clear();
                mValues.addAll(items);
                if (m_isWaiting) {
                    WaitingPatientList.clearItems();
                    for (int i = 0; i < items.size(); i++) {
                        WaitingPatientList.addItem(mValues.get(i));
                    }
                } else {
                    ActivePatientList.clearItems();
                    for (int i = 0; i < items.size(); i++) {
                        ActivePatientList.addItem(mValues.get(i));
                    }
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_twoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        arguments.putBoolean("isWaiting", m_isWaiting);
                        ItemDetailFragment fragment = new ItemDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public PatientItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    private void createAppList() {

        String station = m_sess.getActiveStationName();

        AppListItems appListItems = new AppListItems();

        final ArrayList<String> names = appListItems.getNames(station);
        final ArrayList<Integer> imageIds = appListItems.getImageIds(station);
        final ArrayList<Integer> selectors = appListItems.getSelectors(station);

        AppsList adapter = new AppsList(StationActivity.this, names, imageIds, selectors);

        ListView list;

        list = (ListView) findViewById(R.id.app_item_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(StationActivity.this, "You Clicked on " + names.get(+position), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
