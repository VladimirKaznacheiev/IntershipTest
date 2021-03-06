package com.example.intershiptest;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserModel extends RealmObject {

        @PrimaryKey
        long id;

        String login;
        String avatar_url;
        String repos_url;
        int changesCount;

        RealmList<ReposModel> repositories;




    public int getChangesCount() {
        return changesCount;
    }

    public void setChangesCount(int changesCount) {
        this.changesCount = changesCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }


    public String getRepos_url() {
        return repos_url;
    }

    public void setRepos_url(String repos_url) {
        this.repos_url = repos_url;
    }


    public RealmList<ReposModel> getRepositories() {
        return repositories;
    }

    public void setRepositories(RealmList<ReposModel> repositories) {
        this.repositories = repositories;
    }
}
