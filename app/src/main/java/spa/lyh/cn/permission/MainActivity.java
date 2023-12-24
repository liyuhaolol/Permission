package spa.lyh.cn.permission;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import spa.lyh.cn.lib_utils.TimeUtils;
import spa.lyh.cn.peractivity.ChinaPermissionActivity;
import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.PermissionActivity;
import spa.lyh.cn.permission.dialog.PerTopDialog;

public class MainActivity extends PermissionActivity {
    private TextView tv;
    private Button btn_io,btn_io_check;

    private int sign = 0;//0为app默认发起的那些请求，1为模拟app相册发起的请求

    private PerTopDialog ptDialog;

    private TextView delay;
    private TextView result;
    private TextView pop;

    private long beginTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        btn_io = findViewById(R.id.btn_io);
        btn_io_check = findViewById(R.id.btn_io_check);
        ptDialog = new PerTopDialog(this);
        delay = findViewById(R.id.delay);
        String content = "长延迟:"+getLongDelay()+"ms 短延迟:"+getShortDelay()+"ms";
        //长短延迟说明:为什么要加这个延迟，因为如果发起权限请求一个被永久拒绝的权限，系统并不是立刻返回被拒绝的结果，有一个明显的系统延迟。如果立刻显示弹窗的话，会出现弹窗出来，又立刻被关闭的闪烁问题。
        //长延迟是从发起权限开始延迟默认600ms，短延迟是从检测到onPause的时候开始延迟默认200ms，两个延迟只会生效最快的一个。作用是回调requestPermissionProceed()方法
        //之所以加一个短延迟200ms，是因为600ms依然感觉速度较慢，总会权限请求弹窗显示后，等很长一下才显示做出的优化。使用短延迟可以加速弹窗从600ms+缩短到300ms+左右，效果还是非常明显的。
        //如果觉得短延迟会造成弹窗闪烁，可以设置enableShortDelay为false不启用短延迟。
        //！！！重要！！！！600ms和200ms算是多次测试多个实际设备和虚拟机出来的一个较为均衡的时间，但是依然不排除虚拟机性能太差，或者远古手机，或者debug模式下，会出现权限结果回调远超600ms的情况。
        //这种情况要么无视，闪烁就闪烁吧，要么可以适当延长longDelay和shortDelay的时间缓解性能较差的手机闪烁问题。
        //这种情况只会出现在权限被永久拒绝后，你依然去对这个权限发起请求才会出现。
        //中国合规版会将这个问题延迟到48小时内只可能出现一次。
        //所以放宽心，我个人觉得，闪就闪吧，不会怎么样。
        delay.setText(content);
        result = findViewById(R.id.result);
        pop = findViewById(R.id.pop);
        //请求权限
        //Log.e("qwer", TimeUtils.getCurrentTimeToString(0,"yyyy-MM-dd HH:mm:ss.SSS"));
        beginTime = System.currentTimeMillis();
        askForPermission(NOT_REQUIRED_LOAD_METHOD,getPermissionList());
        /*askForPermission(REQUIRED_LOAD_METHOD,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "定位");*/

        btn_io.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign = 1;
                beginTime = System.currentTimeMillis();
                askForPermission(REQUIRED_LOAD_METHOD,getIOPermissionList());
            }
        });
/*        btn_io_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(REQUIRED_LOAD_METHOD,getIOPermissionList());
            }
        });*/
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
