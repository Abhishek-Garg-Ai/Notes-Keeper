package com.abhigarg.notepadapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import yuku.ambilwarna.AmbilWarnaDialog;

public class Brush extends AppCompatActivity {
    public static int defaultColor=Color.BLACK;
    public static int defaultBgColor=Color.WHITE;
    ImageView brushColor,eraseBtn;
    public static boolean eraserMode=false;
    //public static boolean eraserMode=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush);
        //Toolbar toolbar=findViewById(R.id.brush_toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SeekBar seekBar = findViewById(R.id.seekBar);
        final TextView textView = findViewById(R.id.current_pen_size);
        textView.setText("Pen Size: " + seekBar.getProgress());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paintView.setStrokeWidth(seekBar.getProgress());
                textView.setText("Pen Size: " + seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        brushColor = findViewById(R.id.colorBtn);
        brushColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenColorPicker(false);
            }
        });
    }

    private boolean isDrawInit;
    private int STORAGE_PERMISSION_CODE=1;
    @Override
    protected void onResume(){
        super.onResume();
        if(!isDrawInit){
            initDraw();


            isDrawInit=true;
        }
    }
    private com.abhigarg.notepadapp.PaintView paintView;

    private void initDraw(){
        paintView=findViewById(R.id.paintView);
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        paintView.init(metrics);
    }

    static  class FingerPath{
        int color;
        int bgColor;
        int strokeWidth;
        Path path;

        FingerPath(int color,int strokeWidth,int bgColor,Path path){
            this.color=color;
            this.bgColor=bgColor;
            this.strokeWidth=strokeWidth;
            this.path=path;
        }

    }


    private void OpenColorPicker(boolean AlphaSupport){
        AmbilWarnaDialog ambilWarnaDialog=new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor=color;

            }
        });
        ambilWarnaDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.brush_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this).setTitle("Permission Needed!")
                    .setMessage("Needed to save Image")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Brush.this,new String[]  {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
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
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Access Granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Access Denied",Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_button:
                paintView.clear();
                return true;
            case R.id.save_button:
                if(ContextCompat.checkSelfPermission(Brush.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    paintView.saveImg();
                }
                return true;
            case R.id.bgColorBtn:
                OpenBgColorPicker(false);
                return true;
            case R.id.undoBtn:
                paintView.undo();
                return true;
            case R.id.redoBtn:
                paintView.redo();
                return true;
     /*       case R.id.colorBtn:
                OpenColorPicker(false);
       */ }
        return super.onOptionsItemSelected(item);
    }

    private void OpenBgColorPicker(boolean AlphaSupport){
        AmbilWarnaDialog ambilWarnaDialog=new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultBgColor=color;
                paintView.setBackgroundColor(color);


            }
        });
        ambilWarnaDialog.show();
    }
}
