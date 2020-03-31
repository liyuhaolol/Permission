package spa.lyh.cn.permission;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import spa.lyh.cn.peractivity.PermissionActivity;

public class MainActivity extends PermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (askForPermission(REQUIRED_LOAD_METHOD, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Log.e("liyuhao","自动授权");
        }

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
