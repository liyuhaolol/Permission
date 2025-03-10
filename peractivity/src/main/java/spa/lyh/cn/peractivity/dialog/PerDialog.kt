package spa.lyh.cn.peractivity.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import spa.lyh.cn.peractivity.R

class PerDialog : Dialog {
    private lateinit var contentView: ViewGroup
    private var mContext: Context
    private lateinit var title: TextView
    private lateinit var content: TextView
    private lateinit var cancel: TextView
    private lateinit var setting: TextView
    private lateinit var cancelListener: View.OnClickListener
    private lateinit var settingListener: View.OnClickListener

    constructor(context: Context) : super(context) {
        mContext = context
        initDialogStyle()
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        mContext = context
        initDialogStyle()
    }

    protected constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener
    ) : super(context, cancelable, cancelListener) {
        mContext = context
        initDialogStyle()
    }

    private fun initDialogStyle() {
        setContentView(createDialogView(R.layout.per_dialog))
        /*WindowManager.LayoutParams layoutParams=getWindow().getAttributes();
      DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
      layoutParams.width= metrics.widthPixels;//设置Dialog的宽度为屏幕宽度
      getWindow().setAttributes(layoutParams);*/setCancelable(false)
        //
        title = contentView.findViewById<TextView>(R.id.title)
        content = contentView.findViewById<TextView>(R.id.content)
        cancel = contentView.findViewById<TextView>(R.id.cancel)
        setting = contentView.findViewById<TextView>(R.id.setting)
        cancel.setOnClickListener(View.OnClickListener { v ->
            if (cancelListener != null) {
                cancelListener.onClick(v)
                dismiss()
            }
        })
        setting.setOnClickListener(View.OnClickListener { v ->
            if (settingListener != null) {
                settingListener.onClick(v)
            }
        })
    }

    private fun createDialogView(layoutId: Int): ViewGroup {
        contentView = LayoutInflater.from(context).inflate(layoutId, null) as ViewGroup
        return contentView
    }

    fun setTitle(title: String?) {
        this.title.text = title
    }

    fun setContent(content: String?) {
        this.content.text = content
    }

    fun setCancel(cancel: String?) {
        this.cancel.text = cancel
    }

    fun setSetting(setting: String?) {
        this.setting.text = setting
    }

    fun setOnCancelClickListener(listener: View.OnClickListener) {
        cancelListener = listener
    }

    fun setOnSettingClickListener(listener: View.OnClickListener) {
        settingListener = listener
    }
}