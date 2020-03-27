/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.notifications;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NotificationViewModel extends AndroidViewModel {
    private NotificationRepository notificationRepository;
    private LiveData<List<Notification>> notifications;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
        notifications = notificationRepository.getNotifications();
    }

    public LiveData<List<Notification>> getAllNotifications() {
        return notifications;
    }

    public void insert(Notification notification) {
        notificationRepository.insert(notification);
    }

    public void deleteAll() {
        notificationRepository.deleteAll();
    }
}
