package gaku.original.myapplication.data.repository.auth

import AuthRepository
import SignInRequest
import SignUpRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.domain.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
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
                        AppUser(
                            id = instance.currentUser!!.uid,
                            email = instance.currentUser!!.email
                        )
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
                val credential = GoogleAuthProvider.getCredential(request.idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                firebaseAuth.currentUser
            }
        }

        return AppUser(
            id = user!!.uid,
            email = user.email
        )
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

        return AppUser(
            id = user!!.uid,
            email = user.email
        )
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
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