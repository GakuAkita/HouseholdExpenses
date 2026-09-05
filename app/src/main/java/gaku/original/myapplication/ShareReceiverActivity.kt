package gaku.original.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.domain.AuthState
import gaku.original.myapplication.ui.navigation.authGraph
import gaku.original.myapplication.ui.navigation.shareReceiverGraph
import gaku.original.myapplication.ui.screens.RootViewModel
import gaku.original.myapplication.ui.theme.HouseholdExpensesTheme
import gaku.original.myapplication.utility.getParcelableExtraCompat
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import timber.log.Timber

// https://developer.android.com/training/basics/intents/filters
class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootViewModel: RootViewModel by viewModels {
            RootViewModel.Factory
        }

        setContent {
            HouseholdExpensesTheme(
                darkTheme = true
            ) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(
                    LocalSnackBarHostState provides snackbarHostState
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        val authState by rootViewModel.authState.collectAsState()
                        LaunchedEffect(authState) {
                            when (authState) {
                                is AuthState.Loading -> {
                                }

                                is AuthState.LoggedIn -> {
                                    Timber.d("AuthState is LoggedIn..")
                                    val isSharedReceiverGraph =
                                        navController.currentBackStackEntry?.destination?.hierarchy?.any {
                                            it.hasRoute<SharedReceiverGraph>()
                                        } == true
                                    if (!isSharedReceiverGraph) {
                                        (application as MyApplication).appContainer.createSession()
                                        navController.navigate(
                                            SharedReceiverGraph.SharedReceiver.Entry(
                                                intent.toSharedData(
                                                    referrer?.toString()
                                                )
                                            )
                                        ) {
                                            popUpTo(AuthGraph.SignIn) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }

                                is AuthState.LoggedOut -> {
                                    val isAuthGraph =
                                        navController.currentBackStackEntry?.destination?.hierarchy?.any {
                                            it.hasRoute<AuthGraph>()
                                        } == true
                                    if (!isAuthGraph) {
                                        navController.navigate(AuthGraph.SignIn) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        NavHost(
                            navController = navController, startDestination = Splash
                        ) {
                            composable<Splash> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LinearProgressIndicator()
                                }
                            }

                            authGraph(navController)
                            shareReceiverGraph(navController)
                        }
                    }
                }
            }
        }
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

fun Intent.toSharedData(
    referrer: String? = null
): SharedData {
    val senderPackage: String? = when {
        referrer != null -> referrer

        this.getStringExtra(Intent.EXTRA_PACKAGE_NAME) != null -> this.getStringExtra(Intent.EXTRA_PACKAGE_NAME)

        this.`package` != null -> this.`package`

        else -> null
    }

    if (this.type?.startsWith("image/") == true) {
        return SharedData.Image(
            senderPackage, this.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM).toString()
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
        override val packageName: String?, val imagePath: String?
    ) : SharedData, Parcelable

    @Serializable
    @Parcelize
    data class Unknown(
        override val packageName: String?
    ) : SharedData, Parcelable
}

