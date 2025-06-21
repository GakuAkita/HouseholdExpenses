package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Repository.FirebaseAuthRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GmailLinkingViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private fun generateOAuthUrl(idToken: String): String {
        val baseUrl = "https://accounts.google.com/o/oauth2/v2/auth"
        val params = listOf(
            "client_id=${BuildConfig.WEB_CLIENT_ID}",
            "redirect_uri=${BuildConfig.REDIRECT_URI}",
            "response_type=code",
            "scope=https://www.googleapis.com/auth/gmail.readonly",
            "access_type=offline",
            "prompt=consent",
            "state=$idToken"
        ).joinToString("&")
        // OAuthのURLを生成するロジックを実装
        // ここでは仮のURLを返す
        return "$baseUrl?$params"
    }

    /**
     * callback内で生成したOAuth URLを受け取り、WebViewやブラウザで開く
     */
    fun getOAuthUrl(callback: (SuspendFuncStatusInfo, String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val idTokenResult = firebaseAuthRepository.getIdToken()
            if (idTokenResult.status != SuspendFuncStatus.SUCCESS) {
                _loading.value = false
                callback(idTokenResult.toSuspendFuncStatusInfo(), "")
            }

            val token: String = idTokenResult.data ?: ""
            val oauthUrl = generateOAuthUrl(token)

            val status = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "OAuth URL generated successfully"
            )
            _loading.value = false
            callback(status, oauthUrl)
        }
    }
}