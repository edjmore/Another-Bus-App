package com.droid.mooresoft.anotherbusapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ed on 8/21/15.
 * <p>
 * An 'HttpRequestTask' performs a server request at the supplied URL on a background thread.
 * Clients can then access the string response as the first element of a 'Pair<String, Exception>'
 * by overriding the 'onPostExecute()' method. If there is an error while making the server request,
 * the 'String' element will be null, and the error will be described in the 'Exception' element. If everything
 * goes well, the 'Exception' will be null.
 * </p>
 */
public abstract class HttpRequestTask extends AsyncTask<URL, Void, Pair<String, Exception>> {

    public final HttpRequestTask execute(URL url, Context context) {
        mContext = context;
        return (HttpRequestTask) super.execute(url);
    }

    @Override
    protected final Pair<String, Exception> doInBackground(URL... urls) {
        Pair<String, Exception> result = null;
        // check connection
        if (AndroidUtils.isConnected(getContext())) {
            // make the request
            URL url = urls[0];
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                result = new Pair<>(sb.toString(), null);
            } catch (IOException e) {
                result = new Pair<>(null, new Exception());
            }
        } else {
            result = new Pair<>(null, new Exception());
        }
        // ToDo: add useful messages to the exceptions
        return result;
    }

    @Override
    protected abstract void onPostExecute(Pair<String, Exception> result);

    protected Context getContext() {
        return mContext;
    }

    private Context mContext;
}
