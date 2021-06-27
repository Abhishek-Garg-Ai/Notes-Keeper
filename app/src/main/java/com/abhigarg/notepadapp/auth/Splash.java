package com.abhigarg.notepadapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.freeNotes.R;
import com.abhigarg.notepadapp.MainActivity;
import com.abhigarg.notepadapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {
    FirebaseAuth fAuth;
    ImageView img;
    TextView appName,disp;
    Animation top,bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {

        }
        setContentView(R.layout.activity_splash);

        img=findViewById(R.id.imageViewSplash);
        appName=findViewById(R.id.textViewSplash);
        disp=findViewById(R.id.textView2Splash);
        top= AnimationUtils.loadAnimation(this,R.anim.top);
        bottom= AnimationUtils.loadAnimation(this,R.anim.bottom);

        img.setAnimation(top);
        appName.setAnimation(bottom);
        disp.setAnimation(bottom);

        fAuth=FirebaseAuth.getInstance();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(fAuth.getCurrentUser()!=null){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }else{
                    //create new anonyoums account
                    fAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(Splash.this,"Logged in With Temporary Account",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Splash.this,"Error"+e.getMessage(),Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        },1000);
    }
}
