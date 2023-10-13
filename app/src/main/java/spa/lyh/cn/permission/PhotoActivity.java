package spa.lyh.cn.permission;

import android.os.Bundle;

import androidx.annotation.Nullable;

import spa.lyh.cn.peractivity.PermissionActivity;

public class PhotoActivity extends PermissionActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
    }
}
