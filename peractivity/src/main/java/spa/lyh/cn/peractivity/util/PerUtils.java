package spa.lyh.cn.peractivity.util;

import android.os.Build;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import spa.lyh.cn.peractivity.ManifestPro;
import spa.lyh.cn.peractivity.R;
import spa.lyh.cn.peractivity.dialog.PerDialog;

public class PerUtils {
    //必须被允许，且自动执行授权后方法
    public static final int REQUIRED_LOAD_METHOD = 1;
    //必须被允许，只进行申请权限，不自动执行授权后方法
    public static final int REQUIRED_ONLY_REQUEST = 2;
    //可以被禁止，且自动执行授权后方法
    public static final int NOT_REQUIRED_LOAD_METHOD = 3;
    //可以被禁止，只进行申请权限，不自动执行授权后方法
    public static final int NOT_REQUIRED_ONLY_REQUEST = 4;

    public static final int SETTING_REQUEST = 8848;

    public static HashMap<String,Integer> getPermissionNameList(){
        HashMap<String,Integer> permissionList = new HashMap<>();
        permissionList.put("android.permission.READ_CALENDAR", R.string.CALENDAR);
        permissionList.put("android.permission.WRITE_CALENDAR",R.string.CALENDAR);
        permissionList.put("android.permission.CAMERA",R.string.CAMERA);
        permissionList.put("android.permission.READ_CONTACTS",R.string.CONTACTS);
        permissionList.put("android.permission.WRITE_CONTACTS",R.string.CONTACTS);
        permissionList.put("android.permission.GET_ACCOUNTS",R.string.CONTACTS);
        permissionList.put("android.permission.ACCESS_FINE_LOCATION",R.string.LOCATION);
        permissionList.put("android.permission.ACCESS_COARSE_LOCATION",R.string.LOCATION);
        permissionList.put("android.permission.ACCESS_BACKGROUND_LOCATION",R.string.LOCATION);
        permissionList.put("android.permission.RECORD_AUDIO",R.string.RECORD_AUDIO);
        permissionList.put("android.permission.READ_PHONE_STATE",R.string.PHONE_STATE);
        permissionList.put("android.permission.CALL_PHONE",R.string.PHONE_STATE);
        permissionList.put("android.permission.READ_CALL_LOG",R.string.PHONE_STATE);
        permissionList.put("android.permission.WRITE_CALL_LOG",R.string.PHONE_STATE);
        permissionList.put("android.permission.ADD_VOICEMAIL",R.string.PHONE_STATE);
        permissionList.put("android.permission.USE_SIP",R.string.PHONE_STATE);
        permissionList.put("android.permission.PROCESS_OUTGOING_CALLS",R.string.PHONE_STATE);
        permissionList.put("android.permission.BODY_SENSORS",R.string.SENSORS);
        permissionList.put("android.permission.SEND_SMS",R.string.SMS);
        permissionList.put("android.permission.RECEIVE_SMS",R.string.SMS);
        permissionList.put("android.permission.READ_SMS",R.string.SMS);
        permissionList.put("android.permission.RECEIVE_WAP_PUSH",R.string.SMS);
        permissionList.put("android.permission.RECEIVE_MMS",R.string.SMS);
        permissionList.put("android.permission.READ_EXTERNAL_STORAGE",R.string.STORAGE);
        permissionList.put("android.permission.WRITE_EXTERNAL_STORAGE",R.string.STORAGE);
        permissionList.put("android.permission.POST_NOTIFICATIONS",R.string.NOTIFACATION);

        return permissionList;
    }


    public static String[] checkNeedPermission(String permission[]){
        List<String> per = Arrays.asList(permission);
        //先处理向上兼容的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //13
            per = new ArrayList<>();
            for (String permi:permission){
                if (!permi.equals(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9)
                        && !permi.equals(ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_10)
                            && !permi.equals(ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_11)
                                && !permi.equals(ManifestPro.permission.READ_EXTERNAL_STORAGE)
                                    && !permi.equals(ManifestPro.permission.WRITE_EXTERNAL_STORAGE)){
                    per.add(permi);

                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //11
            per = new ArrayList<>();
            for (String permi:permission){
                if (!permi.equals(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9)
                && !permi.equals(ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_10)){
                    if (permi.equals(ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_11)){
                        per.add(ManifestPro.permission.READ_EXTERNAL_STORAGE);
                    }else {
                        per.add(permi);
                    }
                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //10
            per = new ArrayList<>();
            for (String permi:permission){
                if (!permi.equals(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9)){
                    if (permi.equals(ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_10)){
                        per.add(ManifestPro.permission.WRITE_EXTERNAL_STORAGE);
                    }else if (permi.equals(ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_11)){
                        per.add(ManifestPro.permission.READ_EXTERNAL_STORAGE);
                    }else {
                        per.add(permi);
                    }


                }
            }
        }else {
            //<=9
            for (int i = 0;i<per.size();i++){
                if (per.get(i).equals(ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9)){
                    per.set(i,ManifestPro.permission.READ_PHONE_STATE);
                }else if (per.get(i).equals(ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_10)){
                    per.set(i,ManifestPro.permission.WRITE_EXTERNAL_STORAGE);
                }else if (per.get(i).equals(ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_11)){
                    per.set(i,ManifestPro.permission.READ_EXTERNAL_STORAGE);
                }
            }
        }
        //再处理向下兼容问题
        List<String> pName = new ArrayList<>();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S){
            //12
            for (int i = 0; i< per.size(); i++){
                if (per.get(i).equals(ManifestPro.permission.POST_NOTIFICATIONS)){
                    //13
                    pName.add(ManifestPro.permission.POST_NOTIFICATIONS);
                }
            }
        }

        //移除掉对应权限
        for (String n:pName){
            per.remove(n);
        }

        return per.toArray(new String[per.size()]);
    }
}
