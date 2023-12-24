package spa.lyh.cn.permission.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import spa.lyh.cn.lib_utils.TimeUtils;
import spa.lyh.cn.lib_utils.translucent.TranslucentUtils;
import spa.lyh.cn.permission.R;

public class PerTopDialog extends Dialog {
    private ViewGroup contentView;

    private Activity mActivity;

    private TextView title;
    private TextView content;

    public PerTopDialog(@NonNull Activity context) {
        this(context, R.style.CommonTextDialog);
    }

    public PerTopDialog(@NonNull Activity activity, int themeResId) {
        super(activity, themeResId);
        this.mActivity = activity;
        initDialogStyle();
    }

    private void initDialogStyle(){
        setContentView(createDialogView(R.layout.pop_per));
        if (getWindow() != null){
            //设置动画
            getWindow().setWindowAnimations(R.style.DialogAnimation);
            //设置布局顶部显示
            getWindow().setGravity(Gravity.TOP);
            //设置背景透明后设置该属性，可去除dialog边框
            getWindow().setBackgroundDrawable(new ColorDrawable());
            //设置横向铺满全屏
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            //开启沉浸式
            TranslucentUtils.setTranslucentBoth(getWindow());
            //兼容刘海屏
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            getWindow().setAttributes(lp);
        }
        RelativeLayout background = contentView.findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        title = contentView.findViewById(R.id.title);
        content = contentView.findViewById(R.id.content);
    }

    public void show(String title,String content){
        this.title.setText(title);
        this.content.setText(content);
        super.show();
    }


    private ViewGroup createDialogView(int layoutId){
        contentView = (ViewGroup) LayoutInflater.from(getContext()).inflate(layoutId, null);
        return contentView;
    }
}
