package com.abhigarg.notepadapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.freeNotes.R;
import com.abhigarg.notepadapp.MainActivity;
import com.abhigarg.notepadapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {
    EditText rUserName,rUserEmail,rUserPass,rUserConfPass;
    Button SyncAccount;
    TextView loginAct;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {

        }setContentView(R.layout.activity_register);

        rUserName=findViewById(R.id.userName);
        rUserEmail=findViewById(R.id.userEmail);
        rUserPass=findViewById(R.id.password);
        rUserConfPass=findViewById(R.id.passwordConfirm);

        backBtn=findViewById(R.id.backBtn2);
        fAuth=FirebaseAuth.getInstance();
        loginAct=findViewById(R.id.login);
        progressBar=findViewById(R.id.progressBar5);
        SyncAccount=findViewById(R.id.createAccount);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
        loginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        SyncAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uUserName=rUserName.getText().toString();
                String uUserEmail=rUserEmail.getText().toString();
                String uUserPass=rUserPass.getText().toString();
                String uUserConfPass=rUserConfPass.getText().toString();

                if(uUserConfPass.isEmpty() || uUserEmail.isEmpty() ||uUserName.isEmpty() ||uUserPass.isEmpty()){
                    Toast.makeText(getApplicationContext(),"All Fields Are Required.",Toast.LENGTH_LONG).show();
                    return;
                }
                if(!uUserConfPass.equals(uUserPass)){
                    rUserConfPass.setError("Password do not Match");
                }

                progressBar.setVisibility(View.VISIBLE);
                AuthCredential credential= EmailAuthProvider.getCredential(uUserEmail,uUserPass);
                fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(getApplicationContext(),"Notes Are Synced.",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent( getApplicationContext(),Splash.class));

                        FirebaseUser usr=fAuth.getCurrentUser();
                        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                                .setDisplayName(uUserName)
                                .build();
                        usr.updateProfile(request);

                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this,"Failed to Connect. Try Again Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });





    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}
