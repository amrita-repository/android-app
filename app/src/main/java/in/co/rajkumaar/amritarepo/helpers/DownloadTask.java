/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.helpers;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import in.co.rajkumaar.amritarepo.BuildConfig;

public class DownloadTask {
    private static final String TAG = "Download Task";
    private Context context;

    private String downloadUrl;
    private String downloadFileName;

    private ProgressDialog progressDialog;

    public DownloadTask(Context context, String downloadUrl) throws UnsupportedEncodingException, URISyntaxException {
        this.context = context;

        downloadFileName = new File(new URI(downloadUrl).getPath()).getName();
        this.downloadUrl = URLDecoder.decode(downloadUrl, "UTF-8");
        Log.e(TAG, downloadFileName);

        new ClearCache().clear(context);
        new DownloadingTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadingTask extends AsyncTask<Void, Void, Void> {

        private File outputFile = null;
        private File apkStorage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    progressDialog.dismiss();
                    Utils.showDownloadedNotification(context, new File(context.getExternalFilesDir(null) + "/AmritaRepo/" + downloadFileName));
                } else {
                    Toast toast = Toast.makeText(context, "Error. Download Failed", Toast.LENGTH_SHORT);
                    if (!toast.getView().isShown())
                        toast.show();
                    progressDialog.dismiss();
                    Log.e(TAG, "Download Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    c.setRequestMethod("POST");//Set Request Method to "GET" since we are grtting data
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
                                context.getExternalFilesDir(null) + "/"
                                        + "AmritaRepo");
                    } else
                        Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                    //If File is not present create directory
                    if (!apkStorage.exists()) {
                        apkStorage.mkdir();
                        Log.e(TAG, "Directory Created.");
                    }

                    outputFile = new File(apkStorage, downloadFileName);//Create Output file in Main File
                    int fileLength = c.getContentLength();
                    //Create New File if not present
                    if (!outputFile.exists()) {
                        outputFile.createNewFile();
                        Log.e(TAG, "File Created");
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                    InputStream is = c.getInputStream();//Get InputStream for connection

                    byte[] buffer = new byte[1024];//Set buffer type
                    int len1;//init length
                    long total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;
                        // publishing the progress....
                        progressDialog.setProgress((int) (total * 100 / fileLength));
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
