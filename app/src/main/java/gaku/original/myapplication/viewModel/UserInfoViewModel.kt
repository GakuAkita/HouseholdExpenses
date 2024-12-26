package gaku.original.myapplication.viewModel

import android.util.Log
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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

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
            Log.d("UserInfoViewModel","AuthStateListener was called:currentUser:${user?.uid}")
        }

        //リスナーを追加
        firebaseAuth.addAuthStateListener(authStateListener!!)

        //ログイン状態を更新
        _isLoggedIn.value = firebaseAuth.currentUser!=null
    }

    //これをやらないとどんどんリスナーが追加されていく?
    override fun onCleared() {
        super.onCleared()
        //ViewModelが破棄される際にリスナーを解除
        authStateListener?.let { firebaseAuth.removeAuthStateListener(it)}
    }
}