package lk.ysk.githubcloner.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.ysk.githubcloner.DetailedRepository;
import lk.ysk.githubcloner.LanguageColors;
import lk.ysk.githubcloner.interfaces.OnRepoClickedListener;
import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.Repository;
import lk.ysk.githubcloner.ui.MainActivity;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.ViewHolder> {

    private final List<DetailedRepository> repos;
    private final OnRepoClickedListener clickedListener;
    private boolean needOwnerName;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtDescription,txtStars,txtWatches,txtUpdate, txtLanguage, txtForks;
        private View baseView;

        public ViewHolder(View view) {
            super(view);
            baseView = view;
            txtName = view.findViewById(R.id.txtName);
            txtDescription = view.findViewById(R.id.txtDescription);
            txtStars = view.findViewById(R.id.txtStars);
            txtWatches = view.findViewById(R.id.txtWatches);
            txtUpdate = view.findViewById(R.id.txtUpdate);
            txtLanguage = view.findViewById(R.id.txtLanguage);
            txtForks = view.findViewById(R.id.txtForks);
        }

        public View getBaseView() {
            return baseView;
        }

        public TextView getTxtName() {
            return txtName;
        }

        public TextView getTxtDescription() {
            return txtDescription;
        }

        public TextView getTxtStars() {
            return txtStars;
        }

        public TextView getTxtWatches() {
            return txtWatches;
        }

        public TextView getTxtUpdate() {
            return txtUpdate;
        }

        public TextView getTxtLanguage() {
            return txtLanguage;
        }

        public TextView getTxtForks() {
            return txtForks;
        }
    }

    public RepositoryAdapter(List<DetailedRepository> repos, OnRepoClickedListener clickedListener) {
        this.repos = repos;
        this.clickedListener = clickedListener;
    }

    public void setNeedOwnerName(boolean needOwnerName) {
        this.needOwnerName = needOwnerName;
    }

    @NonNull
    @Override
    public RepositoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.repos_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RepositoryAdapter.ViewHolder holder, int position) {
        DetailedRepository repo = repos.get(position);
        holder.getTxtName().setText(repo.getName());
        holder.getTxtDescription().setText(needOwnerName ? "By " + repo.getOwner() + "\r\n\r\n" + repo.getDescription() : repo.getDescription());
        holder.getTxtStars().setText(repo.getStarsString());
        holder.getTxtWatches().setText(repo.getWatchesString());
        holder.getTxtUpdate().setText(String.format("Updated %s", repo.getUpdated()));
        holder.getTxtForks().setText(repo.getForksString());
        if (repo.getLanguage() == null)
            holder.getTxtLanguage().setVisibility(View.GONE);
        else {
            holder.getTxtLanguage().setText(repo.getLanguage());
            int color = MainActivity.languageColors.getColor(repo.getLanguage());
            if (color != Color.TRANSPARENT) {
                Drawable drawable = holder.baseView.getContext().getDrawable(R.drawable.white_square);
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                holder.getTxtLanguage().setBackground(drawable);
                holder.getTxtLanguage().setTextColor(isBrightColor(color) ? Color.BLACK : Color.WHITE);
            }
        }
        holder.getBaseView().setOnClickListener(view -> {
            clickedListener.OnClick(repo);
        });
    }

    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return true;

        boolean rtnValue = false;

        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light
        if (brightness >= 200) {
            rtnValue = true;
        }

        return rtnValue;
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }
}
