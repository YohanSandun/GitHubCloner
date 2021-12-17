package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;

import lk.ysk.githubcloner.FavouriteItem;
import lk.ysk.githubcloner.Favourites;
import lk.ysk.githubcloner.LanguageColors;
import lk.ysk.githubcloner.MenuHelper;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.adapters.FavouritesAdapter;
import lk.ysk.githubcloner.interfaces.OnFavouriteClickedListener;

public class MainActivity extends AppCompatActivity {

    public static LanguageColors languageColors;
    public static Favourites favourites;
    public static Theme theme;
    public static SharedPreferences preferences;

    private static File favouritesFile;

    private FavouritesAdapter favouritesAdapter;
    private RecyclerView lstFavs;
    private TextView txtFavourites;

    public enum Theme {
        SYSTEM_DEFAULT(0), LIGHT(1), DARK(2);

        private final int value;
        Theme(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Theme fromInteger(int i) {
            switch (i) {
                case 0:
                default:
                    return SYSTEM_DEFAULT;
                case 1:
                    return LIGHT;
                case 2:
                    return DARK;
            }
        }
    }

    public static void loadStaticStuff(Context context) {
        favouritesFile = new File(context.getFilesDir(), "favourites.json");
        loadFavourites();
        languageColors = new LanguageColors(loadJSONFromAsset(context));
    }

    private static void loadFavourites() {
        if (!favouritesFile.exists()){
            favourites = new Favourites();
            return;
        }
        try {
            FileReader reader = new FileReader(favouritesFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String read;
            StringBuilder builder = new StringBuilder();
            while((read = bufferedReader.readLine()) != null){
                builder.append(read);
            }
            bufferedReader.close();
            reader.close();

            Gson gson = new Gson();
            favourites = gson.fromJson(builder.toString(), Favourites.class);
        } catch (Exception e) {
            favouritesFile.delete();
            loadFavourites();
        }
    }

    public static void saveFavourites() {
        try {
            if (favouritesFile.exists())
                favouritesFile.delete();
            FileWriter fw = new FileWriter(favouritesFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            Gson gson = new Gson();
            bufferedWriter.write(gson.toJson(favourites));
            bufferedWriter.flush();
            bufferedWriter.close();
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshFavouritesText() {
        if (favourites.getFavourites().size() == 0) {
            txtFavourites.setVisibility(View.VISIBLE);
            lstFavs.setVisibility(View.GONE);
        } else {
            txtFavourites.setVisibility(View.GONE);
            lstFavs.setVisibility(View.VISIBLE);
        }
    }

    private final ActivityResultLauncher<Intent> searchActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (favouritesAdapter != null) {
                        favouritesAdapter.notifyDataSetChanged();
                        refreshFavouritesText();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        theme = Theme.fromInteger(preferences.getInt("theme", 0));
        if (theme == Theme.SYSTEM_DEFAULT)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else if (theme == Theme.LIGHT)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (theme == Theme.DARK)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        loadStaticStuff(this);

        txtFavourites = findViewById(R.id.txtFavourites);

        favouritesAdapter = new FavouritesAdapter(favourites.getFavourites(), new OnFavouriteClickedListener() {
            @Override
            public void onClick(FavouriteItem item) {
                Intent intent = new Intent(MainActivity.this, RepoActivity.class);
                intent.putExtra("url", item.getUrl());
                searchActivityLauncher.launch(intent);
            }

            @Override
            public boolean onLongClick(FavouriteItem item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Delete");
                builder.setMessage("Do you want to remove this favourite item?");

                builder.setPositiveButton("YES", (dialog, which) -> {
                    favourites.remove(item);
                    favouritesAdapter.notifyDataSetChanged();
                    saveFavourites();
                    refreshFavouritesText();
                });

                builder.setNegativeButton("NO", (dialog, which) -> {

                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
        lstFavs = findViewById(R.id.lstFavorites);
        lstFavs.setLayoutManager(new LinearLayoutManager(this));
        lstFavs.setItemAnimator(new DefaultItemAnimator());
        lstFavs.setAdapter(favouritesAdapter);
        refreshFavouritesText();

        EditText txtTerm = findViewById(R.id.txtSearch);

        findViewById(R.id.btnSearchUsers).setOnClickListener(view -> {
            String term = parseQuery(txtTerm.getText().toString());
            if (!term.equals("")) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("term", term);
                intent.putExtra("type", "user");
                searchActivityLauncher.launch(intent);
            } else
                Toast.makeText(MainActivity.this, "Enter search query first!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnSearchRepos).setOnClickListener(v -> {
            String term = parseQuery(txtTerm.getText().toString());
            if (!term.equals("")) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("term", term);
                intent.putExtra("type", "repo");
                searchActivityLauncher.launch(intent);
            } else
                Toast.makeText(MainActivity.this, "Enter search query first!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnSettings).setOnClickListener(view -> MenuHelper.showSettingsMenu(this, view, searchActivityLauncher));
    }

    private static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("colors.json");
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