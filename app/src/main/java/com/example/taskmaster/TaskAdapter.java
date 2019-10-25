package com.example.taskmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // list of tasks from the task class
    public List<Task> tasks;

    // listener
    private OnTaskItemInteractionListener listener;

    // Adapter constructor
    public TaskAdapter(List<Task> tasks, OnTaskItemInteractionListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    // view holder holds onto the view data that you need.
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        Task task;
        TextView itemTitleView;
        TextView itemBodyView;

        // ViewHolder constructor
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
        final TaskViewHolder holder = new TaskViewHolder((v));

        // Set an OnClick Listener
        v.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                listener.taskItemClickedOn(holder.task);
            }
        });
        return holder;
    }


    // RecyclerView has a row that needs to be updated for a particular location/index
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task taskAtPosition = this.tasks.get(position);
        holder.task = taskAtPosition;
        holder.itemTitleView.setText(taskAtPosition.getTitle());
        holder.itemBodyView.setText(taskAtPosition.getBody());

    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    // Allow adapter to communicate with any activity that it's a part of that implements this interface
    public static interface OnTaskItemInteractionListener {
        public void taskItemClickedOn(Task task);
    }
}
