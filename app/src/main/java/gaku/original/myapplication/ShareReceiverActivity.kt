package gaku.original.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 画像uriを取得 */
        val imageUri =
            if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            } else null

        setContent {
            MaterialTheme {
                if (imageUri != null) {
                    Text("画像受信に成功しました")
                } else {
                    Text("画像が受け取れませんでした")
                }
            }
        }
    }
}