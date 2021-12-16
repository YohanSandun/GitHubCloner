package lk.ysk.githubcloner;

public class FavouriteItem {

    private String url;
    private String name;
    private String description;
    private String owner;
    private String avatarUrl;

    public FavouriteItem(DetailedRepository repository) {
        url = repository.getUrl();
        name = repository.getName();
        description = repository.getDescription();
        owner = repository.getOwner();
        avatarUrl = repository.getAvatarUrl();
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
