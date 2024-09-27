package spa.lyh.cn.permission;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import spa.lyh.cn.lib_utils.TimeUtils;
import spa.lyh.cn.peractivity.ChinaPermissionActivity;
import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;
import spa.lyh.cn.permission.dialog.PerTopDialog;
import spa.lyh.cn.permission.test.PickImage;

public class MainActivity extends PermissionActivity {
    private TextView tv;
    private Button btn_io,btn_io_check;

    private int sign = 0;//0为app默认发起的那些请求，1为模拟app相册发起的请求

    private PerTopDialog ptDialog;
    private TextView result;
    private TextView pop;

    private long beginTime = 0;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    String mimeType = "image/gif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        btn_io = findViewById(R.id.btn_io);
        btn_io_check = findViewById(R.id.btn_io_check);
        ptDialog = new PerTopDialog(this);
        result = findViewById(R.id.result);
        pop = findViewById(R.id.pop);
        //请求权限
        //Log.e("qwer", TimeUtils.getCurrentTimeToString(0,"yyyy-MM-dd HH:mm:ss.SSS"));
        beginTime = System.currentTimeMillis();
        askForPermission(NOT_REQUIRED_LOAD_METHOD,getPermissionList());
        /*askForPermission(REQUIRED_LOAD_METHOD,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "定位");*/
        boolean result = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this);
        Log.e("qwer",result+"");
        pickMedia =
                registerForActivityResult(new PickImage(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.e("qwer", "Selected URI: " + uri);
                    } else {
                        Log.e("qwer", "No media selected");
                    }
                });

/*        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(new ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType))
                .build());*/


        btn_io.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*sign = 1;
                beginTime = System.currentTimeMillis();
                askForPermission(REQUIRED_LOAD_METHOD,getIOPermissionList());*/
                /*pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());*/
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(new ActivityResultContracts.PickVisualMedia.SingleMimeType("*/*"))
                        .build());
            }
        });
/*        btn_io_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(REQUIRED_LOAD_METHOD,getIOPermissionList());
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) { // 检测请求码
            if (resultCode == Activity.RESULT_OK && data != null) {
                Log.e("qwer","有数据返回");
            }
        }
    }

    private String[] getPermissionList(){
        List<String> pList = new ArrayList<>();
        pList.add(ManifestPro.permission.CAMERA);
/*        pList.add(ManifestPro.permission.ACCESS_FINE_LOCATION);
        pList.add(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9);//自定义的权限，可以减少Android版本的判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //13
            pList.add(ManifestPro.permission.POST_NOTIFICATIONS);
        }*/
        return pList.toArray(new String[pList.size()]);
    }

    private String[] getIOPermissionList(){
        List<String> pList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //13
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                //14
                pList.add(ManifestPro.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
            pList.add(ManifestPro.permission.READ_MEDIA_IMAGES);
            pList.add(ManifestPro.permission.READ_MEDIA_VIDEO);
        }else {
            pList.add(ManifestPro.permission.READ_EXTERNAL_STORAGE);
        }
        return pList.toArray(new String[pList.size()]);
    }

    @Override
    public void permissionAllowed() {
        long time = System.currentTimeMillis() - beginTime;
        result.setText("权限允许用时约"+time+"ms");
        Log.e("qwer","获取所有权限成功");
        if (sign == 0){
            tv.setText("获取所有权限成功");
        }else if (sign == 1){
            Intent intent = new Intent(this,PhotoActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void permissionRejected() {
        long time = System.currentTimeMillis() - beginTime;
        result.setText("权限拒绝用时约"+time+"ms");
        Log.e("qwer","获取某些权限失败");
        if (sign == 0){
            tv.setText("获取某些权限失败");
        }else if (sign == 1){
            Toast.makeText(this,"权限不通过",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void requestPermissionProceed() {
        super.requestPermissionProceed();
        long time = System.currentTimeMillis() - beginTime;
        pop.setText("弹窗实际延迟用时约"+time+"ms");
        Log.e("qwer","发起了权限请求");
        if (!ptDialog.isShowing()){
            ptDialog.show("权限说明标题","权限说明内容，你为什么要申请这个权限，要做什么。");
        }
    }

    @Override
    public void requestPermissionOver() {
        super.requestPermissionOver();
        Log.e("qwer","结束了权限请求");
        if (ptDialog.isShowing()){
            ptDialog.dismiss();
        }
    }

/*        @Override
    public void permissionCheck48HPass() {
        Toast.makeText(this,"权限检查通过",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionCheck48HDenied(@NonNull ArrayList<String> permissions) {
        Toast.makeText(this,"权限检查不通过",Toast.LENGTH_SHORT).show();
        for(String per:permissions){
            Log.e("qwer",per);
        }
    }*/
}
