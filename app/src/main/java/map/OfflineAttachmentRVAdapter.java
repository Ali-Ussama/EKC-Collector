package map;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.collector.R;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OfflineAttachmentRVAdapter extends RecyclerView.Adapter<OfflineAttachmentRVAdapter.viewHolder> {

    private ArrayList<File> data;
    private Context context;

    public OfflineAttachmentRVAdapter(ArrayList<File> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_rv_row_item, parent, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new viewHolder(view);
    }

    void addImageBitmap(File image) {
        try {
            data.add(image);
            notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.imageView.setImageURI(Uri.fromFile(data.get(position)));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.offline_attachments_rv_row_item_image_view)
        ImageView imageView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ButterKnife.bind(this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
