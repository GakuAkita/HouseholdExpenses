package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.SignInResult
import gaku.original.myapplication.data.SingOutResult
import gaku.original.myapplication.data.SingUpResult

class AuthManagerViewModel(
    private val userInfoViewModel: UserInfoViewModel = UserInfoViewModel()
) {

    private val authManagerFirebaseAuth:FirebaseAuth
        get() = userInfoViewModel.firebaseAuth

    fun signIn(email: String, password: String, callback: (SignInResult) -> Unit) {
        authManagerFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(userInfoViewModel.userId.value != null) {
                        Log.d("AuthManagerViewModel","Signed in with Email:$email")
                        callback(SignInResult.SUCCESS)
                    }else{
                        //ここに来ることはまずないが。
                        Log.d("AuthManagerViewModel","SignIn success but userId is null")
                        callback(SignInResult.USER_ID_NULL)
                    }
                } else {
                    Log.d("AuthManagerViewModel","signIn failed.")
                }
            }
    }

    fun signUp(email: String, password: String, callback: (SingUpResult) -> Unit) {
        authManagerFirebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthManagerViewModel","Created a user with Email:$email")
                    callback(SingUpResult.SUCCESS)
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("AuthManagerViewModel", "signUp failed:$errorMessage")
                    callback(SingUpResult.SIGN_UP_FAILED)
                }
            }
    }

    fun signOut():SingOutResult{
        try {
            authManagerFirebaseAuth.signOut()
            if(userInfoViewModel.currentUser.value == null){
                Log.d("AuthManagerViewModel","signOut successful")
                return SingOutResult.SUCCESS
            }else{
                Log.d("AuthManagerViewModel","signOut failed. currentUser is not null\n")
                return SingOutResult.SIGN_OUT_FAILED
            }
        }catch (e:Exception){
            Log.d("AuthManagerViewModel","signOut failed : ${e.message}")
            return SingOutResult.SIGN_OUT_FAILED
        }
    }
}