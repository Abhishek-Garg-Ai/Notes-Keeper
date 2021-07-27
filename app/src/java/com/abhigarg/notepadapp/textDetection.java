package com.abhigarg.notepadapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhigarg.notepadapp.note.AddNote;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;



public class textDetection extends AppCompatActivity {
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int STORAGE_REQUEST_CODE=1;
    EditText mResultEt;
    ImageView mPreviewIv;
    ImageView okBtn,cancelBtn;
    private int IMAGE_PICK_GALLERY_CODE=1000;
    private int IMAGE_PICK_CAMMERA_CODE=1001;
    Uri image_uri;
    public String text="";
    Intent prevIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_text_detection);

        mResultEt=findViewById(R.id.resultEt);
        mPreviewIv=findViewById(R.id.imageIv);
        okBtn=findViewById(R.id.textOk);
        cancelBtn=findViewById(R.id.textNo);
           }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.addImage:
                showImageImportDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void showImageImportDialog() {
        String items[]={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                        requestStoragePermission();
                    }else{
                    pickCamera();
                    }
                }

                if(which==1){
                    if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                        requestStoragePermission();
                    }else {
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void pickGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take image from camera, iot will also be save to storage to get high quallity image
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pic");  //title of picture
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");
        image_uri =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMMERA_CODE);

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_REQUEST_CODE);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_HANDLE_CAMERA_PERM:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickCamera();
                }else{
                    Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickGallery();
                }else {
                    Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                }
                break;

        }

        /*
        if(requestCode!=RC_HANDLE_CAMERA_PERM){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }if(grantResults.length!=0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            startTextRecognizer();
            return;
        }
*/
    }


    //handle image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        //got image from cammera
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //got image from gallery now crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)  //enable image guideline
                        .start(this);
            }
            if (requestCode == IMAGE_PICK_CAMMERA_CODE) {
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }

        //get cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resultUri=result.getUri();  //get image uri

                //set image to image view
                mPreviewIv.setImageURI(resultUri);


                //get drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable=(BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap=bitmapDrawable.getBitmap();

                TextRecognizer recognizer=new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }else {
                    Frame frame=new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items=recognizer.detect(frame);
                    final StringBuilder sb=new StringBuilder();

                    //get text from sb until there is no text
                    for(int i =0;i<items.size();i++){
                        TextBlock myitem=items.valueAt(i);
                        sb.append(myitem.getValue());
                        sb.append("\n");
                    }

                    //set text to edit text
                    mResultEt.setText(sb.toString());
                    okBtn.setVisibility(View.VISIBLE);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String title,content;
                            prevIntent=getIntent();
                            title=prevIntent.getStringExtra("title");
                            String previousText=prevIntent.getStringExtra("content");
                            content=previousText+sb.toString();
                            Intent intent=new Intent(getApplicationContext(),AddNote.class);
                            intent.putExtra("title",title);
                            intent.putExtra("content",content);
                            startActivity(intent);
                            finish();


                        }
                    });
                    cancelBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                }
            }
            else if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, (CharSequence) result.getError(),Toast.LENGTH_SHORT).show();
            }
        }

    }
}
