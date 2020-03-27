/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.notifications;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class NotificationRepository {
    private NotificationDao notificationDao;
    private LiveData<List<Notification>> notifications;

    public NotificationRepository(Application application) {
        NotificationDB notificationDB = NotificationDB.getDatabase(application);
        notificationDao = notificationDB.notificationDao();
        notifications = notificationDao.getAll();
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public void insert(final Notification notification) {
        NotificationDB.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                notificationDao.insertNotification(notification);
            }
        });
    }

    public void deleteAll() {
        NotificationDB.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                notificationDao.clearAllNotifications();
            }
        });
    }
}