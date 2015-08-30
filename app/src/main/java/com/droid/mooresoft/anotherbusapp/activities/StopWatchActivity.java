package com.droid.mooresoft.anotherbusapp.activities;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.mooresoft.anotherbusapp.AndroidUtils;
import com.droid.mooresoft.anotherbusapp.Data;
import com.droid.mooresoft.anotherbusapp.Departure;
import com.droid.mooresoft.anotherbusapp.GlobalVariables;
import com.droid.mooresoft.anotherbusapp.HttpRequestTask;
import com.droid.mooresoft.anotherbusapp.ParseUtils;
import com.droid.mooresoft.anotherbusapp.R;
import com.droid.mooresoft.anotherbusapp.Stop;
import com.droid.mooresoft.anotherbusapp.TabLayout;
import com.droid.mooresoft.anotherbusapp.UrlFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ed on 8/23/15.
 */
public class StopWatchActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);
        setContentView(R.layout.stop_watch_activity);
        setupCustomActionBar();
        // initial setup
        String stopName = getIntent().getStringExtra("stop_name");
        setTitle(stopName);
        Log.d(getClass().toString(), "onCreate() " + stopName);
        mStop = GlobalVariables.getInstance(this).getStopMap().get(stopName);
        // view pager setup
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new DepartureListAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        // tab setup
        mTabs = (TabLayout) findViewById(R.id.tab_layout);
        mTabs.setViewPager(mViewPager);
        // progress spinner setup
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.accent),
                PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        // error layout
        mErrorText = (TextView) findViewById(R.id.error_text);
        mErrorRefreshButton = (Button) findViewById(R.id.refresh_button);
        mErrorRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE); // show spinner
                // hide error stuff
                mErrorText.setVisibility(View.GONE);
                mErrorRefreshButton.setVisibility(View.GONE);
                refresh();
            }
        });
        // initial data refresh
        refresh();
    }

    public void refresh() {
        // should we make a server request?
        long lastRequestTime = Data.getRequestTime(Data.REQUEST_ID_GET_DEPARTURES_BY_STOP, mStop.stopId, this);
        if (System.currentTimeMillis() - lastRequestTime < MIN_REQUEST_INTERVAL) {
            Log.i(getClass().toString(), "attempting to use old departure data...");
            // try to load saved data (if we fail, we will fall through and make the server request)
            String json = Data.getJsonResponse(Data.REQUEST_ID_GET_DEPARTURES_BY_STOP, mStop.stopId, this);
            if (json != null) {
                try {
                    mStopPointToDepartureListMap = ParseUtils.parseGetDeparturesByStopJson(json);
                    // refresh views with old data
                    mPagerAdapter.notifyDataSetChanged();
                    mTabs.refresh();
                    Log.i(getClass().toString(), "...successfully refreshed with old data!");
                    mProgressBar.setVisibility(View.GONE);
                    if (mStopPointToDepartureListMap.isEmpty()) {
                        // no error, but also no departures
                        mErrorText.setText(R.string.no_departures);
                        mErrorText.setVisibility(View.VISIBLE);
                    }
                    return; // done!
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(getClass().toString(), "requesting new departure data from server...");
        URL url = UrlFactory.buildGetDeparturesByStopUrl(mStop.stopId);
        new HttpRequestTask() {
            @Override
            protected void onPostExecute(Pair<String, Exception> result) {
                String json = result.first;
                Exception error = result.second;
                boolean ok = true;
                if (json == null) {
                    // no new data
                    if (error != null) error.printStackTrace();
                    ok = false;
                } else {
                    try {
                        // new data
                        mStopPointToDepartureListMap = ParseUtils.parseGetDeparturesByStopJson(json);
                        // save the JSON and the request time
                        Data.saveJsonResponse(
                                Data.REQUEST_ID_GET_DEPARTURES_BY_STOP, mStop.stopId, json, getContext());
                        Data.saveRequestTime(
                                Data.REQUEST_ID_GET_DEPARTURES_BY_STOP, mStop.stopId, System.currentTimeMillis(), getContext());
                        Log.i(getClass().toString(), "...successfully fetched new data!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ok = false;
                    }
                }
                // error stuff
                mErrorText.setVisibility(ok ? View.GONE : View.VISIBLE);
                mErrorRefreshButton.setVisibility(ok ? View.GONE : View.VISIBLE);
                if (mStopPointToDepartureListMap.isEmpty()) {
                    // no error, but also no departures
                    mErrorText.setText(R.string.no_departures);
                    mErrorText.setVisibility(View.VISIBLE);
                }
                // spinner
                mProgressBar.setVisibility(View.GONE);
                // refresh views
                mPagerAdapter.notifyDataSetChanged();
                mTabs.refresh();
            }
        }.execute(url, this);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        TextView titleView = (TextView) findViewById(R.id.action_bar_title);
        titleView.setText(title);
    }

    private void setupCustomActionBar() {
        ImageView homeIconView = (ImageView) findViewById(R.id.home);
        Drawable homeIcon = AndroidUtils.getTintedDrawable(R.mipmap.ic_action_back_white,
                getResources().getColor(android.R.color.white), this);
        homeIconView.setImageDrawable(homeIcon);
        homeIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // return to parent activity
                finish();
            }
        });
    }

    public static final int MIN_REQUEST_INTERVAL = 1000 * 60; // millis

    private Stop mStop;
    private ViewPager mViewPager;
    private TextView mErrorText;
    private Button mErrorRefreshButton;
    private ProgressBar mProgressBar;
    private TabLayout mTabs;
    private DepartureListAdapter mPagerAdapter;
    private Map<String, ArrayList<Departure>> mStopPointToDepartureListMap;

    private final SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Log.d(getClass().toString(), "refreshing...");
            refresh();
        }
    };

    private class DepartureListAdapter extends PagerAdapter {
        @Override
        public void notifyDataSetChanged() {
            if (mStopPointToDepartureListMap == null) {
                // there must have been an error
                mViewPager.removeAllViews();
            } else {
                // proceed with normal view setup
                Set<String> tags = mStopPointToDepartureListMap.keySet();
                for (String tag : tags) {
                    View root = mViewPager.findViewWithTag(tag);
                    // the view may not exist yet
                    if (root != null) {
                        // get the list adapter or set it if necessary
                        ListView list = (ListView) root.findViewById(R.id.list);
                        DepartureListItemAdapter adapter = (DepartureListItemAdapter) list.getAdapter();
                        if (adapter == null) {
                            adapter = new DepartureListItemAdapter(getApplicationContext());
                            list.setAdapter(adapter);
                        }
                        // swap in the new data
                        adapter.clear();
                        adapter.addAll(mStopPointToDepartureListMap.get(tag));
                        // remove the refresh indicator
                        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
                        refreshLayout.setRefreshing(false);
                    }
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View root = View.inflate(getApplicationContext(), R.layout.refreshable_departure_list, null);
            // need to tag this view so we can find it while refreshing
            String tag = (String) mStopPointToDepartureListMap.keySet().toArray()[position];
            root.setTag(tag);
            container.addView(root);
            // populate view with data
            ListView list = (ListView) root.findViewById(R.id.list);
            DepartureListItemAdapter adapter = new DepartureListItemAdapter(getApplicationContext());
            list.setAdapter(adapter);
            adapter.clear();
            adapter.addAll(mStopPointToDepartureListMap.get(tag));
            // setup refresh behavior
            SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
            refreshLayout.setOnRefreshListener(mRefreshListener);
            refreshLayout.setColorSchemeColors(getResources().getColor(R.color.accent));
            return root;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String stopPointId = (String) mStopPointToDepartureListMap.keySet().toArray()[position];
            String title = mStop.stopName;
            for (Stop.StopPoint sp : mStop.stopPoints) {
                if (sp.stopId.equals(stopPointId)) {
                    title = sp.stopName;
                    if (title.length() > mStop.stopName.length()) {
                        // want to remove the redundant info
                        title = title.replaceAll("[()]", "");
                        title = title.replace(mStop.stopName, "");
                        // the stop name may have had 'and' replaced with '&'
                        String altName = mStop.stopName.replace("and", "&");
                        title = title.replace(altName, "");
                    }
                }
            }
            return title;
        }

        @Override
        public int getCount() {
            if (mStopPointToDepartureListMap == null) return 0;
            // number of stop points
            return mStopPointToDepartureListMap.keySet().size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object o) {
            container.removeView((View) o);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        private class DepartureListItemAdapter extends ArrayAdapter<Departure> {
            public DepartureListItemAdapter(Context context) {
                super(context, R.layout.departure_list_item);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.departure_list_item, null);
                }
                Departure departure = getItem(position);
                // populate view with departure data
                TextView headsignView = (TextView) convertView.findViewById(R.id.headsign);
                TextView tripHeadsignView = (TextView) convertView.findViewById(R.id.trip_headsign);
                TextView expectedMinsView = (TextView) convertView.findViewById(R.id.expected_mins);
                headsignView.setText(departure.headsign);
                tripHeadsignView.setText(departure.tripHeadsign);
                expectedMinsView.setText(String.valueOf(departure.getExpectedMins()));
                // need to get tinted drawables for the decoration
                ImageView outerCircle = (ImageView) convertView.findViewById(R.id.outer_circle);
                ImageView innerCircle = (ImageView) convertView.findViewById(R.id.inner_circle);
                Drawable outer = AndroidUtils.getTintedDrawable(R.drawable.circle, departure.routeColor, getContext());
                Drawable inner = AndroidUtils.getTintedDrawable(
                        R.drawable.circle, getResources().getColor(R.color.background), getContext());
                outerCircle.setImageDrawable(outer);
                innerCircle.setImageDrawable(inner);
                return convertView;
            }
        }
    }
}
