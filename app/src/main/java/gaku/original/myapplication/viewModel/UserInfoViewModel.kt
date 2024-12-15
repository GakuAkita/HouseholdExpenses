package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gaku.original.myapplication.data.USER_ID_NULL_REPLACEMENT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserInfoViewModel(): ViewModel(){
    /****************ユーザー管理*****************/
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userId = MutableStateFlow(firebaseAuth.currentUser?.uid)
    val userId: StateFlow<String?> = _userId

    fun getUserId(): String {
        return _userId.value?: USER_ID_NULL_REPLACEMENT
    }

    //AuthStateListenerを保持
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        //@TODO これでListenerが複数回追加されないかチェック
        /*firebaseAuth.removeAuthStateListener(authStateListener)*/

        // AuthStateListenerでサインイン・サインアウトを監視
        authStateListener = FirebaseAuth.AuthStateListener {auth->
            val user = auth.currentUser
            _currentUser.value = user // currentUserを更新
            _userId.value = user?.uid // currentUserが更新されると自動でuserIdも更新
            //signInした後userIdがnullではないかどうかは、signInの関数で確認
        }

        //リスナーを追加
        firebaseAuth.addAuthStateListener(authStateListener!!)
    }

    //これをやらないとどんどんリスナーが追加されていく?
    override fun onCleared() {
        super.onCleared()
        //ViewModelが破棄される際にリスナーを解除
        authStateListener?.let { firebaseAuth.removeAuthStateListener(it)}
    }
}