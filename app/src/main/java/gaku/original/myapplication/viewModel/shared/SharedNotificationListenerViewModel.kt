package gaku.original.myapplication.viewModel.shared

/**
 * @HiltViewModelをつけたら、どうやらアクティビティ内で単一になった気がする。
 * つけなくても動くは動くが、どうやら複数インスタンスが作られる形になっていそう。
 * まじでどういう仕組なのかわからん。
 */
//class SharedNotificationListenerViewModel @Inject constructor() : ViewModel() {
//    /**
//     * NotificationListenerの値を格納しておく
//     */
//    private val _notificationData = MutableStateFlow<NotificationData?>(null)
//    val notificationData: StateFlow<NotificationData?> get() = _notificationData
//
//    var isMovedToNLProcess: Boolean = false
//
//    fun setIsMovedToNLProcess(value: Boolean = true) {
//        isMovedToNLProcess = value
//    }
//
//    fun setNotificationData(data: NotificationData?) {
//        _notificationData.value = data
//    }
//
//    fun getNotificationData(): NotificationData? {
//        return _notificationData.value
//    }
//
//    fun clearNotificationData() {
//        _notificationData.value = null
//    }
//}