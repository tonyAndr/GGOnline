package online.gameguides.utils;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import online.gameguides.adapters.DBControllerAdapter;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReadFileIntentService extends Service {
    public static final String INTENT_ACTION_READFILE = "com.publishadventures.gameq.utils.action.READFILE";
    public static final String INTENT_ACTION_READFILE_PROGRESS = "com.publishadventures.gameq.utils.action.READFILE_PROGRESS";
    public static final String INTENT_ACTION_READFILE_HTML = "com.publishadventures.gameq.utils.action.READFILE_HTML";
    public static final String INTENT_ACTION_READFILE_ERROR = "com.publishadventures.gameq.utils.action.READFILE_ERROR";

    private static final String EXTRA_FILE_PATH= "com.publishadventures.gameq.utils.extra.EXTRA_FILEPATH";

    private Intent mBroadcastIntent;

    private String mHtml;
    private String mPath;

    private LoadFromFileTask loadFromFileTask;


    public ReadFileIntentService() {
        super();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startReadingService (Context context, String path) {
        Intent intent = new Intent(context, ReadFileIntentService.class);
        intent.setAction(INTENT_ACTION_READFILE);
        intent.putExtra(EXTRA_FILE_PATH, path);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (loadFromFileTask == null) {
            mPath = intent.getStringExtra(EXTRA_FILE_PATH);
            mBroadcastIntent = new Intent();
            mBroadcastIntent.setAction(INTENT_ACTION_READFILE);
            createTask();
        } else {
            if (!mPath.equals(intent.getStringExtra(EXTRA_FILE_PATH))) {
                loadFromFileTask.cancel(true);
                mPath = intent.getStringExtra(EXTRA_FILE_PATH);
                createTask();
            }
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void createTask () {
        loadFromFileTask= new LoadFromFileTask();
        loadFromFileTask.execute();
    }

    private void sendProgress (int progress) {
        mBroadcastIntent.putExtra(INTENT_ACTION_READFILE, INTENT_ACTION_READFILE_PROGRESS);
        mBroadcastIntent.putExtra(INTENT_ACTION_READFILE_PROGRESS, progress);
        sendBroadcast(mBroadcastIntent);
    }
    private void sendHTML () {
        ((GGApplication)getApplication()).mHtmlBuffer = mHtml;
        mBroadcastIntent.putExtra(INTENT_ACTION_READFILE, INTENT_ACTION_READFILE_HTML);
        sendBroadcast(mBroadcastIntent);
    }

    private void sendError () {
        mBroadcastIntent.putExtra(INTENT_ACTION_READFILE, INTENT_ACTION_READFILE_ERROR);
        sendBroadcast(mBroadcastIntent);
    }

    private class LoadFromFileTask extends AsyncTask<Void, Integer, Void> {


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {


                    FileInputStream fileIn = openFileInput(mPath.substring(mPath.indexOf("=") + 1));
                    long bytes = fileIn.getChannel().size();

                    InputStreamReader InputRead = new InputStreamReader(fileIn);

                    char[] inputBuffer = new char[1000];
                    String s = "";
                    int charRead;
                    int readDone = 0;

                    while ((charRead = InputRead.read(inputBuffer)) > 0) {
                        if (isCancelled()) {
                            break;
                        }
                        // char to string conversion
                        String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                        s += readstring;
                        readDone = readDone + charRead;
                        int progress = (int)(((double)readDone / (double) bytes ) * 100);
                        publishProgress(progress);
                    }

                    publishProgress(100);
                    InputRead.close();
                    mHtml = s;


            } catch (IOException io) {
                mHtml = null;
                sendError();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            Toast.makeText(WebActivity.this, "" + values[0], Toast.LENGTH_SHORT).show();
            sendProgress(values[0]);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sendHTML();
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(DownloadIntentService.this, "Destroyed", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
