package lk.ysk.githubcloner.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.adapters.FavouritesAdapter;
import lk.ysk.githubcloner.interfaces.OnFavouriteClickedListener;

public class MainActivity extends AppCompatActivity {

    public static LanguageColors languageColors;
    public static Favourites favourites;

    private static File favouritesFile;

    private FavouritesAdapter favouritesAdapter;
    private RecyclerView lstFavs;
    private TextView txtFavourites;

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

    private ActivityResultLauncher<Intent> searchActivityLauncher = registerForActivityResult(
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
                    searchActivityLauncher.launch(intent);
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
                    searchActivityLauncher.launch(intent);
                }
            }
        });
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