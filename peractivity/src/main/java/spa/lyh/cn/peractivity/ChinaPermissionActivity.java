package spa.lyh.cn.peractivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import spa.lyh.cn.peractivity.dialog.PerDialog;
import spa.lyh.cn.peractivity.util.LanguageUtils;
import spa.lyh.cn.peractivity.util.PerUtils;

/**
 * Created by liyuhao on 2022/9/21.
 * 这个类是为了配合国内应用平台的奇葩审核规则而创建
 * vivo，oppo等平台的审核规则为，权限只允许请求一次，用户拒绝以后，不管用户是否勾选不再询问，通常48小时内不允许再次发起请求。
 * 1次权限请求就是硬按iOS的权限请求逻辑来要求Android，本人只能表示无语。Android明明有不在询问选项，非要按照苹果的逻辑来。库克是他们爹。
 *
 * 本Acitivity跟默认的PermissionActivity逻辑保持一致，但是加入了本地持久化，记录权限申请状态。如果1次请求被拒绝，48小时内直接返回权限被拒绝的状态。
 * 而不会去判断系统是否真的将此权限拒绝。后续操作需要开发人员自己重写方法去操作。
 *
 * 使用事项，权限是按照权限组来授权的，所以申请权限时，尽量不要同时申请同一权限组的权限，比如
 * WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE，只要申请其中一个权限，整个group.STORAGE都会被赋予权限
 * <p>
 * 不同权限需求种类，不要在同一个权限组里发起申请，因为code你一次只能传1种，4种需求种类对应4种应用场景，所以
 * 不要尝试使用一套逻辑来同时兼容4种模式，应该是不现实的。
 * <p>
 */
public class ChinaPermissionActivity extends AppCompatActivity {
    //必须被允许，且自动执行授权后方法
    public static final int REQUIRED_LOAD_METHOD = PerUtils.REQUIRED_LOAD_METHOD;
    //必须被允许，只进行申请权限，不自动执行授权后方法
    public static final int REQUIRED_ONLY_REQUEST = PerUtils.REQUIRED_ONLY_REQUEST;
    //可以被禁止，且自动执行授权后方法
    public static final int NOT_REQUIRED_LOAD_METHOD = PerUtils.NOT_REQUIRED_LOAD_METHOD;
    //可以被禁止，只进行申请权限，不自动执行授权后方法
    public static final int NOT_REQUIRED_ONLY_REQUEST = PerUtils.NOT_REQUIRED_ONLY_REQUEST;

    private static final int SETTING_REQUEST = PerUtils.SETTING_REQUEST;


    private String FILLNAME = "chinapermission";// 文件名称
    private SharedPreferences mSharedPreferences;
    private ArrayList<String> missPerList = new ArrayList<>();
    private boolean loadMethodFlag;//是否自动加载方法

    private List<String> missPermission;

    //被永久拒绝之后显示的dialog
    private PerDialog perDialog;

    private final static HashMap<String,Integer> permissionList;

    static {
        permissionList = PerUtils.getPermissionNameList();
    }

    /**
     * 判断是否拥有权限
     *
     * @param permissions 不定长数组
     */
    public void askForPermission(int code, String... permissions) {
        missPerList.clear();
        List<String> realMissPermission = new ArrayList<>();
        boolean flag = true;
        String per[] = PerUtils.checkNeedPermission(permissions);
        for (String permission : per) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                realMissPermission.add(permission);
                flag = false;
            }
        }
        if (mSharedPreferences == null){
            mSharedPreferences = getSharedPreferences(FILLNAME, Context.MODE_PRIVATE);
        }
        for (int i = 0;i<realMissPermission.size();i++){
            String perName = realMissPermission.get(i);
            long mark = mSharedPreferences.getLong(perName,0);
            long time = System.currentTimeMillis();
            if ((time - mark) <= 172800000){
                missPerList.add(perName);
            }
        }
        if (realMissPermission.size() > 0) {
            if (missPerList.size() == realMissPermission.size()){
                makeReject(code);
                return;
            }
            for (String perName:missPerList){
                realMissPermission.remove(perName);
            }
/*            for (String perName:realMissPermission){
                mSharedPreferences.edit().putLong(perName,System.currentTimeMillis()).apply();
            }*/
            String[] missPermissions = realMissPermission.toArray(new String[realMissPermission.size()]);
            requestPermission(code, missPermissions);
        }
        if (flag){
            permissionAllowed();
        }
    }

    private void makeReject(int code){
        initMissingPermissionDialog();
        boolean requiredFlag = false;//是否为项目必须的权限
        switch (code) {
            case REQUIRED_LOAD_METHOD:
                loadMethodFlag = true;
                requiredFlag = true;
                break;
            case REQUIRED_ONLY_REQUEST:
                loadMethodFlag = false;
                requiredFlag = true;
                break;
            case NOT_REQUIRED_LOAD_METHOD:
                loadMethodFlag = true;
                requiredFlag = false;
                break;
            case NOT_REQUIRED_ONLY_REQUEST:
                loadMethodFlag = false;
                requiredFlag = false;
                break;
        }
        if (requiredFlag){
            //显示缺少权限，并解释为何需要这个权限
            if (missPermission != null) {
                missPermission.clear();
            } else {
                missPermission = new ArrayList<>();
            }
            missPermission.addAll(missPerList);
            showMissingPermissionDialog(missPermission);
        }else {
            if (loadMethodFlag){
                permissionRejected();
            }
        }

    }

    /**
     * 请求权限
     *
     * @param code        请求码
     * @param permissions 权限列表
     */
    private void requestPermission(int code, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionFlag = missPerList.size()<=0;//权限是否全部通过
        boolean requiredFlag = false;//是否为项目必须的权限
        ArrayList<String> per = new ArrayList<>();//保存被拒绝的权限列表
        initMissingPermissionDialog();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //存在被拒绝的权限
                per.add(permissions[i]);
                permissionFlag = false;
                mSharedPreferences.edit().putLong(permissions[i],System.currentTimeMillis()).apply();
            }else {
                mSharedPreferences.edit().putLong(permissions[i],0).apply();
            }
        }
        switch (requestCode) {
            case REQUIRED_LOAD_METHOD:
                loadMethodFlag = true;
                requiredFlag = true;
                break;
            case REQUIRED_ONLY_REQUEST:
                loadMethodFlag = false;
                requiredFlag = true;
                break;
            case NOT_REQUIRED_LOAD_METHOD:
                loadMethodFlag = true;
                requiredFlag = false;
                break;
            case NOT_REQUIRED_ONLY_REQUEST:
                loadMethodFlag = false;
                requiredFlag = false;
                break;
        }
        //List<Integer> ids = selectGroup(per);//判断被拒绝的权限组名称

        if (permissionFlag) {
            //通过了申请的权限
            if (loadMethodFlag) {
                permissionAllowed();//权限通过，执行对应方法
            }
        } else {
            if (requiredFlag) {
                if (per.size() > 0) {//严谨判断大于0
                    //显示缺少权限，并解释为何需要这个权限
                    if (missPermission != null) {
                        missPermission.clear();
                    } else {
                        missPermission = new ArrayList<>();
                    }
                    missPermission.addAll(per);
                    missPermission.addAll(missPerList);
                    showMissingPermissionDialog(missPermission);
                }
            } else {
                Log.e("Permission:", "Permission had been rejected");
                if (loadMethodFlag) {
                    permissionRejected();
                }
            }
        }
    }

    private void initMissingPermissionDialog() {
        if (perDialog == null){
            perDialog = new PerDialog(this);
            perDialog.setTitle(getTrueString(this,R.string.help));
            perDialog.setCancel(getTrueString(this,R.string.cancal));
            perDialog.setSetting(getTrueString(this,R.string.setting_name));
            perDialog.setOnCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (loadMethodFlag){
                        permissionRejected();
                    }
                }
            });
            perDialog.setOnSettingClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到，设置的对应界面
                    startAppSettings();
                }
            });
        }
    }

    /**
     * 给子类提供重写的成功接口
     */
    public void permissionAllowed() {
    }

    /**
     * 给子类提供重写的失败接口
     */
    public void permissionRejected() {
    }

    /**
     * 显示解释设置dialog
     *
     * @param per 权限组名
     */
    public void showMissingPermissionDialog(List<String> per) {
        List<Integer> ids = selectGroup(per);
        String content = "";
        //将权限组名字转换为字符串
        if (ids.size() > 0) {
            for (int id : ids) {
                content = content + getTrueString(this,id) + "\n";
            }
        }
        perDialog.setContent(getTrueString(this,R.string.leak)+"\n" + content + getTrueString(this,R.string.go_setting));
        perDialog.show();
    }

    /**
     * 启动应用的设置
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent,SETTING_REQUEST);
    }

    /**
     * 将权限，对应到权限组名，并去重
     *
     * @param permissions 权限名
     * @return 权限组名
     */
    private List<Integer> selectGroup(List<String> permissions) {
        List<Integer> group = new ArrayList<>();
        for (String permission : permissions) {
            if (permissionList != null){
                group.add(permissionList.get(permission));
            }
        }
        //去重
        group = new ArrayList<>(new HashSet<>(group));
        return group;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_REQUEST){
            //从设置返回的回调
            recheckPermission();
        }
    }

    private void recheckPermission(){
        if (missPermission != null && missPermission.size() > 0){
            List<String> per = new ArrayList<>();
            //重新检查权限
            for (String permission:missPermission){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    per.add(permission);
                }else {
                    mSharedPreferences.edit().putLong(permission,0).apply();
                }
            }
            if (per.size() > 0){
                //依然有权限未通过
                missPermission.clear();
                missPermission.addAll(per);
                showMissingPermissionDialog(per);
            }else {
                missPermission.clear();
                if (perDialog != null && perDialog.isShowing()){
                    perDialog.dismiss();
                }
                permissionAllowed();
            }
        }
    }

    private String getTrueString(Context context,@StringRes int id){
        if (LanguageUtils.isActivited()){
            //启动了国际化
            return LanguageUtils.getLanguageString(context,id);
        }else {
            //没有启动国际化
            return getString(id);
        }
    }
}
