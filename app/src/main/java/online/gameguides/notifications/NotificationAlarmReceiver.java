package online.gameguides.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import online.gameguides.App;

/**
 * Created by Tony on 01-Mar-16.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";


    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        Log.i("NOTIF", id+"");

        SharedPreferences mPrefs = context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        if (mPrefs.getBoolean(App.PREFS_NOTIFICATIONS_ENABLED, true))
            notificationManager.notify(id, notification);

    }
}