package lk.ysk.githubcloner;

public class FavouriteItem {

    private final String url;
    private final String name;
    private final String owner;
    private final String avatarUrl;

    public FavouriteItem(DetailedRepository repository) {
        url = repository.getUrl();
        name = repository.getName();
        owner = repository.getOwner();
        avatarUrl = repository.getAvatarUrl();
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
