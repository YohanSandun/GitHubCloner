package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import lk.ysk.githubcloner.DetailedRepository;
import lk.ysk.githubcloner.MenuHelper;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.User;
import lk.ysk.githubcloner.adapters.RepositoryAdapter;
import lk.ysk.githubcloner.adapters.UsersAdapter;

public class SearchActivity extends AppCompatActivity {

    private String type, term;
    private RepositoryAdapter repositoryAdapter;
    private List<DetailedRepository> repositoryList;
    private UsersAdapter usersAdapter;
    private List<User> userList;

    private int page = 1;
    private boolean noMoreResults;

    private ProgressBar pbLoading;
    private TextView txtThatsAll, txtTotalResults;
    private NestedScrollView nestedScrollView;
    private RelativeLayout rlLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView imgOctocat = findViewById(R.id.imgOctocat);
        Glide.with(this).load(R.drawable.octocat_black).into(imgOctocat);

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        term = intent.getStringExtra("term");

        pbLoading = findViewById(R.id.pbLoading);
        txtThatsAll = findViewById(R.id.txtThatsAll);
        nestedScrollView = findViewById(R.id.nestedScroll);
        rlLoading = findViewById(R.id.rlLoading);
        txtTotalResults = findViewById(R.id.txtTotal);

        RecyclerView rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setItemAnimator(new DefaultItemAnimator());

        if (type.equals("repo")) {
            repositoryList = new ArrayList<>();
            repositoryAdapter = new RepositoryAdapter(repositoryList, repo -> {
                Intent repoIntent = new Intent(SearchActivity.this, RepoActivity.class);
                repoIntent.putExtra("url", repo.getUrl());
                startActivity(repoIntent);
            });
            repositoryAdapter.setNeedOwnerName(true);

            rvResults.setAdapter(repositoryAdapter);
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (noMoreResults) {
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
        else if (type.equals("user")) {
            userList = new ArrayList<>();
            usersAdapter = new UsersAdapter(userList, user -> {
                Intent userIntent = new Intent(SearchActivity.this, UserActivity.class);
                userIntent.putExtra("user", user.getLogin());
                startActivity(userIntent);
            });

            rvResults.setAdapter(usersAdapter);
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (noMoreResults) {
                    pbLoading.setVisibility(View.GONE);
                    return;
                }
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    pbLoading.setVisibility(View.VISIBLE);
                    loadUsers();
                }
            });
            loadUsers();
        }

        findViewById(R.id.btnSettings).setOnClickListener(view ->
                MenuHelper.showSettingsMenu(this, view, activityLauncher)
        );

        findViewById(R.id.btnBack).setOnClickListener(view -> onBackPressed());

    }

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { });

    private void loadRepositories() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlRepos = "https://api.github.com/search/repositories?q="+term+"&per_page=30&page=" + page;

        JsonObjectRequest reposRequest = new JsonObjectRequest(urlRepos, response -> {
            try {
                txtTotalResults.setText(String.format("About %d results", response.getLong("total_count")));
                JSONArray results = response.getJSONArray("items");
                int i;
                for (i = 0; i < results.length(); i++)
                    repositoryList.add(new DetailedRepository(results.getJSONObject(i)));

                if (i == 0 || (page == 1 && i < 30)){
                    noMoreResults = true;
                    txtThatsAll.setVisibility(View.VISIBLE);
                }
                repositoryAdapter.notifyDataSetChanged();
                pbLoading.setVisibility(View.GONE);
                rlLoading.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
            } catch (Exception ignore) {
                Toast.makeText(SearchActivity.this, getString(R.string.error_no_data), Toast.LENGTH_LONG).show();
                finish();
            }
        }, error -> {
            Toast.makeText(SearchActivity.this, getString(R.string.error_no_data), Toast.LENGTH_LONG).show();
            finish();
        });

        queue.add(reposRequest);
    }

    private void loadUsers() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlRepos = "https://api.github.com/search/users?q="+term+"&per_page=30&page=" + page;

        JsonObjectRequest reposRequest = new JsonObjectRequest(urlRepos, response -> {
            try {
                txtTotalResults.setText(String.format("About %d results", response.getLong("total_count")));
                JSONArray results = response.getJSONArray("items");
                int i;
                for (i = 0; i < results.length(); i++)
                    userList.add(new User(results.getJSONObject(i)));

                if (i == 0 || (page == 1 && i < 30)){
                    noMoreResults = true;
                    txtThatsAll.setVisibility(View.VISIBLE);
                }
                usersAdapter.notifyDataSetChanged();
                pbLoading.setVisibility(View.GONE);
                rlLoading.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
            } catch (Exception ignore) {
                Toast.makeText(SearchActivity.this, getString(R.string.error_no_data), Toast.LENGTH_LONG).show();
                finish();
            }
        }, error -> {
            Toast.makeText(SearchActivity.this, getString(R.string.error_no_data), Toast.LENGTH_LONG).show();
            finish();
        });

        queue.add(reposRequest);
    }
}