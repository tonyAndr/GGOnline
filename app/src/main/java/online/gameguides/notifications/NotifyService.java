package online.gameguides.notifications;

/**
 * Created by Tony on 31-Mar-16.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import online.gameguides.R;
import online.gameguides.activities.LaunchActivity;


/**
 * This service is started when an Alarm has been raised
 *
 * We pop a notification into the status bar for the user to click on
 * When the user clicks the notification a new activity is opened
 *
 * @author paul.blundell
 */
public class NotifyService extends Service {

    /**
     * Class for clients to access
     */
    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");

        scheduleNotification(getNotification());
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients
    private final IBinder mBinder = new ServiceBinder();

    /**
     * Creates a notification and shows it in the OS drag-down status bar
     */

    private void scheduleNotification(Notification notification) {

        Intent notificationIntent = new Intent(this, NotificationAlarmReceiver.class);
        notificationIntent.putExtra(NotificationAlarmReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationAlarmReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        long futureInMillis = SystemClock.elapsedRealtime() + 172800000; // 172800000 = 2x86400000 == 48h
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification() {
        Intent dismissIntent = new Intent(this, NotificationDismissReceiver.class);
        dismissIntent.putExtra(NotificationAlarmReceiver.NOTIFICATION_ID, 2);
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0);

        Intent resultIntent = new Intent(this, LaunchActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("Check updates for " + Uri.decode(getString(R.string.game_name)));
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setDeleteIntent(pendingDismissIntent);
        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);
        builder.setColor(getResources().getColor(R.color.colorPrimary));
        return builder.build();
    }
}