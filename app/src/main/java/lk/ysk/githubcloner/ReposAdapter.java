package lk.ysk.githubcloner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
    private final LanguageColors colors;
    private final Context context;

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

    public ReposAdapter(Context context, List<RepoModel> repos, LanguageColors colors) {
        this.repos = repos;
        this.colors = colors;
        this.context = context;
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
        else {
            holder.getTxtLanguage().setText(repo.getLanguage());
            int color = colors.getColor(repo.getLanguage());
            if (color != Color.TRANSPARENT) {
                Drawable drawable = context.getDrawable(R.drawable.orange_square);
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                holder.getTxtLanguage().setBackground(drawable);
                holder.getTxtLanguage().setTextColor(isBrightColor(color) ? Color.BLACK : Color.WHITE);
            }
        }
    }

    private boolean isBrightColor(int color) {
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
