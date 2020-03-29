/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.core.content.FileProvider;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.Objects;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.downloads.DownloadsActivity;

/**
 * Implementation of App Widget functionality.
 */
public class ImageWidget extends AppWidgetProvider {

    private static final String MyOnClick = "openWidget";
    private static SharedPreferences preferences;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.academic_timetable);
        //.setTextViewText(R.id.appwidget_text, widgetText);
        String path = preferences.getString("path", null);
        if (path != null) {
            try {
                path = "/AmritaRepo/" + path;
                File file = new File(context.getExternalFilesDir(null) + path);
                Bitmap bmp = BitmapFactory.decodeFile(file.toString());
                int nh = (int) (bmp.getHeight() * (512.0 / bmp.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);
                views.setImageViewBitmap(R.id.image_widget, scaled);
                views.setTextViewText(R.id.class_name, (path.split("\\.")[0]).split("/")[2]);
            } catch (Exception e) {
                Crashlytics.log(e.getMessage());
                e.printStackTrace();
            }
        } else {
            views.setTextViewText(R.id.class_name, "Click me");
        }
        views.setOnClickPendingIntent(R.id.image_widget, getPendingSelfIntent(context, MyOnClick));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, ImageWidget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), MyOnClick)) {
            preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            String path = preferences.getString("path", null);
            if (path != null) {
                try {
                    path = "/AmritaRepo/" + path;
                    File file = new File(context.getExternalFilesDir(null) + path);
                    Intent intentToOpen = new Intent(Intent.ACTION_VIEW);
                    Uri data = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                    intentToOpen.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intentToOpen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentToOpen.setDataAndType(data, "image/*");
                    context.startActivity(intentToOpen);
                } catch (Exception e) {
                    Crashlytics.log(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                context.startActivity(new Intent(context, DownloadsActivity.class).putExtra("widget", true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
        super.onReceive(context, intent);
    }
}

