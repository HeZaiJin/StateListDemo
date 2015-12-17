package com.haozhang.statelist;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by haozhang on 2015/12/4.
 * 可根据包名生成图片
 */
public class BitmapUtils {
    final String TAG = "BitmapUtils";
    private static  int LEFT= 1;
    private static  int RIGHT= 2;
    private static  int TOP= 3;
    private static  int BOTTOM= 4;
    private LruCache<String ,Bitmap> mLruCache;
    private ExecutorService mThreadEs = null;
    private FileUtils mFileUtils;
    private PackageManager mManager;
    private Resources mRes;
    private Context mContext;

    public interface Callback{
        abstract void onComplete(String tag,Drawable drawable);
    }

    public BitmapUtils(Context context){
        this.mContext = context;
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(metrics);
        mRes= new Resources(mContext.getAssets(), metrics, null);
        mFileUtils = new FileUtils(context);
        mManager = context.getPackageManager();
        ActivityManager manager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //设置集合容量为可用内存的1/2
        int maxSize=manager.getMemoryClass()/8;
        mLruCache = new LruCache<String , Bitmap>(maxSize);
    }

    /**
     * 取消
     */
    public void cancle(){
        if (null!=mThreadEs){
            mThreadEs.shutdownNow();
            mThreadEs = null;
        }
    }

    public ExecutorService getThread(){
        if(mThreadEs == null){
            synchronized(ExecutorService.class){
                if(mThreadEs == null){
                    mThreadEs = Executors.newFixedThreadPool(2);
                }
            }
        }
        return mThreadEs;
    }

    /**
     * 根据app信息获取点击select drawable
     * @param tag
     * @param callback
     */
    public void getDrawableWithInfo(final String tag, final Callback callback) {

        getThread().execute(new Runnable() {
            @Override
            public void run() {
                // 判断lrc , 如果本地内存中含有，则直接返回
                Drawable drawable = null;
                if (null!=mLruCache.get(tag+".png") && null !=mLruCache.get(tag+"_sel.png")){
                    drawable=createStatedListDrawable(new BitmapDrawable(mRes,mLruCache.get(tag+"_sel.png")),new BitmapDrawable(mRes, mLruCache.get(tag+".png")));
                }
                if (null != drawable) {
                    callback.onComplete(tag, drawable);
                    return;
                }
                // 判断本地文件
                drawable = getSelectorDrawableWithPath(mContext, tag);
                if (null != drawable) {
                    callback.onComplete(tag, drawable);
                    return;
                }

                // 动态构建图片
                Bitmap bitmapWithInfo = getBitmapWithPackageName(tag);
                Bitmap bg = drawableToBitmapByBD(mContext.getResources().getDrawable(R.mipmap.ic_bg));
                bitmapWithInfo= getSaturationBitmap(bitmapWithInfo);
                bitmapWithInfo = Bitmap.createScaledBitmap(bitmapWithInfo, bg.getWidth() / 2, bg.getWidth() / 2, true);
                drawable =createStatedListDrawable(
                        createBehindDrawableWithBitmap( bg.copy(Bitmap.Config.ARGB_8888, true),bitmapWithInfo, bg.getWidth(), tag),
                        createFrontDrawableWithBitmap(bitmapWithInfo, bg.getWidth(), tag));
                callback.onComplete(tag,drawable);
            }
        });
    }

    public Drawable createStatedListDrawable(Drawable behind,Drawable front){
        StateListDrawable statedrawable = new StateListDrawable();
        int selected = android.R.attr.state_selected;
        statedrawable.addState(new int[]{selected}, behind);
        statedrawable.addState(new int[]{},front);
        return statedrawable;
    }


    public Drawable getSelectorDrawableWithPath(Context context, String path){
        Bitmap front = mFileUtils.getBitmap(path+".png");
        Bitmap bg = mFileUtils.getBitmap(path + "_sel.png");

        if (null!=front && null!=bg){
            // 放入lrucache
            mLruCache.put(path + ".png", front);
            mLruCache.put(path + "_sel.png", bg);
            Drawable drawable=createStatedListDrawable(new BitmapDrawable(mRes,bg),new
                    BitmapDrawable(mRes, front));
            return drawable;
        }
        return null;
    }
    public Drawable createFrontDrawableWithBitmap(Bitmap front,int width,String path){
        Bitmap bitmap  =Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        canvas.drawBitmap(front, width / 4, width / 4, p);
        try {
            mLruCache.put(path+".png",front);
            mFileUtils.savaBitmap(path+".png" ,bitmap);
        }catch (IOException e){
            e.printStackTrace();
        }
        return new BitmapDrawable(mRes,bitmap);
    }


    public Drawable createBehindDrawableWithBitmap(Bitmap behind,Bitmap front,int  width,String path){
        Canvas canvas = new Canvas(behind);
        Paint p = new Paint();
        canvas.drawBitmap(front, width / 4, width / 4, p);
        try {
            mLruCache.put(path+"_sel.png",behind);
            mFileUtils.savaBitmap(path + "_sel.png",behind);
        }catch (IOException e){
            e.printStackTrace();
        }
        return new BitmapDrawable(mRes,behind);
    }



    public Bitmap getBitmapWithPackageName(String pck){
        Bitmap bitmap = null;
        try {
            bitmap = drawableToBitmapByBD(mManager.getApplicationIcon(pck));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }


    /**
     * 灰化后的图片
     * @param bitmap
     * @returnint
     */
    public static Bitmap getSaturationBitmap(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(faceIconGreyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return faceIconGreyBitmap;
    }

    /**
     * 保存图片为PNG
     *
     * @param bitmap
     * @param name
     */
    public static void savePNG_After(Bitmap bitmap, String name) {
        File file = new File(name);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After(Context context,Bitmap bitmap, String path) {
        File file = new File(context.getExternalCacheDir()+path);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap createBitmapForFotoMix(Bitmap first, Bitmap second, int direction) {
        if (first == null) {
            return null;
        }
        if (second == null) {
            return first;
        }
        int fw = first.getWidth();
        int fh = first.getHeight();
        int sw = second.getWidth();
        int sh = second.getHeight();
        Bitmap newBitmap = null;
        if (direction == LEFT) {
            newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, sw, 0, null);
            canvas.drawBitmap(second, 0, 0, null);
        } else if (direction == RIGHT) {
            newBitmap = Bitmap.createBitmap(fw + sw, fh > sh ? fh : sh, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, fw, 0, null);
        } else if (direction == TOP) {
            newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, sh, null);
            canvas.drawBitmap(second, 0, 0, null);
        } else if (direction == BOTTOM) {
            newBitmap = Bitmap.createBitmap(sw > fw ? sw : fw, fh + sh, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, 0, fh, null);
        }
        return newBitmap;
    }
    /**
     * 将Bitmap转换成指定大小
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createBitmapBySize(Bitmap bitmap,int width,int height)
    {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    /**
     * Drawable 转 Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmapByBD(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
        return bitmapDrawable.getBitmap();
    }
    /**
     * Bitmap 转 Drawable
     *
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawableByBD(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }
    /**
     * byte[] 转 bitmap
     *
     * @param b
     * @return
     */
    public static Bitmap bytesToBimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
    /**
     * bitmap 转 byte[]
     *
     * @param bm
     * @return
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * @param res
     * @param resId
     * @param reqWidth  缩小后的宽,单位dp
     * @param reqHeight  缩小后的长,单位dp
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {
        // First decode with 'inJustDecodeBounds=true' to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
