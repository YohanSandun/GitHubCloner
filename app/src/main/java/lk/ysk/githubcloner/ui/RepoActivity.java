package lk.ysk.githubcloner.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lk.ysk.githubcloner.Content;
import lk.ysk.githubcloner.ContentFile;
import lk.ysk.githubcloner.ContentsAdapter;
import lk.ysk.githubcloner.OnContentClickedListener;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.RepoModel;
import lk.ysk.githubcloner.ReposAdapter;
import lk.ysk.githubcloner.Repository;
import lk.ysk.githubcloner.ui.widgets.SegmentProgressBar;

public class RepoActivity extends AppCompatActivity {

    private Repository repository;
    private FlexboxLayout gridLayout;
    private SegmentProgressBar segmentPb;

    private File githubDir, cloneDir;
    private Dialog dialogColning;
    private TextView txtFileName, txtClone;
    private TextView txtName, txtPercentage;
    private ProgressBar pbDownload;

    private final List<ContentFile> filesToDownload = new ArrayList<>();
    private RequestQueue queue;
    private int requestCount = 0;
    private int currentIndex = 0;

    private RecyclerView rvContents;
    private ContentsAdapter contentsAdapter;
    private List<Content> contentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo);

        contentList = new ArrayList<>();

        Intent intent = getIntent();
        String repoUrl = intent.getStringExtra("url");

        txtName = findViewById(R.id.appBarText);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtUpdated = findViewById(R.id.txtUpdated);
        TextView txtStars = findViewById(R.id.txtStars);
        TextView txtWatches = findViewById(R.id.txtWatches);
        TextView txtForks = findViewById(R.id.txtForks);
        gridLayout = findViewById(R.id.gridLanguages);
        segmentPb = findViewById(R.id.segmentPb);
        rvContents = findViewById(R.id.rvContent);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest repoRequest = new JsonObjectRequest(repoUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    repository = new Repository(response);
                    txtName.setText(repository.getName());
                    txtDescription.setText(repository.getDescription());
                    txtUpdated.setText("Last Update " + repository.getUpdated());
                    txtStars.setText(repository.getStarsString());
                    txtWatches.setText(repository.getWatchesString());
                    txtForks.setText(repository.getForksString());
                    loadLanguages();
                    loadContents(repository.getContentsUrl().substring(0, repository.getContentsUrl().lastIndexOf('/')), false);
                } catch (Exception e) {
                    Log.d("YOHAN", "onErrorResponse(User): " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse(User): " + error.getMessage());
            }
        });
        queue.add(repoRequest);

        dialogColning = new Dialog(this);
        dialogColning.setContentView(R.layout.dialog_clone);
        dialogColning.setCancelable(false);
        txtFileName = dialogColning.findViewById(R.id.txtFileName);
        txtClone = dialogColning.findViewById(R.id.txtCloning);
        pbDownload = dialogColning.findViewById(R.id.filePb);
        txtPercentage = dialogColning.findViewById(R.id.txtFilePercent);

        this.queue = Volley.newRequestQueue(this);

//        findViewById(R.id.btnClone).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                githubDir = new File(Environment.getExternalStorageDirectory(), "GitHub");
//                githubDir.mkdirs();
//                cloneDir = new File(githubDir, repository.getName());
//                cloneDir.mkdirs();
//
//                dialogColning.show();
//                txtClone.setText("Reading Meta-data");
//                cloneRepo( repository.getContentsUrl().substring(0, repository.getContentsUrl().lastIndexOf('/')), cloneDir);
//            }
//        });

        findViewById(R.id.btnDownloadZip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloneAsArchive(repoUrl + "/zipball", ".zip");
            }
        });

        findViewById(R.id.btnDownloadTar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cloneAsArchive(repoUrl + "/tarball", ".tar.gz");
            }
        });

        rvContents.setLayoutManager(new LinearLayoutManager(this));
        rvContents.setItemAnimator(new DefaultItemAnimator());
        contentsAdapter = new ContentsAdapter(contentList, new OnContentClickedListener() {
            @Override
            public void onClick(Content content) {
                if (content.getType() == Content.Type.GO_BACK) {
                    goBack(content);
                } else if (content.getType() == Content.Type.DIR) {
                    contentList.clear();
                    contentsAdapter.notifyDataSetChanged();
                    loadContents(content.getUrl(), true);
                } else {

                }
            }
        });
        rvContents.setAdapter(contentsAdapter);

    }

    private void goBack(Content content) {
        contentList.clear();
        contentsAdapter.notifyDataSetChanged();
        String url = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
        Log.d("YOHAN", "last: " + url);
        if (url.equals(repository.getContentsUrl().substring(0, repository.getContentsUrl().lastIndexOf('/'))))
            loadContents(url, false);
        else
            loadContents(url, true);
    }

    @Override
    public void onBackPressed() {
        if (contentList.size() > 0 && contentList.get(0).getType() == Content.Type.GO_BACK) {
            goBack(contentList.get(0));
            return;
        }
        super.onBackPressed();
    }

    private String currentUrl;

    private void loadContents(String url, boolean goBackAvailable) {
        currentUrl = url;

        contentList.clear();
        if (goBackAvailable)
            contentList.add(new Content("..", Content.Type.GO_BACK, "", "", 0));

        //currentUrl = url;
        JsonArrayRequest reposRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject file = response.getJSONObject(i);
                        String fname = file.getString("name");
                        Content.Type type = file.getString("type").equals("dir") ? Content.Type.DIR : Content.Type.FILE;
                        contentList.add(new Content(fname, type, file.getString("url"), file.getString("download_url"), file.getLong("size")));
                    }
                } catch (Exception error) {
                    Log.d("YOHAN", "Download Error : " + error.getMessage());
                }
                contentsAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse: " + error.getMessage());
            }
        });
        queue.add(reposRequest);
    }

    private void cloneAsArchive(String url, String extention) {
        githubDir = new File(Environment.getExternalStorageDirectory(), "GitHub");
        githubDir.mkdirs();

        //findViewById(R.id.btnDownloadZip).setEnabled(false);
        String fname = repository.getName() + "-" + repository.getDefaultBranch() + extention;
        File file = new File(githubDir, fname);
        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RepoActivity.this);

            builder.setTitle("Confirm");
            builder.setMessage("There is a file already with same name. Do you want to overwrite it?");

            builder.setPositiveButton("YES", (dialog, which) -> {
                file.delete();
                new DownloadArchive(fname).execute(url, file.toString());
                dialog.dismiss();
            });

            builder.setNegativeButton("NO", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else
            new DownloadArchive(fname).execute(url, file.toString());
    }

    private void loadLanguages() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest langsRequest = new JsonObjectRequest(repository.getLanguagesUrl(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    List<Pair<String, Long>> languages = new ArrayList<>();
                    long total = 0;
                    for (Iterator<String> it = response.keys(); it.hasNext();) {
                        String key = it.next();
                        long s = response.getLong(key);
                        total += s;
                        languages.add(new Pair<>(key, s));
                    }
                    if (languages.size() > 0) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMarginEnd(10);
                    lp.setMargins(0,10,10,0);

                    for (Pair<String, Long> language: languages) {
                        segmentPb.setVisibility(View.VISIBLE);
                        gridLayout.setVisibility(View.VISIBLE);

                        TextView txtLang = new TextView(RepoActivity.this);
                        txtLang.setTypeface(txtLang.getTypeface(), Typeface.BOLD);
                        txtLang.setPadding(10, 5, 10, 5);
                        txtLang.setLayoutParams(lp);
                        float percentage = (float) ((double) language.second / total);
                        txtLang.setText(String.format("%s %.1f%%", language.first, percentage * 100f));
                        int color = UserActivity.colors.getColor(language.first);
                        if (color != Color.TRANSPARENT) {
                            Drawable drawable = RepoActivity.this.getDrawable(R.drawable.white_square);
                            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                            txtLang.setBackground(drawable);
                            txtLang.setTextColor(ReposAdapter.isBrightColor(color) ? Color.BLACK : Color.WHITE);
                            segmentPb.addSegment(color, percentage);
                        } else {
                            Drawable drawable = RepoActivity.this.getDrawable(R.drawable.orange_square);
                            txtLang.setBackground(drawable);
                            segmentPb.addSegment(Color.LTGRAY, percentage);
                        }
                        gridLayout.addView(txtLang);
                    }
                    }
                } catch (Exception e) {
                    Log.d("YOHAN", "onErrorResponse(User): " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse(User): " + error.getMessage());
            }
        });
        queue.add(langsRequest);
    }

    private void cloneRepo(String url, File dir) {
        JsonArrayRequest reposRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject file = response.getJSONObject(i);
                        String fname = file.getString("name");
                        String type = file.getString("type");
                        txtFileName.setText(fname);

                        if (type.equals("file")) {
                            String url = file.getString("download_url");

                            File localFile = new File(dir, fname);
                            if (localFile.exists())
                                localFile.delete();
                            long size = file.getLong("size");
                            filesToDownload.add(new ContentFile(fname, localFile.toString(), url, size));
                        } else {
                            File folder = new File(dir, fname);
                            folder.mkdirs();
                            cloneRepo(file.getString("url"), folder);
                        }

                    }
                } catch (Exception error) {
                    Log.d("YOHAN", "Download Error : " + error.getMessage());
                }
                requestCount--;
                if (requestCount == 0) {
                    txtClone.setText("Cloning...");
                    try {
                        pbDownload.setMax(filesToDownload.size());
                        new DownloadFileFromURL(filesToDownload.get(currentIndex).getFileName()).execute(filesToDownload.get(currentIndex).getUrl(), filesToDownload.get(currentIndex).getLocalFile());
                    } catch (Exception e) {

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse: " + error.getMessage());
            }
        });
        requestCount++;
        queue.add(reposRequest);
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private final String fileName;

        public DownloadFileFromURL(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtFileName.setText(fileName);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    //publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //Log.d("YOHAN", "Downloaded : ");
            currentIndex++;
            pbDownload.setProgress(currentIndex);
            txtPercentage.setText(String.format("%.1f%%", (currentIndex/(float)pbDownload.getMax())*100f));
            if (currentIndex < filesToDownload.size())
                new DownloadFileFromURL(filesToDownload.get(currentIndex).getFileName()).execute(filesToDownload.get(currentIndex).getUrl(), filesToDownload.get(currentIndex).getLocalFile());
            else {
                dialogColning.dismiss();
            }
        }

    }

    class DownloadArchive extends AsyncTask<String, String, String> {

        private final String fileName;

        public DownloadArchive(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogColning.show();
            txtFileName.setText(fileName);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pbDownload.setProgress(Integer.parseInt(progress[0]));
            txtPercentage.setText(String.format("%d%%", pbDownload.getProgress()));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //Log.d("YOHAN", "Downloaded : ");
            //currentIndex++;
            pbDownload.setProgress(100);
            txtPercentage.setText("100%");
            dialogColning.dismiss();
        }

    }

}