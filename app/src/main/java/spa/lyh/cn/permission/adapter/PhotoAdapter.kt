package spa.lyh.cn.permission.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chad.library.adapter4.BaseQuickAdapter
import spa.lyh.cn.lib_image.app.ImageLoadUtil
import spa.lyh.cn.permission.bean.PictureBean
import spa.lyh.cn.permission.databinding.ItemPhotoBinding

class PhotoAdapter(data: List<PictureBean>) :
    BaseQuickAdapter<PictureBean, PhotoAdapter.VH>(data) {

    class VH(val b:ItemPhotoBinding):ViewHolder(b.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: PictureBean?) {
        ImageLoadUtil.displayImage(context, item!!.firstImageUrl, holder.b.photo)
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(ItemPhotoBinding.inflate(LayoutInflater.from(context),parent,false))
    }
}
