package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen

fun NavHostController.navigateToBottom(
    route: Screen
){
    navigate(route){
        popUpTo(graph.id) {
            inclusive = false
        }
        launchSingleTop = true
    }
}