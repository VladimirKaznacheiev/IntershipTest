package com.example.intershiptest;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    RecyclerView usersView;
    ProgressBar progressBar;
    DialogFragment reposDialog;
    Realm realm;
    Document doc;
    Document doc1;

    String users;

    ArrayList<String> logins = new ArrayList<>();
    ArrayList<String> logins_temp = new ArrayList<>();
    ArrayList<String> avatar_urls = new ArrayList<>();
    ArrayList<String> avatar_urls_temp = new ArrayList<>();
    ArrayList<String> repos_urls = new ArrayList<>();
    ArrayList<String> repos_urls_temp = new ArrayList<>();
    ArrayList<String> repos_words = new ArrayList<>();
    ArrayList<Integer> user_id = new ArrayList<>();
    ArrayList<Integer> user_id_temp = new ArrayList<>();
    ArrayList<Integer> changes_count = new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(myReceiver, new IntentFilter(MyfirebaseMessagingService.INTENT_FILTER));
       setUpRealmConfig();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("FCM", token);
                    }
                });

        realm = Realm.getDefaultInstance();
        usersView = findViewById(R.id.usersList);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        reposDialog = new ReposDialogFragment();
        getSupportActionBar().hide();

        List<UserModel> userModels = realm.where(UserModel.class).findAll();
        if(userModels.size() == 0){
            progressBar.setVisibility(View.VISIBLE);
            Log.d("REALMLOG","DB is empty");
            getUsers();
            //progressbar
        }else {
            Log.d("REALMLOG","DB is not empty");
            Toast.makeText(MainActivity.this, "Loading data Realm", Toast.LENGTH_SHORT).show();

            for (int i = 0; i < userModels.size(); i++) {
                logins.add(userModels.get(i).getLogin());
                avatar_urls.add(userModels.get(i).getAvatar_url());
                repos_urls.add(userModels.get(i).getRepos_url());
                repos_words.add(userModels.get(i).getRepositories());
                user_id.add(userModels.get(i).getUserId());
                changes_count.add(i,0);
                changes_count.set(i, userModels.get(i).getChangesCount());
            }
            initRecyclerView();



            getUsers();

        }



        onUserClick();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            List<UserModel> userModels = realm.where(UserModel.class).findAll();
            for (int i = 0; i < userModels.size(); i++) {
                changes_count.set(i, userModels.get(i).getChangesCount());
            }
            initRecyclerView();
        }
    };


    public void getUsers(){

            clearData();
        Toast.makeText(MainActivity.this, "Loading data from internet", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    doc = Jsoup.connect("https://api.github.com/users").ignoreContentType(true).get();
                    users = doc.text().replace("[", "").replace("]","").replace("{","").replace("}","");
                    String[] words = users.split(",");
                    for (String word : words) {
                        word = word.replaceAll(" ","");
                        if(word.contains("\"id\"")){
                            word = word.replaceAll("\"id\":", "");

                            user_id_temp.add(Integer.parseInt(word));
                        }

                        word = word.replaceAll("\"","");


                        if(word.contains("login")){
                            word = word.replaceAll("login:", "");
                            logins_temp.add(word);


                            repos_words.add(null);
                            changes_count.add(0);

                        } else if(word.contains("avatar_url")){
                            word = word.replaceAll("avatar_url:", "");
                            avatar_urls_temp.add(word);
                        }else if(word.contains("repos_url")){
                            word = word.replaceAll("repos_url:", "");
                            repos_urls_temp.add(word);
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                logins.clear();
                avatar_urls.clear();
                repos_urls.clear();
                user_id.clear();

                for (int i = 0; i < logins_temp.size(); i++) {

                    logins.add( logins_temp.get(i));
                    avatar_urls.add(avatar_urls_temp.get(i));
                    repos_urls.add(repos_urls_temp.get(i));
                    user_id.add(user_id_temp.get(i));

                }
                logins_temp.clear();
                avatar_urls_temp.clear();
                repos_urls_temp.clear();
                user_id_temp.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        updateRealmList();
                        initRecyclerView();
                    }
                });
            }
        }).start();
    }
    
public void clearData(){
    List<UserModel> userModels = realm.where(UserModel.class).findAll();
    for (int i = 0; i < userModels.size(); i++) {
        repos_words.add(userModels.get(i).getRepositories());
    }
    realm.executeTransaction(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
            realm.deleteAll();
        }
    });
    logins_temp.clear();
    avatar_urls_temp.clear();
    repos_urls_temp.clear();
    user_id_temp.clear();

}

    public void updateRealmList(){






        Toast.makeText(MainActivity.this, "Writing data to Realm", Toast.LENGTH_SHORT).show();


        for (int i = 0; i < logins.size(); i++) {


            UserModel userModel = new UserModel();
            userModel.setId(i);
            userModel.setUserId(user_id.get(i));
            userModel.setLogin(logins.get(i));
            userModel.setAvatar_url(avatar_urls.get(i));
            userModel.setRepos_url(repos_urls.get(i));
            userModel.setRepositories(repos_words.get(i));
            userModel.setChangesCount(changes_count.get(i));

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {


                    realm.copyToRealm(userModel);

                }
            });
        }
    }

    public void onUserClick(){
        usersView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, usersView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        if(repos_words.get(position) == null) {
                            Toast.makeText(MainActivity.this, "Loading repositories list from internet", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String reposUrl = repos_urls.get(position);
                                String repositoriesDoc;
                                repos_words.add(position, "");

                                try {
                                    doc1 = Jsoup.connect(reposUrl).ignoreContentType(true).get();
                                    repositoriesDoc = doc1.text().replace("[", "").replace("]", "").replace("{", "").replace("}", "");
                                    String[] reposWords = repositoriesDoc.split(",");
                                    for (String word : reposWords) {
                                        if (word.contains("\"full_name\"")) {
                                            word = word.replaceAll("\"", "");
                                            word = word.replaceAll(" ", "");
                                            word = word.replaceAll("full_name:", "");

                                            word = word.split("/", 2)[1];


                                            repos_words.set(position, repos_words.get(position) + word + ".SPLIT.");
                                        }
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }






                                UserModel userModel = new UserModel();
                                userModel.setId(position);
                                userModel.setUserId(user_id.get(position));
                                userModel.setLogin(logins.get(position));
                                userModel.setAvatar_url(avatar_urls.get(position));
                                userModel.setRepos_url(repos_urls.get(position));
                                userModel.setRepositories(repos_words.get(position));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.copyToRealmOrUpdate(userModel);

                                            }
                                        });

                                        Bundle args = new Bundle();
                                        args.putString("repositories", repos_words.get(position));
                                        reposDialog.setArguments(args);



                                        //reposDialog.setArguments();
                                        reposDialog.show(getSupportFragmentManager(),"tag");

                                    }
                                });

                            }
                        }).start();
                    } else{
                            Bundle args = new Bundle();
                            args.putString("repositories", repos_words.get(position));
                            reposDialog.setArguments(args);



                            //reposDialog.setArguments();
                            reposDialog.show(getSupportFragmentManager(),"tag");
                        }







                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
    }

    private void initRecyclerView() {


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.usersList);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, logins, avatar_urls, changes_count);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

    }


    private void setUpRealmConfig(){

        // initialize Realm
        Realm.init(this);

        // create your Realm configuration
        RealmConfiguration config = new RealmConfiguration.
                Builder().
                allowWritesOnUiThread(true).
                build();
        Realm.setDefaultConfiguration(config);
    }

}