package online.gameguides.utils;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "com.publishadventures.gameq.utils.action.ACTION_DOWNLOAD";

    // TODO: Rename parameters
    private static final String EXTRA_URL_LIST = "com.publishadventures.gameq.utils.extra.EXTRA_URL_LIST";
    private static final String EXTRA_HTML = "com.publishadventures.gameq.utils.extra.EXTRA_HTML";
    private static final String EXTRA_GUIDE_ID = "com.publishadventures.gameq.utils.extra.EXTRA_GUIDE_ID";

    private String mHtml;
    private String mGuideId;


    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDownload(Context context) {
        Intent intent = new Intent(context, DownloadIntentService.class);
        intent.setAction(ACTION_DOWNLOAD);
//        intent.putExtra(EXTRA_URL_LIST, urls);
//        intent.putExtra(EXTRA_HTML, html);
//        intent.putExtra(EXTRA_GUIDE_ID, gameId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
//                final ArrayList<String> param1 = intent.getStringArrayListExtra(EXTRA_URL_LIST);
//                final String param2 = intent.getStringExtra(EXTRA_HTML);
//                final String param3 = intent.getStringExtra(EXTRA_GUIDE_ID);
        }
            GGApplication application = (GGApplication) getApplication();
            handleActionFoo(application.favImgs, application.favHTML, application.favGuideId);

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(ArrayList<String> param1, String param2, String param3) {
        mHtml = param2;
        mGuideId = param3;
        new ConvertImageBase64Task().execute(param1);
    }


    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bitmapToBase64String(Bitmap bmp, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bmp == null)
            return null;
        bmp.compress(format, quality, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    class ConvertImageBase64Task extends AsyncTask<ArrayList<String>, Void, Void> {

//        ArrayList<String> base64List = new ArrayList<>();

        @SafeVarargs
        @Override
        protected final Void doInBackground(ArrayList<String>... lists) {
            String base64;
//            for (String url : lists[0]) {
//                base64 = bitmapToBase64String(getBitmapFromURL(url), Bitmap.CompressFormat.JPEG, 60);
//                if (base64 != null)
//                    mHtml = mHtml.replaceAll(url, "data:image/jpg;base64," + base64);
//            }

            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(mGuideId, Context.MODE_PRIVATE);
                outputStream.write(mHtml.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(DownloadIntentService.this, "Offline access available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(DownloadIntentService.this, "Destroyed", Toast.LENGTH_SHORT).show();
    }
}
