package gaku.original.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignUpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel:ViewModel() {

    /****************ユーザー管理*****************/
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userId = MutableStateFlow(firebaseAuth.currentUser?.uid)
    val userId: StateFlow<String?> = _userId

    val isSignedIn:StateFlow<Boolean> = MutableStateFlow(firebaseAuth.currentUser!=null)

    //サインアップした後ログインする場合はこのフラグをTrueにいれる。
    //最初に追加すべきデータがあるから。
    var isAfterSignUp = MutableStateFlow(false)

    //ログアウトして、もう一度別アカウントでログインをしようとしたときに、フラグを下げないと
    //observeExpenseが最新のユーザーに対応しなくなる。したがって、フラグはこっちで管理
    var addObserveExpensesDoneFlag=false

    fun setUserId(id:String?){
        _userId.value= id
    }

    fun signIn(email: String, password: String, callback: (SignInStatus) -> Unit={}){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if(user!=null){
                        //明示的に入れておく。uidがnullになる可能性はかなり低いが。
                        _currentUser.value = user
                        _userId.value = user.uid
                        Log.d("SharedViewModel","signIn successful")
                        callback(SignInStatus.SUCCESS)
                    }else{
                        Log.d("SharedViewModel", "signIn successful but currentUser is null")
                        callback(SignInStatus.USER_ID_NULL)
                    }
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("SharedViewModel", "signIn failed:$errorMessage")
                    callback(SignInStatus.SIGN_IN_FAILED)
                }
            }
    }

    fun signUp(email: String, password: String, callback:(SignUpStatus)->Unit = {}) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid
                    if(userId!=null){
                        isAfterSignUp.value =true
                        Log.d("SharedViewModel","Created a user with Email:$email")
                        callback(SignUpStatus.SUCCESS)
                    }else{
                        Log.d("SharedViewModel","Signed Up successful but userId is null")
                        callback(SignUpStatus.USER_ID_NULL)
                    }
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("SharedViewModel", "signUp failed:$errorMessage")
                    callback(SignUpStatus.SIGN_UP_FAILED)
                }
            }
    }

    fun signOut(){
        firebaseAuth.signOut()
        _currentUser.value = null
        _userId.value = null
        addObserveExpensesDoneFlag=false
    }

    /****************デバイス管理*****************/
    private val _deviceId = MutableLiveData<String>()
    val deviceId: LiveData<String> get() = _deviceId

    //アプリ起動時に一回だけセットする
    fun setDeviceId(id:String){
        if(_deviceId.value == null){
            _deviceId.value = id
        }
    }
}