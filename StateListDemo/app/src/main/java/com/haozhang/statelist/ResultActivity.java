package com.haozhang.statelist;

import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView img1,img2,img3,img4,img5,img6;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });


        initView();
    }

    public void initView(){
        img1 = (ImageView) findViewById(R.id.img_1);
        img1.setOnClickListener(this);
        img2 = (ImageView) findViewById(R.id.img_2);
        img2.setOnClickListener(this);
        img1.setImageResource(R.drawable.jidou_set_selector_n);

        ResolveInfo info = getIntent().getParcelableExtra("info");
        if (null!=info){
            Drawable d = info.loadIcon(getPackageManager());
            mBitmap = MainActivity.drawableToBitmap(d);
        }
        img3 = (ImageView) findViewById(R.id.img_3);
        img4 = (ImageView) findViewById(R.id.img_4);
        img5 = (ImageView) findViewById(R.id.img_5);

        img3.setOnClickListener(this);
        img4.setOnClickListener(this);
        img5.setOnClickListener(this);
        img6 = (ImageView) findViewById(R.id.img_6);
        img6.setOnClickListener(this);
        createSel();
    }

    public void createSel(){
        // create sel bitmap
        img2.setImageBitmap(mBitmap);
        // 去灰
        mBitmap = getDayImg(mBitmap);
        img3.setImageBitmap(mBitmap);
        // 获取背景图
        Bitmap bg = MainActivity.drawableToBitmap(getResources().getDrawable(R.mipmap.ic_bg));
        Canvas canvas = new Canvas(bg);

        img4.setImageBitmap(bg);

        Paint paint = new Paint();
        int width = bg.getWidth();
        // front 前置图
        Bitmap front = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas fr = new Canvas(front);
        // 缩小的中心图
        Bitmap normal = Bitmap.createScaledBitmap(mBitmap, width / 2, width / 2, false);

        fr.drawBitmap(normal, width / 4, width / 4, paint);
        img5.setImageBitmap(front);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(normal, width/4, width/4, paint);

        //配置点击
        int pressed = android.R.attr.state_pressed;
        StateListDrawable statedrawable = new StateListDrawable();
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources r = new Resources(this.getAssets(), metrics, null);
        statedrawable.addState(new int[]{pressed}, new BitmapDrawable(r,bg));
        statedrawable.addState(new int[]{}, new BitmapDrawable(r, front));

        img6.setImageDrawable(statedrawable);
    }

    public Bitmap getDayImg(Bitmap bitmap){
        int width = bitmap.getWidth();
        Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(faceIconGreyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return faceIconGreyBitmap;
    }


    @Override
    public void onClick(View v) {

    }
}
