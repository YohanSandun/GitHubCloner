package lk.ysk.githubcloner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lk.ysk.githubcloner.FavouriteItem;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.interfaces.OnFavouriteClickedListener;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder>  {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtOwner;
        private CircleImageView imgAvatar;
        private View baseView;

        public ViewHolder(View view) {
            super(view);
            baseView = view;
            txtName = view.findViewById(R.id.txtName);
            txtOwner = view.findViewById(R.id.txtOwner);
            imgAvatar = view.findViewById(R.id.imgAvatar);
        }

        public View getBaseView() {
            return baseView;
        }

        public TextView getTxtName() {
            return txtName;
        }

        public TextView getTxtOwner() {
            return txtOwner;
        }

        public CircleImageView getImgAvatar() {
            return imgAvatar;
        }
    }

    private final List<FavouriteItem> favouritesList;
    private final OnFavouriteClickedListener favouriteClickedListener;

    public FavouritesAdapter(List<FavouriteItem> favouritesList, OnFavouriteClickedListener favouriteClickedListener) {
        this.favouritesList = favouritesList;
        this.favouriteClickedListener = favouriteClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favourite_list_item, parent, false);
        return new FavouritesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavouriteItem favouriteItem = favouritesList.get(position);
        holder.getTxtName().setText(favouriteItem.getName());
        holder.getTxtOwner().setText(favouriteItem.getOwner());
        Glide.with(holder.getBaseView().getContext()).load(favouriteItem.getAvatarUrl()).into(holder.getImgAvatar());
        holder.getBaseView().setOnClickListener(v -> favouriteClickedListener.onClick(favouriteItem));
        holder.getBaseView().setOnLongClickListener(v -> favouriteClickedListener.onLongClick(favouriteItem));
    }

    @Override
    public int getItemCount() {
        return favouritesList.size();
    }

}
