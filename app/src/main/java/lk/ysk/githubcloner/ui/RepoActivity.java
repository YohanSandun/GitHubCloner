package lk.ysk.githubcloner.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.ReposAdapter;
import lk.ysk.githubcloner.Repository;
import lk.ysk.githubcloner.ui.widgets.SegmentProgressBar;

public class RepoActivity extends AppCompatActivity {

    private Repository repository;
    private FlexboxLayout gridLayout;
    private SegmentProgressBar segmentPb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo);

        Intent intent = getIntent();
        String repoUrl = intent.getStringExtra("url");

        TextView txtName = findViewById(R.id.appBarText);
        TextView txtDescription = findViewById(R.id.txtDescription);
        TextView txtUpdated = findViewById(R.id.txtUpdated);
        TextView txtStars = findViewById(R.id.txtStars);
        TextView txtWatches = findViewById(R.id.txtWatches);
        TextView txtForks = findViewById(R.id.txtForks);
        gridLayout = findViewById(R.id.gridLanguages);
        segmentPb = findViewById(R.id.segmentPb);

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
}