package spa.lyh.cn.permission.adapter;

import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import spa.lyh.cn.lib_image.app.ImageLoadUtil;
import spa.lyh.cn.permission.R;
import spa.lyh.cn.permission.bean.PictureBean;

public class PhotoAdapter extends BaseQuickAdapter<PictureBean, BaseViewHolder> {
    public PhotoAdapter(@Nullable List<PictureBean> data) {
        super(R.layout.item_photo, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, PictureBean pictureBean) {
        ImageView img = baseViewHolder.getView(R.id.photo);
        ImageLoadUtil.displayImage(getContext(),pictureBean.getFirstImageUrl(),img);
    }
}
