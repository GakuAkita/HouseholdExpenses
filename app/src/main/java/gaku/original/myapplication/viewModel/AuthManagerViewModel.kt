package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Status.SignInResult
import gaku.original.myapplication.data.Status.SingOutResult
import gaku.original.myapplication.data.Status.SingUpResult
import javax.inject.Inject

@HiltViewModel
class AuthManagerViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val expenseSharedViewModel: ExpenseSharedViewModel
): ViewModel() {

    val isSignedIn:Boolean
        get() = firebaseAuth.currentUser != null

    val userId:String?
        get() = firebaseAuth.currentUser?.uid

    fun signIn(email: String, password: String, callback: (SignInResult) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if(userId != null) {
                        Log.d("AuthManagerViewModel","Signed in with Email:$email")
                        //サインイン直後にやらないと行けない作業はここでやる
                        expenseSharedViewModel.fetchAllExpenses(
                            onComplete = {
                                expenseSharedViewModel.addExpenseCategoryChildEventListener()
                            }
                        )
                        expenseSharedViewModel.fetchAllCategories()
                        callback(SignInResult.SUCCESS)
                    }else{
                        Log.d("AuthManagerViewModel","SignIn success but userId is null")
                        callback(SignInResult.USER_ID_NULL)
                    }
                } else {
                    Log.d("AuthManagerViewModel","signIn failed.")
                    callback(SignInResult.SIGN_IN_FAILED)
                }
            }
    }

    fun signUp(email: String, password: String, callback: (SingUpResult) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthManagerViewModel","Created a user with Email:$email")
                    //アカウント作成後に行う処理はここでやる
                    expenseSharedViewModel.addUserInitialData(email)
                    callback(SingUpResult.SUCCESS)
                } else {
                    // エラーハンドリング
                    val errorMessage = task.exception?.message ?: "Unknown error occurred"
                    Log.d("AuthManagerViewModel", "signUp failed:$errorMessage")
                    callback(SingUpResult.SIGN_UP_FAILED)
                }
            }
    }

    fun signOut(): SingOutResult {
        try {
            expenseSharedViewModel.clearExpenseChildEventListener()

            firebaseAuth.signOut()
            if(userId == null){
                Log.d("AuthManagerViewModel","signOut successful")
                return SingOutResult.SUCCESS
            }else{
                //サイン・アウトしてからすぐだとここに来てしまうが、時間経って結局nullになる。
                //したがって、扱いとしてはSUCCESSにする
                Log.d("AuthManagerViewModel","!!Warning!!signOut failed. currentUser is not null\n")
                return SingOutResult.SUCCESS
            }
        }catch (e:Exception){
            Log.d("AuthManagerViewModel","signOut failed : ${e.message}")
            return SingOutResult.SIGN_OUT_FAILED
        }
    }

}