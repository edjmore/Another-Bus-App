package com.droid.mooresoft.anotherbusapp.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droid.mooresoft.anotherbusapp.AndroidUtils;
import com.droid.mooresoft.anotherbusapp.Favorites;
import com.droid.mooresoft.anotherbusapp.R;
import com.droid.mooresoft.anotherbusapp.Stop;
import com.droid.mooresoft.anotherbusapp.activities.MainActivity;
import com.droid.mooresoft.anotherbusapp.activities.StopWatchActivity;

import java.util.ArrayList;

/**
 * Created by Ed on 9/5/15.
 */
public class FavoritesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // now we populate the list view with favorite stops
        mListView = (ListView) view.findViewById(R.id.list);
        ArrayList<Stop> favStops = Favorites.getFavorites(getActivity());
        if (favStops.isEmpty()) {
            // show the error text view
            TextView tv = (TextView) view.findViewById(R.id.text);
            tv.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mListView.setAdapter(mAdapter = new FavoritesAdapter(favStops, getActivity()));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // launch the corresponding stopwatch activity
                    Stop s = mAdapter.getItem(position);
                    Intent intent = new Intent(getActivity(), StopWatchActivity.class)
                            .putExtra("stop_name", s.stopName);
                    startActivity(intent);
                }
            });
            // todo: just one location update?
            AndroidUtils.requestCurrentLocation(mLocationListener, getActivity());
        }
        // setup the action bar
        ImageView drawerIconView = (ImageView) view.findViewById(R.id.drawer_icon);
        Drawable drawerIcon = AndroidUtils.getTintedDrawable(
                R.mipmap.ic_drawer_white, Color.WHITE, getActivity());
        drawerIconView.setImageDrawable(drawerIcon);
        // drawer toggle
        drawerIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListView != null) {
            // refresh list data (it may have changed)
            ArrayList<Stop> favStops = Favorites.getFavorites(getActivity());
            mAdapter = new FavoritesAdapter(favStops, getActivity());
            mListView.setAdapter(mAdapter);
        }
    }

    private Location mCurrentLocation;
    private ListView mListView;
    private ArrayAdapter<Stop> mAdapter;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            mListView.setAdapter(mAdapter); // force the listview to refresh
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            // do nothing
        }

        @Override
        public void onProviderDisabled(String provider) {
            // do nothing
        }
    };

    private static final long MIN_ELAPSED_TIME = 0; // NOW!
    private static final float MIN_ELAPSED_DISTANCE = 402.336f; // quarter of a mile

    private class FavoritesAdapter extends ArrayAdapter<Stop> {
        public FavoritesAdapter(ArrayList<Stop> favStops, Context context) {
            super(context, R.layout.favorite_stop_item, favStops);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.favorite_stop_item, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon_stop);
            Drawable busIcon = AndroidUtils.getTintedDrawable(
                    R.mipmap.ic_bus, getResources().getColor(R.color.icon_inactive), getContext());
            icon.setImageDrawable(busIcon);
            // populate with individual stop data
            Stop stop = getItem(position);
            // stop name
            TextView nameView = (TextView) convertView.findViewById(R.id.name_view);
            nameView.setText(stop.stopName);
            // distance to stop
            // might still be waiting for location
            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            progressBar.setVisibility(mCurrentLocation == null ? View.VISIBLE : View.GONE);
            if (mCurrentLocation != null) {
                TextView distanceView = (TextView) convertView.findViewById(R.id.distance_view);
                String distString = String.format("%.2f", AndroidUtils.getDistanceToStop(mCurrentLocation, stop));
                distanceView.setText(distString);
                // todo: units preference?
                TextView distUnitsView = (TextView) convertView.findViewById(R.id.distance_units_view);
                distUnitsView.setText("miles");
            }
            return convertView;
        }
    }
}
