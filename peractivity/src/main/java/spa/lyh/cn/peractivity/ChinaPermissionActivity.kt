package spa.lyh.cn.peractivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
 *
 *
 * 不同权限需求种类，不要在同一个权限组里发起申请，因为code你一次只能传1种，4种需求种类对应4种应用场景，所以
 * 不要尝试使用一套逻辑来同时兼容4种模式，应该是不现实的。
 *
 *
 */
open class ChinaPermissionActivity : AppCompatActivity() {
    private val FILLNAME = "chinapermission" // 文件名称
    private var mSharedPreferences: SharedPreferences? = null
    private val missPerList = ArrayList<String>()
    private var loadMethodFlag = false//是否自动加载方法
    private var missPermission: ArrayList<String>? = null
    private var hasReadMediaVisualUserSelected = false//是否请求了这个权限

    //被永久拒绝之后显示的dialog
    private var perDialog: PerDialog? = null
    private var perCheckDialog: PerDialog? = null

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
        private val permissionList: HashMap<String, Int> = getPermissionNameList()
    }

    /**
     * 判断是否拥有权限
     *
     * @param permissions 不定长数组
     */
    fun askForPermission(code: Int, vararg permissions: String?) {
        hasReadMediaVisualUserSelected = false//重置为false
        missPerList.clear()
        val realMissPermission: MutableList<String> = ArrayList()
        var flag = true
        val per = checkNeedPermission(permissions as Array<String>)
        for (permission in per) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                //14以上需要判断新权限
                if (permission == ManifestPro.permission.READ_MEDIA_VISUAL_USER_SELECTED){
                    //用户发起了本权限请求，需要记录
                    hasReadMediaVisualUserSelected = true
                }
            }
            //判断权限是否已经被授权
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                realMissPermission.add(permission)
                flag = false
            }
        }
        if (mSharedPreferences == null) {
            mSharedPreferences = getSharedPreferences(FILLNAME, MODE_PRIVATE)
        }
        for (i in realMissPermission.indices) {
            val perName = realMissPermission[i]
            val mark = mSharedPreferences!!.getLong(perName, 0)
            val time = System.currentTimeMillis()
            if (time - mark <= 172800000) {//权限请求间隔不能小于48小时
                missPerList.add(perName)
            }
        }
        if (realMissPermission.size > 0) {
            if (missPerList.size == realMissPermission.size) {
                makeReject(code)
                return
            }
            for (perName in missPerList) {
                realMissPermission.remove(perName)
            }
            /*            for (String perName:realMissPermission){
                mSharedPreferences.edit().putLong(perName,System.currentTimeMillis()).apply();
            }*/
            val missPermissions = realMissPermission.toTypedArray()
            requestPermission(code, *missPermissions)
        }
        if (flag) {
            when (code) {
                REQUIRED_LOAD_METHOD -> {
                    loadMethodFlag = true
                }

                REQUIRED_ONLY_REQUEST -> {
                    loadMethodFlag = false
                }

                NOT_REQUIRED_LOAD_METHOD -> {
                    loadMethodFlag = true
                }

                NOT_REQUIRED_ONLY_REQUEST -> {
                    loadMethodFlag = false
                }
            }
            if (loadMethodFlag){
                permissionAllowed()
            }
        }
    }

    /**
     * 仅仅检查对应的权限，不进行权限请求，这个需求似乎仅仅在中国的适配权限才需要检查
     * check方法不响应METHOD是否加载的选项，因为不加载没有意义
     */
    fun checkPermissions(code: Int, vararg permissions: String) {
        var hasPassMediaVisualUserSelected = false//是否已经授权本权限
        missPerList.clear()
        val realMissPermission: ArrayList<String> = ArrayList()
        var flag = true
        if (mSharedPreferences == null) {
            mSharedPreferences = getSharedPreferences(FILLNAME, MODE_PRIVATE)
        }
        for (permission in permissions) {
            var isIt = false//是他
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                //14以上需要判断新权限
                if (permission == ManifestPro.permission.READ_MEDIA_VISUAL_USER_SELECTED){
                    //用户发起了本权限请求，需要记录
                    isIt = true
                }
            }
            //判断权限是否已经被授权
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val mark = mSharedPreferences!!.getLong(permission, -1L)
                if (mark != -1L){
                    //代表权限曾被请求过
                    realMissPermission.add(permission)
                }
            }else{
                //已被授权
                if (isIt){
                    hasPassMediaVisualUserSelected = true
                }
            }
        }
        for (i in realMissPermission.indices) {
            val perName = realMissPermission[i]
            val mark = mSharedPreferences!!.getLong(perName, 0)
            val time = System.currentTimeMillis()
            if (time - mark <= 172800000) {//权限请求间隔不能小于48小时
                missPerList.add(perName)
                flag = false
            }
        }
        if (missPerList.size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                //14以上需要判断新权限
                if(hasPassMediaVisualUserSelected){
                    //这个权限被通过了，所以需要忽略IMAGE和VIDEO权限
                    val iterator = missPerList.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next()
                        if (item == ManifestPro.permission.READ_MEDIA_IMAGES || item == ManifestPro.permission.READ_MEDIA_VIDEO) {
                            iterator.remove()
                        }
                    }
                }
            }
            if (missPerList.size > 0){
                //依旧大于0，存在被拒绝的权限
                makeRejectCheck(code,missPerList)
            }else{
                //没有被拒绝的权限了
                permissionCheck48HPass()
            }

        }

        if (flag) {
            permissionCheck48HPass()
        }
    }

    private fun makeReject(code: Int) {
        initMissingPermissionDialog()
        var requiredFlag = false //是否为项目必须的权限
        when (code) {
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
        if (requiredFlag) {
            //显示缺少权限，并解释为何需要这个权限
            if (missPermission != null) {
                missPermission!!.clear()
            } else {
                missPermission = ArrayList()
            }
            missPermission!!.addAll(missPerList)
            showMissingPermissionDialog(missPermission!!)
        } else {
            if (loadMethodFlag) {
                permissionRejected()
            }
        }
    }
    private fun makeRejectCheck(code: Int,permissions: ArrayList<String>) {
        initMissingPermissionCheckDialog(permissions)
        var requiredFlag = false //是否为项目必须的权限
        when (code) {
            REQUIRED_LOAD_METHOD -> {
                requiredFlag = true
            }

            REQUIRED_ONLY_REQUEST -> {
                requiredFlag = true
            }

            NOT_REQUIRED_LOAD_METHOD -> {
                requiredFlag = false
            }

            NOT_REQUIRED_ONLY_REQUEST -> {
                requiredFlag = false
            }
        }
        if (requiredFlag) {
            //显示缺少权限，并解释为何需要这个权限
            if (missPermission != null) {
                missPermission!!.clear()
            } else {
                missPermission = ArrayList()
            }
            missPermission!!.addAll(missPerList)
            showMissingPermissionCheckDialog(missPermission!!)
        } else {
            permissionCheck48HDenied(missPermission!!)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionFlag = missPerList.size <= 0 //权限是否全部通过
        var requiredFlag = false //是否为项目必须的权限
        val per = ArrayList<String>() //保存被拒绝的权限列表
        var allowJump = false//是否允许忽略图片和视频权限的拒绝情况
        initMissingPermissionDialog()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            //14
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
                        mSharedPreferences!!.edit().putLong(permissions[i], System.currentTimeMillis())
                            .apply()
                    }else{
                        //将这两个权限的请求时间记录清除，重置为0
                        mSharedPreferences!!.edit().putLong(permissions[i], 0).apply()
                    }
                }else{
                    per.add(permissions[i])
                    permissionFlag = false
                    mSharedPreferences!!.edit().putLong(permissions[i], System.currentTimeMillis())
                        .apply()
                }
            } else {
                //权限通过了，重置为0
                mSharedPreferences!!.edit().putLong(permissions[i], 0).apply()
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
            //通过了申请的权限，但是存在可能得48小时拒绝权限，所以这里要额外判断
            if (missPerList.size > 0){
                makeReject(requestCode)
            }else{
                if (loadMethodFlag) {
                    permissionAllowed() //权限通过，执行对应方法
                }
            }
        } else {
            if (requiredFlag) {
                if (per.size > 0) { //严谨判断大于0
                    //显示缺少权限，并解释为何需要这个权限
                    if (missPermission != null) {
                        missPermission!!.clear()
                    } else {
                        missPermission = ArrayList()
                    }
                    missPermission!!.addAll(per)
                    missPermission!!.addAll(missPerList)
                    showMissingPermissionDialog(missPermission!!)
                }
            } else {
                Log.e("Permission:", "Permission had been rejected")
                if (loadMethodFlag) {
                    permissionRejected()
                }
            }
        }
    }

    private fun initMissingPermissionDialog() {
        if (perDialog == null) {
            perDialog = PerDialog(this)
            perDialog!!.setTitle(getTrueString(this, R.string.help))
            perDialog!!.setCancel(getTrueString(this, R.string.cancal))
            perDialog!!.setSetting(getTrueString(this, R.string.setting_name))
            perDialog!!.setOnCancelClickListener {
                if (loadMethodFlag) {
                    permissionRejected()
                }
            }
            perDialog!!.setOnSettingClickListener { //跳转到，设置的对应界面
                startAppSettings()
            }
        }
    }
    private fun initMissingPermissionCheckDialog(permissions: ArrayList<String>) {
        if (perCheckDialog == null) {
            perCheckDialog = PerDialog(this)
            perCheckDialog!!.setTitle(getTrueString(this, R.string.help))
            perCheckDialog!!.setCancel(getTrueString(this, R.string.cancal))
            perCheckDialog!!.setSetting(getTrueString(this, R.string.setting_name))
            perCheckDialog!!.setOnCancelClickListener {
                permissionCheck48HDenied(permissions)
            }
            perCheckDialog!!.setOnSettingClickListener { //跳转到，设置的对应界面
                startAppSettings()
            }
        }
    }

    /**
     * 给子类提供重写的成功接口
     */
    open fun permissionAllowed() {}

    /**
     * 给子类提供重写的失败接口
     */
    open fun permissionRejected() {}

    /**
     * 给子类提供重写的检查成功接口
     */
    open fun permissionCheck48HPass() {}

    /**
     * 给子类提供重写的检查失败接口
     */
    open fun permissionCheck48HDenied(permissions: ArrayList<String>) {}

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
        perDialog!!.setContent("${getTrueString(this, R.string.leak)}\n$content${getTrueString(this, R.string.go_setting)}")
        perDialog!!.show()
    }
    fun showMissingPermissionCheckDialog(per: List<String>) {
        val ids = selectGroup(per)
        var content = ""
        //将权限组名字转换为字符串
        if (ids.size > 0) {
            for (id in ids) {
                content = "$content${getTrueString(this,id)}\n"
            }
        }
        perCheckDialog!!.setContent("${getTrueString(this, R.string.leak)}\n$content${getTrueString(this, R.string.go_setting)}")
        perCheckDialog!!.show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTING_REQUEST) {
            //从设置返回的回调
            recheckPermission()
        }
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
                } else {
                    mSharedPreferences!!.edit().putLong(permission, 0).apply()
                }
            }
            if (per.size > 0) {
                //依然有权限未通过
                missPermission!!.clear()
                missPermission!!.addAll(per)
                showMissingPermissionDialog(per)
            } else {
                missPermission!!.clear()
                if (perDialog != null && perDialog!!.isShowing) {
                    perDialog!!.dismiss()
                }
                permissionAllowed()
            }
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