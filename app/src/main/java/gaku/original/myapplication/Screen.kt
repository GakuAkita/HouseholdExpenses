package gaku.original.myapplication

sealed class Screen(val route:String){
    //Mainスクリーン関連
    object MainScreen: Screen("main_screen")
    object AddScreen: Screen("add_screen")

    //bottom barのその他
    object GraphScreen: Screen("graph_screen")
    object NotCategorizedScreen: Screen("not_categorized_screen")
    object SettingScreen: Screen("setting_screen")
}