package spa.lyh.cn.permission;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import spa.lyh.cn.peractivity.PermissionActivity;
import spa.lyh.cn.permission.adapter.PhotoAdapter;
import spa.lyh.cn.permission.bean.PictureBean;

public class PhotoActivity extends PermissionActivity {
    private Handler handler;
    private  List<PictureBean> beanList;

    private PhotoAdapter adapter;

    private RecyclerView recy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        beanList = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                adapter.notifyDataSetChanged();
            }
        };
        adapter = new PhotoAdapter(beanList);
        recy = findViewById(R.id.recy);
        recy.setLayoutManager(new GridLayoutManager(this,4,RecyclerView.VERTICAL,false));
        recy.setAdapter(adapter);
        getImages();
    }




    private void getImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver = PhotoActivity.this.getContentResolver();
                String[] projection = {
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.MIME_TYPE
                };

                String selection = MediaStore.MediaColumns.MIME_TYPE + "=? OR " +
                        MediaStore.MediaColumns.MIME_TYPE + "=?";
                String[] selectionArgs = {"image/jpeg", "video/mp4"};

                String sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";

                Uri uri = MediaStore.Files.getContentUri("external");

                Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);


                if (cursor == null) {
                    return;
                }
                String mParentPath = "";//所在文件夹的绝对路径
                while (cursor.moveToNext()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (index > 0){
                        String path = cursor.getString(index);//图片的绝对路径
                        PictureBean bean = new PictureBean();//我们之前写好的模型
                        bean.setFirstImageUrl(path); //第一张图片
                        bean.setParentPath(mParentPath);//所在文件夹的绝对路径
                        beanList.add(bean);//把模型放到步骤3定义好的list
                    }
                }
                handler.sendEmptyMessage(1);
            }
        }).start();
    }
}
