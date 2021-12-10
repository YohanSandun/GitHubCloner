package lk.ysk.githubcloner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RepositoryActivity extends AppCompatActivity {

    private List<RepoModel> repos;
    private int page = 1;
    private String user;
    private ReposAdapter adapter;
    private boolean noMoreRepos;

    private ProgressBar loadingPb;
    private TextView txtNoMoreRepos;
    private LanguageColors colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository);

        repos = new ArrayList<>();
        colors = new LanguageColors(loadJSONFromAsset());
        adapter = new ReposAdapter(this, repos, colors);

        Intent intent = getIntent();
        user = intent.getStringExtra("user");

        String urlUser = "https://api.github.com/users/" + user;

        TextView txtTitle = findViewById(R.id.appBarText);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtBio = findViewById(R.id.txtBio);
        txtNoMoreRepos = findViewById(R.id.txtNoMoreRepos);
        TextView txtLocation = findViewById(R.id.txtLocation);
        CircleImageView imgAvatar = findViewById(R.id.imgAvatar);
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
        loadingPb = findViewById(R.id.loadingMore);

        RecyclerView lstRepos = findViewById(R.id.lstRepos);
        lstRepos.setLayoutManager(new LinearLayoutManager(this));
        lstRepos.setAdapter(adapter);

        RequestQueue queue = Volley.newRequestQueue(this);
        final Activity activity = this;

        JsonObjectRequest userRequest = new JsonObjectRequest(urlUser, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Glide.with(activity).load(response.getString("avatar_url")).into(imgAvatar);
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

                } catch (JSONException e) {
                    Log.d("YOHAN", "onErrorResponse(User): " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse(User): " + error.getMessage());
            }
        });

        NestedScrollView nsv = findViewById(R.id.nesterScroll);
        nsv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (noMoreRepos) {
                    loadingPb.setVisibility(View.GONE);
                    return;
                }

                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    loadRepos();
                }
            }
        });

        queue.add(userRequest);

        loadRepos();
    }

    private void loadRepos() {
        loadingPb.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);
        String urlRepos = "https://api.github.com/users/" + user + "/repos?per_page=30&page="+page;

        JsonArrayRequest reposRequest = new JsonArrayRequest(urlRepos, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int i = 0;
                for (i = 0; i < response.length(); i++) {
                    try {
                        repos.add(new RepoModel(response.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (i == 0 || (page == 1 && i < 30)){
                    noMoreRepos = true;
                    txtNoMoreRepos.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
                loadingPb.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("YOHAN", "onErrorResponse: " + error.getMessage());
            }
        });

        queue.add(reposRequest);
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("colors.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}