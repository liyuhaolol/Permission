package spa.lyh.cn.permission;


import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;

public class MainActivity extends PermissionActivity {
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        //请求权限
        askForPermission(REQUIRED_LOAD_METHOD,getPermissionList());
        /*askForPermission(REQUIRED_LOAD_METHOD,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "定位");*/
    }

    private String[] getPermissionList(){
        List<String> pList = new ArrayList<>();
        pList.add(ManifestPro.permission.CAMERA);
        pList.add(ManifestPro.permission.ACCESS_FINE_LOCATION);
        pList.add(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9);
        pList.add(ManifestPro.permission.POST_NOTIFICATIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            pList.add(ManifestPro.permission.READ_MEDIA_IMAGES);
            pList.add(ManifestPro.permission.READ_MEDIA_VIDEO);
            pList.add(ManifestPro.permission.READ_MEDIA_AUDIO);
        }else {
            pList.add(ManifestPro.permission.READ_EXTERNAL_STORAGE);
        }
        return pList.toArray(new String[pList.size()]);
    }

    @Override
    public void permissionAllowed() {
        tv.setText("所有获取权限成功");
    }

    @Override
    public void permissionRejected() {
        tv.setText("获取某些权限失败");
    }

/*    @Override
    public void showMissingPermissionDialog(List<String> per) {
        Toast.makeText(this,"这里显示权限被拒绝dialog",Toast.LENGTH_SHORT).show();
    }*/
}
