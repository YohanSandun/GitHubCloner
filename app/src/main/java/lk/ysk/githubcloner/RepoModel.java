package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class RepoModel {
    private int stars, forks, watches;
    private String name, description, updated, language;

    public RepoModel(JSONObject object) {
        try {
            stars = object.getInt("stargazers_count");
            forks = object.getInt("forks");
            watches = object.getInt("watchers");
            name = object.getString("name").trim().replace("\r\n", " ").replace("\n"," ");
            description = object.getString("description").trim().replace("\r\n", " ").replace("\n"," ");
            updated = object.getString("updated_at").substring(0,10);
            if (!object.isNull("language"))
                language = object.getString("language");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public int getWatches() {
        return watches;
    }

    public void setWatches(int watches) {
        this.watches = watches;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
