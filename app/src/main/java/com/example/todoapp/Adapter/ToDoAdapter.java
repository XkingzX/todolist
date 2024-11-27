package com.example.todoapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.MainActivity;
import com.example.todoapp.R;
import com.example.todoapp.ThemNhiemVu;
import com.example.todoapp.model.ToDoModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> todoList;
    private MainActivity activity;
    private FirebaseFirestore firestore;

    public ToDoAdapter(MainActivity mainActivity, List<ToDoModel> todoList){
        this.todoList = todoList;
        firestore = FirebaseFirestore.getInstance();
        activity = mainActivity;
    }
    public Context getContext(){
        return activity;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.nhiem_vu, parent, false);
        return new MyViewHolder(view);
    }

    public void deleteTask(int position){
        ToDoModel toDoModel = todoList.get(position);
        firestore.collection("task").document(toDoModel.TaskId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        todoList.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        Toast.makeText(activity, "Xóa không thành công", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void editTask(int position){
        ToDoModel toDoModel = todoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);

        ThemNhiemVu themNhiemVu = new ThemNhiemVu();
        themNhiemVu.setArguments(bundle);
        themNhiemVu.show(activity.getSupportFragmentManager(), themNhiemVu.getTag());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);
        holder.mchecKbox.setText(toDoModel.getTask());
        holder.date_tv.setText(toDoModel.getDue());

        holder.mchecKbox.setChecked(toBoolean(toDoModel.getStatus()));

        holder.mchecKbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            int status = isChecked ? 1 : 0;
            firestore.collection("task")
                    .document(toDoModel.TaskId)
                    .update("status", status)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            toDoModel.setStatus(status);
                            notifyItemChanged(position); // Use position directly here
                        } else {
                            Toast.makeText(activity, "Cập nhật không thành công", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean toBoolean(int status) {
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void updateList(List<ToDoModel> newList) {
        this.todoList = newList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date_tv;
        CheckBox mchecKbox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            date_tv = itemView.findViewById(R.id.date_tv);
            mchecKbox = itemView.findViewById(R.id.mcheckbox);
        }
    }
}
