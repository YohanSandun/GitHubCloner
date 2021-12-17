package lk.ysk.githubcloner;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailedRepository extends Repository {

    private String languagesUrl, contentsUrl, defaultBranch, branchesUrl, owner, avatarUrl, githubUrl;

    public DetailedRepository(JSONObject object) {
        super(object);
        try {
            languagesUrl = object.getString("languages_url");
            contentsUrl = object.getString("contents_url");
            defaultBranch = object.getString("default_branch");
            branchesUrl = object.getString("branches_url").substring(0, object.getString("branches_url").indexOf("{"));
            githubUrl = object.getString("html_url");
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

    public String getGithubUrl() {
        return githubUrl;
    }
}
