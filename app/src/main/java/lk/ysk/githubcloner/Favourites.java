package lk.ysk.githubcloner;

import java.util.ArrayList;
import java.util.List;

public class Favourites {

    private List<FavouriteItem> repositories = new ArrayList<>();

    public void addFavourite(FavouriteItem url) {
        repositories.add(url);
    }

    public List<FavouriteItem> getFavourites() {
        return repositories;
    }

    public boolean isAvailable(String name, String owner) {
        for(FavouriteItem item : repositories) {
            if (item.getName().equals(name) && item.getOwner().equals(owner))
                return true;
        }
        return false;
    }

    public void removeFavourite(String name, String owner) {
        for(FavouriteItem item : repositories) {
            if (item.getName().equals(name) && item.getOwner().equals(owner)) {
                repositories.remove(item);
                return;
            }
        }
    }

    public void remove(FavouriteItem item) {
        repositories.remove(item);
    }
}
