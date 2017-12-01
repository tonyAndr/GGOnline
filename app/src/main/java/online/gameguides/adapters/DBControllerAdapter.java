package online.gameguides.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import online.gameguides.models.GuideItem;

import java.util.ArrayList;

/**
 * Created by Tony on 14-Feb-15.
 */
public class DBControllerAdapter {

    private final DBController dbController;

    private static DBControllerAdapter mInstance = null;

    public static DBControllerAdapter getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DBControllerAdapter(context.getApplicationContext());
        }
        return mInstance;
    }

    private DBControllerAdapter(Context context) {
        dbController = new DBController(context);
    }


    public long insertGuide(GuideItem item) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbController.guide.LINK, item.getLink());
        cv.put(dbController.guide.IMGLINK, item.getImgLink());
        cv.put(dbController.guide.HEADER, item.getHeader());
        cv.put(dbController.guide.DESC, item.getDesc());
        cv.put(dbController.guide.RATING, item.getRating());

        long id = db.insert(dbController.guide.TABLE_NAME, null, cv);
        db.close();
        return id;
    }

    public boolean isAlreadyInFavs(String link) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        String[] columns = {dbController.guide.LINK, dbController.guide.IMGLINK, dbController.guide.HEADER, dbController.guide.DESC, dbController.guide.RATING};
        Cursor cursor = db.query(dbController.guide.TABLE_NAME, columns, dbController.guide.LINK + " = ?", new String[] {link}, null, null, null);
//        db.close();
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void removeGuide(String link) {
        SQLiteDatabase db = dbController.getWritableDatabase();
        db.delete(dbController.guide.TABLE_NAME, dbController.guide.LINK + " = ?", new String[] {link});
        db.close();
    }


    public int eraseGuides() {
        SQLiteDatabase db = dbController.getWritableDatabase();
        int count = db.delete(dbController.guide.TABLE_NAME, null, null);
        db.close();
        return count;
    }



    public ArrayList<GuideItem> getGuides() {
        int link, imglink, header, desc, rating;
        ArrayList<GuideItem> guideItems = new ArrayList<>();

        SQLiteDatabase db = dbController.getWritableDatabase();
        String[] columns = {dbController.guide.LINK, dbController.guide.IMGLINK, dbController.guide.HEADER, dbController.guide.DESC, dbController.guide.RATING};
        Cursor cursor = db.query(dbController.guide.TABLE_NAME, columns, null, null, null, null, null);
        GuideItem item;
        while (cursor.moveToNext()) {
            link = cursor.getColumnIndex(dbController.guide.LINK);
            imglink = cursor.getColumnIndex(dbController.guide.IMGLINK);
            header = cursor.getColumnIndex(dbController.guide.HEADER);
            desc = cursor.getColumnIndex(dbController.guide.DESC);
            rating = cursor.getColumnIndex(dbController.guide.RATING);

            item = new GuideItem();
            item.setLink(cursor.getString(link));
            item.setImgLink(cursor.getString(imglink));
            item.setHeader(cursor.getString(header));
            item.setDesc(cursor.getString(desc));
            item.setRating(cursor.getInt(rating));

            guideItems.add(item);
        }
        db.close();
        return guideItems;
    }

    public void closeConnection() {
        dbController.close();
    }

    static class DBController extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "guideDB";
        private static final int DATABASE_VERSION = 1;
        private final Guide guide = new Guide();
        private final Context context;


        final class Guide {
            final String TABLE_NAME = "GUIDE";
            final String UID = "_id";
            final String LINK = "link";
            final String IMGLINK = "imglink";
            final String HEADER = "header";
            final String DESC = "desc";
            final String RATING = "rating";
        }


        public DBController(Context applicationcontext) {
            super(applicationcontext, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = applicationcontext;
        }

        //Creates Table
        @Override
        public void onCreate(SQLiteDatabase database) {
            String query;

            query = "CREATE TABLE " + guide.TABLE_NAME + " ( " + guide.UID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " " + guide.LINK + " TEXT UNIQUE," + " " + guide.IMGLINK + " TEXT," +
                    " " + guide.HEADER + " TEXT," + " " + guide.DESC + " TEXT," + " " + guide.RATING + " INTEGER)";
            database.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
            String query;

            query = "DROP TABLE IF EXISTS " + guide.TABLE_NAME;
            database.execSQL(query);

            onCreate(database);
        }
    }

}
