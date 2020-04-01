package spa.lyh.cn.permission;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;

public class MainActivity extends PermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermission(REQUIRED_LOAD_METHOD,
                ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_9,
                ManifestPro.permission.CAMERA,
                ManifestPro.permission.ACCESS_FINE_LOCATION,
                ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9);

    }


    @Override
    public void permissionAllowed() {
        Log.e("liyuhao","手动授权");
    }

    @Override
    public void permissionRejected() {
        Log.e("liyuhao","没有授权");
    }
}
