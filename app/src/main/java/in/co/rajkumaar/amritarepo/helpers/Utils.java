/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;

public class Utils {
    public static String THEME_LIGHT = "light";
    public static String THEME_DARK = "dark";
    public static String folderCheck = "Folder";
    public static String[] web = {"html", "htm", "mhtml"};
    public static String[] computer = {"exe", "dmg", "iso", "msi"};
    public static String[] document = {"doc", "docx", "rtf", "odt"};
    public static String[] pdf = {"pdf"};
    public static String[] powerpoint = {"ppt", "pps", "pptx"};
    public static String[] excel = {"xls", "xlsx", "ods"};
    public static String[] image = {"png", "gif", "jpg", "jpeg", "bmp"};
    public static String[] video = {"mp4", "mp3", "avi", "mov", "mpg", "mkv", "wmv"};
    public static String[] compressed = {"rar", "zip", "zipx", "tar", "7z", "gz"};

    public static boolean isConnected(Context context) {
        try {
            if (context == null) return false;
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return true;
                        } else
                            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                    }
                } else {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void showSnackBar(Context context, String message) {
        View parentLayout = ((Activity) context).findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public static void hideKeyboard(Context context) {
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager.isAcceptingText())
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(((Activity) context).getCurrentFocus()).getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showUnexpectedError(Context context) {
        showToast(context, "An unexpected error occurred. Please try again later.");
    }

    public static void showInternetError(Context context) {
        showSnackBar(context, "Connection unavailable. Please connect to internet");
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String getUrlWithoutParameters(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return new URI(uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                null, // Ignore the query part of the input url
                uri.getFragment()).toString();
    }

    public static ArrayList<String> getAcademicYears() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        years.add("[Choose year]");
        for (int i = 4; i > -1; --i) {
            years.add((currentYear - i) + "_" + String.valueOf(currentYear - (i - 1)).substring(2, 4));
        }
        return years;
    }

    public static HashMap<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        HashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void shareFileIntent(Context context, File file) {
        Intent shareFileIntent = new Intent(Intent.ACTION_SEND);
        Uri data = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        shareFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareFileIntent.putExtra(Intent.EXTRA_STREAM, data);
        shareFileIntent.setType(getMimeType(context, data));
        shareFileIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey there! I got this file from Amrita Repository, the user-friendly, " +
                        "all-in-one, must-have app for an Amritian. \n" +
                        "You can get it here : http://bit.ly/amritarepo \n" +
                        "(or) you can use the Telegram bot : https://t.me/amrepobot \n" +
                        "\n" +
                        "Engineered by Rajkumar (https://rajkumaar.co.in)"
        );
        if (shareFileIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(shareFileIntent);
        else {
            Utils.showToast(context, "Sorry, there's no appropriate app in the device to open this file.");
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private static Intent getFileChooserIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setData(data);

        return intent;
    }

    public static void openFileIntent(Context context, File file) {
        Intent fileChooserIntent = getFileChooserIntent(context, file);
        if (fileChooserIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(fileChooserIntent);
        else {
            Utils.showToast(context, "Sorry, there's no appropriate app in the device to open this file.");
        }
    }

    public static void showDownloadedNotification(Context context, File file) {
        String CHANNEL_ID = "DOWNLOAD_ALERT";
        String CHANNEL_NAME = "Show download alert";
        Toast.makeText(context, "Downloaded Successfully", Toast.LENGTH_SHORT).show();

        Intent fileChooserIntent = getFileChooserIntent(context, file);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, fileChooserIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(file.getName())
                .setContentText("Download complete.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(2, notification.build());
        }
    }

    public static boolean isExtension(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    public static void clearUnsafeCredentials(Context context) {
        SharedPreferences userPrefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        if (!userPrefs.getBoolean("old_credentials_cleared", false)) {
            context.getSharedPreferences("aums-lite", Context.MODE_PRIVATE).edit().clear().apply();
            SharedPreferences.Editor oldCredsEditor = userPrefs.edit();
            String[] oldCredKeys = {"username", "password", "OPAC_username", "OPAC_password", "encrypted_prefs_value"};
            for (String item : oldCredKeys) {
                oldCredsEditor.remove(item);
            }
            oldCredsEditor.putBoolean("old_credentials_cleared", true);
            oldCredsEditor.apply();
        }
    }
}
