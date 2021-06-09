package com.example.intershiptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //vars
    List<UserModel> userModelList;
    Context context;


    public RecyclerViewAdapter(Context context, List<UserModel> userModelList) {

        this.context = context;
        this.userModelList = userModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.login.setText(userModelList.get(position).getLogin());
        Glide.with(context).load(userModelList.get(position).getAvatar_url()).into(holder.avatar);
        holder.login.setTextColor(ContextCompat.getColor(context,R.color.black));

        if(userModelList.get(position).getChangesCount() == 0){
            holder.circle.setVisibility(View.GONE);
            holder.changesCounter.setVisibility(View.GONE);
        }else{
            holder.changesCounter.setText(userModelList.get(position).getChangesCount()+"");
        }

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        ImageView circle;
        TextView login;
        TextView changesCounter;


        public ViewHolder(View itemView) {
            super(itemView);
            login = itemView.findViewById(R.id.userLogin);
            changesCounter = itemView.findViewById(R.id.changesCounter);
            avatar = itemView.findViewById(R.id.userImage);
            circle = itemView.findViewById(R.id.redCircle);

        }
    }
}

