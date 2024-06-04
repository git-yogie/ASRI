package com.yogie.anemiaapps.helper;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.yogie.anemiaapps.signin_Activity;

import java.util.HashMap;
import java.util.Objects;

public class UserPref{
    private String documentId, name, usia, username,password,role;
    private Context context;
    private SharedPreferences userData;
    public UserPref(Context context){
        this.context = context;
        userData = context.getSharedPreferences("user",MODE_PRIVATE);
    }

    public void setUserData(String documentId, String name, String usia, String username, String password){
        SharedPreferences.Editor editor = userData.edit();
        editor.putString("documentId", documentId);
        editor.putString("nama", name);
        editor.putString("usia", usia);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }

    public  boolean isLogin(){
        return !Objects.equals(userData.getString("documentId", ""), "");
    }

    public HashMap<String, Object> getUserData(){

        documentId = userData.getString("documentId", "");
        name = userData.getString("nama", "");
        usia = userData.getString("usia", "");
        username = userData.getString("username", "");
        password = userData.getString("password", "");
        role = userData.getString("role", "");

        return new HashMap<String, Object>(){
            {
                put("documentId", documentId);
                put("nama", name);
                put("usia", usia);
                put("role", role);
                put("username", username);
                put("password", password);
            }
        };
    }

    public void clearUserData(){
        SharedPreferences.Editor editor = userData.edit();
        editor.clear();
        editor.apply();
    }

    public void logout(){
        clearUserData();
        Intent intent = new Intent(context, signin_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}

