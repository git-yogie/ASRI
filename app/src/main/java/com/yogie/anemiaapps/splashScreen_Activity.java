package com.yogie.anemiaapps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.FoldingCube;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

public class splashScreen_Activity extends AppCompatActivity {

    ImageView imageView;
    private SpinKitView spinKitView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        imageView = findViewById(R.id.imageViews);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        imageView.startAnimation(animation);

        TextView textView = findViewById(R.id.textViws);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        textView.startAnimation(animation1);

        SharedPreferences userData = getSharedPreferences("user",MODE_PRIVATE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        spinKitView = findViewById(R.id.spin_kit);
        Sprite plane = new FoldingCube();
        spinKitView.setIndeterminateDrawable(plane);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(userData.getString("documentId","").equals("")){

                    Intent intent = new Intent(splashScreen_Activity.this, signin_Activity.class);
                    startActivity(intent);
                    finish();

                }else{
                    db.collection("users").document(userData.getString("documentId","")).get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){

                            if(task.getResult().exists()){
                                Intent intent = new Intent(splashScreen_Activity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(splashScreen_Activity.this, signin_Activity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                }

            }
        }, 300);



    }
}