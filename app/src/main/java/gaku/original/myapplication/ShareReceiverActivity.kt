package gaku.original.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import gaku.original.myapplication.ui.navigation.navTypeOf
import gaku.original.myapplication.ui.screens.receiver.ShareReceiverScreenRoot
import gaku.original.myapplication.ui.screens.receiver.ShareReceiverViewModel
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import timber.log.Timber
import kotlin.reflect.typeOf

// https://developer.android.com/training/basics/intents/filters
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HouseholdExpensesTheme(
                darkTheme = true
            ) {
                val navController = rememberNavController()

                Timber.d("callingPackage = ${callingPackage}")
                Timber.d("referrer = ${referrer}")
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SharedReceiverGraph.SharedReceiver.Entry(intent.toSharedData())
                    ) {
                        composable<SharedReceiverGraph.SharedReceiver.Entry>(
                            typeMap = mapOf(typeOf<SharedData>() to navTypeOf<SharedData>())
                        ) { backStackEntry ->
                            val sharedData =
                                backStackEntry.toRoute<SharedReceiverGraph.SharedReceiver.Entry>().data
                            ShareReceiverScreenRoot(
                                viewModel = viewModel(
                                    factory = ShareReceiverViewModel.Factory(
                                        sharedData
                                    )
                                )
                            )
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
    Timber.d("senderPackage:$senderPackage")

    Timber.d("action:${this.action} type:${this.type} data:${this.data.toString()}")

    if (this.action == Intent.ACTION_SEND && this.type?.startsWith("image/") == true) {
        return SharedData.Image(
            senderPackage,
            this.data.toString()
        )
    }
    return SharedData.Unknown(senderPackage)
}

@Serializable
@Parcelize
sealed interface SharedData : Parcelable {
    val packageName: String?

    @Serializable
    @Parcelize
    data class Image(
        override val packageName: String?,
        val imageUri: String?
    ) : SharedData, Parcelable

    @Serializable
    @Parcelize
    data class Unknown(
        override val packageName: String?
    ) : SharedData, Parcelable
}

