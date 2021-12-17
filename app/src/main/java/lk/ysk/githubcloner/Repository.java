package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class Repository {
    private int stars, forks, watches;
    private String name, description, updated, language, url;

    public Repository(JSONObject object) {
        try {
            stars = object.getInt("stargazers_count");
            forks = object.getInt("forks");
            watches = object.getInt("watchers");
            name = object.getString("name").trim().replace("\r\n", " ").replace("\n"," ");
            description = object.getString("description").trim().replace("\r\n", " ").replace("\n"," ");
            updated = object.getString("updated_at").substring(0,10);
            url = object.getString("url");
            if (!object.isNull("language"))
                language = object.getString("language");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getUpdated() {
        return updated;
    }

    public String getLanguage() {
        return language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStarsString() {
        return parseThousand(stars);
    }

    public String getForksString() {
        return parseThousand(forks);
    }

    public String getWatchesString() {
        return parseThousand(watches);
    }

    private String parseThousand(int n) {
        if (n >= 1000)
            return String.format("%.1fk", n/1000f);
        return String.valueOf(n);
    }
}
