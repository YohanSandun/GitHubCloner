package lk.ysk.githubcloner.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lk.ysk.githubcloner.LanguageColors;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.Repository;
import lk.ysk.githubcloner.adapters.RepositoryAdapter;

public class UserActivity extends AppCompatActivity {

    public static LanguageColors languageColors;

    private List<Repository> repositoriesList;
    private int page = 1;
    private String userName;
    private RepositoryAdapter repositoryAdapter;
    private boolean noMoreRepos;

    private ProgressBar pbLoading;
    private TextView txtNoMoreRepos;
    private RelativeLayout rlProfile, rlLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        userName = intent.getStringExtra("user");

        rlLoading = findViewById(R.id.rlLoading);
        rlProfile = findViewById(R.id.profileContainer);
        pbLoading = findViewById(R.id.pbLoading);
        txtNoMoreRepos = findViewById(R.id.txtThatsAll);
        TextView txtTitle = findViewById(R.id.appBarText);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtBio = findViewById(R.id.txtBio);
        TextView txtLocation = findViewById(R.id.txtLocation);
        TextView txtFollowers = findViewById(R.id.txtFollowers);
        TextView txtFollowing = findViewById(R.id.txtFollowing);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TextView txtBlog = findViewById(R.id.txtBlog);
        TextView txtCompany = findViewById(R.id.txtCompany);
        TextView txtTwitter = findViewById(R.id.txtTwitter);
        TextView txtRepos = findViewById(R.id.txtRepos);
        TextView txtCreated = findViewById(R.id.txtCreated);
        TextView txtGists = findViewById(R.id.txtGists);
        TextView txtAdmin = findViewById(R.id.txtAdmin);
        CircleImageView imgAvatar = findViewById(R.id.imgAvatar);

        RecyclerView lstRepos = findViewById(R.id.recyclerRepositories);
        lstRepos.setLayoutManager(new LinearLayoutManager(this));
        lstRepos.setItemAnimator(new DefaultItemAnimator());

        // Octocat GIF
        ImageView imgOctocat = findViewById(R.id.imgOctocat);
        Glide.with(this).load(getDrawable(R.drawable.octocat_black)).into(imgOctocat);

        repositoriesList = new ArrayList<>();
        languageColors = new LanguageColors(loadJSONFromAsset());
        repositoryAdapter = new RepositoryAdapter(this, repositoriesList, languageColors, repo -> {
            Intent repoIntent = new Intent(UserActivity.this, RepoActivity.class);
            repoIntent.putExtra("url", repo.getUrl());
            startActivity(repoIntent);
        });
        lstRepos.setAdapter(repositoryAdapter);

        String urlUser = "https://api.github.com/users/" + userName;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest userRequest = new JsonObjectRequest(urlUser, response -> {
            try {
                Glide.with(UserActivity.this).load(response.getString("avatar_url")).into(imgAvatar);
                txtTitle.setText(response.getString("login").trim());
                txtName.setText(response.getString("name").trim());
                txtBio.setText(response.getString("bio").trim().replace("\r\n"," ").replace("\n", " "));
                txtLocation.setText(response.getString("location").trim());
                txtFollowers.setText(String.format("%d followers", response.getInt("followers")));
                txtFollowing.setText(String.format("· %d following", response.getInt("following")));
                txtRepos.setText(String.format("%d Repos", response.getInt("public_repos")));
                txtGists.setText(String.format("%d Gists", response.getInt("public_gists")));
                txtCreated.setText(String.format("Member since %s", response.getString("created_at").substring(0, 10)));

                String blog = response.getString("blog").trim();
                if (!blog.equals("null")&& !blog.equals(""))
                    txtBlog.setText(blog);
                else
                    txtBlog.setVisibility(View.GONE);

                String email = response.getString("email").trim();
                if (!email.equals("null")&& !email.equals(""))
                    txtEmail.setText(email);
                else
                    txtEmail.setVisibility(View.GONE);

                String company = response.getString("company").trim();
                if (!company.equals("null")&& !company.equals(""))
                    txtCompany.setText(company);
                else
                    txtCompany.setVisibility(View.GONE);

                String twitter = response.getString("twitter_username").trim();
                if (!twitter.equals("null") && !twitter.equals(""))
                    txtTwitter.setText(twitter);
                else
                    txtTwitter.setVisibility(View.GONE);

                if (response.getBoolean("site_admin"))
                    txtAdmin.setVisibility(View.VISIBLE);

            } catch (Exception ignore) {
                Toast.makeText(UserActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                finish();
            }
        }, error -> {
            Toast.makeText(UserActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });
        queue.add(userRequest);

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScroll);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (noMoreRepos) {
                pbLoading.setVisibility(View.GONE);
                return;
            }
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                page++;
                pbLoading.setVisibility(View.VISIBLE);
                loadRepositories();
            }
        });

        loadRepositories();
    }

    private void loadRepositories() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlRepos = "https://api.github.com/users/" + userName + "/repos?per_page=30&page=" + page;

        JsonArrayRequest reposRequest = new JsonArrayRequest(urlRepos, response -> {
            int i = 0;
            for (i = 0; i < response.length(); i++) {
                try {
                    repositoriesList.add(new Repository(response.getJSONObject(i)));
                } catch (Exception ignore) {
                    Toast.makeText(UserActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            if (i == 0 || (page == 1 && i < 30)){
                noMoreRepos = true;
                txtNoMoreRepos.setVisibility(View.VISIBLE);
            }
            repositoryAdapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.GONE);
            rlLoading.setVisibility(View.GONE);
            rlProfile.setVisibility(View.VISIBLE);
        }, error -> {
            Toast.makeText(UserActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });

        queue.add(reposRequest);
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("colors.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ignore) {
            return null;
        }
        return json;
    }
}