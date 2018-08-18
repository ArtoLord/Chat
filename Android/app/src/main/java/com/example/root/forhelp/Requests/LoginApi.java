package com.example.root.forhelp.Requests;

import com.example.root.forhelp.Adapters.RoomAdapter;
import com.example.root.forhelp.User;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by root on 07.03.18.
 */

public interface LoginApi {

    @POST("user")
    Call<User> setUser(@Body RegUser user);

    @POST("login")
    Call<User> login(@Body LogUser body);





    @Multipart
    @POST("/upload")
    Call<String> upload(
            @Part("description")RequestBody description,
            @Part MultipartBody.Part file
            );
    @GET("/imageload")
    Call<ResponseBody> imageload(
           @Query("id") String id
    );

    @GET("/getroomlist")
    Call<ArrayList<ArrayList<String>>> getroomlist(@Header("Authorization") String jwt);

    @GET("/getallroom")
    Call<ArrayList<ArrayList<String>>> getallroom(@Header("Authorization") String jwt);

    @POST("/getmessages")
    Call<List<Message>> getmessages(@Header("Authorization") String jwt,@Body To to);

    @POST("/getusers")
    Call<ArrayList<ArrayList<String>>> getusers(@Header("Authorization") String jwt, @Body To to);
    @POST("/set_room_avatar")
    Call<ResponseBody> setavatar(@Body RoomAvatar roomAvatar);
}
