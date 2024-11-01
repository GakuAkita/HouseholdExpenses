package gaku.original.myapplication

//もう少しわかりやすい構造にしよう。AddScreenをMainの配下にいれたり。後日やな。
sealed class Screen(val route:String){
    //Mainスクリーン関連
    object MainScreen: Screen("main"){
        object Content:Screen("main/content")
        object Add:Screen("main/add")
    }

    //bottom barのその他
    object GraphScreen: Screen("graph_screen")
    object NotCategorizedScreen: Screen("not_categorized_screen")
    object SettingScreen: Screen("setting_screen")
}