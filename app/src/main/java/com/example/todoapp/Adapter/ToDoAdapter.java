package com.example.todoapp.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.MainActivity;
import com.example.todoapp.R;
import com.example.todoapp.ThemNhiemVu;
import com.example.todoapp.model.ToDoModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> todoList;
    private MainActivity activity;
    private FirebaseFirestore firestore;


    public ToDoAdapter(MainActivity mainActivity, List<ToDoModel> todoList){
        this.todoList = todoList;
        activity = mainActivity;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.nhiem_vu, parent, false);
        firestore = FirebaseFirestore.getInstance();
        return new MyViewHolder(view);
    }

    public void deleteTask(int position){
        ToDoModel toDoModel = todoList.get(position);
        firestore.collection("task").document(toDoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }
    public Context getContext(){
        return activity;
    }
    public void editTask(int position){
        ToDoModel  toDoModel = todoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);

        ThemNhiemVu themNhiemVu = new ThemNhiemVu();
        themNhiemVu.setArguments(bundle);
        themNhiemVu.show(activity.getSupportFragmentManager() , themNhiemVu.getTag());
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);
        holder.mchecKbox.setText(toDoModel.getTask());
        holder.date_tv.setText("" + toDoModel.getDue());

        holder.mchecKbox.setChecked(toBoolean(toDoModel.getStatus()));
        holder.mchecKbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    firestore.collection("task").document(toDoModel.TaskId).update("status", 1);
                }else{
                    firestore.collection("task").document(toDoModel.TaskId).update("status", 0);
                }
            }
        });
    }

    private boolean toBoolean(int status)
    {
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView date_tv;
        CheckBox mchecKbox;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);

            date_tv = itemView.findViewById(R.id.date_tv);
            mchecKbox = itemView.findViewById(R.id.mcheckbox);
        }
    }
}
