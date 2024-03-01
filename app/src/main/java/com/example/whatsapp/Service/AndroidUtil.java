package com.example.whatsapp.Service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.whatsapp.ModelClass.User;

public class AndroidUtil {

    public static  void showToast(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(Intent intent, User model){
        intent.putExtra("username",model.getUsername());
        intent.putExtra("phone",model.getUsername());
        intent.putExtra("userId",model.getId());
        intent.putExtra("fcmToken",model.getToken());

    }

    public static User getUserModelFromIntent(Intent intent){
        User userModel = new User();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setUsername(intent.getStringExtra("phone"));
        userModel.setId(intent.getStringExtra("userId"));
        userModel.setToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }
}
