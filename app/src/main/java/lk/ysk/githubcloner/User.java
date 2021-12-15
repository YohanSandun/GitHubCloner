package lk.ysk.githubcloner;

import org.json.JSONObject;

public class User {

    private  String login, avatarUrl;

    public User(JSONObject object) {
        try {
            login = object.getString("login");
            avatarUrl = object.getString("avatar_url");
        } catch (Exception ignore) {

        }
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
