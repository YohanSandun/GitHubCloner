package lk.ysk.githubcloner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.ysk.githubcloner.Content;
import lk.ysk.githubcloner.interfaces.OnContentClickedListener;
import lk.ysk.githubcloner.R;

public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View baseView;
        private final TextView txtName;
        private final TextView txtSize;
        private final CheckBox chkSelect;
        private final ImageView imgIcon;

        public ViewHolder(View view) {
            super(view);
            baseView = view;
            txtName = view.findViewById(R.id.txtName);
            txtSize = view.findViewById(R.id.txtSize);
            chkSelect = view.findViewById(R.id.chkSelect);
            imgIcon = view.findViewById(R.id.imgIcon);
        }

        public View getBaseView() {
            return baseView;
        }

        public TextView getTxtName() {
            return txtName;
        }

        public TextView getTxtSize() {
            return txtSize;
        }

        public CheckBox getChkSelect() {
            return chkSelect;
        }

        public ImageView getImgIcon() {
            return imgIcon;
        }
    }

    private final List<Content> contents;
    private final OnContentClickedListener contentClickedListener;

    public ContentsAdapter(List<Content> contents, OnContentClickedListener contentClickedListener) {
        this.contents = contents;
        this.contentClickedListener = contentClickedListener;
    }

    @NonNull
    @Override
    public ContentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_list_item, parent, false);
        return new ContentsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentsAdapter.ViewHolder holder, int position) {
        Content content = contents.get(position);
        holder.getTxtName().setText(content.getName());
        if (content.getType() == Content.Type.FILE) {
            holder.getImgIcon().setImageResource(R.drawable.ic_file);
            holder.getTxtSize().setText(content.getSizeString());
            holder.getTxtSize().setVisibility(View.VISIBLE);
            holder.getChkSelect().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    content.setSelected(b);
                }
            });
            holder.getChkSelect().setChecked(content.isSelected());
        } else if (content.getType() == Content.Type.GO_BACK) {
            holder.getChkSelect().setVisibility(View.GONE);
            content.setSelected(false);
            holder.getImgIcon().setImageResource(R.drawable.ic_dir);
            holder.getTxtSize().setVisibility(View.GONE);
        }
        else {
            holder.getImgIcon().setImageResource(R.drawable.ic_dir);
            holder.getTxtSize().setVisibility(View.GONE);
            holder.getChkSelect().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    content.setSelected(b);
                }
            });
            holder.getChkSelect().setChecked(content.isSelected());
        }
        holder.getBaseView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentClickedListener.onClick(content);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }
}
