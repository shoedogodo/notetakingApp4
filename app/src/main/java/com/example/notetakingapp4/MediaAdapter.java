package com.example.notetakingapp4;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.collection.BuildConfig;

import java.io.File;
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
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                Uri uri;
                if (item.isDownloaded){
                    File file = new File(item.getUri().getPath());
                    uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                }
                else{
                    uri = item.getUri();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "audio/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (intent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(intent);
                else
                    Utility.showToast(context, "没有找到可用的音乐播放器");
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