package spa.lyh.cn.permission;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import spa.lyh.cn.peractivity.ChinaPermissionActivity;
import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;
import spa.lyh.cn.peractivity.dialog.PerDialog;

public class MainActivity extends PermissionActivity {
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        //请求权限
        askForPermission(REQUIRED_LOAD_METHOD,
                ManifestPro.permission.CAMERA,
                ManifestPro.permission.ACCESS_FINE_LOCATION,
                ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9,
                ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_10,
                ManifestPro.permission.POST_NOTIFICATIONS);
        /*askForPermission(REQUIRED_LOAD_METHOD,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "定位");*/
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
