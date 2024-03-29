package spa.lyh.cn.peractivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import spa.lyh.cn.peractivity.dialog.PerDialog
import spa.lyh.cn.peractivity.util.LanguageUtils.getLanguageString
import spa.lyh.cn.peractivity.util.LanguageUtils.isActivited
import spa.lyh.cn.peractivity.util.PerUtils
import spa.lyh.cn.peractivity.util.PerUtils.checkNeedPermission
import spa.lyh.cn.peractivity.util.PerUtils.getPermissionNameList

/**
 * Created by liyuhao on 2020/4/1.
 * 使用事项，权限是按照权限组来授权的，所以申请权限时，尽量不要同时申请同一权限组的权限，比如
 * WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE，只要申请其中一个权限，整个group.STORAGE都会被赋予权限
 *
 *
 * 不同权限需求种类，不要在同一个权限组里发起申请，因为code你一次只能传1种，4种需求种类对应4种应用场景，所以
 * 不要尝试使用一套逻辑来同时兼容4种模式，应该是不现实的。
 *
 *
 */
open class PermissionActivity : AppCompatActivity() {
    private var missPermission: MutableList<String>? = null
    private var hasReadMediaVisualUserSelected = false//是否请求了这个权限
    private val FILLNAME_OK = "permissionok" // 文件名称
    private lateinit var mSPok: SharedPreferences

    //被永久拒绝之后显示的dialog
    private lateinit var perDialog: PerDialog
    private var loadMethodFlag = false //是否自动加载方法
    private var needShow = false
    companion object {
        //必须被允许，且自动执行授权后方法
        const val REQUIRED_LOAD_METHOD = PerUtils.REQUIRED_LOAD_METHOD

        //必须被允许，只进行申请权限，不自动执行授权后方法
        const val REQUIRED_ONLY_REQUEST = PerUtils.REQUIRED_ONLY_REQUEST

        //可以被禁止，且自动执行授权后方法
        const val NOT_REQUIRED_LOAD_METHOD = PerUtils.NOT_REQUIRED_LOAD_METHOD

        //可以被禁止，只进行申请权限，不自动执行授权后方法
        const val NOT_REQUIRED_ONLY_REQUEST = PerUtils.NOT_REQUIRED_ONLY_REQUEST
        private const val SETTING_REQUEST = PerUtils.SETTING_REQUEST
        private var permissionList: HashMap<String, Int> = getPermissionNameList()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSPok = getSharedPreferences(FILLNAME_OK, MODE_PRIVATE)
    }

    /**
     * 判断是否拥有权限
     *
     * @param permissions 不定长数组
     */
    fun askForPermission(code: Int, vararg permissions: String) {
        hasReadMediaVisualUserSelected = false//重置为false
        needShow = false//是否显示弹窗
        val realMissPermission: MutableList<String> = ArrayList()//这个是真正要去请求的权限
        val per = checkNeedPermission(permissions as Array<String>)//将传入的权限进行过滤，去掉一些特殊无用权限
        for (permission in per) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                //14以上需要判断新权限
                if (permission == ManifestPro.permission.READ_MEDIA_VISUAL_USER_SELECTED){
                    //用户发起了本权限请求，需要记录
                    hasReadMediaVisualUserSelected = true
                }
            }
            //判断权限是否已经被授权
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //用户未授权加入列表
                realMissPermission.add(permission)
                if (mSPok.getBoolean(permission,true)){
                    //存在可以授权的权限，则弹窗
                    needShow = true
                }
            }else{
                //权限已经授权，则强制修改SP为true
                mSPok.edit().putBoolean(permission,true).apply()
            }
        }
        if (realMissPermission.size > 0) {
            //确实存在要请求的权限，则发起请求
            val missPermissions = realMissPermission.toTypedArray()
            if (needShow){
                //需要显示，则调用方法
                requestPermissionProceed()
            }
            requestPermission(code, *missPermissions)
        }else{
            //没有要发起请求的权限，则按照方案进行回调
            when (code) {
                ChinaPermissionActivity.REQUIRED_LOAD_METHOD -> {
                    loadMethodFlag = true
                }

                ChinaPermissionActivity.REQUIRED_ONLY_REQUEST -> {
                    loadMethodFlag = false
                }

                ChinaPermissionActivity.NOT_REQUIRED_LOAD_METHOD -> {
                    loadMethodFlag = true
                }

                ChinaPermissionActivity.NOT_REQUIRED_ONLY_REQUEST -> {
                    loadMethodFlag = false
                }
            }
            if (loadMethodFlag){
                permissionAllowed()
            }
        }
    }

    /**
     * 请求权限
     *
     * @param code        请求码
     * @param permissions 权限列表
     */
    private fun requestPermission(code: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, code)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray, ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (needShow){
            //发起方法被回调过，所以这里要回调结束方法
            requestPermissionOver()
        }
        var permissionFlag = true //权限是否全部通过
        var dialogFlag = false //是否显示设置dialog
        var requiredFlag = false //是否为项目必须的权限
        val per = ArrayList<String>() //保存被拒绝的权限列表
        var allowJump = false//是否允许忽略图片和视频权限的拒绝情况
        initMissingPermissionDialog()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            //14下对特殊权限，强制判断是否可以跳过
            if (hasReadMediaVisualUserSelected){
                var found = false
                for ((i,permission) in permissions.withIndex()) {
                    if (permission == ManifestPro.permission.READ_MEDIA_VISUAL_USER_SELECTED){
                        //匹配此权限
                        found = true
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            //同意权限
                            allowJump = true
                        }
                    }
                }
                if (!found){
                    //说明已授权
                    allowJump = true
                }
            }
        }
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //存在被拒绝的权限
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) && allowJump){
                    if (permissions[i] != ManifestPro.permission.READ_MEDIA_IMAGES &&
                        permissions[i] != ManifestPro.permission.READ_MEDIA_VIDEO){
                        per.add(permissions[i])
                        permissionFlag = false
                    }else{
                        //是这两个权限，且在Android14以上，所以需要重置他俩的记录，避免造成用不显示弹窗的问题。
                        mSPok.edit().putBoolean(permissions[i],true).apply()
                    }
                }else{
                    per.add(permissions[i])
                    permissionFlag = false
                }
            }else{
                //这里都是被允许的权限，写到SP里记录
                mSPok.edit().putBoolean(permissions[i],true).apply()
            }
        }
        when (requestCode) {
            REQUIRED_LOAD_METHOD -> {
                loadMethodFlag = true
                requiredFlag = true
            }

            REQUIRED_ONLY_REQUEST -> {
                loadMethodFlag = false
                requiredFlag = true
            }

            NOT_REQUIRED_LOAD_METHOD -> {
                loadMethodFlag = true
                requiredFlag = false
            }

            NOT_REQUIRED_ONLY_REQUEST -> {
                loadMethodFlag = false
                requiredFlag = false
            }
        }
        //List<Integer> ids = selectGroup(per);//判断被拒绝的权限组名称
        if (permissionFlag) {
            //通过了申请的权限
            if (loadMethodFlag) {
                permissionAllowed() //权限通过，执行对应方法
            }
        } else {
            for (permission in per) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //当前权限被设置了"不在询问",永远不会弹出进入这里，将dialog显示标志设为true
                    dialogFlag = true
                    //不再询问的权限，SP记录为false
                    mSPok.edit().putBoolean(permission,false).apply()
                }else{
                    //还可以询问的权限要重置为true，避免用户手动修改，这辈子都不显示弹窗
                    mSPok.edit().putBoolean(permission,true).apply()
                }
            }
            if (requiredFlag) {
                if (dialogFlag) {
                    //显示缺少权限，并解释为何需要这个权限
                    if (missPermission != null) {
                        missPermission!!.clear()
                    } else {
                        missPermission = ArrayList()
                    }
                    //per包含当次拒绝和永久拒绝的权限。并未将他们分开
                    missPermission!!.addAll(per)
                    showMissingPermissionDialog(per)
                } else {
                    if (loadMethodFlag) {
                        permissionRejected()
                    }
                }
            } else {
                Log.e("Permission:", "Permission had been rejected")
                if (loadMethodFlag) {
                    permissionRejected()
                }
            }
        }
    }

    /**
     * 给子类提供重写的成功方法
     */
    open fun permissionAllowed() {}

    /**
     * 给子类提供重写的失败方法
     */
    open fun permissionRejected() {}
    /**
     * 给子类提供重写的请求开始方法
     */
    open fun requestPermissionProceed() {}

    /**
     * 给子类提供重写的请求结束方法
     */
    open fun requestPermissionOver() {}
    private fun initMissingPermissionDialog() {
        perDialog = PerDialog(this)
        perDialog.setTitle(getTrueString(this, R.string.help))
        perDialog.setCancel(getTrueString(this, R.string.cancal))
        perDialog.setSetting(getTrueString(this, R.string.setting_name))
        perDialog.setOnCancelClickListener {
            if (loadMethodFlag) {
                permissionRejected()
            }
        }
        perDialog.setOnSettingClickListener { //跳转到，设置的对应界面
            startAppSettings()
        }
    }

    /**
     * 显示解释设置dialog
     *
     * @param per 权限组名
     */
    fun showMissingPermissionDialog(per: List<String>) {
        val ids = selectGroup(per)
        var content = ""
        //将权限组名字转换为字符串
        if (ids.size > 0) {
            for (id in ids) {
                content = "$content${getTrueString(this,id)}\n"
            }
        }
        perDialog.setContent("${getTrueString(this, R.string.leak)}\n$content${getTrueString(this, R.string.go_setting)}")
        perDialog.show()
    }

    /**
     * 启动应用的设置
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, SETTING_REQUEST)
    }

    /**
     * 将权限，对应到权限组名，并去重
     *
     * @param permissions 权限名
     * @return 权限组名
     */
    private fun selectGroup(permissions: List<String>): List<Int> {
        var group: MutableList<Int> = ArrayList()
        for (permission in permissions) {
            if (permissionList != null) {
                group.add(permissionList[permission]!!)
            }
        }
        //去重
        group = ArrayList(HashSet(group))
        return group
    }

    private fun recheckPermission() {
        if (missPermission != null && missPermission!!.size > 0) {
            val per: MutableList<String> = ArrayList()
            //重新检查权限
            for (permission in missPermission!!) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    per.add(permission)
                }
            }
            if (per.size > 0) {
                //依然有权限未通过
                missPermission!!.clear()
                missPermission!!.addAll(per)
                showMissingPermissionDialog(per)
            } else {
                missPermission!!.clear()
                if (perDialog != null && perDialog.isShowing) {
                    perDialog.dismiss()
                }
                permissionAllowed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTING_REQUEST) {
            //从设置返回的回调
            recheckPermission()
        }
    }

    private fun getTrueString(context: Context, @StringRes id: Int): String {
        return if (isActivited()) {
            //启动了国际化
            getLanguageString(context, id)
        } else {
            //没有启动国际化
            getString(id)
        }
    }
}