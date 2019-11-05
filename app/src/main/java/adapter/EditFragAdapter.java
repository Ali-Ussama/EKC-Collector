package adapter;


import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class EditFragAdapter extends RecyclerView.Adapter<EditFragAdapter.viewHolder> {


    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class viewHolder extends RecyclerView.ViewHolder {

        public viewHolder(View itemView) {
            super(itemView);
        }
    }
}
