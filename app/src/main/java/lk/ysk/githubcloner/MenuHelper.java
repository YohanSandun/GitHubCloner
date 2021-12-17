package lk.ysk.githubcloner;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

import lk.ysk.githubcloner.ui.AboutActivity;
import lk.ysk.githubcloner.ui.MainActivity;
import lk.ysk.githubcloner.ui.RepoActivity;

public class MenuHelper {

    public static void showSettingsMenu(Context context, View view, ActivityResultLauncher<Intent> launcher) {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.options, menu.getMenu());

        menu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.mnuTheme) {
                Dialog dlgTheme = new Dialog(context);
                dlgTheme.setContentView(R.layout.dialog_theme);
                RadioButton rbLight, rbDark, rbSystem;
                rbLight = dlgTheme.findViewById(R.id.radioLight);
                rbDark = dlgTheme.findViewById(R.id.radioDark);
                rbSystem = dlgTheme.findViewById(R.id.radioSystem);
                if (MainActivity.theme == MainActivity.Theme.SYSTEM_DEFAULT)
                    rbSystem.setChecked(true);
                else if (MainActivity.theme  == MainActivity.Theme.LIGHT)
                    rbLight.setChecked(true);
                else if (MainActivity.theme  == MainActivity.Theme.DARK)
                    rbDark.setChecked(true);
                dlgTheme.findViewById(R.id.btnOk).setOnClickListener(view1 -> {
                    if (rbSystem.isChecked()) {
                        MainActivity.theme  = MainActivity.Theme.SYSTEM_DEFAULT;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    } else if (rbLight.isChecked()) {
                        MainActivity.theme  = MainActivity.Theme.LIGHT;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (rbDark.isChecked()) {
                        MainActivity.theme  = MainActivity.Theme.DARK;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    MainActivity.preferences.edit().putInt("theme", MainActivity.theme.getValue()).apply();
                    dlgTheme.dismiss();
                });
                dlgTheme.findViewById(R.id.btnCancel).setOnClickListener(view1 -> dlgTheme.dismiss());
                dlgTheme.show();
            } else if (menuItem.getItemId() == R.id.mnuAbout) {
                Intent aboutIntent = new Intent(context, AboutActivity.class);
                context.startActivity(aboutIntent);
            } else if (menuItem.getItemId() == R.id.mnuRate) {
                rateApp(context);
            } else if (menuItem.getItemId() == R.id.mnuSource) {
                Intent intent = new Intent(context, RepoActivity.class);
                intent.putExtra("url", "https://api.github.com/repos/YohanSandun/githubcloner");
                launcher.launch(intent);
            } else if (menuItem.getItemId() == R.id.mnuLocation) {
                new ChooserDialog(context)
                        .withFilter(true, false)
                        .withStringResources("Download Location", "Choose", "Cancel")
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                MainActivity.preferences.edit().putString("download", path).apply();
                                Toast.makeText(context, "Download loaction updated!", Toast.LENGTH_LONG).show();
                            }
                        })
                        // to handle the back key pressed or clicked outside the dialog:
                        .withOnCancelListener(dialog -> {
                            dialog.cancel(); // MUST have
                        })
                        .build()
                        .show();
            }
            return false;
        });
        menu.show();
    }

    public static void rateApp(Context context) {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details", context);
            context.startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details", context);
            context.startActivity(rateIntent);
        }
    }

    private static Intent rateIntentForUrl(String url, Context context)  {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, context.getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        intent.addFlags(flags);
        return intent;
    }


}
