package lk.ysk.githubcloner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.User;
import lk.ysk.githubcloner.interfaces.OnUserClickedListener;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>  {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final CircleImageView imgAvatar;
        private final View baseView;

        public ViewHolder(View view) {
            super(view);
            baseView = view;
            txtName = view.findViewById(R.id.txtName);
            imgAvatar = view.findViewById(R.id.imgAvatar);
        }

        public View getBaseView() {
            return baseView;
        }

        public CircleImageView getImgAvatar() {
            return imgAvatar;
        }

        public TextView getTxtName() {
            return txtName;
        }
    }

    private final List<User> users;
    private final OnUserClickedListener userClickedListener;

    public UsersAdapter(List<User> users, OnUserClickedListener userClickedListener) {
        this.users = users;
        this.userClickedListener = userClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new UsersAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTxtName().setText(users.get(position).getLogin());
        Glide.with(holder.getBaseView().getContext()).load(users.get(position).getAvatarUrl()).into(holder.getImgAvatar());
        holder.getBaseView().setOnClickListener(view -> {
            userClickedListener.onClick(users.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
