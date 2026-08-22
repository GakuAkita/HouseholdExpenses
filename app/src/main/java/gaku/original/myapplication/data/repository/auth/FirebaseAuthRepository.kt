package gaku.original.myapplication.data.repository.auth

import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import gaku.original.myapplication.ui.screens.start.signin.GoogleCredentialProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {


    override val user: AppUser?
        get() = firebaseAuth.currentUser?.toAppUser()
    private var _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Loading)

    override val authState: StateFlow<AuthState>
        get() = _authState

    init {
        firebaseAuth.addAuthStateListener { instance ->
            _authState.update {
                if (instance.currentUser == null) {
                    AuthState.LoggedOut
                }else{
                    AuthState.LoggedIn(
                        instance.currentUser!!.toAppUser()
                    )
                }
            }
        }
    }

    override suspend fun signIn(request: SignInRequest): AppUser {
        val user = when(request){
            is SignInRequest.Email->{
                firebaseAuth.signInWithEmailAndPassword(
                    request.email,
                    request.password
                ).await().user
            }

            is SignInRequest.Google->{
                throw Exception("Bug: Google Sign In requires Activity-based Context")
            }
        }

        return user!!.toAppUser()
    }

    override suspend fun signUp(request: SignUpRequest): AppUser {
        val user = when(request){
            is SignUpRequest.Email->{
                firebaseAuth.createUserWithEmailAndPassword(
                    request.email,
                    request.password
                ).await().user
            }
        }

        return user!!.toAppUser()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

fun FirebaseUser.toAppUser():AppUser{
    return AppUser(
        id = this.uid,
        email = this.email
    )
}

//class FirebaseAuthRepository @Inject constructor(
//    private val firebaseAuth: FirebaseAuth
//) {
//
//    suspend fun getIdToken(
//        timeout: Long = 3000
//    ): FuncResultWithData<String> {
//        val user = firebaseAuth.currentUser
//        if (user == null) {
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "User is null"
//            )
//            return result
//        }
//
//        return try {
//            val token = withTimeout(timeout) {
//                val idTokenResult = user.getIdToken(true).await()
//                val token = idTokenResult.token
//                if (token.isNullOrEmpty()) {
//                    throw Exception("ID Token is null or empty")
//                }
//                token
//            }
//            val result = FuncResultWithData.Success(
//                data = token
//            )
//            result
//        } catch (e: TimeoutCancellationException) {
//            FuncResultWithData.Failure.Timeout()
//        } catch (e: Exception) {
//            val result = FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = e.message ?: "Unknown error"
//            )
//            result
//        }
//    }
//
//}