package com.droid.mooresoft.anotherbusapp.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.mooresoft.anotherbusapp.AndroidUtils;
import com.droid.mooresoft.anotherbusapp.Data;
import com.droid.mooresoft.anotherbusapp.GlobalVariables;
import com.droid.mooresoft.anotherbusapp.HttpRequestTask;
import com.droid.mooresoft.anotherbusapp.ParseUtils;
import com.droid.mooresoft.anotherbusapp.R;
import com.droid.mooresoft.anotherbusapp.Stop;
import com.droid.mooresoft.anotherbusapp.UrlFactory;
import com.droid.mooresoft.materiallibrary.widgets.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Ed on 8/16/15.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // initial fragments
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        MapFragment mapFragment = MapFragment.newInstance(getDefaultMapOptions());
        mapFragment.getMapAsync(mOnMapReadyListener);
        trans.replace(R.id.bottom_fragment_container, mapFragment);
        trans.replace(R.id.top_fragment_container, new SearchFragment());
        trans.commit();
        // other initialization
        initializeDrawer();
    }

    private void initializeDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // ToDo: drawer list view
        // ToDo: change fragments based on drawer selection
        // ToDo: dummy on click listener
    }

    private GoogleMapOptions getDefaultMapOptions() {
        CameraPosition camera = new CameraPosition.Builder()
                .target(new LatLng(40.1150, -88.2728)) // Champaign, IL
                .zoom(MIN_ZOOM)
                .build();
        GoogleMapOptions options = new GoogleMapOptions()
                .camera(camera);
        return options;
    }

    private DrawerLayout mDrawerLayout;
    private GoogleMap mGoogleMap;

    private final OnMapReadyCallback mOnMapReadyListener = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.setOnCameraChangeListener(mOnCameraChangeListener);
            mGoogleMap.setMyLocationEnabled(true); // want to show location indicator
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false); // don't want to show the button
            // using custom 'my location' button
            FloatingActionButton myLocationButton = (FloatingActionButton) findViewById(R.id.my_location_button);
            myLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location myLocation = mGoogleMap.getMyLocation();
                    if (myLocation == null) return;
                    LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    // animate to user position
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
                }
            });
            // ToDo: it would be nice if we could avoid adding ALL the stops and just show nearby ones
            Collection<Stop> allStops = GlobalVariables.getInstance(getApplicationContext()).getStopMap().values();
            for (Stop s : allStops) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(s.getLocation())
                        .title(s.stopName);
                mGoogleMap.addMarker(markerOptions);
            }
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    // launch the stopwatch activity for this marker
                    String stopName = marker.getTitle();
                    Intent intent = new Intent(getApplicationContext(), StopWatchActivity.class)
                            .putExtra("stop_name", stopName);
                    startActivity(intent);
                }
            });
        }
    };

    private final GoogleMap.OnCameraChangeListener mOnCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition updateCamera) {
            // want to limit how much the user can zoom out for performance reasons
            // (there are 1300+ stops marked on the map)
            if (updateCamera.zoom < MIN_ZOOM) {
                // disable user interaction until we fix the zoom
                mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
                CameraPosition correctedCamera = // use all old values except zoom
                        new CameraPosition(updateCamera.target, MIN_ZOOM, updateCamera.tilt, updateCamera.bearing);
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(correctedCamera),
                        new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                mGoogleMap.getUiSettings().setAllGesturesEnabled(true); // all clear
                            }

                            @Override
                            public void onCancel() {
                                mGoogleMap.getUiSettings().setAllGesturesEnabled(true); // all clear
                            }
                        });
            }
        }
    };

    private static final int MIN_ZOOM = 17;

    public static class SearchFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.search_fragment, container, false);
            initialize(root);
            return root;
        }

        @Override
        public void onPause() {
            super.onPause();
            // make sure the keyboard goes away
            toggleKeyboard(false);
        }

        private void initialize(View root) {
            mEditText = (EditText) root.findViewById(R.id.edit_query);
            mTextView = (TextView) root.findViewById(R.id.bar_placeholder);
            mLeftIconDrawer = (ImageView) root.findViewById(R.id.left_icon_drawer);
            mLeftIconBack = (ImageView) root.findViewById(R.id.left_icon_back);
            mRightIcon = (ImageView) root.findViewById(R.id.right_icon);
            mListCard = (CardView) root.findViewById(R.id.list_card);
            mListView = (ListView) mListCard.findViewById(R.id.list);
            mApp = (GlobalVariables) getActivity().getApplication();
            mAdapter = new AutocompleteItemAdapter(getActivity(), R.layout.search_list_item);
            // perform initial setup:
            // hide list card
            float transY = getResources().getDisplayMetrics().heightPixels;
            mListCard.setTranslationY(transY);
            // set icon images
            mLeftIconBack.setImageDrawable(
                    AndroidUtils.getTintedDrawable(R.mipmap.ic_action_back,
                            getResources().getColor(R.color.icon_active), getActivity()));
            mLeftIconDrawer.setImageDrawable(
                    AndroidUtils.getTintedDrawable(R.mipmap.ic_drawer,
                            getResources().getColor(R.color.icon_active), getActivity()));
            mRightIcon.setImageDrawable(
                    AndroidUtils.getTintedDrawable(R.mipmap.ic_action_cancel,
                            getResources().getColor(R.color.icon_active), getActivity()));
            // setup click listeners:
            // drawer toggle
            mLeftIconDrawer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
            // exit search button
            mLeftIconBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleKeyboard(false);
                    // show nav drawer icon instead
                    v.setVisibility(View.GONE);
                    mLeftIconDrawer.setVisibility(View.VISIBLE);
                    // clear text and hide cancel button
                    mEditText.setText("");
                    mRightIcon.setVisibility(View.GONE);
                    // hide list card and clear contents
                    animateListCard(false);
                    // switch back to text view
                    mTextView.setVisibility(View.VISIBLE);
                    mEditText.setVisibility(View.GONE);
                }
            });
            // text view will serve as button to begin search
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // hide text and show edit text
                    v.setVisibility(View.GONE);
                    mEditText.setVisibility(View.VISIBLE);
                    // switch to back icon
                    mLeftIconDrawer.setVisibility(View.GONE);
                    mLeftIconBack.setVisibility(View.VISIBLE);
                    // show list card with recents
                    showRecentStops();
                    animateListCard(true);
                    // show keyboard
                    toggleKeyboard(true);
                }
            });
            // query listener
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // do nothing
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        // hide cancel icon and show recent stops
                        mRightIcon.setVisibility(View.GONE);
                        showRecentStops();
                    } else {
                        // show cancel button and do search
                        mRightIcon.setVisibility(View.VISIBLE);
                        URL url = UrlFactory.buildAutocompleteUrl(s.toString());
                        new HttpRequestTask() {
                            @Override
                            protected void onPostExecute(Pair<String, Exception> result) {
                                String json = result.first;
                                Exception error = result.second;
                                boolean ok = true;
                                ArrayList<String> stopNames = null;
                                if (json == null) {
                                    if (error != null) error.printStackTrace();
                                    ok = false;
                                } else {
                                    try {
                                        stopNames = ParseUtils.parseAutocompleteJson(json);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        ok = false;
                                    }
                                }
                                if (ok) {
                                    mAdapter.clear();
                                    mAdapter.addAll(stopNames);
                                    mAdapter.setMode(false);
                                    mListView.setAdapter(mAdapter);
                                } else {
                                    Toast.makeText(getActivity(), R.string.default_error, Toast.LENGTH_SHORT).show();
                                    mAdapter.clear();
                                    mListView.setAdapter(mAdapter);
                                }
                            }
                        }.execute(url, getActivity());
                    }
                }
            });
            // list item click listener
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String stopName = (String) mListView.getItemAtPosition(position);
                    Data.saveRecentStop(stopName, getActivity());
                    // launch stop detail activity
                    Intent intent = new Intent(getActivity(), StopWatchActivity.class);
                    intent.putExtra("stop_name", stopName);
                    startActivity(intent);
                }
            });
            // clear text button
            mRightIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditText.setText("");
                    v.setVisibility(View.GONE);
                    // show recents
                    showRecentStops();
                }
            });
        }

        private void animateListCard(boolean slideUp) {
            float transY = getResources().getDisplayMetrics().heightPixels;
            float[] values;
            if (slideUp) {
                values = new float[]{transY, 0f};
            } else {
                values = new float[]{0f, transY};
            }
            ValueAnimator animator = ValueAnimator.ofFloat(values);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float y = (float) animation.getAnimatedValue();
                    mListCard.setTranslationY(y);
                }
            });
            animator.start();
        }

        private void toggleKeyboard(boolean show) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            if (show) {
                mEditText.requestFocus();
                imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
            } else {
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            }
        }

        private void showRecentStops() {
            mAdapter.clear();
            mAdapter.setMode(true);
            mAdapter.addAll(Data.getRecentStops(getActivity()));
            mListView.setAdapter(mAdapter);
        }

        private EditText mEditText;
        private TextView mTextView;
        private ImageView mLeftIconDrawer, mLeftIconBack, mRightIcon;
        private CardView mListCard;
        private ListView mListView;
        private AutocompleteItemAdapter mAdapter;
        private GlobalVariables mApp;

        private class AutocompleteItemAdapter extends ArrayAdapter<String> {
            public AutocompleteItemAdapter(Context context, int resLayoutId) {
                super(context, resLayoutId);
                mResLayoutId = resLayoutId;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), mResLayoutId, null);
                }
                // change icon based on whether this is a recent stop or a search result
                ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
                Drawable d;
                if (mRecentsMode) {
                    d = AndroidUtils.getTintedDrawable(R.mipmap.ic_recent,
                            getResources().getColor(R.color.icon_inactive), getActivity());
                } else {
                    d = AndroidUtils.getTintedDrawable(R.mipmap.ic_bus,
                            getResources().getColor(R.color.icon_inactive), getActivity());
                }
                icon.setImageDrawable(d);
                // show stop name
                String stopName = getItem(position);
                TextView tv = (TextView) convertView.findViewById(R.id.stop_name);
                tv.setText(stopName);
                return convertView;
            }

            public void setMode(boolean recents) {
                mRecentsMode = recents;
            }

            private boolean mRecentsMode;
            private int mResLayoutId;
        }
    }
}
