package online.gameguides.utils;


import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

import online.gameguides.models.GuideItem;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {Tracker}.
 */
public class GGApplication extends Application {
//    private Tracker mTracker;

    public String mHtmlBuffer;
    public ArrayList<GuideItem> guideItemArrayList;
    public GuideItem guideItem;
    public String HTML;

    // For saveToFav
    public String favHTML;
    public ArrayList<String> favImgs;
    public String favGuideId;

    /**
     * Gets the default {Tracker} for this {@link Application}.
     * @return tracker
     */
//    synchronized public Tracker getDefaultTracker() {
//        if (mTracker == null) {
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//            mTracker = analytics.newTracker(getString(R.string.tracker_id));
//        }
//        return mTracker;
//    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}