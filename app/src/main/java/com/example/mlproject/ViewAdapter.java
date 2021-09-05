package com.example.mlproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<ObjectResult> list;
    private Context context;

    public ViewAdapter(List<ObjectResult> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.holder,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ObjectResult objectResult= list.get(position);
        holder.imageView.setImageBitmap(objectResult.getBitmap());
        holder.textView.setText(objectResult.getLabel());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
