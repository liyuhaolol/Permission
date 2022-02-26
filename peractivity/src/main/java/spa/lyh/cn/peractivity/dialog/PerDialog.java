package spa.lyh.cn.peractivity.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import spa.lyh.cn.peractivity.R;

public class PerDialog extends Dialog {
   private ViewGroup contentView;
   private Context mContext;
   private TextView title,content,cancel,setting;
   private View.OnClickListener cancelListener,settingListener;

   public PerDialog(@NonNull Context context) {
      super(context);
      this.mContext = context;
      initDialogStyle();
   }

   public PerDialog(@NonNull Context context, int themeResId) {
      super(context, themeResId);
      this.mContext = context;
      initDialogStyle();
   }

   protected PerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
      super(context, cancelable, cancelListener);
      this.mContext = context;
      initDialogStyle();
   }

   private void initDialogStyle(){
      setContentView(createDialogView(R.layout.per_dialog));
      /*WindowManager.LayoutParams layoutParams=getWindow().getAttributes();
      DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
      layoutParams.width= metrics.widthPixels;//设置Dialog的宽度为屏幕宽度
      getWindow().setAttributes(layoutParams);*/
      setCancelable(false);
      //
      title = contentView.findViewById(R.id.title);
      content = contentView.findViewById(R.id.content);
      cancel = contentView.findViewById(R.id.cancel);
      setting = contentView.findViewById(R.id.setting);
      cancel.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (cancelListener != null){
               cancelListener.onClick(v);
               dismiss();
            }
         }
      });
      setting.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (settingListener != null){
               settingListener.onClick(v);
            }
         }
      });
   }

   private ViewGroup createDialogView(int layoutId){
      contentView = (ViewGroup) LayoutInflater.from(getContext()).inflate(layoutId, null);
      return contentView;
   }

   public void setTitle(String title){
      this.title.setText(title);
   }

   public void setContent(String content){
      this.content.setText(content);
   }

   public void setCancel(String cancel){
      this.cancel.setText(cancel);
   }

   public void setSetting(String setting){
      this.setting.setText(setting);
   }

   public void setOnCancelClickListener(View.OnClickListener listener){
      this.cancelListener = listener;
   }

   public void setOnSettingClickListener(View.OnClickListener listener){
      this.settingListener = listener;
   }
}
