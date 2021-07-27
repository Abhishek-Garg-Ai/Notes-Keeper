package com.abhigarg.notepadapp.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.abhigarg.notepadapp.MainActivity;
import com.abhigarg.notepadapp.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import petrov.kristiyan.colorpicker.ColorPicker;

public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    String noteTitle,noteContent;
    int Textcolor;
    int bgColor;
    private AdView mAdView;
    private int CAMERA_PERMISSION_CODE=3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data = getIntent();
        fStore = fStore.getInstance();
        editNoteContent = findViewById(R.id.editNoteContent);
        editNoteTitle = findViewById(R.id.editNoteTitle);
        noteTitle = data.getStringExtra("title");
        noteContent = data.getStringExtra("content");

        //Textcolor= Color.BLACK;
        //bgColor=Color.WHITE;
        Textcolor=NoteDetails.textColor;
        bgColor=NoteDetails.backgroundColor;
        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);
        editNoteContent.setTextColor(data.getIntExtra("textcolor", Textcolor));
        editNoteContent.setBackgroundColor(data.getIntExtra("backgroundColor",bgColor));
        progressBar = findViewById(R.id.progressBar2);

        FloatingActionButton fab=findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nTitle = editNoteTitle.getText().toString();
                String nContent = editNoteContent.getText().toString();
                if (nTitle.isEmpty() && nContent.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Not saved notes with Empty Fields",Toast.LENGTH_LONG).show();
                    return;
                }else if(nTitle.isEmpty()){
                    nTitle="Untitled";
                    progressBar.setVisibility(View.VISIBLE);

                }else {
                    progressBar.setVisibility(View.VISIBLE);

                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String, Object> note = new HashMap<>();
                note.put("title", nTitle);
                note.put("content", nContent);
                note.put("textColor",Textcolor);
                note.put("backgroundColor",bgColor);


                docref.update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditNote.this, "Note Saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),com.abhigarg.notepadapp.note_list.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error,Try Again", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
            }

        });


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }
    public boolean onOptionItemSelected(@NonNull MenuItem item){
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.editnote_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.colorPickerBtn:
                openColorPicker_textColor();
                return true;
            case R.id.speech_text_mic:
                if(ContextCompat.checkSelfPermission(EditNote.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                    requestRecordAudioPermission();
                }else {
                    getSpeechInput();
                }return true;
            case R.id.backgroundColorBtn:
                openColorPicker_bgBtn();
                return true;
            case R.id.scanText:
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    requestCameraPermission();

                }else {
                    Intent i=new Intent(getApplicationContext(), com.abhigarg.notepadapp.EditNote_textDetection.class);
                    i.putExtra("title",editNoteTitle.getText().toString());
                    i.putExtra("content",editNoteContent.getText().toString());
                    i.putExtra("noteId",data.getStringExtra("noteId"));
                    i.putExtra("textColor",Textcolor);
                    i.putExtra("backgroundColor",bgColor);
                    startActivity(i);
                }
        }
        return super.onOptionsItemSelected(item);
    }


    private void openColorPicker_bgBtn() {
        final ColorPicker colorPicker=new ColorPicker(this);
        ArrayList<String> colors=new ArrayList<>();
        colors.add("#00FFFDFD");
        colors.add("#FAEF8E");//1
        colors.add("#F4E18E");//7
        colors.add("#F4BF7D");//10
        colors.add("#CEFA9F");
        colors.add("#C1FA80");//2
        colors.add("#3DF344");
        colors.add("#EA79FD");//5
        colors.add("#FF8279");//3
        colors.add("#EC2B26");
        colors.add("#BFE4F4");//8
        colors.add("#7E8EEF");
        colors.add("#687EF4");//4
        colors.add("#000000");

        colorPicker.setColors(colors)
                .setColumns(5).setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        editNoteContent.setBackgroundColor(color);
                        bgColor=color;
                    }

                    @Override
                    public void onCancel() { }
                }).show();

    }


    public void openColorPicker_textColor(){
        final ColorPicker colorPicker=new ColorPicker(this);
        ArrayList<String> colors=new ArrayList<>();
        colors.add("#00FFFDFD");
        colors.add("#FAEF8E");//1
        colors.add("#F4E18E");//7
        colors.add("#F4BF7D");//10
        colors.add("#CEFA9F");
        colors.add("#C1FA80");//2
        colors.add("#3DF344");
        colors.add("#EA79FD");//5
        colors.add("#FF8279");//3
        colors.add("#EC2B26");
        colors.add("#BFE4F4");//8
        colors.add("#7E8EEF");
        colors.add("#687EF4");//4
        colors.add("#000000");

        colorPicker.setColors(colors)
                .setColumns(5).setRoundColorButton(true)
                .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        editNoteContent.setTextColor(color);
                        Textcolor=color;
                    }

                    @Override
                    public void onCancel() { }
                }).show();
    }


    public void getSpeechInput(){
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something! ");
        try{
            startActivityForResult(intent,10);
        }
        catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK && data!=null){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String previousText=editNoteContent.getText().toString();
                    noteContent=previousText+result.get(0);
                    editNoteContent.setText(noteContent);
                }
        }
    }
    private int RECORD_PERMISSION_CODE=2;
    private void requestRecordAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Input Speech Text!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditNote.this,new String[]  {Manifest.permission.RECORD_AUDIO},RECORD_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},RECORD_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==RECORD_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Access Granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Access Denied",Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Input Scan Text!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditNote.this,new String[]  {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }
    }
}
