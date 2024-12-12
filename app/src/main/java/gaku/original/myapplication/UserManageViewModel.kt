package gaku.original.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignOutStatus
import gaku.original.myapplication.data.SignUpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserManageViewModel: ViewModel(){
    /****************ユーザー管理*****************/
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userId = MutableStateFlow(firebaseAuth.currentUser?.uid)
    val userId: StateFlow<String?> = _userId

    //サインアップした後ログインする場合はこのフラグをTrueにいれる。
    //最初に追加すべきデータがあるから。
    var isAfterSignUp = MutableStateFlow(false)


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
                        Log.d("UserManageViewModel","signIn successful")
                        callback(SignInStatus.SUCCESS)
                    }else{
                        Log.d("UserManageViewModel", "signIn successful but currentUser is null")
                        callback(SignInStatus.USER_ID_NULL)
                    }
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("UserManageViewModel", "signIn failed:$errorMessage")
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
                        Log.d("UserManageViewModel","Created a user with Email:$email")
                        callback(SignUpStatus.SUCCESS)
                    }else{
                        Log.d("UserManageViewModel","Signed Up successful but userId is null")
                        callback(SignUpStatus.USER_ID_NULL)
                    }
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("UserManageViewModel", "signUp failed:$errorMessage")
                    callback(SignUpStatus.SIGN_UP_FAILED)
                }
            }
    }

    fun signOut():SignOutStatus{
        try {
            firebaseAuth.signOut()
            if(currentUser.value == null){
                Log.d("UserManageViewModel","signOut successful")
                return SignOutStatus.SUCCESS
            }else{
                Log.d("UserManageViewModel","signOut failed. currentUser is not null\n")
                return SignOutStatus.SIGN_OUT_FAILED
            }
        }catch (e:Exception){
            Log.d("UserManageViewModel","signOut failed : ${e.message}")
            return SignOutStatus.SIGN_OUT_FAILED
        }
    }

}