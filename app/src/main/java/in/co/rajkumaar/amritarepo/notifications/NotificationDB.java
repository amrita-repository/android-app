/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.notifications;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Notification.class}, version = 1, exportSchema = false)
public abstract class NotificationDB extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static NotificationDB INSTANCE;

    public static NotificationDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NotificationDB.class) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(context.getApplicationContext(),
                                    NotificationDB.class,
                                    "notification_db").build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract NotificationDao notificationDao();
}
