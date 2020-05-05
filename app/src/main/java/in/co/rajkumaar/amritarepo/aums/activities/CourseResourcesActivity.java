/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.CourseResAdapter;
import in.co.rajkumaar.amritarepo.aums.helpers.LogInResponse;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.aums.models.CourseResource;
import in.co.rajkumaar.amritarepo.helpers.CheckForSDCard;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static android.view.View.GONE;

public class CourseResourcesActivity extends BaseActivity {

    private String courseId;
    private String courseName;
    private ListView list;
    private ProgressDialog progressDialog;
    private Stack<ArrayList<CourseResource>> courseResourceStack;
    private Stack<String> curFolder;
    private boolean firstEntry;

    /**
     * The courseResourceStack is used for cases when there are folders within the Resources uploaded by the teachers.
     * It handles displaying the correct resource folder and helps in navigating.
     * If the overall structure becomes too long, onKeyLongPress can be used to quickly get out of the Resources Activity.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courseresources);

        list = findViewById(R.id.resources_list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        courseResourceStack = new Stack<>();
        curFolder = new Stack<>();
        firstEntry = true;

        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        Bundle bundle = getIntent().getExtras();
        courseId = bundle.get("courseID").toString();
        courseName = bundle.get("courseName").toString();
        curTitle(courseName);
        curFolder.push(courseName);

        setDomain(UserData.client);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ContextCompat.checkSelfPermission(CourseResourcesActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(CourseResourcesActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    if (Utils.isConnected(CourseResourcesActivity.this)) {
                        CourseResource courseRes = (CourseResource) list.getItemAtPosition(position);
                        if (courseRes.getType().equals("Folder")) {
                            progressDialog.show();
                            getCourseResources(UserData.client, courseRes.getResourceUrl());
                            curFolder.push(courseRes.getResourceFileName());
                            curTitle(courseRes.getResourceFileName());
                        } else {
                            progressDialog.show();
                            getResource(UserData.client, courseRes.getResourceUrl());
                        }
                    } else {
                        Snackbar.make(view, "Device not connected to Internet.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void curTitle(String title) {
        this.setTitle(title);
    }

    private void setDomain(final AsyncHttpClient client) {
        System.out.println("Trying " + UserData.domain);
        client.get(UserData.domain + "/access/content/group/" + courseId + "/", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Element loginPage = doc.select("body.loginbody").first();
                if (loginPage == null) {
                    System.out.println("Domain Set " + UserData.domain);
                    getCourseResources(new String(responseBody));
                } else {
                    if (UserData.domainIndex + 1 < UserData.domains.size()) {
                        UserData.domain = UserData.domains.get(++UserData.domainIndex);
                        System.out.println("Logging in " + UserData.domain);
                        UserData.getSession(new LogInResponse() {
                            @Override
                            public void onSuccess() {
                                UserData.login(new LogInResponse() {
                                    @Override
                                    public void onSuccess() {
                                        setDomain(client);
                                    }

                                    @Override
                                    public void onFailure() {
                                        Utils.showToast(CourseResourcesActivity.this, getString(R.string.server_error));
                                        finish();
                                    }

                                    @Override
                                    public void onException(Exception e) {
                                        Utils.showToast(CourseResourcesActivity.this, getString(R.string.site_change));
                                        e.printStackTrace();
                                        finish();
                                    }
                                });
                            }

                            @Override
                            public void onFailure() {
                                Utils.showToast(CourseResourcesActivity.this, getString(R.string.server_error));
                                finish();
                            }

                            @Override
                            public void onException(Exception e) {
                                Utils.showToast(CourseResourcesActivity.this, getString(R.string.site_change));
                                e.printStackTrace();
                                finish();
                            }
                        });
                    } else {
                        Utils.showUnexpectedError(CourseResourcesActivity.this);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(CourseResourcesActivity.this);
                finish();
            }
        });
    }

    private void getCourseResources(final String responseBody) {
        parseResources("", responseBody);
    }

    private void getCourseResources(final AsyncHttpClient client, final String folder) {
        client.get(UserData.domain + "/access/content/group/" + courseId + "/" + folder, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                parseResources(folder, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showUnexpectedError(CourseResourcesActivity.this);
                finish();
            }
        });
    }

    private void parseResources(String folder, String responseBody) {
        final String notFolder = "";
        Document doc = Jsoup.parse(responseBody);
        Elements tRows = doc.select("body > div > table > tbody > tr");
        final ArrayList<CourseResource> courseResourceList = new ArrayList<>();
        try {
            if (tRows.isEmpty()) {
                if (folder.equals(notFolder)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourseResourcesActivity.this, "No resources uploaded for this course!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourseResourcesActivity.this, "No resources uploaded in this folder!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            } else {
                String lastRes = tRows.last().select("td:nth-child(1) > a").first().text().trim();
                for (Element row : tRows) {
                    String resName = row.select("td:nth-child(1) > a").first().text().trim();
                    String resUrl = row.select("td:nth-child(1) > a").first().attr("href").trim();
                    String resType;
                    String oneUp = "../";
                    if (!(resUrl.equals(oneUp))) {
                        if (resUrl.substring(resUrl.length() - 1).equals("/")) {
                            resType = "Folder";
                        } else {
                            resType = resUrl.substring(resUrl.lastIndexOf('.') + 1);
                        }
                        resUrl = folder + resUrl;
                        courseResourceList.add(new CourseResource(resName, resUrl, resType));
                        if (resName.equals(lastRes)) {
                            courseResourceStack.push(courseResourceList);
                            CourseResAdapter courseResAdapter = new CourseResAdapter(CourseResourcesActivity.this, courseResourceList);
                            list.setAdapter(courseResAdapter);
                            if (firstEntry) {
                                findViewById(R.id.progressBar).setVisibility(GONE);
                                list.setVisibility(View.VISIBLE);
                                firstEntry = false;
                            } else {
                                progressDialog.dismiss();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(CourseResourcesActivity.this, getString(R.string.site_change), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (courseResourceStack.size() <= 1) {
            finish();
        } else {
            courseResourceStack.pop();
            CourseResAdapter courseResAdapter = new CourseResAdapter(CourseResourcesActivity.this, courseResourceStack.peek());
            list.setAdapter(courseResAdapter);

            curFolder.pop();
            setTitle(curFolder.peek());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (courseResourceStack.size() > 1) {
                    onBackPressed();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void getResource(final AsyncHttpClient client, final String resourceCode) {
        boolean alreadyExists = false;
        final String resourceFolderPath;
        String notFolder = "";
        final File resourceFolders;
        File resourceFile = null;

        if (resourceCode.lastIndexOf("/") == -1) {
            resourceFolderPath = "";
        } else {
            resourceFolderPath = resourceCode.substring(0, resourceCode.lastIndexOf("/"));
        }

        if (resourceFolderPath.equals(notFolder)) {
            resourceFolders = new File(getExternalFilesDir(null), ".AmritaRepoCache/CourseResources/" + courseName);
        } else {
            resourceFolders = new File(getExternalFilesDir(null), ".AmritaRepoCache/CourseResources/" + courseName + "/" + resourceFolderPath);
        }

        if (resourceFolders.exists()) {
            resourceFile = new File(resourceFolders, resourceCode.substring(resourceCode.lastIndexOf("/") + 1));
            if (resourceFile.exists()) {
                alreadyExists = true;
            }
        } else {
            resourceFolders.mkdirs();
            Log.v("AUMS Course Resources/" + courseName + "/" + resourceFolderPath, "Directory Created.");
        }

        if (alreadyExists) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = FileProvider.getUriForFile(CourseResourcesActivity.this, BuildConfig.APPLICATION_ID + ".provider", resourceFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(data);
                Intent fileChooserIntent = Intent.createChooser(intent, "Open " + resourceCode.substring(resourceCode.lastIndexOf("/") + 1) + " with:");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(fileChooserIntent);
                else {
                    Utils.showToast(CourseResourcesActivity.this, "Sorry, there's no appropriate app in the device to open this file.");
                }
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("AUMS", "Exception in AUMS Course Resources", e);
                Utils.showUnexpectedError(CourseResourcesActivity.this);
            }
        } else {
            client.get(UserData.domain + "/access/content/group/" + courseId + "/" + resourceCode, new AsyncHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Utils.showUnexpectedError(CourseResourcesActivity.this);
                    progressDialog.dismiss();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    new SaveCourseResource(resourceFolders, resourceCode.substring(resourceCode.lastIndexOf("/") + 1)).execute(responseBody);
                }
            });
        }
    }


    @SuppressLint("StaticFieldLeak")
    class SaveCourseResource extends AsyncTask<byte[], String, String> {

        private File resourceFolders;
        private String resourceName;

        SaveCourseResource(File resourceFolders, String resName) {
            this.resourceFolders = resourceFolders;
            this.resourceName = resName;
        }

        @Override
        protected String doInBackground(byte[]... file) {
            if (!new CheckForSDCard().isSDCardPresent()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CourseResourcesActivity.this, "Oops!! There is no storage.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                File resourceFile = new File(resourceFolders, resourceName);
                if (resourceFile.exists()) {
                    resourceFile.delete();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(resourceFile.getPath());
                    fos.write(file[0]);
                    fos.close();
                    Log.v("AUMS Course Resource: " + resourceName, "Saved");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = FileProvider.getUriForFile(CourseResourcesActivity.this, BuildConfig.APPLICATION_ID + ".provider", resourceFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(data);

                    Intent fileChooserIntent = Intent.createChooser(intent, "Open " + resourceName + " with:");

                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(fileChooserIntent);
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(CourseResourcesActivity.this, "Sorry, there's no appropriate app in the device to open this file.");
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                } catch (java.io.IOException e) {
                    Log.e("AUMS", "Exception in AUMS Course Resources", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                    Utils.showUnexpectedError(CourseResourcesActivity.this);
                }
            }
            return (null);
        }
    }
}