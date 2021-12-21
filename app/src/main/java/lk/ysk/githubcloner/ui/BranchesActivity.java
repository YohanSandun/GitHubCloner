package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

import lk.ysk.githubcloner.MenuHelper;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.adapters.BranchesAdapter;

public class BranchesActivity extends AppCompatActivity {

    private int page = 1;
    private String branchesUrl;
    private List<String> branches;
    private boolean noMoreBranches;
    private TextView txtThatsAll;
    private BranchesAdapter branchesAdapter;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);

        branches = new ArrayList<>();
        branchesAdapter = new BranchesAdapter(branches, branch -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("branch", branch);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });

        Intent intent = getIntent();
        branchesUrl = intent.getStringExtra("url");

        txtThatsAll = findViewById(R.id.txtThatsAll);
        pbLoading = findViewById(R.id.pbLoading);
        RecyclerView rvBranches = findViewById(R.id.rvBranches);
        rvBranches.setLayoutManager(new LinearLayoutManager(BranchesActivity.this));
        rvBranches.setItemAnimator(new DefaultItemAnimator());
        rvBranches.setAdapter(branchesAdapter);

        NestedScrollView nestedScrollView = findViewById(R.id.nestedScroll);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (noMoreBranches) {
                pbLoading.setVisibility(View.GONE);
                return;
            }
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                page++;
                pbLoading.setVisibility(View.VISIBLE);
                loadBranches();
            }
        });

        loadBranches();
    }

    private void loadBranches() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String urlRepos = branchesUrl + "?per_page=30&page=" + page;
        Log.d("YOHAN", "loadBranches: " + urlRepos);
        JsonArrayRequest reposRequest = new JsonArrayRequest(urlRepos, response -> {
            int i;
            for (i = 0; i < response.length(); i++) {
                try {
                    branches.add(response.getJSONObject(i).getString("name"));
                } catch (Exception ignore) {
                    Toast.makeText(BranchesActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            if (i == 0 || (page == 1 && i < 30)){
                noMoreBranches = true;
                txtThatsAll.setVisibility(View.VISIBLE);
            }
            branchesAdapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.GONE);
        }, error -> {
            Toast.makeText(BranchesActivity.this, "Error occurred while trying to fetch information!", Toast.LENGTH_LONG).show();
            finish();
        });

        queue.add(reposRequest);

        findViewById(R.id.btnSettings).setOnClickListener(view ->
                MenuHelper.showSettingsMenu(this, view, activityLauncher)
        );

        findViewById(R.id.btnBack).setOnClickListener(view -> onBackPressed());
    }

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { });
}