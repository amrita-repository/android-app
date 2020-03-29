/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import in.co.rajkumaar.amritarepo.BuildConfig;

public class OpenTask {


    private static final String TAG = "Open Task";
    private Context context;
    private String downloadUrl;
    private String downloadFileName;

    private ProgressDialog progressDialog;

    public OpenTask(Context context, String downloadUrl, int act) {
        this.context = context;

        this.downloadUrl = downloadUrl;

        if (act == 0) {
            downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'));

        } else if (act == 1) {
            downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'));
//            downloadFileName = downloadFileName.substring(0, downloadFileName.indexOf("?"));
//            downloadFileName = downloadFileName.replaceAll("%20", "_");
        } else if (act == 2) {
            downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/'));
            downloadFileName = downloadFileName.replaceAll("%20", "_");
            downloadFileName = downloadFileName.replaceAll("%26", "&");
            downloadFileName = downloadFileName.replaceAll("%28", "(");
            downloadFileName = downloadFileName.replaceAll("%29", ")");
        }
        //Create file name by picking download file name from URL
        Log.e(TAG, downloadFileName);
        new ClearCache().clear(context);
        //Start Downloading Task
        new DownloadingTask().execute();

    }

    private String getMime(String url) {
        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            return "application/msword";
        } else if (url.contains(".pdf")) {
            // PDF file
            return "application/pdf";
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            return "application/vnd.ms-excel";
        } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            // JPG file
            return "image/jpeg";
        } else {
            return "application/pdf";
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        private File outputFile = null;
        private File apkStorage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading");
            //progressDialog.setIndeterminate(false);
            //progressDialog.setMax(100);
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    progressDialog.dismiss();
                    File file = new File(context.getExternalFilesDir(null) + "/"
                            + ".AmritaRepoCache/" + downloadFileName);
                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                    String type = map.getMimeTypeFromExtension(ext);

                    if (type == null)
                        type = "*/*";

                    Log.e("FILE NAME", file.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(data, getMime(downloadFileName));
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                        context.startActivity(intent);
                    else
                        Utils.showToast(context, "Sorry, there's no appropriate app in the device to open this file.");


                } else {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 3000);
                    Toast toast = Toast.makeText(context, "Error. Opening Failed", Toast.LENGTH_SHORT);
                    if (!toast.getView().isShown())     // true if visible
                        toast.show();
                    progressDialog.dismiss();
                    Log.e(TAG, "Download Failed");

                }
            } catch (Exception e) {
                e.printStackTrace();

                //Change button text if exception occurs

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 3000);
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

            }


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String proxy = BuildConfig.PROXY_URL;
            try {

                URL url;
                HttpURLConnection c;
                //Open Url Connection
                try {
                    url = new URL(downloadUrl);//Create Download URl
                    c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                    c.connect();//connect the URL Connection
                } catch (Exception e) {
                    Log.e("POSTING", "Post Activated");
                    url = new URL(proxy);//Create Download URl
                    c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(c.getOutputStream());
                    String data = URLEncoder.encode("data", "UTF-8")
                            + "=" + URLEncoder.encode(downloadUrl, "UTF-8")
                            + "&" + URLEncoder.encode("hash", "UTF-8") + "=" + URLEncoder.encode(BuildConfig.SECRET_HASH, "UTF-8");
                    wr.write(data);
                    wr.flush();
                    c.connect();
                    e.printStackTrace();
                }

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    progressDialog.dismiss();
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());
                    return null;

                } else {


                    //Get File if SD card is present
                    if (new CheckForSDCard().isSDCardPresent()) {

                        apkStorage = new File(
                                context.getExternalFilesDir(null) + "/" + ".AmritaRepoCache");
                    } else
                        Utils.showToast(context, "Oops!! There is no SD Card.");

                    //If File is not present create directory
                    if (!apkStorage.exists()) {
                        Log.v("DIRECTORY CREATION : ", String.valueOf(apkStorage.mkdir()));
                        Log.e(TAG, "Directory Created.");
                    }

                    outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File
                    //Create New File if not present
                    if (!outputFile.exists()) {
                        outputFile.createNewFile();
                        Log.e(TAG, "File Created");
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                    InputStream is = c.getInputStream();//Get InputStream for connection

                    byte[] buffer = new byte[1024];//Set buffer type
                    int len1 = 0;//init length
                    long total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;
                        // publishing the progress....
                        //progressDialog.setProgress((int) (total * 100 / fileLength));
                        fos.write(buffer, 0, len1);//Write new file
                    }

                    //Close all connection after doing task
                    fos.close();
                    is.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }
}
