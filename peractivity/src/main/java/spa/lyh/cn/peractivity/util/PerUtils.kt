package spa.lyh.cn.peractivity.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import spa.lyh.cn.peractivity.ManifestPro
import spa.lyh.cn.peractivity.R


object PerUtils {
    //必须被允许，且自动执行授权后方法
    const val REQUIRED_LOAD_METHOD = 1

    //必须被允许，只进行申请权限，不自动执行授权后方法
    const val REQUIRED_ONLY_REQUEST = 2

    //可以被禁止，且自动执行授权后方法
    const val NOT_REQUIRED_LOAD_METHOD = 3

    //可以被禁止，只进行申请权限，不自动执行授权后方法
    const val NOT_REQUIRED_ONLY_REQUEST = 4

    const val SETTING_REQUEST = 8848

    @JvmStatic
    fun getPermissionNameList(): HashMap<String, Int> {
        val permissionList = HashMap<String, Int>()
        permissionList["android.permission.READ_CALENDAR"] = R.string.CALENDAR
        permissionList["android.permission.WRITE_CALENDAR"] = R.string.CALENDAR
        permissionList["android.permission.CAMERA"] = R.string.CAMERA
        permissionList["android.permission.READ_CONTACTS"] = R.string.CONTACTS
        permissionList["android.permission.WRITE_CONTACTS"] = R.string.CONTACTS
        permissionList["android.permission.GET_ACCOUNTS"] = R.string.CONTACTS
        permissionList["android.permission.ACCESS_FINE_LOCATION"] = R.string.LOCATION
        permissionList["android.permission.ACCESS_COARSE_LOCATION"] = R.string.LOCATION
        permissionList["android.permission.ACCESS_BACKGROUND_LOCATION"] = R.string.LOCATION
        permissionList["android.permission.RECORD_AUDIO"] = R.string.RECORD_AUDIO
        permissionList["android.permission.READ_PHONE_STATE"] = R.string.PHONE_STATE
        permissionList["android.permission.CALL_PHONE"] = R.string.PHONE_STATE
        permissionList["android.permission.READ_CALL_LOG"] = R.string.PHONE_STATE
        permissionList["android.permission.WRITE_CALL_LOG"] = R.string.PHONE_STATE
        permissionList["android.permission.ADD_VOICEMAIL"] = R.string.PHONE_STATE
        permissionList["android.permission.USE_SIP"] = R.string.PHONE_STATE
        permissionList["android.permission.PROCESS_OUTGOING_CALLS"] = R.string.PHONE_STATE
        permissionList["android.permission.BODY_SENSORS"] = R.string.SENSORS
        permissionList["android.permission.SEND_SMS"] = R.string.SMS
        permissionList["android.permission.RECEIVE_SMS"] = R.string.SMS
        permissionList["android.permission.READ_SMS"] = R.string.SMS
        permissionList["android.permission.RECEIVE_WAP_PUSH"] = R.string.SMS
        permissionList["android.permission.RECEIVE_MMS"] = R.string.SMS
        permissionList["android.permission.READ_EXTERNAL_STORAGE"] = R.string.STORAGE
        permissionList["android.permission.WRITE_EXTERNAL_STORAGE"] = R.string.STORAGE
        permissionList["android.permission.POST_NOTIFICATIONS"] = R.string.NOTIFACATION
        permissionList["android.permission.READ_MEDIA_IMAGES"] = R.string.IMAGE
        permissionList["android.permission.READ_MEDIA_VIDEO"] = R.string.VIDEO
        permissionList["android.permission.READ_MEDIA_AUDIO"] = R.string.AUDIO
        permissionList["android.permission.READ_MEDIA_VISUAL_USER_SELECTED"] = R.string.MEDIA_MODE
        return permissionList
    }

    @JvmStatic
    fun checkNeedPermission(permission: Array<String>): Array<String> {
        val per = ArrayList<String>()
        //先处理向上兼容的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //12
            for (permi in permission) {
                if (permi != ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9
                    && permi != ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_9
                    && permi != ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13
                ) {
                    per.add(permi)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //10
            for (permi in permission) {
                if (permi != ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9
                    && permi != ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_9
                ) {
                    if (permi == ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13) {
                        per.add(ManifestPro.permission.READ_EXTERNAL_STORAGE)
                    } else {
                        per.add(permi)
                    }
                }
            }
        } else {
            //<=9
            for (permi in permission) {
                if (permi == ManifestPro.permission.READ_PHONE_STATE_BLOW_ANDROID_9) {
                    per.add(ManifestPro.permission.READ_PHONE_STATE)
                } else if (permi == ManifestPro.permission.WRITE_EXTERNAL_STORAGE_BLOW_ANDROID_9) {
                    per.add(ManifestPro.permission.WRITE_EXTERNAL_STORAGE)
                } else if (permi == ManifestPro.permission.READ_EXTERNAL_STORAGE_BLOW_ANDROID_13) {
                    per.add(ManifestPro.permission.READ_EXTERNAL_STORAGE)
                } else {
                    per.add(permi)
                }
            }
        }
        //底层库不应该处理这个问题，向下兼容应该开发者自己判断android版本，故注释掉
        //再处理向下兼容问题
/*        val pName: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            //12.1
            for (i in per.indices) {
                if (per[i] == ManifestPro.permission.POST_NOTIFICATIONS) {
                    //13
                    pName.add(ManifestPro.permission.POST_NOTIFICATIONS)
                }
            }
        }

        //移除掉对应权限
        for (n in pName) {
            per.remove(n)
        }*/
        return per.toTypedArray()
    }

}