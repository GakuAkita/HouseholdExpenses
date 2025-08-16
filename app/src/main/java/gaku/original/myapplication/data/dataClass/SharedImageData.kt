package gaku.original.myapplication.data.dataClass

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedImageData(
    val packageName: String?,
    val imageUri: Uri,
    val receivedTime: Long = System.currentTimeMillis()
) : Parcelable {
    companion object {
        const val EXTRA_KEY = "shared_image_data"
    }
}