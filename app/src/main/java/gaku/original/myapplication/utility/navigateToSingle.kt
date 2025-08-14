package gaku.original.myapplication.utility

import androidx.navigation.NavHostController

fun navigateToSingle(navController: NavHostController, route: String) {
    navController.navigate(route) {
        // すでにスタックにある OcrRead まで戻す
        popUpTo(route) {
            inclusive = false // OcrRead 自体は消さない
        }
        launchSingleTop = true // 再生成せず、既存の OcrRead を利用
    }
}