package com.example.notetakingapp4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private List<MediaItem> mediaItems;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private Context context;

    // 构造器
    public MediaAdapter(Context context, List<MediaItem> mediaItems) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mediaItems = mediaItems;
    }

    public void changeMedia(List<MediaItem> mediaItems){
        this.mediaItems = mediaItems;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        if (item.getType() == MediaItem.IMAGE) {
            holder.imageView.setImageURI(item.getUri());
            holder.imageView.setVisibility(View.VISIBLE);
            holder.audioIcon.setVisibility(View.GONE);
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("image_uri", item.getUri().toString());
                context.startActivity(intent);
            });
            holder.audioIcon.setOnLongClickListener(v -> {
                mClickListener.onItemLongClick(v, position);
                return true;
            });

        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.audioIcon.setVisibility(View.VISIBLE);
            holder.audioIcon.setOnClickListener(v -> {
                Intent intent = new Intent(context, AudioPlayerActivity.class);
                intent.putExtra("audio_uri", item.getUri().toString());
                context.startActivity(intent);
            });
            holder.imageView.setOnLongClickListener(v -> {
                mClickListener.onItemLongClick(v, position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imageView;
        ImageView audioIcon;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_media);
            audioIcon = itemView.findViewById(R.id.img_audio_icon);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        public boolean onLongClick(View view) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                mediaItems.remove(position);
                                notifyItemRemoved(position);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
    }

    // 允许点击和长按
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}