package com.haozhang.statelist;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "ImgTest";
    ListView mListView;
    MyAdapter mAdapter;
    InitAppsTask mInitAppTask ;
    private int mCurrentUserTheme = 0;
    BitmapUtils mBitmapUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SecondActivity.class));
            }
        });
        mBitmapUtils = new BitmapUtils(this);
        initView();
    }

    private void initView(){
        mListView = (ListView) findViewById(R.id.list);
        mAdapter  =new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*ResolveInfo info = mAdapter.getItem(position);
               Intent i =  new Intent(MainActivity.this,SecondActivity.class);
                i.putExtra("info",info);
                startActivity(i);*/
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInitAppTask = new InitAppsTask();
        mInitAppTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null!=mInitAppTask&&!mInitAppTask.isCancelled()){
            mInitAppTask.cancel(true);
        }
    }

    class MyAdapter extends BaseAdapter{

        private PackageManager mManager;
        private Activity mContext;
        private List<ResolveInfo> mList = new ArrayList<ResolveInfo>();


        public MyAdapter(Activity context) {
            super();
            mContext = context;
            mManager = mContext.getPackageManager();

        }

        public void refresh(List<ResolveInfo> list) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public ResolveInfo getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView){
                convertView = mContext.getLayoutInflater().inflate(R.layout.list_item,null);
            }
            final  ImageView imgDef = ViewHolder.get(convertView,R.id.image_def);
            TextView title = ViewHolder.get(convertView,R.id.text_title);
            TextView detail  = ViewHolder.get(convertView,R.id.text_detail);
            final ImageView imgDay = ViewHolder.get(convertView,R.id.image_day);
            ImageView imgNight = ViewHolder.get(convertView,R.id.image_night);
            ImageView imgSel = ViewHolder.get(convertView,R.id.image_sel);

            ResolveInfo info = mList.get(position);
            if (null!=info) {
            imgDef.setTag(info.activityInfo.packageName + "img");
                imgDay.setTag(info.activityInfo.packageName);
                mBitmapUtils.getDrawableWithInfo(info.activityInfo.packageName,
                         new BitmapUtils.Callback() {
                            @Override
                            public void onComplete(String tag, final Drawable drawable) {
                                if (tag.equals(tag)&& null!=drawable) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgDay.setImageDrawable(drawable);
                                        }
                                    });
                                }
                            }
                        });
            }
            title.setText(info.loadLabel(mManager).toString());
            detail.setText(info.activityInfo.packageName);

            return convertView;
        }

        public Bitmap getDayImg(Bitmap bitmap){
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(faceIconGreyBitmap);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.set(getValue3());
            ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
            paint.setColorFilter(colorMatrixFilter);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            return faceIconGreyBitmap;
        }
    }

    public  float[] getValue3(){
        float[] carray = new float[20];
        //黑白
        carray[0]=(float) 0.213;
        carray[1]=(float) 0.715;
        carray[2]=(float) 0.072;
        carray[3]=0;
        carray[4]=0;
        carray[5]=(float) 0.213;
        carray[6]=(float) 0.715;
        carray[7]=(float) 0.072;
        carray[8]=0;
        carray[9]=0;
        carray[10]=(float) 0.213;
        carray[11]=(float) 0.715;
        carray[12]=(float) 0.072;
        carray[13]=0;
        carray[14]=0;
        carray[15]=0;
        carray[16]=0;
        carray[17]=0;
        carray[18]=1;
        carray[19]=0;
        return carray;
    }

    public ColorMatrix getColorMatrix(){
        float a = 0.3086f * 256;
        float b = 0.6094f * 256;
        float c = 0.0820f * 256;
        float lum = -256 * 0;
        ColorMatrix matrix = new ColorMatrix();
        matrix.set(new float[]
                {a, b, c, 0, lum,
                        a, b, c, 0, lum,
                        a, b, c, 0, lum,
                        0, 0, 0, 1, 0});
        return matrix;

    }

    public static Bitmap toHeibai(Bitmap mBitmap)
    {
        int mBitmapWidth = 0;
        int mBitmapHeight = 0;
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        Bitmap bmpReturn = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.ARGB_8888);
        int iPixel = 0;
        for (int i = 0; i < mBitmapWidth; i++)
        {
            for (int j = 0; j < mBitmapHeight; j++)
            {
                int curr_color = mBitmap.getPixel(i, j);
                int avg = (Color.red(curr_color) + Color.green(curr_color) + Color.blue(curr_color)) / 3;
                int modif_color;
                if (avg >= 100)
                {
                    iPixel = 100;
                    modif_color = Color.argb(255, iPixel, iPixel, iPixel);
                }
                else
                {
                    iPixel = 0;
                    modif_color = Color.argb(0, iPixel, iPixel, iPixel);
                }
                bmpReturn.setPixel(i, j, modif_color);
            }
        }
        return bmpReturn;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    class InitAppsTask extends AsyncTask<Void,Void,List<ResolveInfo>> {

        @Override
        protected void onPostExecute(List<ResolveInfo> resolveInfos) {
            // refresh adapter
            if (null!=resolveInfos){
                mAdapter.refresh(resolveInfos);
            }
        }
        @Override
        protected List<ResolveInfo> doInBackground(Void... voids) {
            Intent intent = new Intent(Intent.ACTION_MAIN,null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager manager = getApplicationContext().getPackageManager();
            List<ResolveInfo> apps =null;
            apps = manager.queryIntentActivities(intent, 0);
            Collections.sort(apps, new ShortcutNameComparator(getPackageManager()));
            return apps;
        }
    }

    static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }

    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private Collator mCollator;
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
            mCollator = Collator.getInstance();
        }
        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }

        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = getComponentNameFromResolveInfo(a);
            ComponentName keyB = getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    }

    public interface CallBack{
        void CallBack(String tag,Bitmap bitmap);
    }

    class AsyncGetBitmap{

        CallBack mCallback;
        PackageManager mManager;
        public AsyncGetBitmap(final int mode,PackageManager manager,final String tag,final ResolveInfo info, CallBack callBack){
            this.mCallback = callBack;
            this.mManager = manager;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (mode){
                        case 0:
                            Bitmap btm = getAppBitmap(info);
                            mCallback.CallBack(tag,btm);
                            break;
                        case 1:
                            mCallback.CallBack(tag, getDayImg(getAppBitmap(info)));
                            break;
                        case 2:
                            break;

                    }
                }
            }).start();

        }
        public Bitmap getDayImg(Bitmap bitmap){
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

        public Bitmap getAppBitmap(ResolveInfo info){
            Drawable drawable = info.loadIcon(mManager);
            if(null==drawable)return null;
            return drawableToBitmap(drawable);
        }
    }
}
