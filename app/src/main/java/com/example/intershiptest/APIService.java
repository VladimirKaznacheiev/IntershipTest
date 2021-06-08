package com.example.intershiptest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {

    @GET("users")
    Call<List<UserModel>> getUsers();

    @GET("/users/{login}/repos")
    Call<List<ReposModel>> getRepos(@Path("login") String login);
}
