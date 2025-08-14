package gaku.original.myapplication.ShareReceiver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.data.Constants.ShareReceiverKeys
import gaku.original.myapplication.utility.copyUriToCache
import gaku.original.myapplication.utility.getParcelableExtraCompat

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * パッケージが取れていない！！
         */
        val senderPackage: String? = when {
            intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME) != null ->
                intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)

            intent.`package` != null ->
                intent.`package`

            callingPackage != null ->
                callingPackage

            referrer != null ->
                referrer?.host

            else -> null
        }

        val allowedPackages = listOf(
            "jp.ne.paypay.android.app",
        )

        if (senderPackage in allowedPackages) {
            Toast.makeText(this, "Accepted ${senderPackage}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "このアプリからの共有は対応していません${senderPackage}",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        /* 画像uriを取得 */
        val imageUri: Uri? =
            if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
                intent.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM)
            } else null

        if (imageUri != null) {
            /* mainActivityに遷移してUriを渡す */
            val copiedUri = copyUriToCache(this, imageUri)
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                putExtra(ShareReceiverKeys.SHARED_IMAGE_URI, copiedUri)
                putExtra(
                    ShareReceiverKeys.IS_FROM_SHARE_RECEIVER,
                    true
                )/* Share ReceiverからMainActivityが起動されたとわかるように */
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION//or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            Log.d("ShareReceiverActivity", "passing ${imageUri}")
            startActivity(mainIntent)
            finish()
            return
        } else {
            Toast.makeText(this, "画像が受け取れませんでした", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ShareReceiverActivity", "ShareReceiverActivity destroyed")
    }
}


