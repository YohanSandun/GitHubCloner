package lk.ysk.githubcloner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.ViewHolder> {

    private final List<RepoModel> repos;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtDescription,txtStars,txtWatches,txtUpdate, txtLanguage, txtForks;

        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            txtDescription = view.findViewById(R.id.txtDescription);
            txtStars = view.findViewById(R.id.txtStars);
            txtWatches = view.findViewById(R.id.txtWatches);
            txtUpdate = view.findViewById(R.id.txtUpdate);
            txtLanguage = view.findViewById(R.id.txtLanguage);
            txtForks = view.findViewById(R.id.txtForks);
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

    public ReposAdapter(List<RepoModel> repos) {
        this.repos = repos;
    }

    @NonNull
    @Override
    public ReposAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.repos_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReposAdapter.ViewHolder holder, int position) {
        RepoModel repo = repos.get(position);
        holder.getTxtName().setText(repo.getName());
        holder.getTxtDescription().setText(repo.getDescription());
        holder.getTxtStars().setText(repo.getStarsString());
        holder.getTxtWatches().setText(repo.getWatchesString());
        holder.getTxtUpdate().setText(String.format("Updated %s", repo.getUpdated()));
        holder.getTxtForks().setText(repo.getForksString());
        if (repo.getLanguage() == null)
            holder.getTxtLanguage().setVisibility(View.GONE);
        else
            holder.getTxtLanguage().setText(repo.getLanguage());
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }
}
