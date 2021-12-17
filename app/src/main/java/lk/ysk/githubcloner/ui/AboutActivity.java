package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import lk.ysk.githubcloner.MenuHelper;
import lk.ysk.githubcloner.R;

public class AboutActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.btnBack).setOnClickListener(view -> onBackPressed());

        findViewById(R.id.btnSettings).setOnClickListener(view -> MenuHelper.showSettingsMenu(this, view, activityLauncher));

        findViewById(R.id.btnRate).setOnClickListener(view -> MenuHelper.rateApp(this));

        findViewById(R.id.btnMoreApps).setOnClickListener(view -> moreApps());

        findViewById(R.id.txtEmail).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "yohan.sandunk@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "APP: GitHub Cloner");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(intent, ""));
        });

        findViewById(R.id.txtPrivacy).setOnClickListener(view -> {
            Intent privacy = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy)));
            startActivity(privacy);
        });

        findViewById(R.id.txtViewOnGithub).setOnClickListener(view -> {
            Intent github = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YohanSandun/GitHubCloner"));
            startActivity(github);
        });
    }

    private void moreApps()
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=YSK+Soft"));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        intent.addFlags(flags);
        startActivity(intent);
    }

}