package lk.ysk.githubcloner.interfaces;

import lk.ysk.githubcloner.FavouriteItem;

public interface OnFavouriteClickedListener {
    void onClick(FavouriteItem item);
    boolean onLongClick(FavouriteItem item);
}
