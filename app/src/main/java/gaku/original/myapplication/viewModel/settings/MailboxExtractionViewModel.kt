package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.repository.FirebaseAuthRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailTemplateSettingState(
    val type: EmailTemplateType,/* これは変えない */
    val setting: EmailTemplateType?,/* これが実際の値 */
    var status: SuspendFuncStatusInfo = SuspendFuncStatusInfo(
        SuspendFuncStatus.SUCCESS,
        "Not loaded yet"
    )
)


@HiltViewModel
class MailboxExtractionViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private var initialized = false

    /**
     * GmailのOAuth関連
     */
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
            if (idTokenResult !is FetchResult.Success) {
                _loading.value = false
                callback(idTokenResult.toSuspendFuncStatusInfo(), "")
                return@launch
            }

            val token: String = idTokenResult.data
            val oauthUrl = generateOAuthUrl(token)

            val status = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "OAuth URL generated successfully"
            )
            _loading.value = false
            callback(status, oauthUrl)
        }
    }

    /* 全部のメール設定の情報を取ってくる */
    /* 取得に失敗したらnullにいれる */
    /* StateFlowで全部持っておく */
    private val _rakutenPaySettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.RakutenPay(),
            setting = null,
            status = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val rakutenPaySettingState: StateFlow<EmailTemplateSettingState> get() = _rakutenPaySettingState

    private val _shikokuElectricPowerSettingState =
        MutableStateFlow(
            EmailTemplateSettingState(
                type = EmailTemplateType.ShikokuElectricPower(),
                setting = null,
                status = SuspendFuncStatusInfo(
                    SuspendFuncStatus.SUCCESS,
                    "Not loaded yet"
                )
            )
        )
    val shikokuElectricPowerSettingState: StateFlow<EmailTemplateSettingState> get() = _shikokuElectricPowerSettingState

    private val _amazonKindleSettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.AmazonKindle(),
            setting = null,
            status = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val amazonKindleSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonKindleSettingState

    private val _amazonItemSettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.AmazonItem(),
            setting = null,
            status = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val amazonItemSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonItemSettingState

    /**
     * 手打ちはやりたくない、、、
     */
    init {
        LogAkitaDebug("$className: Initializing MailboxExtractionViewModel")
    }

    private val allEmailTemplateStateFlowsList: List<MutableStateFlow<EmailTemplateSettingState>> =
        listOf(
            _rakutenPaySettingState,
            _shikokuElectricPowerSettingState,
            _amazonKindleSettingState,
            _amazonItemSettingState
        )

    /**
     * 最初にすべての設定をリモートから取ってくる
     * 初回にしかやらない、、初回で失敗した場合はnullに入るから
     */
    fun startInit() {
        if (initialized) {
            LogAkitaDebug("$className: Already initialized, skipping re-initialization.")
            return
        }
        _loading.value = true
        loadAllEmailTemplateTypeSetting(
            callback = { initialized = false }
        )
    }

    private fun loadAllEmailTemplateTypeSetting(callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        var count = 0
        _loading.value = true
        viewModelScope.launch {
            for (settingState in allEmailTemplateStateFlowsList) {
                val fetchResult =
                    mailboxExtractionRepository.getMailTypeSetting(settingState.value.type)
                if (fetchResult is FetchResult.Success) {
                    if (fetchResult.isEmpty) {
                        /* まだ未設定。明示的にデフォルト値をいれる */
                        settingState.value = settingState.value.copy(
                            setting = settingState.value.type.defaultInstance(),
                            status = SuspendFuncStatusInfo(
                                SuspendFuncStatus.SUCCESS,
                                "Default value set for ${settingState.value.type.menuName}"
                            )
                        )
                    } else {
                        settingState.value = settingState.value.copy(
                            setting = fetchResult.data,
                            status = SuspendFuncStatusInfo(
                                SuspendFuncStatus.SUCCESS,
                                "Loaded ${settingState.value.type.menuName} setting successfully."
                            )
                        )
                    }
                    count++
                } else {
                    /* ステータスだけ取得しておく */
                    settingState.value = settingState.value.copy(
                        status = fetchResult.toSuspendFuncStatusInfo()
                    )
                }
            }

            var statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.SUCCESS,
                "All email template settings loaded successfully."
            )
            if (count == allEmailTemplateStateFlowsList.size) {
                // 全ての設定が正常にロードされた場合の処理
                LogAkitaDebug("$className: All email template settings loaded successfully.")
            } else {
                // 何かしらの設定がロードできなかった場合の処理
                LogAkitaDebug("$className: Some email template settings failed to load.")
                statusInfo = SuspendFuncStatusInfo(
                    SuspendFuncStatus.FAILED,
                    "Some email template settings failed to load."
                )
            }
            callback(statusInfo)
            _loading.value = false
        }
    }

    fun updateEmailTemplateSetting(
        settingState: EmailTemplateSettingState,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (settingState.setting == null) {
                /* そもそもnullだったらこの関数が実行されないようにUIになっているはずだが、、 */
                callback(
                    SuspendFuncStatusInfo(
                        SuspendFuncStatus.FAILED,
                        "Email template setting is null, cannot update."
                    )
                )
                return@launch
            }

            val statusInfo = mailboxExtractionRepository.updateMailTypeSetting(settingState.setting)
            callback(statusInfo)
        }
    }

    fun updateEmailTemplateSettingWithLocalUpdate(
        settingState: EmailTemplateSettingState,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            updateEmailTemplateSetting(settingState) {
                if (it.status == SuspendFuncStatus.SUCCESS) {
                    for (state in allEmailTemplateStateFlowsList) {
                        if (state.value.type == settingState.type) {
                            state.value = state.value.copy(
                                setting = settingState.setting,
                                status = it
                            )
                            callback(it)
                        }
                    }
                }
            }
        }
    }

}