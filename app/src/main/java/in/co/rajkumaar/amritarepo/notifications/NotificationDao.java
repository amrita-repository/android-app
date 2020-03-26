/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.notifications;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insertNotification(Notification notification);

    @Query("SELECT * FROM notifications WHERE id = :id")
    List<Notification> findNotification(int id);

    @Query("DELETE FROM notifications")
    void clearAllNotifications();

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    LiveData<List<Notification>> getAll();
}
