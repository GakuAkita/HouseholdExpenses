package gaku.original.myapplication.viewModel

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignOutStatus
import gaku.original.myapplication.data.SignUpStatus

class AuthManagerViewModel(
    private val userInfoViewModel: UserInfoViewModel = UserInfoViewModel()
) {
    private val authManagerFirebaseAuth:FirebaseAuth
        get() = userInfoViewModel.firebaseAuth

    fun signIn(email: String, password: String, callback: (SignInStatus) -> Unit) {
        authManagerFirebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(userInfoViewModel.userId.value != null) {
                        Log.d("AuthManagerViewModel","Signed in with Email:$email")
                        callback(SignInStatus.SUCCESS)
                    }else{
                        //ここに来ることはまずないが。
                        Log.d("AuthManagerViewModel","SignIn success but userId is null")
                        callback(SignInStatus.USER_ID_NULL)
                    }
                } else {
                    Log.d("AuthManagerViewModel","signIn failed.")
                }
            }
    }

    fun signUp(email: String, password: String, callback: (SignUpStatus) -> Unit) {
        authManagerFirebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthManagerViewModel","Created a user with Email:$email")
                    callback(SignUpStatus.SUCCESS)
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("AuthManagerViewModel", "signUp failed:$errorMessage")
                    callback(SignUpStatus.SIGN_UP_FAILED)
                }
            }
    }

    fun signOut():SignOutStatus{
        try {
            authManagerFirebaseAuth.signOut()
            if(userInfoViewModel.currentUser.value == null){
                Log.d("AuthManagerViewModel","signOut successful")
                return SignOutStatus.SUCCESS
            }else{
                Log.d("AuthManagerViewModel","signOut failed. currentUser is not null\n")
                return SignOutStatus.SIGN_OUT_FAILED
            }
        }catch (e:Exception){
            Log.d("AuthManagerViewModel","signOut failed : ${e.message}")
            return SignOutStatus.SIGN_OUT_FAILED
        }
    }
}