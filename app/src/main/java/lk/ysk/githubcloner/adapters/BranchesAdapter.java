package lk.ysk.githubcloner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.ysk.githubcloner.R;
import lk.ysk.githubcloner.interfaces.OnBranchClickedListener;

public class BranchesAdapter extends RecyclerView.Adapter<BranchesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final View baseView;

        public ViewHolder(View view) {
            super(view);
            baseView = view;
            txtName = view.findViewById(R.id.txtName);
        }

        public View getBaseView() {
            return baseView;
        }

        public TextView getTxtName() {
            return txtName;
        }
    }

    private final List<String> branches;
    private final OnBranchClickedListener branchClickedListener;

    public BranchesAdapter(List<String> branches, OnBranchClickedListener branchClickedListener) {
        this.branches = branches;
        this.branchClickedListener = branchClickedListener;
    }

    @NonNull
    @Override
    public BranchesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.branch_list_item, parent, false);
        return new BranchesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchesAdapter.ViewHolder holder, int position) {
        holder.getTxtName().setText(branches.get(position));
        holder.getBaseView().setOnClickListener(v -> branchClickedListener.onClicked(branches.get(position)));
    }

    @Override
    public int getItemCount() {
        return branches.size();
    }
}
