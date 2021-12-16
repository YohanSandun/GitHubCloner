package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONObject;

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

import de.hdodenhof.circleimageview.CircleImageView;
import lk.ysk.githubcloner.Content;
import lk.ysk.githubcloner.ContentFile;
import lk.ysk.githubcloner.FavouriteItem;
import lk.ysk.githubcloner.adapters.ContentsAdapter;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.adapters.RepositoryAdapter;
import lk.ysk.githubcloner.DetailedRepository;
import lk.ysk.githubcloner.ui.widgets.SegmentProgressBar;

import static androidx.core.content.FileProvider.getUriForFile;

public class RepoActivity extends AppCompatActivity {

    private DetailedRepository detailedRepository;
    private FlexboxLayout languageContainer;
    private SegmentProgressBar pbLanguages;

    private File githubDir, cloneDir, tempDir;
    private Dialog dlgCloning;
    private TextView txtFileName, txtClone, txtTitle, txtPercentage, txtBranch;
    private ProgressBar pbDownload, pbLoading;

    private final List<ContentFile> filesToDownload = new ArrayList<>();
    private RequestQueue contentQueue;
    private int requestCount = 0;
    private int currentIndex = 0;
    private String currentUrl;

    private RecyclerView rvContents;
    private ContentsAdapter contentsAdapter;
    private List<Content> contentList;
    private String branch;

    private ActivityResultLauncher<Intent> branchesActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code
                        Intent data = result.getData();
                        String newBranch = data.getStringExtra("branch");
                        if (!branch.equals(newBranch)) {
                            branch = newBranch;
                            if (newBranch.length() > 20)
                                txtBranch.setText(getString(R.string.default_branch) + ' ' + newBranch.substring(0,19)+"...");
                            else
                                txtBranch.setText(getString(R.string.default_branch) + ' ' + newBranch);
                            requestCount = 0;
                            currentIndex = 0;
                            contentList.clear();
                            filesToDownload.clear();
                            loadContents(detailedRepository.getContentsUrl().substring(0, detailedRepository.getContentsUrl().lastIndexOf('/')), false);
                        }
                    }
                }
            });

    private boolean isFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo);

        githubDir = new File(Environment.getExternalStorageDirectory(), "GitHub");
        githubDir.mkdirs();
        tempDir = new File(getFilesDir(), ".temp");

        contentList = new ArrayList<>();

        Intent intent = getIntent();
        String repoUrl = intent.getStringExtra("url");

        txtTitle = findViewById(R.id.appBarText);
        languageContainer = findViewById(R.id.gridLanguages);
        pbLanguages = findViewById(R.id.segmentPb);
        rvContents = findViewById(R.id.rvContent);
        pbLoading = findViewById(R.id.pbLoading);
        txtBranch = findViewById(R.id.txtBranch);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtUpdated = findViewById(R.id.txtUpdated);
        TextView txtStars = findViewById(R.id.txtStars);
        TextView txtWatches = findViewById(R.id.txtWatches);
        TextView txtForks = findViewById(R.id.txtForks);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtOwner = findViewById(R.id.txtOwner);
        CircleImageView imgAvatar = findViewById(R.id.imgAvatar);
        ImageView imgFavourite = findViewById(R.id.btnAddToFav);

        imgFavourite.setOnClickListener(v -> {
            if (!isFavourite) {
                MainActivity.favourites.addFavourite(new FavouriteItem(detailedRepository));
                MainActivity.saveFavourites();
                isFavourite = true;
                imgFavourite.setImageResource(R.drawable.ic_star_filled);
            } else {
                MainActivity.favourites.removeFavourite(detailedRepository.getName(), detailedRepository.getOwner());
                MainActivity.saveFavourites();
                isFavourite = false;
                imgFavourite.setImageResource(R.drawable.ic_star_outline);
            }
        });

        findViewById(R.id.llOwner).setOnClickListener(v -> {
            Intent userIntent = new Intent(RepoActivity.this, UserActivity.class);
            userIntent.putExtra("user", detailedRepository.getOwner());
            startActivity(userIntent);
            finish();
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest repoRequest = new JsonObjectRequest(repoUrl, response -> {
            try {
                detailedRepository = new DetailedRepository(response);
                txtOwner.setText(detailedRepository.getOwner());
                Glide.with(this).load(detailedRepository.getAvatarUrl()).into(imgAvatar);
                branch = detailedRepository.getDefaultBranch();
                if (detailedRepository.getName().length() > 15)
                    txtTitle.setText(detailedRepository.getName().substring(0, 14)+"...");
                else
                    txtTitle.setText(detailedRepository.getName());
                txtName.setText(detailedRepository.getName());
                txtDescription.setText(detailedRepository.getDescription());
                txtUpdated.setText("Last Update " + detailedRepository.getUpdated());
                txtStars.setText(detailedRepository.getStarsString());
                txtWatches.setText(detailedRepository.getWatchesString());
                txtForks.setText(detailedRepository.getForksString());
                if (detailedRepository.getDefaultBranch().length() > 20)
                    txtBranch.setText(getString(R.string.default_branch) + ' ' + detailedRepository.getDefaultBranch().substring(0, 19) + "...");
                else
                    txtBranch.setText(getString(R.string.default_branch) + ' ' + detailedRepository.getDefaultBranch());

                if (MainActivity.favourites.isAvailable(detailedRepository.getName(), detailedRepository.getOwner())) {
                    isFavourite = true;
                    imgFavourite.setImageResource(R.drawable.ic_star_filled);
                }

                loadLanguages();
                loadContents(detailedRepository.getContentsUrl().substring(0, detailedRepository.getContentsUrl().lastIndexOf('/')), false);
            } catch (Exception ignore) {
                Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                finish();
            }
        }, error -> {
            Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });
        queue.add(repoRequest);

        dlgCloning = new Dialog(this);
        dlgCloning.setContentView(R.layout.dialog_clone);
        dlgCloning.setCancelable(false);
        txtFileName = dlgCloning.findViewById(R.id.txtFileName);
        txtClone = dlgCloning.findViewById(R.id.txtCloning);
        pbDownload = dlgCloning.findViewById(R.id.filePb);
        txtPercentage = dlgCloning.findViewById(R.id.txtFilePercent);

        contentQueue = Volley.newRequestQueue(this);

        findViewById(R.id.btnDownloadZip).setOnClickListener(view -> cloneAsArchive(repoUrl + "/zipball/"+branch, ".zip"));
        findViewById(R.id.btnDownloadTar).setOnClickListener(view -> cloneAsArchive(repoUrl + "/tarball/"+branch, ".tar.gz"));

        findViewById(R.id.btnDownloadSelected).setOnClickListener(view -> {
            filesToDownload.clear();
            currentIndex = 0;
            requestCount = 0;

            cloneDir = new File(githubDir, detailedRepository.getName());
            cloneDir.mkdirs();

            dlgCloning.show();
            txtClone.setText("Reading Meta-data");

            boolean dirFound = false;
            int count = 0;
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i).isSelected()) {
                    count++;
                    if (contentList.get(i).getType() == Content.Type.FILE)
                        filesToDownload.add(new ContentFile(contentList.get(i).getName(), new File(cloneDir, contentList.get(i).getName()).toString(), contentList.get(i).getDownloadUrl(), contentList.get(i).getSize()));
                    else if (contentList.get(i).getType() == Content.Type.DIR) {
                        dirFound = true;
                        cloneRepo(contentList.get(i).getUrl(), new File(cloneDir, contentList.get(i).getName()));
                    }
                }
            }

            if (!dirFound && count > 0) {
                pbDownload.setMax(filesToDownload.size());
                new DownloadFileFromURL(filesToDownload.get(currentIndex).getFileName()).execute(filesToDownload.get(currentIndex).getUrl() + "?ref=" + branch, filesToDownload.get(currentIndex).getLocalFile());
            }

        });

        rvContents.setLayoutManager(new LinearLayoutManager(this));
        rvContents.setItemAnimator(new DefaultItemAnimator());
        contentsAdapter = new ContentsAdapter(contentList, content -> {
            if (content.getType() == Content.Type.GO_BACK) {
                goBack();
            } else if (content.getType() == Content.Type.DIR) {
                contentList.clear();
                contentsAdapter.notifyDataSetChanged();
                loadContents(content.getUrl(), true);
            } else {
                if (tempDir.exists())
                    tempDir.delete();
                tempDir.mkdirs();
                File file = new File(tempDir, content.getName());
                new DownloadArchive(content.getName(), true).execute(content.getDownloadUrl(), file.toString());
            }
        });
        rvContents.setAdapter(contentsAdapter);

        findViewById(R.id.btnChange).setOnClickListener(v -> {
            Intent branchesIntent = new Intent(RepoActivity.this, BranchesActivity.class);
            branchesIntent.putExtra("url", detailedRepository.getBranchesUrl());
            branchesActivityLauncher.launch(branchesIntent);
        });

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, detailedRepository.getGithubUrl());
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });

        findViewById(R.id.btnViewOnGitHub).setOnClickListener(v -> {
            Intent webInt = new Intent(Intent.ACTION_VIEW, Uri.parse(detailedRepository.getGithubUrl()));
            startActivity(webInt);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void goBack() {
        contentList.clear();
        String url = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
        if (url.equals(detailedRepository.getContentsUrl().substring(0, detailedRepository.getContentsUrl().lastIndexOf('/'))))
            loadContents(url, false);
        else
            loadContents(url, true);
    }

    @Override
    public void onBackPressed() {
        if (contentList.size() > 0 && contentList.get(0).getType() == Content.Type.GO_BACK) {
            goBack();
            return;
        }
        super.onBackPressed();
    }

    private void loadContents(String url, boolean goBackAvailable) {
        currentUrl = url;

        rvContents.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);

        contentList.clear();
        if (goBackAvailable)
            contentList.add(new Content("..", Content.Type.GO_BACK, "", "", 0));

        if (url.contains("?ref"))
            url = url.substring(0, url.lastIndexOf("?ref"));

        JsonArrayRequest reposRequest = new JsonArrayRequest(url + "?ref=" + branch, response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject file = response.getJSONObject(i);
                    String fname = file.getString("name");
                    Content.Type type = file.getString("type").equals("dir") ? Content.Type.DIR : Content.Type.FILE;
                    contentList.add(new Content(fname, type, file.getString("url"), file.getString("download_url"), file.getLong("size")));
                }
            } catch (Exception ignore) {

                Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                finish();
            }
            contentsAdapter.notifyDataSetChanged();
            rvContents.setVisibility(View.VISIBLE);
            pbLoading.setVisibility(View.GONE);
        }, error -> {

            Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });
        contentQueue.add(reposRequest);
    }

    private void cloneAsArchive(String url, String extension) {
        String fname = detailedRepository.getName() + "-" + branch + extension;
        File file = new File(githubDir, fname);

        if (file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RepoActivity.this);

            builder.setTitle("Confirm");
            builder.setMessage("This file is already exists! Do you want to overwrite it?");

            builder.setPositiveButton("YES", (dialog, which) -> {
                file.delete();
                new DownloadArchive(fname, false).execute(url, file.toString());
                dialog.dismiss();
            });

            builder.setNegativeButton("NO", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else
            new DownloadArchive(fname, false).execute(url, file.toString());
    }

    private void loadLanguages() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest langsRequest = new JsonObjectRequest(detailedRepository.getLanguagesUrl(), response -> {
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
                    pbLanguages.setVisibility(View.VISIBLE);
                    languageContainer.setVisibility(View.VISIBLE);

                    TextView txtLang = new TextView(RepoActivity.this);
                    txtLang.setTypeface(txtLang.getTypeface(), Typeface.BOLD);
                    txtLang.setPadding(10, 5, 10, 5);
                    txtLang.setLayoutParams(lp);
                    float percentage = (float) ((double) language.second / total);
                    txtLang.setText(String.format("%s %.1f%%", language.first, percentage * 100f));
                    int color = MainActivity.languageColors.getColor(language.first);
                    if (color != Color.TRANSPARENT) {
                        Drawable drawable = RepoActivity.this.getDrawable(R.drawable.white_square);
                        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                        txtLang.setBackground(drawable);
                        txtLang.setTextColor(RepositoryAdapter.isBrightColor(color) ? Color.BLACK : Color.WHITE);
                        pbLanguages.addSegment(color, percentage);
                    } else {
                        Drawable drawable = RepoActivity.this.getDrawable(R.drawable.orange_square);
                        txtLang.setBackground(drawable);
                        pbLanguages.addSegment(Color.LTGRAY, percentage);
                    }
                    languageContainer.addView(txtLang);
                }
                }
            } catch (Exception ignore) {
                Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                finish();
            }
        }, error -> {
            Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });
        queue.add(langsRequest);
    }

    private void cloneRepo(String url, File dir) {
        if (!dir.exists())
            dir.mkdirs();

        JsonArrayRequest reposRequest = new JsonArrayRequest(url + "?ref=" + branch, response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject file = response.getJSONObject(i);
                    String fname = file.getString("name");
                    String type = file.getString("type");
                    txtFileName.setText(fname);

                    if (type.equals("file")) {
                        String downloadUrl = file.getString("download_url");
                        File localFile = new File(dir, fname);
                        if (localFile.exists())
                            localFile.delete();
                        filesToDownload.add(new ContentFile(fname, localFile.toString(), downloadUrl, file.getLong("size")));
                    } else {
                        File folder = new File(dir, fname);
                        folder.mkdirs();
                        cloneRepo(file.getString("url"), folder);
                    }

                }
            } catch (Exception error) {
                Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                finish();
            }
            requestCount--;
            if (requestCount == 0) {
                txtClone.setText("Cloning...");
                try {
                    pbDownload.setMax(filesToDownload.size());
                    new DownloadFileFromURL(filesToDownload.get(currentIndex).getFileName()).execute(filesToDownload.get(currentIndex).getUrl() + "?ref=" + branch, filesToDownload.get(currentIndex).getLocalFile());
                } catch (Exception e) {
                    Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, error -> {
            Toast.makeText(RepoActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });
        requestCount++;
        contentQueue.add(reposRequest);
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private final String fileName;

        public DownloadFileFromURL(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtFileName.setText(fileName);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(f_url[1]);

                byte data[] = new byte[1024];

                while ((count = input.read(data)) != -1)
                    output.write(data, 0, count);

                output.flush();
                output.close();
                input.close();

            } catch (Exception ignore) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            currentIndex++;
            pbDownload.setProgress(currentIndex);
            txtPercentage.setText(String.format("%.1f%%", (currentIndex/(float)pbDownload.getMax())*100f));
            if (currentIndex < filesToDownload.size())
                new DownloadFileFromURL(filesToDownload.get(currentIndex).getFileName()).execute(filesToDownload.get(currentIndex).getUrl(), filesToDownload.get(currentIndex).getLocalFile());
            else {
                dlgCloning.dismiss();
                Toast.makeText(RepoActivity.this, "Successfully downloaded to " + cloneDir, Toast.LENGTH_LONG).show();
            }
        }

    }

    class DownloadArchive extends AsyncTask<String, String, String> {

        private final String fileName;
        private final boolean tempFile;

        public DownloadArchive(String fileName, boolean tempFile) {
            this.fileName = fileName;
            this.tempFile = tempFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlgCloning.show();
            txtFileName.setText(fileName);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                OutputStream output = new FileOutputStream(f_url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;

                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception ignore) {

            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pbDownload.setProgress(Integer.parseInt(progress[0]));
            txtPercentage.setText(String.format("%d%%", pbDownload.getProgress()));
        }

        @Override
        protected void onPostExecute(String file_url) {
            pbDownload.setProgress(100);
            txtPercentage.setText("100%");
            dlgCloning.dismiss();

            if (tempFile) {
                File file = new File(tempDir, fileName);
                String type = "text/*";
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".ico") || fileName.endsWith(".jpeg") || fileName.endsWith(".bmp") || fileName.endsWith(".gif") || fileName.endsWith(".tif") || fileName.endsWith(".webp"))
                    type = "image/*";
                else if (fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".avi"))
                    type = "video/";
                else if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
                    type = "text/html";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(getUriForFile(RepoActivity.this, "lk.ysk.githubcloner.fileprovider", file), type);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(intent);
            } else
                Toast.makeText(RepoActivity.this, "Successfully downloaded to " + githubDir, Toast.LENGTH_LONG).show();
        }
    }
}