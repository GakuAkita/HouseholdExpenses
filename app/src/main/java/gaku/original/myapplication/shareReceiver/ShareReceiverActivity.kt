package gaku.original.myapplication.shareReceiver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import timber.log.Timber

// https://developer.android.com/training/basics/intents/filters
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HouseholdExpensesTheme(
                darkTheme = true
            ) {
                val data: Uri? = intent?.data

                val flag = remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("I'm Gaku. I came from heaven.identity=${System.identityHashCode(this)}")

                        Button(
                            onClick = {
                                flag.value = true
                            }
                        ) {
                            if (flag.value) {
                                Text("I'm clicked")
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }

//        /**
//         * パッケージが取れていない！！
//         */
//        val senderPackage: String? = when {
//            intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME) != null ->
//                intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
//
//            intent.`package` != null ->
//                intent.`package`
//
//            callingPackage != null ->
//                callingPackage
//
//            referrer != null ->
//                referrer?.host
//
//            else -> null
//        }
//
//        val allowedPackages = listOf(
//            AppPackageNames.PAYPAY,
//        )
//
//        if (senderPackage in allowedPackages) {
//            Toast.makeText(this, "Accepted ${senderPackage}", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(
//                this,
//                "このアプリからの共有は対応していません${senderPackage}",
//                Toast.LENGTH_SHORT
//            ).show()
//            finish()
//            return
//        }
//
//        /* 画像uriを取得 */
//        val imageUri: Uri? =
//            if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
//                intent.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM)
//            } else null
//
//        if (imageUri != null) {
//            /* mainActivityに遷移してUriを渡す */
//            val copiedUri = copyUriToCache(this, imageUri)
//            if (copiedUri == null) {
//                Toast.makeText(
//                    this,
//                    "受け取った画像をキャッシュにコピーできませんでした",
//                    Toast.LENGTH_SHORT
//                ).show()
//                finish()
//                return
//            }
//
//            val data = SharedImageData(
//                senderPackage,
//                copiedUri
//            )
//
//            val mainIntent = Intent(this, MainActivity::class.java).apply {
//                /* onNewIntent側で処理を区別するためのキー */
//                putExtra(
//                    IntentKey, IntentSourceKeys.SHARE_IMAGE_FOR_OCR
//                )
//                putExtra(SharedImageData.EXTRA_KEY, data)
//                flags =
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION//or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            }
//            Log.d("ShareReceiverActivity", "passing ${imageUri}")
//            startActivity(mainIntent)
//            finish()
//            return
//        } else {
//            Toast.makeText(this, "画像が受け取れませんでした", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("ShareReceiverActivity onNewIntent")
    }

    override fun onStart() {
        super.onStart()
        Timber.d("ShareReceiverActivity started.${hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("ShareReceiverActivity stopped.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("ShareReceiverActivity destroyed")
    }
}

fun Intent.toSharedData(): SharedData {
    val senderPackage: String? = when {
        this.getStringExtra(Intent.EXTRA_PACKAGE_NAME) != null ->
            this.getStringExtra(Intent.EXTRA_PACKAGE_NAME)

        this.`package` != null ->
            this.`package`

        else -> null
    }

    if (this.action == Intent.ACTION_SEND && this.type?.startsWith("image/") == true) {
        return SharedData.Image(
            senderPackage,
            this.data
        )
    }
    return SharedData.Unknown(senderPackage)
}

sealed interface SharedData {
    val packageName: String?

    data class Image(
        override val packageName: String?,
        val imageUri: Uri?
    ) : SharedData

    data class Unknown(
        override val packageName: String?
    ) : SharedData
}

