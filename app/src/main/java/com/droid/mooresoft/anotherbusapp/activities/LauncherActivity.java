package com.droid.mooresoft.anotherbusapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.droid.mooresoft.anotherbusapp.AndroidUtils;
import com.droid.mooresoft.anotherbusapp.Data;
import com.droid.mooresoft.anotherbusapp.GlobalVariables;
import com.droid.mooresoft.anotherbusapp.HttpRequestTask;
import com.droid.mooresoft.anotherbusapp.ParseUtils;
import com.droid.mooresoft.anotherbusapp.R;
import com.droid.mooresoft.anotherbusapp.Stop;
import com.droid.mooresoft.anotherbusapp.UrlFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Map;

/**
 * Created by Ed on 8/16/15.
 */
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inital view setup
        setContentView(R.layout.launcher_activity);
        Drawable bus = AndroidUtils.getTintedDrawable(R.mipmap.large_bus, getResources().getColor(R.color.accent), this);
        ImageView busView = (ImageView) findViewById(R.id.bus_image);
        busView.setImageDrawable(bus);
        // main purpose of the launcher activity is just to get all the stop data
        // and save it as a global variable
        // if we fail, we exit the application
        URL url = UrlFactory.buildGetStopsUrl(this);
        new GetStopsRequest().execute(url, this);
    }

    private class GetStopsRequest extends HttpRequestTask {
        @Override
        protected void onPostExecute(Pair<String, Exception> result) {
            String json = result.first;
            Exception error = result.second;
            boolean ok = true;
            if (json == null) {
                // not sure, but it may be possible for 'json' and 'error' to be null
                // in either case, exit the application
                if (error != null) error.printStackTrace();
                ok = false;
            } else {
                try {
                    // is this new data?
                    JSONObject jsonObject = new JSONObject(json);
                    boolean newData = jsonObject.getBoolean("new_changeset");
                    if (newData) {
                        Log.i(getClass().toString(), "new stops data downloaded");
                        // save the changeset ID and the JSON
                        String changesetId = jsonObject.getString("changeset_id");
                        Data.saveChangesetId(Data.REQUEST_ID_GET_STOPS, changesetId, getContext());
                        Data.saveJsonResponse(Data.REQUEST_ID_GET_STOPS, null, json, getContext());
                    } else {
                        Log.i(getClass().toString(), "using stored stop data");
                        // try to load data from storage
                        json = Data.getJsonResponse(Data.REQUEST_ID_GET_STOPS, null, getContext());
                    }
                    if (json == null) {
                        // the saved data could be null
                        ok = false;
                    } else {
                        // everything went well!
                        Map<String, Stop> stopMap = ParseUtils.parseGetStopsJson(json);
                        GlobalVariables globalVars = GlobalVariables.getInstance(getContext());
                        globalVars.setStopMap(stopMap);
                    }
                } catch (JSONException e) {
                    // if the parse method threw an exception there was an unsolvable problem
                    e.printStackTrace();
                    ok = false;
                }
            }
            if (ok) {
                // launch the main activity
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getContext(), R.string.default_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
