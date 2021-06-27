package com.abhigarg.notepadapp.note;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.abhigarg.notepadapp.MainActivity;
import com.abhigarg.notepadapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNote extends AppCompatActivity {
    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ProgressBar progressBar;
    FirebaseUser user;
    String nContent,nTitle;
    //ImageView micInput;
    FloatingActionButton fab;
    int defaultTextcolor= Color.BLACK;
    int defaultBackgroundColor=Color.WHITE;
    private int RECORD_AUDIO_PERMISSION_CODE=2;
    private int CAMERA_PERMISSION_CODE=3;
//    private int PDF_PICK_CODE=1;
    private final int STORAGE_REQUEST_CODE=100;
    Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);


        data=getIntent();
        fStore=FirebaseFirestore.getInstance();
        noteContent=findViewById(R.id.addNoteContent);
        noteTitle=findViewById(R.id.addNoteTitle);

        progressBar=findViewById(R.id.addNoteprogressBar);
        noteContent.setTextColor(defaultTextcolor);
        noteContent.setBackgroundColor(defaultBackgroundColor);
        try {
            noteTitle.setText(data.getStringExtra("title"));
            noteContent.setText(data.getStringExtra("content"));
        } catch (Exception e) {

        }
        fab = findViewById(R.id.btnEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nTitle = noteTitle.getText().toString();
                nContent = noteContent.getText().toString();


                if (nTitle.isEmpty() && nContent.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Not saved notes with Empty Fields",Toast.LENGTH_LONG).show();
                    return;
                }else {
                    if(nTitle.isEmpty()){
                        nTitle="Untitled";
                    }
                    progressBar.setVisibility(View.VISIBLE);
                    //progressBarSave.setVisibility(View.VISIBLE);
                    user = FirebaseAuth.getInstance().getCurrentUser();

                    DocumentReference docref = fStore.collection("notes").document(user.getUid()).collection("myNotes").document();
                    Map<String, Object> note = new HashMap<>();
                    note.put("title", nTitle);
                    note.put("content", nContent);
                    note.put("textColor", defaultTextcolor);
                    note.put("backgoundColor", defaultBackgroundColor);
                    docref.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddNote.this, "Note Added", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), com.abhigarg.notepadapp.note_list.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddNote.this, "Error,Try Again", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



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
        menuInflater.inflate(R.menu.addnote_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.speechTextBtn:
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                requestRecordAudioPermission();
            }else {
                    getSpeechInput();
                }return true;
            case R.id.textDetectionBtn:
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    requestCameraPermission();

                    }else {
                    Intent i=new Intent(getApplicationContext(), com.abhigarg.notepadapp.textDetection.class);
                    i.putExtra("title",noteTitle.getText().toString());
                    i.putExtra("content",noteContent.getText().toString());
                    startActivity(i);
                }return true;
            case R.id.readPdfBtn:
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else{
                    Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent,1);


                }return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestStoragePermission() {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_REQUEST_CODE);

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
                    String previousText=noteContent.getText().toString();
                    nContent=previousText+result.get(0);
                    noteContent.setText(nContent);
                }break;
               // File file=new File();
            case 1:
                if(resultCode==RESULT_OK && data!=null) {
                    Uri pdfFileUri = data.getData();
                    File pdfFile = new File(String.valueOf(pdfFileUri));
                    //String filePath=pdfFile.getAbsolutePath();
                    String stringParser;
                    String filePath = null;
                    if (pdfFileUri.toString().startsWith("content://")) {
                        String[] projetion = {MediaStore.Images.Media.DATA};
                        CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), pdfFileUri, projetion, null, null, null);
                        Cursor cursor = cursorLoader.loadInBackground();
                        int column = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        filePath = cursor.getString(column);
                        cursor.close();
                    } else if (pdfFileUri.toString().startsWith("file://")) {
                        filePath = pdfFile.getName();

                    }
                    try {
                        PdfReader pdfReader = new PdfReader(filePath);
                        int ln=pdfReader.getNumberOfPages();
                        String content=new String();
                        for(int i=1;i<=ln;i++){
                            content += PdfTextExtractor.getTextFromPage(pdfReader, i);
                        }

                        pdfReader.close();
                        noteContent.setText(content);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
                break;

        }
    }
    private void requestRecordAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Input Speech Text!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AddNote.this,new String[]  {Manifest.permission.RECORD_AUDIO},RECORD_AUDIO_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Input Scan Text!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AddNote.this,new String[]  {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
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
