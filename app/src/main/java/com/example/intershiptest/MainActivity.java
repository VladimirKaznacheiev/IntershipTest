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
    ArrayList<String> logins = new ArrayList<>();
    ArrayList<String> avatar_urls = new ArrayList<>();
    ArrayList<Integer> changes_count = new ArrayList<>();

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
        List<UserModel> userModels = realm.where(UserModel.class).findAll();

        for (UserModel user: userModels) {
            logins.add(user.getLogin());
            avatar_urls.add(user.getAvatar_url());
            changes_count.add(user.getChangesCount());
        }
        if(realm.where(UserModel.class).findAll().size() != 0){
            progressBar.setVisibility(View.GONE);
        }
        initRecyclerView();
        realmListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm realm) {
                List<UserModel> userModels = realm.where(UserModel.class).findAll();
                logins.clear();
                avatar_urls.clear();
                changes_count.clear();
                for (int i = 0; i<userModels.size();i++) {
                    UserModel user = userModels.get(i);
                        logins.add(i, user.getLogin());
                        avatar_urls.add(i, user.getAvatar_url());
                        changes_count.add(i, user.getChangesCount());
                }
                initRecyclerView();
            }
        };
        realm.addChangeListener(realmListener);
        getUsers();
        onUserClick();
    }
    public void getUsers(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService apiService = retrofit.create(APIService.class);
        Call<List<UserModel>> call = apiService.getUsers();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                realm.beginTransaction();
                for (int i = 0;i<response.body().size();i++) {
                    UserModel user = response.body().get(i);
                    int changesCountTemp = realm.where(UserModel.class).findAll().get(i).getChangesCount();
                    RealmList<ReposModel> reposListTemp = realm.where(UserModel.class).findAll().get(i).getRepositories();
                    user.setChangesCount(changesCountTemp);
                    user.setRepositories(reposListTemp);
                    realm.copyToRealmOrUpdate(user);
                }
                realm.commitTransaction();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {}
        });
    }
    public void onUserClick(){
        usersView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, usersView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(realm.where(UserModel.class).findAll().get(position).getRepositories().size() == 0){
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://api.github.com/users/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        APIService apiService = retrofit.create(APIService.class);
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
                    @Override
                    public void onLongItemClick(View view, int position) { }
                })
        );
    }
    private void initRecyclerView() {
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, logins, avatar_urls, changes_count);
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