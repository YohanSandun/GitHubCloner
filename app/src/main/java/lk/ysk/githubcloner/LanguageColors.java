package lk.ysk.githubcloner;

import android.graphics.Color;

import org.json.JSONObject;

public class LanguageColors {

    private JSONObject json;

    public LanguageColors(String json) {
        try {
            this.json = new JSONObject(json);
        }catch (Exception ignore) {

        }
    }

    public int getColor(String lang) {
        try {
            if (json.isNull(lang))
                return Color.TRANSPARENT;
            return Color.parseColor(json.getString(lang));
        }catch (Exception ignore) {}
        return Color.TRANSPARENT;
    }

}
