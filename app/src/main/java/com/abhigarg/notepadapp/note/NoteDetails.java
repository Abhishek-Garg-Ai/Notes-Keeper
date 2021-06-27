package com.abhigarg.notepadapp.note;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.abhigarg.notepadapp.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Locale;

public class NoteDetails extends AppCompatActivity {
    private TextToSpeech mtts;
    private ImageView mic;
    TextView content,title;
    private AdView mAdView;
    CoordinatorLayout myLayout;
    String noteContent,noteTitle;
    //Button shareBtn;
    Intent data;
    //ImageView editBtn;
    FirebaseFirestore fStore;
    public static int textColor;
    public static int backgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        fStore=fStore.getInstance();
        mic = findViewById(R.id.mic);
        data = getIntent();

        content = findViewById(R.id.noteDetailsContent);
        title = findViewById(R.id.noteDetailsTitle);
        content.setMovementMethod(new ScrollingMovementMethod());

        noteTitle=data.getStringExtra("title");
        noteContent=data.getStringExtra("content");

        String noteTitleStr;
        if(noteTitle.length()<15){
            noteTitleStr=noteTitle;
        }else{
            noteTitleStr=noteTitle.substring(0,15)+"...";
        }
        textColor=data.getIntExtra("textColor",Color.BLACK);
        backgroundColor=data.getIntExtra("backgroundColor",Color.WHITE);
        myLayout=findViewById(R.id.noteDetailsLayout);
        content.setText(noteContent);
        title.setText(noteTitleStr);
        content.setTextColor(data.getIntExtra("textColor",Color.BLACK));
        myLayout.setBackgroundColor(data.getIntExtra("backgroundColor",Color.WHITE));


        FloatingActionButton fab = findViewById(R.id.btnEdit);
        fab=findViewById(R.id.btnEdit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mtts.stop();
                Intent i = new Intent(view.getContext(), com.abhigarg.notepadapp.note.EditNote.class);
                i.putExtra("title", data.getStringExtra("title"));
                i.putExtra("content",noteContent);
                i.putExtra("noteId", data.getStringExtra("noteId"));
                i.putExtra("textColor", data.getIntExtra("textColor",textColor));
                i.putExtra("backgroundColor",data.getIntExtra("backgroundColor",backgroundColor));
                startActivity(i);
                finish();

            }
        });

        mtts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mtts.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language Not Supported");
                    } else {
                        mic.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialisation failed");
                }
            }
        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mtts.speak(noteContent, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        myLayout=findViewById(R.id.noteDetailsLayout);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void export() {
        int count=0;
        File sdDirectory = Environment.getExternalStorageDirectory();
        String FilePath = sdDirectory.toString() + "/Free Notes";
        File subDirectory = new File(FilePath);
        File txt = new File(subDirectory, "Notes");

        if (!subDirectory.exists()) {
            subDirectory.mkdir();
        }
        if(subDirectory.exists()){
            if (txt.exists()) {
                File[] existing=txt.listFiles();
                try{
                    for(File file:existing){
                        if(file.getName().startsWith(noteTitle)){
                            count++;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }else{
                txt.mkdir();
            }
        }

        if (subDirectory.exists()) {
            if (txt.exists()) {
                File text_name = new File(txt, noteTitle +count+ ".txt");

                try {
                    FileWriter writer = new FileWriter(text_name);
                    writer.append(noteContent);
                    writer.flush();
                    writer.close();
                    Toast.makeText(getApplicationContext(),"Saved in Internal Storage", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(mtts!=null){
            mtts.stop();
            mtts.shutdown();
        }
        super.onDestroy();
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
        menuInflater.inflate(R.menu.notedetails_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.shareTxtBtn:
                if(ContextCompat.checkSelfPermission(NoteDetails.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    shareText();
                }
                return true;
            case R.id.sharePdfBtn:
                if(ContextCompat.checkSelfPermission(NoteDetails.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestReadStoragePermission();
                }else {
                    sharePdf();
                }return true;

            case R.id.exportTxtBtn:
                if(ContextCompat.checkSelfPermission(NoteDetails.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    export();
                }return true;
            case R.id.exportPdfBtn:
                if(ContextCompat.checkSelfPermission(NoteDetails.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    createPdf();
                }return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePdf() {
        File pdf_file_name=createPdf();
        try {
            Uri uri = Uri.fromFile(pdf_file_name);

            File file = new File(uri.getPath());Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            Uri myuri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            sendIntent.putExtra(Intent.EXTRA_STREAM, myuri);
            sendIntent.setType("application/pdf");
            Intent shareIntent = Intent.createChooser(sendIntent, "Share Via");
            startActivity(shareIntent);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    private void shareText() {
        Intent sendIntent=new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,noteTitle+"\n"+noteContent);
        sendIntent.setType("text/plain");
        Intent shareIntent=Intent.createChooser(sendIntent,"Share Via");
        startActivity(shareIntent);

    }
    private int STORAGE_PERMISSION_CODE=1;
    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Export File")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(NoteDetails.this,new String[]  {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE || requestCode==READ_STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Access Granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Access Denied",Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private int READ_STORAGE_PERMISSION_CODE=1;
    private void requestReadStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to Share File")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(NoteDetails.this,new String[]  {Manifest.permission.READ_EXTERNAL_STORAGE},READ_STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},READ_STORAGE_PERMISSION_CODE);
        }
    }

    public File createPdf(){
        File pdf_file_name = null;
        int count=0;
        File subDirectory=new File(Environment.getExternalStorageDirectory(),"Free Notes");
        File pdf=new File(subDirectory,"Pdf");

        if(!subDirectory.exists()){
            subDirectory.mkdir();
        }
        if(subDirectory.exists()){
            if (pdf.exists()) {
                File[] existing=pdf.listFiles();
                try{
                    for(File file:existing){
                        if(file.getName().startsWith(noteTitle)){
                            count++;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }else{
                pdf.mkdir();
            }
        }

        if(subDirectory.exists()){
            if(pdf.exists()){
             Document doc=new Document();
             File pdf_name ;
             if(count!=0) {
                 pdf_name = new File(pdf, noteTitle + count + ".pdf");
             }else{
                 pdf_name = new File(pdf, noteTitle + ".pdf");
             }
             pdf_file_name=pdf_name;
                try{
                    PdfWriter pdfWriter = null;
                    pdfWriter.getInstance(doc,new FileOutputStream(pdf_name));
                    doc.open();
                    //Font paraFont= new Font(Font.BOLDITALIC);
                    Paragraph para= new Paragraph(noteContent,FontFactory.getFont(FontFactory.TIMES,9, Font.NORMAL, BaseColor.BLACK));
                    doc.add(para);
                    doc.close();
                    Toast.makeText(this,"Saved in Internal Storage ",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        return pdf_file_name;
    }
}
