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

/*
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmilesstation;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.woxthebox.draglistview.BoardView;
import com.woxthebox.draglistview.DragItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppRoutingSlipFragment extends Fragment {
    private BoardView mBoardView;
    private int mColumns;
    private boolean m_dirty = false;
    private boolean m_goingDown = false;
    private SessionSingleton m_sess;
    private ArrayList<RoutingSlipEntry> m_routingSlipEntries;
    private ArrayList<Station> m_stations;
    private ArrayList<RoutingSlipEntry> m_available;
    private ArrayList<RoutingSlipEntry> m_current;
    private int m_routingSlipId;

    private boolean stationInRoutingSlipList(Station s)
    {
        boolean ret = false;
        for (int i = 0; i < m_routingSlipEntries.size(); i++) {
            if (s.getStation() == m_routingSlipEntries.get(i).getStation()) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void createAvailableList()
    {
        m_available = new ArrayList<RoutingSlipEntry>();
        for (int i = 0; i < m_stations.size(); i++) {
            Station p = m_stations.get(i);
            if (stationInRoutingSlipList(p) == false && p.getStation() != m_sess.getStationStationId()) {
                RoutingSlipEntry q = new RoutingSlipEntry();
                q.setName(p.getName());
                q.setSelector(p.getSelector());
                q.setStation(p.getStation());
                q.setVisited(false);
                m_available.add(q);
            }
        }
    }

    private void createCurrentList()
    {
        m_current = new ArrayList<RoutingSlipEntry>();
        for (int i = 0; i < m_routingSlipEntries.size(); i++) {
            RoutingSlipEntry p = m_routingSlipEntries.get(i);
            if (p.getVisited() == false) {
                RoutingSlipEntry q = new RoutingSlipEntry(p);
                m_current.add(q);
            }
        }
    }

    public static AppRoutingSlipFragment newInstance()
    {
        return new AppRoutingSlipFragment();
    }

    private RoutingSlipEntry removeFromCurrentList(int fromRow)
    {
        RoutingSlipEntry ref = null;

        if (fromRow < m_current.size()) {
            ref = m_current.get(fromRow);
            if (ref != null) {
                m_current.remove(ref);
            }
            return ref;
        }
        return ref;
    }

    private boolean addToAvailableList(RoutingSlipEntry ref)
    {
        boolean ret = true;

        m_available.add(ref);
        return ret;
    }

    private RoutingSlipEntry removeFromAvailableList(int fromRow)
    {
        RoutingSlipEntry ref = null;

        if (fromRow < m_available.size()) {
            ref = m_available.get(fromRow);
            if (ref != null) {
                m_available.remove(ref);
            }
        }
        return ref;
    }

    private boolean addToCurrentList(RoutingSlipEntry ref)
    {
        boolean ret = true;

        m_current.add(ref);
        return ret;
    }

    private void createColumns() {
        m_dirty = false;
        mBoardView.clearBoard();
        createAvailableList();
        createCurrentList();
        addColumnList(1);   // stations not yet visited and not in routing slip
        addColumnList(2);   // stations not yet visited
    }

    private void initializeRoutingSlipData() {
        if (m_current != null) {
            m_current.clear();
        }
        if (m_available != null) {
            m_available.clear();
        }
        m_sess = SessionSingleton.getInstance();
        m_stations = m_sess.getStationList();

        new Thread(new Runnable() {
            public void run() {
                Thread thread = new Thread(){
                    public void run() {
                        m_routingSlipEntries = m_sess.getRoutingSlipEntries(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                        if (m_routingSlipEntries == null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), "Unable to get routing slip data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    JSONObject o = m_sess.getDisplayPatientRoutingSlip();
                                    try {
                                        m_routingSlipId = o.getInt("id");
                                    } catch (JSONException e) {
                                    }
                                    createColumns();
                                    Toast.makeText(getActivity(), "Successfully got routing slip data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                };
                thread.start();
            }
        }).start();
    }

    private boolean isInCurrent(RoutingSlipEntry ent) {
        boolean ret = false;

        for (int i = 0; i < m_current.size(); i++) {
            if (ent.getStation() == m_current.get(i).getStation()) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private boolean isInRoutingSlipEntries(RoutingSlipEntry ent) {
        boolean ret = false;

        for (int i = 0; i < m_routingSlipEntries.size(); i++) {
            if (ent.getStation() == m_routingSlipEntries.get(i).getStation()) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    boolean updateRoutingSlip()
    {
        boolean ret = false;
        final ArrayList <RoutingSlipEntry> itemsToAdd = new ArrayList<RoutingSlipEntry>();
        final ArrayList <RoutingSlipEntry> itemsToRemove = new ArrayList<RoutingSlipEntry>();

        // if item is in current but not the initial entries list, add.

        for (int i = 0; i < m_current.size(); i++) {
            RoutingSlipEntry ent = m_current.get(i);
            if (isInRoutingSlipEntries(ent) == false) {
                itemsToAdd.add(ent);
            }
        }

        // if item is not in current but was in the initial entries list, remove.

        for (int i = 0; i < m_routingSlipEntries.size(); i++) {
            RoutingSlipEntry ent = m_routingSlipEntries.get(i);
            if (isInCurrent(ent) == false) {
                itemsToRemove.add(ent);
            }
        }

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                RoutingSlipEntryREST rest = new RoutingSlipEntryREST(m_sess.getContext());
                Object lock;
                int status;

                for (int i = 0; i < itemsToAdd.size(); i++) {
                    lock = rest.createRoutingSlipEntry(m_routingSlipId, itemsToAdd.get(i).getStation());

                    synchronized (lock) {
                        // we loop here in case of race conditions or spurious interrupts
                        while (true) {
                            try {
                                lock.wait();
                                break;
                            } catch (InterruptedException e) {
                                continue;
                            }
                        }
                    }
                    status = rest.getStatus();
                    if (status != 200) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_sess.getContext(), "Unable to add routing slip entry", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                for (int i = 0; i < itemsToRemove.size(); i++) {
                    lock = rest.deleteRoutingSlipEntry(itemsToRemove.get(i).getId());
                    synchronized (lock) {
                        // we loop here in case of race conditions or spurious interrupts
                        while (true) {
                            try {
                                lock.wait();
                                break;
                            } catch (InterruptedException e) {
                                continue;
                            }
                        }
                    }
                    status = rest.getStatus();
                    if (status != 200) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_sess.getContext(), "Unable to remove routing slip entry", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                if (m_goingDown == false) {
                    m_routingSlipEntries = m_sess.getRoutingSlipEntries(m_sess.getClinicId(), m_sess.getDisplayPatientId());
                    if (m_routingSlipEntries == null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_sess.getContext(), "Unable to get routing slip data", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(m_sess.getContext(), "Successfully got routing slip data", Toast.LENGTH_LONG).show();
                            }
                        });

                        initializeRoutingSlipData();
                    }
                }
            }
        };
        thread.start();
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }

        super.onPause();

        if (m_dirty) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Unsaved Changes to Routing Slip");
            builder.setMessage("Save routing slip changes?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    m_goingDown = true;
                    updateRoutingSlip();
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_routing_slip_board_layout, container, false);

        mBoardView = (BoardView) view.findViewById(R.id.board_view);
        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
        mBoardView.setCustomDragItem(new MyDragItem(getActivity(), R.layout.app_routing_slip_column_item));
        mBoardView.setSnapToColumnInLandscape(false);
        mBoardView.setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER);
        mBoardView.setBoardListener(new BoardView.BoardListener() {
            @Override
            public void onItemDragStarted(int column, int row) {
                //Toast.makeText(mBoardView.getContext(), "Start - column: " + column + " row: " + row, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                //Toast.makeText(mBoardView.getContext(), "Position changed - column: " + newColumn + " row: " + newRow, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {

                /*
                TextView itemCount1 = (TextView) mBoardView.getHeaderView(oldColumn).findViewById(R.id.item_count);
                itemCount1.setText(String.valueOf(mBoardView.getAdapter(oldColumn).getItemCount()));
                TextView itemCount2 = (TextView) mBoardView.getHeaderView(newColumn).findViewById(R.id.item_count);
                itemCount2.setText(String.valueOf(mBoardView.getAdapter(newColumn).getItemCount()));
                */
            }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                m_dirty = true;
                View button_bar_item = getActivity().findViewById(R.id.save_button);
                button_bar_item.setVisibility(View.VISIBLE);
                button_bar_item.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        updateRoutingSlip();
                    }

                });
                if (fromColumn != toColumn || fromRow != toRow) {
                    RoutingSlipEntry ref;
                    if (toColumn < fromColumn) {
                        // remove
                        ref = removeFromCurrentList(fromRow);
                        addToAvailableList(ref);
                    } else {
                        // add
                        ref = removeFromAvailableList(fromRow);
                        addToCurrentList(ref);
                    }
                    //Toast.makeText(mBoardView.getContext(), "End - column: " + toColumn + " row: " + toRow, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mBoardView.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) {
                // Add logic here to prevent an item to be dragged
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                // Add logic here to prevent an item to be dropped
                boolean ret = true;

                if (oldColumn == newColumn) {
                    ret = false;
                }
                return ret;
            }
        });

        initializeRoutingSlipData();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(String.format("Routing Slip For Patient %d", m_sess.getActivePatientId()));
    }

    private void addColumnList(int which) {
        final ArrayList<Pair<Long, String>> mItemArray = new ArrayList<>();

        if (which == 1) {
            for (int i = 0; i < m_available.size(); i++) {
                //long id = sCreatedItems++;
                mItemArray.add(new Pair<>((long)m_available.get(i).getSelector(), m_available.get(i).getName()));
            }
        } else {
            for (int i = 0; i < m_current.size(); i++) {
                //long id = sCreatedItems++;
                mItemArray.add(new Pair<>((long)m_current.get(i).getSelector(), m_current.get(i).getName()));
            }
        }

        final int column = mColumns;
        final AppRoutingSlipItemAdapter listAdapter = new AppRoutingSlipItemAdapter(mItemArray, R.layout.app_routing_slip_column_item, R.id.item_layout, true);
        final View header = View.inflate(getActivity(), R.layout.app_routing_slip_column_header, null);
        if (which == 1) {
            ((TextView) header.findViewById(R.id.text)).setText("Available Stations");
        } else {
            ((TextView) header.findViewById(R.id.text)).setText("Current Routing Slip");
        }
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //long id = sCreatedItems++;
                //Pair item = new Pair<>(id, "Test " + id);
                //mBoardView.addItem(column, 0, item, true);
                //mBoardView.moveItem(4, 0, 0, true);
                //mBoardView.removeItem(column, 0);
                //mBoardView.moveItem(0, 0, 1, 3, false);
                //mBoardView.replaceItem(0, 0, item1, true);
                //((TextView) header.findViewById(R.id.item_count)).setText(String.valueOf(mItemArray.size()));
            }
        });

        mBoardView.addColumnList(listAdapter, header, false);
        mColumns++;
    }

    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            Drawable imageResource = ((ImageView) clickedView.findViewById(R.id.image)).getDrawable();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            ((ImageView) dragView.findViewById(R.id.image)).setImageDrawable(imageResource);
            CardView dragCard = ((CardView) dragView.findViewById(R.id.card));
            CardView clickedCard = ((CardView) clickedView.findViewById(R.id.card));

            dragCard.setMaxCardElevation(40);
            dragCard.setCardElevation(clickedCard.getCardElevation());
            // I know the dragView is a FrameLayout and that is why I can use setForeground below api level 23
            dragCard.setForeground(clickedView.getResources().getDrawable(R.drawable.app_routing_slip_card_view_drag_foreground));
        }

        @Override
        public void onMeasureDragView(View clickedView, View dragView) {
            CardView dragCard = ((CardView) dragView.findViewById(R.id.card));
            CardView clickedCard = ((CardView) clickedView.findViewById(R.id.card));
            int widthDiff = dragCard.getPaddingLeft() - clickedCard.getPaddingLeft() + dragCard.getPaddingRight() -
                    clickedCard.getPaddingRight();
            int heightDiff = dragCard.getPaddingTop() - clickedCard.getPaddingTop() + dragCard.getPaddingBottom() -
                    clickedCard.getPaddingBottom();
            int width = clickedView.getMeasuredWidth() + widthDiff;
            int height = clickedView.getMeasuredHeight() + heightDiff;
            dragView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            dragView.measure(widthSpec, heightSpec);
        }

        @Override
        public void onStartDragAnimation(View dragView) {
            CardView dragCard = ((CardView) dragView.findViewById(R.id.card));
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }

        @Override
        public void onEndDragAnimation(View dragView) {
            CardView dragCard = ((CardView) dragView.findViewById(R.id.card));
            ObjectAnimator anim = ObjectAnimator.ofFloat(dragCard, "CardElevation", dragCard.getCardElevation(), 6);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }
    }
}