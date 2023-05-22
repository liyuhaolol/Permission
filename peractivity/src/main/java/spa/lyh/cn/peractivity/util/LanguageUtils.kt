package spa.lyh.cn.peractivity.util

import android.content.Context

object LanguageUtils {
    val className:String = "spa.lyh.cn.languagepack.LanguagesPack"

    @JvmStatic
    fun isActivited():Boolean{
        return try {
            Class.forName(className)
            true
        }catch (ignored:Exception){
            false
        }
    }

    @JvmStatic
    fun getLanguageString(context:Context,id:Int):String{
        val obj: Any
        return try {
            val clazz = Class.forName(className)
            val method = clazz.getMethod(
                "getString",
                Context::class.java,
                Int::class.javaPrimitiveType
            )
            obj = method.invoke(clazz, context, id) as Any
            (obj as String)
        } catch (ignored:Exception) {
            ""
        }
    }
}