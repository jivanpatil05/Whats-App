package com.example.whatsapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.whatsapp.ModelClass.User;
import com.example.whatsapp.R;
import com.example.whatsapp.Service.AndroidUtil;
import com.example.whatsapp.Service.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    ImageView logo;
    TextView name, own1, own2;
    Animation topAnim, bottomAnim;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        logo = findViewById(R.id.logoimg);
        name = findViewById(R.id.logonameimg);
        own1 = findViewById(R.id.ownone);
        own2 = findViewById(R.id.owntwo);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logo.setAnimation(topAnim);
        name.setAnimation(bottomAnim);
        own1.setAnimation(bottomAnim);
        own2.setAnimation(bottomAnim);

        if (getIntent().getExtras() != null) {
            String userId = getIntent().getExtras().getString("userId");
            if (userId != null) {
                // Retrieve user data from Firebase
                FirebaseUtil.allUserReference().child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User model = snapshot.getValue(User.class);
                            // Start SendMessageActivity with user data
                            Intent intent = new Intent(SplashActivity.this, SendMessageActivity.class);
                            intent.putExtra("userid", userId);
                            intent.putExtra("recevarToken", model.getToken()); // Assuming you have a method to get the receiver's token
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled event if needed
                    }
                });
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }, 3000);
        }

    }
}

