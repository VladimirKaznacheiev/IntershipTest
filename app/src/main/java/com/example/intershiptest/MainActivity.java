package com.example.intershiptest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    RecyclerView usersView;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    DialogFragment reposDialog;
    Realm realm;
    RealmChangeListener realmListener;
    Retrofit retrofit;
    APIService apiService;
    List<UserModel> userModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpRealmConfig();
        realm = Realm.getDefaultInstance();
        usersView = findViewById(R.id.usersList);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.usersList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        progressBar.setVisibility(View.VISIBLE);
        reposDialog = new ReposDialogFragment();
        getSupportActionBar().hide();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(APIService.class);
        userModels = realm.where(UserModel.class).findAll();
        if(realm.where(UserModel.class).findAll().size() != 0){
            progressBar.setVisibility(View.GONE);
        }
        initRecyclerView();
        realmListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                userModels = realm.where(UserModel.class).findAll();
                initRecyclerView();
            }
        };
        realm.addChangeListener(realmListener);
        getUsers();
        onUserClick();
    }
    public void onUserClick(){
        usersView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, usersView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        getRepos(position);
                    }
                    @Override
                    public void onLongItemClick(View view, int position) { }
                })
        );
    }
    public void getUsers(){
        Call<List<UserModel>> call = apiService.getUsers();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                realm.beginTransaction();
                for (int i = 0;i<response.body().size();i++) {
                    UserModel user = response.body().get(i);
                    if(realm.where(UserModel.class).findAll().size() == response.body().size()) {
                        user.setChangesCount(realm.where(UserModel.class).findAll().get(i).getChangesCount());
                        user.setRepositories(realm.where(UserModel.class).findAll().get(i).getRepositories());
                    }
                    realm.copyToRealmOrUpdate(user);
                }
                realm.commitTransaction();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {}
        });
    }
    public void getRepos(int position){
        if(realm.where(UserModel.class).findAll().get(position).getRepositories().size() == 0){
            Call<List<ReposModel>> call = apiService.getRepos(realm.where(UserModel.class).findAll().get(position).getLogin());
            call.enqueue(new Callback<List<ReposModel>>() {
                @Override
                public void onResponse(Call<List<ReposModel>> call, Response<List<ReposModel>> response) {
                    realm.beginTransaction();
                    RealmList<ReposModel> reposModels = new RealmList<>();
                    UserModel userModel = realm.where(UserModel.class).findAll().get(position);
                    for (ReposModel reposModel : response.body()) {
                        userModel.repositories.add(reposModel);
                    }
                    realm.copyToRealmOrUpdate(userModel);
                    realm.commitTransaction();
                    Bundle args = new Bundle();
                    args.putString("login", realm.where(UserModel.class).findAll().get(position).getLogin());
                    reposDialog.setArguments(args);
                    reposDialog.show(getSupportFragmentManager(), "tag");
                }

                @Override
                public void onFailure(Call<List<ReposModel>> call, Throwable t) {
                }
            });

        }else{
            Bundle args = new Bundle();
            args.putString("login", realm.where(UserModel.class).findAll().get(position).getLogin());
            reposDialog.setArguments(args);
            reposDialog.show(getSupportFragmentManager(), "tag");
        }
    }
    private void initRecyclerView() {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, userModels);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }
    private void setUpRealmConfig(){
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.
                Builder().
                allowWritesOnUiThread(true).
                build();
        Realm.setDefaultConfiguration(config);
    }
}