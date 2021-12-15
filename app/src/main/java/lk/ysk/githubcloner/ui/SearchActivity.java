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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import lk.ysk.githubcloner.DetailedRepository;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.Repository;
import lk.ysk.githubcloner.adapters.RepositoryAdapter;
import lk.ysk.githubcloner.interfaces.OnRepoClickedListener;

public class SearchActivity extends AppCompatActivity {

    private String type, term;
    private RepositoryAdapter repositoryAdapter;
    private List<DetailedRepository> repositoryList;

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
            repositoryAdapter = new RepositoryAdapter(repositoryList, new OnRepoClickedListener() {
                @Override
                public void OnClick(DetailedRepository repo) {
                    Intent repoIntent = new Intent(SearchActivity.this, RepoActivity.class);
                    repoIntent.putExtra("url", repo.getUrl());
                    startActivity(repoIntent);
                }
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
    }

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

            }
        }, error -> {

        });

        queue.add(reposRequest);
    }
}