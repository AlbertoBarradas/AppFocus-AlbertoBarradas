package mx.unam.fc.icat.focusmony.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

import mx.unam.fc.icat.focusmony.data.DatabaseHelper;

public class SessionManager {

    private DatabaseHelper dbHelper;

    public SessionManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // CREATE
    public void insertSession(Session session) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_TYPE, session.getType());
            values.put(DatabaseHelper.COL_DATE, session.getDate());
            values.put(DatabaseHelper.COL_START_TIME, session.getStartTime());
            values.put(DatabaseHelper.COL_DURATION, session.getDuration());
            values.put(DatabaseHelper.COL_COMPLETED, session.isCompleted() ? 1 : 0);

            db.insert(DatabaseHelper.TABLE_NAME, null, values);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }

    // READ ALL
    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " ORDER BY id DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    list.add(cursorToSession(cursor));
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }

    // HOY
    public List<Session> getSessionsToday() {
        List<Session> all = getAllSessions();
        List<Session> todayList = new ArrayList<>();

        String today = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                .format(new Date());

        for (Session s : all) {
            if (s.getDate().equals(today)) {
                todayList.add(s);
            }
        }

        return todayList;
    }

    // SEMANA
    public List<Session> getSessionsThisWeek() {
        List<Session> all = getAllSessions();
        List<Session> weekList = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

        for (Session s : all) {
            try {
                Date date = sdf.parse(s.getDate());
                Calendar sessionCal = Calendar.getInstance();
                sessionCal.setTime(date);

                if (sessionCal.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                    weekList.add(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return weekList;
    }

    // DELETE
    public void deleteAllSessions() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete(DatabaseHelper.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // helper
    private Session cursorToSession(Cursor cursor) {
        return new Session(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_START_TIME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COMPLETED)) == 1
        );
    }
}