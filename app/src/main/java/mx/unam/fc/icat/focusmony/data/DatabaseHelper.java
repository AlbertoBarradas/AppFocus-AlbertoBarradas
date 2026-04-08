package mx.unam.fc.icat.focusmony.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "focusbuddy.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "sessions";
    public static final String COL_ID = "id";
    public static final String COL_TYPE = "type";
    public static final String COL_DATE = "date";
    public static final String COL_START_TIME = "start_time";
    public static final String COL_DURATION = "duration";
    public static final String COL_COMPLETED = "completed";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_START_TIME + " TEXT, " +
                COL_DURATION + " INTEGER, " +
                COL_COMPLETED + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}