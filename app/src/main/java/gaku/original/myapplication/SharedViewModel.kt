package gaku.original.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignUpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//これいいのかな。Repositoryもらってしまって。
//普通にきもい設計かも。
class SharedViewModel():ViewModel() {

    /****************ユーザー管理*****************/
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userId = MutableStateFlow(firebaseAuth.currentUser?.uid)
    val userId: StateFlow<String?> = _userId

    //サインアップした後ログインする場合はこのフラグをTrueにいれる。
    //最初に追加すべきデータがあるから。
    var isAfterSignUp = MutableStateFlow(false)

    //ログアウトして、もう一度別アカウントでログインをしようとしたときに、フラグを下げないと
    //observeExpenseが最新のユーザーに対応しなくなる。したがって、フラグはこっちで管理
    var addObserveExpensesDoneFlag=false
}