package com.droid.mooresoft.anotherbusapp.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.droid.mooresoft.anotherbusapp.AndroidUtils;
import com.droid.mooresoft.anotherbusapp.R;
import com.droid.mooresoft.anotherbusapp.activities.MainActivity;

/**
 * Created by Ed on 9/6/15.
 */
public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
}
