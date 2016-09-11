package com.rakatan.tremendsizeexplorer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rakatan.tremendsizeexplorer.utils.MyFileUtils;
import com.rakatan.tremendsizeexplorer.R;
import com.rakatan.tremendsizeexplorer.events.FileClickedEvent;
import com.rakatan.tremendsizeexplorer.models.FileModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays relevant file information.
 */

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.FileViewHolder> {
    private ArrayList<FileModel> fileList;
    private long maxSize = 0;

    public FileViewAdapter(ArrayList<FileModel> fileList) {
        this.fileList = fileList;
        maxSize = fileList.get(0).getSize();
    }

    @Override
    public FileViewAdapter.FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FileViewAdapter.FileViewHolder holder, int position) {
        final FileModel thisFile = fileList.get(position);

        if (thisFile.getFile().isDirectory()) { // Provide navigation to the directory's children
            holder.typeImageView.setImageResource(R.drawable.ic_folder_black_48dp);
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new FileClickedEvent(thisFile));
                }
            });
        }
        else if (thisFile.getFile().isFile()) { // Remove any possibly left over click listener.
            holder.typeImageView.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
            holder.itemLayout.setOnClickListener(null);
        }

        holder.nameTextView.setText(thisFile.getFile().getName());
        holder.sizeTextView.setText(MyFileUtils.humanReadableByteCount(thisFile.getSize(), true));
        holder.sizeIndicator.setProgress(sizeToPercent(thisFile.getSize()));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progressBar_blend_progress)
        ProgressBar sizeIndicator;
        @BindView(R.id.imageView_fileType)
        ImageView typeImageView;
        @BindView(R.id.textView_fileName)
        TextView nameTextView;
        @BindView(R.id.textView_fileSize)
        TextView sizeTextView;
        @BindView(R.id.relativeLayout_item)
        RelativeLayout itemLayout;


        FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * This method calculates the given size in respect to the largest file in the list.
     * It enables us to set the progress bars in such a way that it shows the relative sizes of folders.
     *
     * @param size The size of the currently assessed file.
     * @return The size percentage of the file, relative to the largest file in this list.
     */
    private int sizeToPercent(long size){
        return (int)(size * (1000000.0f / maxSize));
    }

    /**
     * Replaces the old list with a new one and displays the contents
     * @param newModels The new file models to be displayed
     */
    public void updateFiles(ArrayList<FileModel> newModels){
        this.fileList = newModels;
        maxSize = newModels.get(0).getSize();

        notifyDataSetChanged();
    }
}
