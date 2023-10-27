package spa.lyh.cn.permission;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import spa.lyh.cn.peractivity.ChinaPermissionActivity;
import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;

public class MainActivity extends ChinaPermissionActivity {
    private TextView tv;
    private Button btn_io,btn_io_check;

    private int sign = 0;//0为app默认发起的那些请求，1为模拟app相册发起的请求

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        btn_io = findViewById(R.id.btn_io);
        btn_io_check = findViewById(R.id.btn_io_check);
        //请求权限
        askForPermission(REQUIRED_LOAD_METHOD,getPermissionList());
        /*askForPermission(REQUIRED_LOAD_METHOD,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "定位");*/

        btn_io.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign = 1;
                askForPermission(REQUIRED_LOAD_METHOD,getIOPermissionList());
            }
        });
        btn_io_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(REQUIRED_LOAD_METHOD,getIOPermissionList());
            }
        });

    }

    private String[] getPermissionList(){
        List<String> pList = new ArrayList<>();
        pList.add(ManifestPro.permission.CAMERA);
        pList.add(ManifestPro.permission.ACCESS_FINE_LOCATION);
        pList.add(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9);//自定义的权限，可以减少Android版本的判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //13
            pList.add(ManifestPro.permission.POST_NOTIFICATIONS);
        }
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
        if (sign == 0){
            tv.setText("所有获取权限成功");
        }else if (sign == 1){
            Intent intent = new Intent(this,PhotoActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void permissionRejected() {
        if (sign == 0){
            tv.setText("获取某些权限失败");
        }else if (sign == 1){
            Toast.makeText(this,"权限不通过",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void permissionCheck48HPass() {
        Toast.makeText(this,"权限检查通过",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void permissionCheck48HDenied(@NonNull ArrayList<String> permissions) {
        Toast.makeText(this,"权限检查不通过",Toast.LENGTH_SHORT).show();
        for(String per:permissions){
            Log.e("qwer",per);
        }
    }
}
