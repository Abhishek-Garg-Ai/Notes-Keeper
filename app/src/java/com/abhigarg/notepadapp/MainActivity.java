package com.abhigarg.notepadapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhigarg.notepadapp.auth.Login;
import com.abhigarg.notepadapp.auth.Register;
import com.abhigarg.notepadapp.auth.Splash;
import com.abhigarg.notepadapp.note.AddNote;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navViewHome;
    FirebaseAuth fAuth;
    FirebaseUser user;
    TextView userEmail, userName;
    CardView addNoteBtn,savedNoteBtn, inviteFriendBtn, syncBtn, logoutBtn, privacyBtn, brushBtn, aboutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_home);

        navViewHome = findViewById(R.id.nav_view_home);
        navViewHome.setNavigationItemSelectedListener(this);
        View headerView = navViewHome.getHeaderView(0);
        TextView nevUserName = headerView.findViewById(R.id.userDisplayName);
        TextView nevUserEmail = headerView.findViewById(R.id.userDisplayEmail);

        //showing name and Email in nav view
        if (user.isAnonymous()) {
            nevUserEmail.setVisibility(View.GONE);
            nevUserName.setText("Temporary  User");
        } else {
            nevUserName.setText(user.getDisplayName());
            nevUserEmail.setText(user.getEmail());
        }

        //showing name and mail in home page
        userEmail = findViewById(R.id.homeUserMail);
        userName = findViewById(R.id.homeUserName);
        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            userName.setText("Temporary  User");
        } else {
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
        }

        //click on images in activity_main
        addNoteBtn = findViewById(R.id.addNoteImg);
        savedNoteBtn = findViewById(R.id.savedNoteImg);
        inviteFriendBtn = findViewById(R.id.inviteImg);
        syncBtn = findViewById(R.id.syncNoteImg);
        logoutBtn = findViewById(R.id.logoutImg);
        privacyBtn = findViewById(R.id.privacyImg);
        brushBtn = findViewById(R.id.brushImg);
        aboutBtn = findViewById(R.id.aboutImg);
        addNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddNote.class));
                overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);
            }
        });
        savedNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), note_list.class));
                overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);
            }
        });
        inviteFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInvite();
            }
        });
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isAnonymous()) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);

                } else {
                    Toast.makeText(getApplicationContext(), "You Are Connected", Toast.LENGTH_SHORT).show();
                }

            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUser();
            }

        });
        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), privacyPolicy.class));
                overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);
            }
        });
        brushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), com.abhigarg.notepadapp.Brush.class));
                overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);
            }
        });
        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), com.abhigarg.notepadapp.about_us.class));
                overridePendingTransition(R.anim.slide_top,R.anim.slide_bottom);
            }
        });

        //for drawer Button
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        //showing drawer
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

    }

    //handle click on navigation menu options
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.addNote:
                startActivity(new Intent(this, AddNote.class));
                break;
            case R.id.logout:
                checkUser();
                break;
            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();

                } else {
                    Toast.makeText(this, "You Are Connected", Toast.LENGTH_SHORT).show();
                }
                break;
            /*case R.id.shareApp:
                shareBtn();
                break;*/
            case R.id.inviteBtn:
                shareInvite();
                break;
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), com.abhigarg.notepadapp.about_us.class));
                break;
            case R.id.brush:
                startActivity(new Intent(this, com.abhigarg.notepadapp.Brush.class));
                break;
            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void shareInvite() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Accept my invite for the Free Notes! Get it from :\n https://play.google.com/store/apps/details?id=com.abhigarg.notepadapp");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Via");
        startActivity(shareIntent);
    }

    private void checkUser() {
        if (user.isAnonymous()) {
            displayAlert();
            return;
        } else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Splash.class));
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are You Sure?")
                .setMessage("You are Logged in with Temporary Account. Logging out will Delete all the Notes.")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(), Splash.class));
                                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                                //Toast.makeText(getApplicationContext(),"Logged in with Temporary Account.",Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });
        warning.show();
    }
}
