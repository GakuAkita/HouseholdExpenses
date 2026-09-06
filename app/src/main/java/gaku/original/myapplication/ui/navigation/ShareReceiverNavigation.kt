package gaku.original.myapplication.ui.navigation

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import gaku.original.myapplication.SharedData
import gaku.original.myapplication.SharedReceiverGraph
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.ShareReceiverScreenRoot
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.ShareReceiverViewModel
import kotlin.reflect.typeOf

fun NavGraphBuilder.shareReceiverGraph(navHostController: NavHostController) {
    navigation<SharedReceiverGraph>(
        startDestination = SharedReceiverGraph.SharedReceiver.Entry::class
    ) {
        composable<SharedReceiverGraph.SharedReceiver.Entry>(
            typeMap = mapOf(typeOf<SharedData>() to navTypeOf<SharedData>())
        ) { backStackEntry ->
            val sharedData =
                backStackEntry.toRoute<SharedReceiverGraph.SharedReceiver.Entry>().data

            val context = LocalContext.current

            ShareReceiverScreenRoot(
                viewModel = viewModel(
                    factory = ShareReceiverViewModel.Factory(
                        sharedData
                    )
                ),
                navHostController,
                onComplete = {
                    (context as Activity?)?.finish()
                })
        }

        composable<SharedReceiverGraph.SharedReceiver.PayPayReceiptMaskRatioAdjust> { backStackEntry ->
            val route =
                backStackEntry.toRoute<SharedReceiverGraph.SharedReceiveer.PayPayReceiptMaskRatioAdjust>()

            PayPayReceipt
                navHostController
            )
        }
    }
}