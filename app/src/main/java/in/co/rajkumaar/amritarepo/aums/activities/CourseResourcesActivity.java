/*
 * Copyright (c) 2020 RAJKUMAR S
 */

package in.co.rajkumaar.amritarepo.aums.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
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

import cz.msebera.android.httpclient.Header;
import in.co.rajkumaar.amritarepo.BuildConfig;
import in.co.rajkumaar.amritarepo.R;
import in.co.rajkumaar.amritarepo.activities.BaseActivity;
import in.co.rajkumaar.amritarepo.aums.helpers.UserData;
import in.co.rajkumaar.amritarepo.helpers.CheckForSDCard;
import in.co.rajkumaar.amritarepo.helpers.Utils;

import static android.view.View.GONE;

public class CourseResourcesActivity extends BaseActivity {
    private String courseId, courseName;
    private ListView list;
    private ProgressDialog progressDialog;

    private ArrayList<ArrayList<CourseResource>> courseResourceStack;
    private ArrayList<String> curFolder;
    boolean firstEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courseresources);

        list = findViewById(R.id.resources_list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        courseResourceStack = new ArrayList<>();
        curFolder = new ArrayList<>();
        curFolder.add("Course Resources");
        firstEntry = true;

        String quote = getResources().getStringArray(R.array.quotes)[new Random().nextInt(getResources().getStringArray(R.array.quotes).length)];
        ((TextView) findViewById(R.id.quote)).setText(quote);

        Bundle bundle = getIntent().getExtras();
        courseId = bundle.get("courseID").toString();
        courseName = bundle.get("courseName").toString();

        getCourseResources(UserData.client, "");
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
                            curFolder.add(courseRes.getResourceFileName());
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

    private void getCourseResources(final AsyncHttpClient client, final String folder) {
        client.get(UserData.domain + "/access/content/group/" + courseId + "/" + folder, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Elements tRows = doc.select("body > div > table > tbody > tr");
                final ArrayList<CourseResource> courseResourceList = new ArrayList<>();
                if (tRows.isEmpty()) {
                    if (folder.equals("")) {
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
                    try {
                        String lastRes = tRows.last().select("td:nth-child(1) > a").first().text().trim();
                        for (Element row : tRows) {
                            String resName = row.select("td:nth-child(1) > a").first().text().trim();
                            String resUrl = row.select("td:nth-child(1) > a").first().attr("href").trim();
                            String resType;

                            if (!(resUrl.equals("../"))) {
                                if (resUrl.substring(resUrl.length() - 1).equals("/")) {
                                    resType = "Folder";
                                } else {
                                    resType = resUrl.substring(resUrl.lastIndexOf('.') + 1);
                                }
                                resUrl = folder + resUrl;
                                courseResourceList.add(new CourseResource(resName, resUrl, resType));
                                if (resName.equals(lastRes)) {
                                    courseResourceStack.add(courseResourceList);
                                    CourseResAdapter courseResAdapter = new CourseResAdapter(CourseResourcesActivity.this, courseResourceList);
                                    list.setAdapter(courseResAdapter);
                                    if (firstEntry) {
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                                        list.setVisibility(View.VISIBLE);
                                        firstEntry = false;
                                    } else {
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(CourseResourcesActivity.this, getString(R.string.site_change), Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        if (courseResourceStack.size() == 1) {
            finish();
        } else {
            int index = courseResourceStack.size() - 1;
            CourseResAdapter courseResAdapter = new CourseResAdapter(CourseResourcesActivity.this, courseResourceStack.get(index - 1));
            list.setAdapter(courseResAdapter);
            courseResourceStack.remove(index);

            setTitle(curFolder.get(index - 1));
            curFolder.remove(index);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (courseResourceStack.size() > 1) {
                    onBackPressed();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
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

    private void getResource(final AsyncHttpClient client, final String ResourceCode) {
        boolean alreadyExists = false;
        final String resourceFolderPath;
        final File resourceFolders;
        File resourceFile = null;

        if (ResourceCode.lastIndexOf("/") == -1) {
            resourceFolderPath = "";
        } else {
            resourceFolderPath = ResourceCode.substring(0, ResourceCode.lastIndexOf("/"));
        }

        if (resourceFolderPath.equals("")) {
            resourceFolders = new File(getExternalFilesDir(null), ".AmritaRepoCache/CourseResources/" + courseName);
        } else {
            resourceFolders = new File(getExternalFilesDir(null), ".AmritaRepoCache/CourseResources/" + courseName + "/" + resourceFolderPath);
        }

        if (resourceFolders.exists()) {
            resourceFile = new File(resourceFolders, ResourceCode.substring(ResourceCode.lastIndexOf("/") + 1));
            if (resourceFile.exists()) {
                alreadyExists = true;
            }
        } else {
            resourceFolders.mkdirs();
            Log.e("AUMS Course Resources/" + courseName + "/" + resourceFolderPath, "Directory Created.");
        }

        if (alreadyExists) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = FileProvider.getUriForFile(CourseResourcesActivity.this, BuildConfig.APPLICATION_ID + ".provider", resourceFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setData(data);
                Intent fileChooserIntent = Intent.createChooser(intent, "Open " + ResourceCode.substring(ResourceCode.lastIndexOf("/") + 1) + " with:");
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
            client.get(UserData.domain + "/access/content/group/" + courseId + "/" + ResourceCode, new AsyncHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Utils.showUnexpectedError(CourseResourcesActivity.this);
                    progressDialog.dismiss();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    new saveCourseResource(resourceFolders, ResourceCode.substring(ResourceCode.lastIndexOf("/") + 1)).execute(responseBody);
                }
            });
        }
    }

    class saveCourseResource extends AsyncTask<byte[], String, String> {

        private File resourceFolders;
        private String ResourceName;

        saveCourseResource(File resourceFolders, String ResName) {
            this.resourceFolders = resourceFolders;
            this.ResourceName = ResName;
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
                File resourceFile = new File(resourceFolders, ResourceName);

                if (resourceFile.exists()) {
                    resourceFile.delete();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(resourceFile.getPath());
                    fos.write(file[0]);
                    fos.close();
                    Log.e("AUMS Course Resource: " + ResourceName, "Saved");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = FileProvider.getUriForFile(CourseResourcesActivity.this, BuildConfig.APPLICATION_ID + ".provider", resourceFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(data);

                    Intent fileChooserIntent = Intent.createChooser(intent, "Open " + ResourceName + " with:");

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

    public class CourseResource {

        private String resourceUrl;
        private String resourceFileName;
        private String type;

        public CourseResource(String resourceFileName, String resourceUrl, String type) {
            this.resourceFileName = resourceFileName;
            this.resourceUrl = resourceUrl;
            this.type = type;
        }


        public String getResourceUrl() {
            return resourceUrl;
        }

        public void setResourceUrl(String resourceUrl) {
            this.resourceUrl = resourceUrl;
        }

        public String getResourceFileName() {
            return resourceFileName;
        }

        public void setResourceFileName(String resourceFileName) {
            this.resourceFileName = resourceFileName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class CourseResAdapter extends ArrayAdapter<CourseResource> {
        private final Random random;
        private final Context context;

        public CourseResAdapter(Context context, ArrayList<CourseResource> Resources) {
            super(context, 0, Resources);
            this.context = context;
            random = new Random();
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if (listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.home_item, parent, false);
            }


            final CourseResource current = getItem(position);
            String resType = current.getType();
            String[] web = {"html", "htm", "mhtml"};
            String[] computer = {"exe", "dmg", "iso", "msi"};
            String[] document = {"doc", "docx", "rtf", "odt"};
            String[] pdf = {"pdf"};
            String[] powerpoint = {"ppt", "pps", "pptx"};
            String[] excel = {"xls", "xlsx", "ods"};
            String[] image = {"png", "gif", "jpg", "jpeg", "bmp"};
            String[] video = {"mp4", "mp3", "avi", "mov", "mpg", "mkv", "wmv"};
            String[] compressed = {"rar", "zip", "zipx", "tar", "7z", "gz"};


            int[] mMaterial_Colors = getContext().getResources().getIntArray(R.array.colors);
            TextView title = listItemView.findViewById(R.id.title);
            ImageView imageView = listItemView.findViewById(R.id.image);
            ImageView toRight = listItemView.findViewById(R.id.right);
            Icon icon;
            int colorVal = random.nextInt(mMaterial_Colors.length);

            if (isExtension(web, resType)) {
                icon = FontAwesomeIcons.fa_file_code_o;
                colorVal = 0;
            } else if (resType.equals("Folder")) {
                icon = FontAwesomeIcons.fa_folder_open;
            } else if (isExtension(computer, resType)) {
                icon = FontAwesomeIcons.fa_file_code_o;
                colorVal = 7;
            } else if (isExtension(document, resType)) {
                icon = FontAwesomeIcons.fa_file_word_o;
                colorVal = 6;
            } else if (isExtension(pdf, resType)) {
                icon = FontAwesomeIcons.fa_file_pdf_o;
                colorVal = 1;
            } else if (isExtension(powerpoint, resType)) {
                icon = FontAwesomeIcons.fa_file_powerpoint_o;
                colorVal = 2;
            } else if (isExtension(excel, resType)) {
                icon = FontAwesomeIcons.fa_file_excel_o;
                colorVal = 4;
            } else if (isExtension(image, resType)) {
                icon = FontAwesomeIcons.fa_file_image_o;
            } else if (isExtension(video, resType)) {
                icon = FontAwesomeIcons.fa_file_video_o;
            } else if (isExtension(compressed, resType)) {
                icon = FontAwesomeIcons.fa_file_zip_o;
                colorVal = 3;
            } else {
                icon = FontAwesomeIcons.fa_file_text;
            }
            title.setText(current.getResourceFileName());
            title.setTextSize(18);
            imageView.setImageDrawable(new IconDrawable(context, icon)
                    .color(mMaterial_Colors[colorVal]));

            if (!(current.getType().equals("Folder"))) {
                toRight.setVisibility(GONE);
            }
            return listItemView;
        }

        private boolean isExtension(String[] arr, String targetValue) {
            for (String s : arr) {
                if (s.equals(targetValue))
                    return true;
            }
            return false;
        }
    }
}

