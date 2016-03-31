package com.thecn.app.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thecn.app.models.user.User;

import java.sql.SQLException;

/**
 * Class used to interface with sql database.
 * Uses {@link com.thecn.app.tools.DatabaseInterface.MySQLiteHelper}
 * which extends {@link android.database.sqlite.SQLiteOpenHelper} to accomplish this.
 * Contains local information/preferences for all users who have logged into the app from the device.
 */
public class DatabaseInterface {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private static final String[] SETTINGS_COLUMNS = new String[] {
        MySQLiteHelper.COLUMN_ID,
        MySQLiteHelper.COLUMN_QUERY_PLAY,

        MySQLiteHelper.COLUMN_SHOW_NOTIFICATIONS,
        MySQLiteHelper.COLUMN_SHOW_GENERAL_NOTIFICATIONS,
        MySQLiteHelper.COLUMN_SHOW_EMAIL_NOTIFICATIONS,
        MySQLiteHelper.COLUMN_SHOW_FOLLOWER_NOTIFICATIONS,

        MySQLiteHelper.COLUMN_CHECK_NOTIFICATION_INTERVAL_USER_SPECIFY,
        MySQLiteHelper.COLUMN_CHECK_NOTIFICATION_INTERVAL,
        MySQLiteHelper.COLUMN_LAST_NOTIFICATION_CHECK_TIME
    };

    /**
     * New instance
     * @param context used to initialize {@link com.thecn.app.tools.DatabaseInterface.MySQLiteHelper}
     */
    public DatabaseInterface(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    /**
     * Opens database
     * @throws SQLException on failure to open
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Closes database
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates default settings for a user with specified id
     * @param id id of user
     * @return new settings object recently inserted into database
     */
    public User.Settings insertDefaultSettings(String id) {
        User.Settings s = new User.Settings();
        s.setId(id);
        s.setShowNotifications(true);
        insertSettings(s);

        return s;
    }

    /**
     * Insert settings into database using given object.
     * @param settings settings to insert into database
     */
    public void insertSettings(User.Settings settings) {
        String sql = "REPLACE INTO " + MySQLiteHelper.TABLE_SETTINGS + " (" +
                MySQLiteHelper.COLUMN_ID + ", " +
                MySQLiteHelper.COLUMN_QUERY_PLAY + ", " +

                MySQLiteHelper.COLUMN_SHOW_NOTIFICATIONS + ", " +
                MySQLiteHelper.COLUMN_SHOW_GENERAL_NOTIFICATIONS + ", " +
                MySQLiteHelper.COLUMN_SHOW_EMAIL_NOTIFICATIONS + ", " +
                MySQLiteHelper.COLUMN_SHOW_FOLLOWER_NOTIFICATIONS + ", " +

                MySQLiteHelper.COLUMN_CHECK_NOTIFICATION_INTERVAL_USER_SPECIFY + ", " +
                MySQLiteHelper.COLUMN_CHECK_NOTIFICATION_INTERVAL + ", " +
                MySQLiteHelper.COLUMN_LAST_NOTIFICATION_CHECK_TIME +
                ") VALUES (" +
                "'" + settings.getId() + "', " +
                (settings.isPlayQuery() ? 1 : 0) + ", " +

                (settings.isShowNotifications() ? 1 : 0) + ", " +
                (settings.isShowGeneralNotifications() ? 1 : 0) + ", " +
                (settings.isShowEmailNotifications() ? 1 : 0) + ", " +
                (settings.isShowFollowerNotifications() ? 1 : 0) + ", " +

                settings.getUserSpecifiedRefreshTime() + ", " +
                settings.getRefreshNotificationInterval() + ", " +
                settings.getLastNotificationRefreshTime() +
                ");";

        database.execSQL(sql);
    }

    /**
     * Get settings for a user from the database using user's id
     * @param id user's id
     * @return new user settings object created from info in database.
     */
    public User.Settings getSettings(String id) {
        Cursor cursor = database.query(
                MySQLiteHelper.TABLE_SETTINGS,
                SETTINGS_COLUMNS,
                MySQLiteHelper.COLUMN_ID + " = '" + id + "'",
                null, null, null, null
        );

        if (!cursor.moveToFirst()) {
            return insertDefaultSettings(id);
        }

        User.Settings s = new User.Settings();
        s.setId(cursor.getString(0));
        s.setPlayQuery(cursor.getInt(1) == 1);

        s.setShowNotifications(cursor.getInt(2) == 1);
        s.setShowGeneralNotifications(cursor.getInt(3) == 1);
        s.setShowEmailNotifications(cursor.getInt(4) == 1);
        s.setShowFollowerNotifications(cursor.getInt(5) == 1);

        s.setUserSpecifiedRefreshTime(cursor.getInt(6));
        s.setRefreshNotificationInterval(cursor.getLong(7));
        s.setLastNotificationRefreshTime(cursor.getLong(8));

        return s;
    }

    /**
     * Helper used to interface with sql database.
     */
    public static class MySQLiteHelper extends SQLiteOpenHelper {

        private static final String NAME = "settings.db";
        private static final int VERSION = 1;

        public static final String TABLE_SETTINGS = "local_settings";

        public static final String COLUMN_ID = "id"; //user id
        public static final String COLUMN_QUERY_PLAY = "query_play"; //whether user queried for google play

        public static final String COLUMN_SHOW_NOTIFICATIONS = "show_notifications";
        public static final String COLUMN_SHOW_GENERAL_NOTIFICATIONS = "show_general_notifications";
        public static final String COLUMN_SHOW_EMAIL_NOTIFICATIONS = "show_email_notifications";
        public static final String COLUMN_SHOW_FOLLOWER_NOTIFICATIONS = "show_follower_notifications";

        //interval user has specified for checking notifications
        public static final String COLUMN_CHECK_NOTIFICATION_INTERVAL_USER_SPECIFY = "check_notification_interval_user_spec";
        //the interval that the app is checking for notifications
        public static final String COLUMN_CHECK_NOTIFICATION_INTERVAL = "check_notification_interval";
        //last time notifications were polled from server
        public static final String COLUMN_LAST_NOTIFICATION_CHECK_TIME = "last_notification_check_time";

        //create sql command
        private static final String CREATE = "create table " + TABLE_SETTINGS + "(" +
                COLUMN_ID + " text primary key, " +
                COLUMN_QUERY_PLAY + " integer, " +

                COLUMN_SHOW_NOTIFICATIONS + " integer," +
                COLUMN_SHOW_GENERAL_NOTIFICATIONS + " integer," +
                COLUMN_SHOW_EMAIL_NOTIFICATIONS + " integer," +
                COLUMN_SHOW_FOLLOWER_NOTIFICATIONS + " integer," +

                COLUMN_CHECK_NOTIFICATION_INTERVAL_USER_SPECIFY + " integer," +
                COLUMN_CHECK_NOTIFICATION_INTERVAL + " bigint," +
                COLUMN_LAST_NOTIFICATION_CHECK_TIME + " bigint" +
        ");";

        /**
         * New instance
         */
        public MySQLiteHelper(Context context) {
            super(context, NAME, null, VERSION);
        }

        /**
         * Execute the CREATE command
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE);
        }

        /**
         * Drop the old table if it's there
         * todo change this when upgrades are implemented
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
            onCreate(db);
        }
    }
}
