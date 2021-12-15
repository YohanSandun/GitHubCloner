package lk.ysk.githubcloner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailedRepository extends Repository {

    private String languagesUrl, contentsUrl, defaultBranch, branchesUrl, owner, avatarUrl;

    public DetailedRepository(JSONObject object) {
        super(object);
        try {
            languagesUrl = object.getString("languages_url");
            contentsUrl = object.getString("contents_url");
            defaultBranch = object.getString("default_branch");
            branchesUrl = object.getString("branches_url").substring(0, object.getString("branches_url").indexOf("{"));
            JSONObject ownerObject = object.getJSONObject("owner");
            owner = ownerObject.getString("login");
            avatarUrl = ownerObject.getString("avatar_url");
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

    public String getBranchesUrl() {
        return branchesUrl;
    }

    public String getOwner() {
        return owner;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
