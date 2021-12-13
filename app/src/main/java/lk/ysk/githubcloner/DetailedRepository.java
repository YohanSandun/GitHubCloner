package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailedRepository extends Repository {

    private String languagesUrl, contentsUrl, defaultBranch;

    public DetailedRepository(JSONObject object) {
        super(object);
        try {
            languagesUrl = object.getString("languages_url");
            contentsUrl = object.getString("contents_url");
            defaultBranch = object.getString("default_branch");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLanguagesUrl() {
        return languagesUrl;
    }

    public String getDefaultBranch() { return defaultBranch; }

    public String getContentsUrl() {
        return contentsUrl;
    }
}
