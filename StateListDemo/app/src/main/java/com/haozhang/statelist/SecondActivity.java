package com.haozhang.statelist;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar mR;
    private SeekBar mG;
    private SeekBar mB;
    private SeekBar mA;
    private SeekBar mD;
    private ImageView mImg;
    private ImageView mPmg;
    private Bitmap mBitmap;
    private TextView mResult;
    private TextView mResults;
    private ImageView mPSimg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent(SecondActivity.this,ResultActivity.class);
                i.putExtra("info", getIntent().getParcelableExtra("info"));
                startActivity(i);*/
            }
        });

        initView();
    }

    public void initView(){
        mR = (SeekBar) findViewById(R.id.seel_R);
        mG = (SeekBar) findViewById(R.id.seel_G);
        mB = (SeekBar) findViewById(R.id.seel_B);
        mA = (SeekBar) findViewById(R.id.seel_A);
        mD = (SeekBar) findViewById(R.id.seel_D);
        mD.setOnSeekBarChangeListener(this);
        mR.setOnSeekBarChangeListener(this);
        mG.setOnSeekBarChangeListener(this);
        mB.setOnSeekBarChangeListener(this);
        mA.setOnSeekBarChangeListener(this);
        mImg = (ImageView) findViewById(R.id.Simg);
        mPmg = (ImageView) findViewById(R.id.Pimg);
        mPSimg = (ImageView) findViewById(R.id.PSimg);
        ResolveInfo info = getIntent().getParcelableExtra("info");
        if (null!=info){
            Drawable d = info.loadIcon(getPackageManager());
            mBitmap = MainActivity.drawableToBitmap(d);
        }else {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_app);
        }
        mImg.setImageBitmap(mBitmap);
        mResult = (TextView) findViewById(R.id.result);
        mResults = (TextView) findViewById(R.id.result_s);

        mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float r = (float) mR.getProgress() / 128;
                float g = (float) mG.getProgress() / 128;
                float b = (float) mB.getProgress() / 128;
                float a = (float) mA.getProgress() / 128;
                ColorMatrix m = new ColorMatrix();
                m.set(getArray(r, g, b, a));
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(m);
                Bitmap bmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bmp);
                Paint paint = new Paint();
                paint.setColorFilter(f);
                canvas.drawBitmap(bmp, 0, 0, paint);
                mImg.setImageBitmap(bmp);
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seel_D){
            float d = (float)mD.getProgress() / 128;
            mPmg.setImageBitmap(getDayImg(d,mBitmap));
            mResults.setText("setSaturation = "+d);
        }else {
            float r = (float)mR.getProgress() / 128;
            float g = (float)mG.getProgress() / 128;
            float b = (float)mB.getProgress() / 128;
            float a = (float)mA.getProgress() / 128;
            ColorMatrix m = new ColorMatrix();
            m.set(getArray(r, g, b, a));
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(m);
            Bitmap bmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();
            paint.setColorFilter(f);
            canvas.drawBitmap(bmp, 0, 0, paint);
            mImg.setImageBitmap(bmp);
            mResult.setText("ARGB : \r\n " + "R = " + r + "\r\n  G = " + g + "\r\n  B = " + b + "\r\n  A = " + a);
        }

        float r = (float)mR.getProgress() / 128;
        float g = (float)mG.getProgress() / 128;
        float b = (float)mB.getProgress() / 128;
        float a = (float)mA.getProgress() / 128;
        ColorMatrix m = new ColorMatrix();
        m.set(getArray(r, g, b, a));
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(m);
        Bitmap bmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColorFilter(f);
        canvas.drawBitmap(bmp, 0, 0, paint);
        float d = (float)mD.getProgress() / 128;
        mPSimg.setImageBitmap(getDayImg(d,bmp));
    }


    public Bitmap getDayImg(float f,Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(faceIconGreyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return faceIconGreyBitmap;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    public float[] getArray(float r,float g,float b,float a){
        float[] carray = new float[20];
        //黑白
        carray[0]=r;
        carray[1]=g;
        carray[2]=b;
        carray[3]=0;
        carray[4]=0;
        carray[5]=r;
        carray[6]=g;
        carray[7]=b;
        carray[8]=0;
        carray[9]=0;
        carray[10]=r;
        carray[11]=g;
        carray[12]=b;
        carray[13]=0;
        carray[14]=0;
        carray[15]=0;
        carray[16]=0;
        carray[17]=0;
        carray[18]=a;
        carray[19]=0;
        return carray;
    }

}
