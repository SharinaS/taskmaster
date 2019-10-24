package com.example.taskmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


    // view holder holds onto the view data that you need.
    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView itemTitleView;
        TextView itemBodyView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemTitleView = itemView.findViewById(R.id.title);
            this.itemBodyView = itemView.findViewById(R.id.body);
        }
    }

    // RecyclerView requires a brand new row to be created from scratch, to hold data
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_task, parent, false);
        TaskViewHolder holder = new TaskViewHolder((v));
        return holder;
    }


    // RecyclerView has a row that needs to be updated for a particular location/index
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
