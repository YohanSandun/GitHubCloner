package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class Repository extends RepoModel {

    private String languagesUrl, contentsUrl;

    public Repository(JSONObject object) {
        super(object);
        try {
            languagesUrl = object.getString("languages_url");
            contentsUrl = object.getString("contents_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLanguagesUrl() {
        return languagesUrl;
    }

    public void setLanguagesUrl(String languagesUrl) {
        this.languagesUrl = languagesUrl;
    }

    public String getContentsUrl() {
        return contentsUrl;
    }

    public void setContentsUrl(String contentsUrl) {
        this.contentsUrl = contentsUrl;
    }
}
