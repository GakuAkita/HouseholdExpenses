package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import gaku.original.myapplication.SharedReceiverGraph
import gaku.original.myapplication.ui.screens.receiver.ShareReceiverScreenRoot

fun NavGraphBuilder.shareReceiverGraph(
    navHostController: NavHostController,
) {

    navigation<SharedReceiverGraph>(
        startDestination = SharedReceiverGraph.SharedReceiver.Entry::class
    ) {
        composable<SharedReceiverGraph.SharedReceiver.Entry> {
            ShareReceiverScreenRoot()
        }
    }
}