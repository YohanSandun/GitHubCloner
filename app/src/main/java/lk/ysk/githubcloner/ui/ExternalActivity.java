package lk.ysk.githubcloner.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import lk.ysk.githubcloner.R;

public class ExternalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external);

        MainActivity.loadStaticStuff(this);

        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();

        String[] data = appLinkData.toString().substring(8).split("/");
        if (data.length >= 3) {
            if (!data[2].trim().equals("")) {
                Intent intent = new Intent(ExternalActivity.this, RepoActivity.class);
                intent.putExtra("url", "https://api.github.com/repos/" + data[1].trim() + "/" + data[2].trim());
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(ExternalActivity.this, UserActivity.class);
                intent.putExtra("user", data[1].trim());
                startActivity(intent);
                finish();
            }
        } else if (data.length == 2) {
            Intent intent = new Intent(ExternalActivity.this, UserActivity.class);
            intent.putExtra("user", data[1].trim());
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(ExternalActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}