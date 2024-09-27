package spa.lyh.cn.permission.test

import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

open class PickImage : ActivityResultContracts.PickVisualMedia(){

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        val intent = super.createIntent(context, input)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/jpg", "image/png"))
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/gif"))
        return intent
    }
}