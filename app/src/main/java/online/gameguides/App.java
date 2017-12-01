package online.gameguides;

import online.gameguides.BuildConfig;

/**
 * Created by Tony on 17-Nov-15.
 */
public final class App {
    public final static String PREFS_NAME = "preferences." + BuildConfig.FLAVOR;
    public final static String PREFS_FIRST_LAUNCH = "first_launch";


    public final static String PREFS_DATA_SAVE = "show_images";
    public final static String PREFS_NOTIFICATIONS_ENABLED = "enable_notifications";
    public final static boolean IMAGES_HIDE = false;
    public final static boolean IMAGES_SHOW = true;


    public final static int METHOD_EMPTYLIST_FAV_PLACEHOLDER = 6;
    public final static int METHOD_EMPTYLIST_FILTER_PLACEHOLDER = 7;
    public final static int METHOD_NO_CONNECTION_PLACEHOLDER = 8;


    // Ads
    public static final String KEY_ADS_COUNTDOWN = "key_ads_countdown";


    public static final String KEY_NIGHTMODE = "key_nightmode";

}
