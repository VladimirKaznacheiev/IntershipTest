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






class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //vars
    private ArrayList<String> logins = new ArrayList<>();
    private ArrayList<String> avatar_urls = new ArrayList<>();
    private ArrayList<Integer> changes_count = new ArrayList<>();
    Context context;


    public RecyclerViewAdapter(Context context, ArrayList<String> logins, ArrayList<String> avatar_urls,ArrayList<Integer> changes_count) {
        this.logins = logins;
        this.avatar_urls = avatar_urls;
        this.context = context;
        this.changes_count = changes_count;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.login.setText(logins.get(position));
        Glide.with(context).load(avatar_urls.get(position)).into(holder.avatar);
        holder.login.setTextColor(ContextCompat.getColor(context,R.color.black));

        if(changes_count.get(position) == 0){
            holder.circle.setVisibility(View.GONE);
            holder.changesCounter.setVisibility(View.GONE);
        }else{
            holder.changesCounter.setText(changes_count.get(position)+"");
        }

    }

    @Override
    public int getItemCount() {
        return logins.size();
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

