package com.abhigarg.notepadapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static com.abhigarg.notepadapp.Brush.defaultBgColor;

public class PaintView extends View {
    int strokeWidth;
    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    public int defaultStrokeWidth=10;
    int bgColor= defaultBgColor;
    private boolean erase=false;

    public PaintView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mPaint = new Paint();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);


    }
    public void init(DisplayMetrics metrics){
        int height=(int)(metrics.heightPixels*0.9);
        int width=metrics.widthPixels;

        mBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        mCanvas= new Canvas(mBitmap);
     //   defaultStrokeWidth=BRUSH_SIZE;

    }
    private ArrayList<Brush.FingerPath> paths=new ArrayList<>();
    private Paint mBitmapPaint=new Paint(Paint.DITHER_FLAG);
    private  ArrayList<Brush.FingerPath> undo= new ArrayList<>();

    @Override
    protected void onDraw(Canvas canvas){
        canvas.save();
        mCanvas.drawColor(defaultBgColor);

        for(Brush.FingerPath fp: paths){
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.path,mPaint);

        }
        canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
        canvas.restore();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x= event.getX();
        float y= event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
/*                if(erase){
                    touchMove(x,y);
                    mCanvas.drawPath(mPath,mPaint);
                    mPath.reset();
                    mPath.moveTo(x,y);
                    invalidate();
                }else {
*/                    touchMove(x, y);
                    invalidate();
  //              }
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    private void touchUp(){
        mPath.lineTo(mX,mY);
    }



    private static final float TOUCH_TOLERANCE=4;

    private void touchMove(float x,float y){
        float dx=Math.abs(x-mX);
        float dy=Math.abs(y-mY);

        if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
            mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
            mX=x;
            mY=y;
        }
    }
    private Path mPath;
    private float mX;
    private float mY;

    private void touchStart(float x,float y){
        int brushColor;
        strokeWidth=defaultStrokeWidth;
        brushColor=Brush.defaultColor;
        mPath=new Path();
        Brush.FingerPath fp=new Brush.FingerPath(brushColor,strokeWidth,bgColor,mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x,y);

        mX=x;
        mY=y;

    }
    public void clear(){
        paths.clear();
        invalidate();
    }


    public void saveImg(){
        int count=0;
        File sdDirectory = Environment.getExternalStorageDirectory();
        String FilePath = sdDirectory.toString() + "/Free Notes";
        File subDirectory = new File(FilePath);
        File drawing = new File(subDirectory, "Drawing");

        if (!subDirectory.exists()) {
            subDirectory.mkdir();
        }
        if (subDirectory.exists()) {
            if (drawing.exists()) {
                File[] existing=drawing.listFiles();
                try{
                    for(File file:existing){
                            if(file.getName().endsWith(".jpg")||file.getName().endsWith(".png")){
                                count++;
                            }
                        }
                    }catch (Exception e){
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                }else {
                drawing.mkdir();
            }
        }

        if (subDirectory.exists()) {
            if (drawing.exists()) {
                FileOutputStream fileOutputStream;
                File image_name=new File(drawing,"drawing"+(count+1)+".png");
                try{
                    fileOutputStream =new FileOutputStream(image_name);
                    mBitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    Toast.makeText(getContext(),"Saved in Internal Storage",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
            }
        }

    public void undo(){
        if(paths.size()>0){
            undo.add(paths.remove(paths.size()-1));
            invalidate();
        }else {
            Toast.makeText(getContext(),"Nothing to undo",Toast.LENGTH_SHORT).show();
        }
    }
    public void redo(){
        if(undo.size()>0){
            paths.add(undo.remove(undo.size()-1));
            invalidate();
        }else {
            Toast.makeText(getContext(),"Nothing to redo",Toast.LENGTH_SHORT).show();
        }
    }

    public void setStrokeWidth(int width)
    {
        defaultStrokeWidth=width;
    }

    public void setEraser(boolean isEraser){
        Toast.makeText(getContext(),"tes",Toast.LENGTH_SHORT).show();
        mPaint=new Paint();
        mPaint.setAlpha(0);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        //mPaint.setMaskFilter(null);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //mPaint.setAntiAlias(true);
        invalidate();
        //setLayerType(View.LAYER_TYPE_SOFTWARE,mPaint);
        /*this.erase=isEraser;
        if(erase) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }else {
            mPaint.setXfermode(null);
        }
*/


  /*  public void setErase(boolean isErase){
//set erase true or false
        erase=isErase;
        if(erase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mPaint.setXfermode(null);
    }*/

}}