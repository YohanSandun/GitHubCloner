package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class Repository extends RepoModel {

    private String languagesUrl, contentsUrl, defaultBranch;

    public Repository(JSONObject object) {
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
