package gaku.original.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    fun setUserId(id:String?){
        _userId.value= id
    }

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit={}){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SharedViewModel","signIn")
                    callback(true)
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("performLogin", "$errorMessage")
                    callback(false)
                }
            }
    }

    fun signUp(email: String, password: String, callback:(Boolean)->Unit = {}) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("performSignUp","Created a user with Email:$email")
                    callback(true)
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("performSignUp", "$errorMessage")
                    callback(false)
                }
            }
    }

    fun signOut(){
        firebaseAuth.signOut()
        _currentUser.value = null
        _userId.value = null
    }
}