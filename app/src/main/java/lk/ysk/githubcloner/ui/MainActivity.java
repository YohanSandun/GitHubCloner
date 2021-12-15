package lk.ysk.githubcloner.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.InputStream;

import lk.ysk.githubcloner.LanguageColors;
import lk.ysk.githubcloner.R;

public class MainActivity extends AppCompatActivity {

    public static LanguageColors languageColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageColors = new LanguageColors(loadJSONFromAsset());

        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                23);

        EditText txtTerm = findViewById(R.id.txtSearch);

        findViewById(R.id.btnSearchUsers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String term = parseQuery(txtTerm.getText().toString());
                if (!term.equals("")) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("term", term);
                    intent.putExtra("type", "user");
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.btnSearchRepos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String term = parseQuery(txtTerm.getText().toString());
                if (!term.equals("")) {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("term", term);
                    intent.putExtra("type", "repo");
                    startActivity(intent);
                }
            }
        });
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

    private String parseQuery(String term) {
        return term.replace(' ', '+');
    }
}