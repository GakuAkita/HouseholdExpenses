package gaku.original.myapplication.utility

import androidx.navigation.NavHostController

fun navigateToSingle(navController: NavHostController, route: String) {
    navController.navigate(route) {
        // すでにスタックにある route まで戻す
        popUpTo(route) {
            inclusive = false // route 自体は消さない
        }
        launchSingleTop = true // 再生成せず、既存の route を利用
    }
}