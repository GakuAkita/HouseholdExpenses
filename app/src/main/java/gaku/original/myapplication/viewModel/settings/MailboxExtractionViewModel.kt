package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.data.dataClass.MailboxExtractionLastExec
import gaku.original.myapplication.repository.FirebaseAuthRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.useCase.CategoryUseCase
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailTemplateSettingState(
    val type: EmailTemplateType,/* これは変えない */
    val setting: EmailTemplateType?,/* これが実際の値 */
    var status: FuncStatusInfo = FuncStatusInfo(
        FuncStatus.SUCCESS,
        "Not loaded yet"
    )
)


@HiltViewModel
class MailboxExtractionViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository,
    private val categoryUseCase: CategoryUseCase
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private var initialized = false

    private val _isGmailTokenExist = MutableStateFlow(false)
    val isGmailTokenExist: StateFlow<Boolean> get() = _isGmailTokenExist

    private val _lastExecMap = MutableStateFlow<Map<String, MailboxExtractionLastExec>>(emptyMap())
    val lastExecMap: StateFlow<Map<String, MailboxExtractionLastExec>> get() = _lastExecMap

    /**
     * GmailのOAuth関連
     */
    private fun generateOAuthUrl(idToken: String): String {
        val baseUrl = "https://accounts.google.com/o/oauth2/v2/auth"
        val params = listOf(
            "client_id=${BuildConfig.WEB_CLIENT_ID}",
            "redirect_uri=${BuildConfig.REDIRECT_URI}",
            "response_type=code",
            "scope=email https://www.googleapis.com/auth/gmail.readonly",
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
    fun getOAuthUrl(callback: (FuncStatusInfo, String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            val idTokenResult = firebaseAuthRepository.getIdToken()
            if (idTokenResult !is FuncResultWithData.Success) {
                _loading.value = false
                callback(idTokenResult.toFuncStatusInfo(), "")
                return@launch
            }

            val token: String = idTokenResult.data
            val oauthUrl = generateOAuthUrl(token)

            val status = FuncStatusInfo(
                FuncStatus.SUCCESS,
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
            status = FuncStatusInfo(
                FuncStatus.SUCCESS,
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
                status = FuncStatusInfo(
                    FuncStatus.SUCCESS,
                    "Not loaded yet"
                )
            )
        )
    val shikokuElectricPowerSettingState: StateFlow<EmailTemplateSettingState> get() = _shikokuElectricPowerSettingState

    private val _amazonKindleSettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.AmazonKindle(),
            setting = null,
            status = FuncStatusInfo(
                FuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val amazonKindleSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonKindleSettingState

    private val _amazonItemSettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.AmazonItem(),
            setting = null,
            status = FuncStatusInfo(
                FuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val amazonItemSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonItemSettingState

    private val _udemySettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.Udemy(),
            setting = null,
            status = FuncStatusInfo(
                FuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val udemySettingState: StateFlow<EmailTemplateSettingState> get() = _udemySettingState

    private val _rakutenCardETCSettingState = MutableStateFlow(
        EmailTemplateSettingState(
            type = EmailTemplateType.RakutenCardETC(),
            setting = null,
            status = FuncStatusInfo(
                FuncStatus.SUCCESS,
                "Not loaded yet"
            )
        )
    )
    val rakutenCardETCSettingState: StateFlow<EmailTemplateSettingState> get() = _rakutenCardETCSettingState

    /**
     * 手打ちはやりたくない、、、
     */
    init {
        LogAkitaDebug("$className: Initializing MailboxExtractionViewModel")
    }

    /**
     * ここに追加しておかないと、
     * ロードもされないし、アップロードもされない
     */
    private val allEmailTemplateStateFlowsList: List<MutableStateFlow<EmailTemplateSettingState>> =
        listOf(
            _rakutenPaySettingState,
            _shikokuElectricPowerSettingState,
            _amazonKindleSettingState,
            _amazonItemSettingState,
            _udemySettingState,
            _rakutenCardETCSettingState
        )

    /**
     * 最初にすべての設定をリモートから取ってくる
     * 初回にしかやらない、、初回で失敗した場合はnullに入るから
     */

    private var activeLoadingCount = 0
    fun startInit() {
        if (initialized) {
            LogAkitaDebug("$className: Already initialized, skipping re-initialization.")
            return
        }
        initialized = true
        _loading.value = true

        fun onFinishOne() {
            activeLoadingCount--
            LogAkitaDebug("$className: One loading task finished, remaining: $activeLoadingCount")
            if (activeLoadingCount <= 0) {
                _loading.value = false
                activeLoadingCount = 0
            }
        }

        activeLoadingCount++
        fetchAllCategories(
            callback = {
                onFinishOne()
            }
        )

        activeLoadingCount++
        loadAllEmailTemplateTypeSetting(
            callback = {
                onFinishOne()
            }
        )

        activeLoadingCount++
        loadAllMailTypeLastExec(
            callback = {
                onFinishOne()
            }
        )

        activeLoadingCount++
        loadIsGmailTokenExistWithLocalUpdate(
            callback = {
                onFinishOne()
            }
        )
    }

    private fun loadAllEmailTemplateTypeSetting(callback: (FuncStatusInfo) -> Unit = {}) {
        var count = 0
        _loading.value = true
        viewModelScope.launch {
            for (settingState in allEmailTemplateStateFlowsList) {
                val fetchResult =
                    mailboxExtractionRepository.getMailTypeSetting(settingState.value.type)
                if (fetchResult is FuncResultWithData.Success) {
                    if (fetchResult.isEmpty) {
                        /* まだ未設定。明示的にデフォルト値をいれる */
                        settingState.value = settingState.value.copy(
                            setting = settingState.value.type.defaultInstance(),
                            status = FuncStatusInfo(
                                FuncStatus.SUCCESS,
                                "Default value set for ${settingState.value.type.menuName}"
                            )
                        )
                    } else {
                        settingState.value = settingState.value.copy(
                            setting = fetchResult.data,
                            status = FuncStatusInfo(
                                FuncStatus.SUCCESS,
                                "Loaded ${settingState.value.type.menuName} setting successfully."
                            )
                        )
                    }
                    count++
                } else {
                    /* ステータスだけ取得しておく */
                    settingState.value = settingState.value.copy(
                        status = fetchResult.toFuncStatusInfo()
                    )
                }
            }

            var statusInfo = FuncStatusInfo(
                FuncStatus.SUCCESS,
                "All email template settings loaded successfully."
            )
            if (count == allEmailTemplateStateFlowsList.size) {
                // 全ての設定が正常にロードされた場合の処理
                LogAkitaDebug("$className: All email template settings loaded successfully.")
            } else {
                // 何かしらの設定がロードできなかった場合の処理
                LogAkitaDebug("$className: Some email template settings failed to load.")
                statusInfo = FuncStatusInfo(
                    FuncStatus.FAILED,
                    "Some email template settings failed to load."
                )
            }
            callback(statusInfo)
        }
    }

    suspend fun updateEmailTemplateSetting(
        settingState: EmailTemplateSettingState
    ): FuncStatusInfo {
        if (settingState.setting == null) {
            /* そもそもnullだったらこの関数が実行されないようにUIになっているはずだが、、 */
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "Email template setting is null, cannot update."
            )
            return statusInfo
        }

        return mailboxExtractionRepository.updateMailTypeSetting(settingState.setting)

    }

    fun updateEmailTemplateSettingWithLocalUpdate(
        settingState: EmailTemplateSettingState,
        callback: (FuncStatusInfo) -> Unit = {}
    ) {
        var index: Int = -1
        for (i in allEmailTemplateStateFlowsList.indices) {
            if (allEmailTemplateStateFlowsList[i].value.type == settingState.type) {
                index = i
                break
            }
        }
        if (index == -1) {
            /* まあここに来ることはほぼないが、、 */
            callback(
                FuncStatusInfo(
                    FuncStatus.FAILED,
                    "Email template type not found in state flows."
                )
            )
            return
        }

        viewModelScope.launch {
            val statusInfo = updateEmailTemplateSetting(settingState)
            if (statusInfo.status != FuncStatus.SUCCESS) {
                /* 失敗したときは、ステータスだけ更新しておく */
                allEmailTemplateStateFlowsList[index].value =
                    allEmailTemplateStateFlowsList[index].value.copy(
                        status = statusInfo
                    )
            } else {
                allEmailTemplateStateFlowsList[index].value =
                    allEmailTemplateStateFlowsList[index].value.copy(
                        setting = settingState.setting,
                        status = statusInfo
                    )
            }
            callback(statusInfo)
        }
    }

    /* ----------------------カテゴリーの扱い--------------------------- */
    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> get() = _allCategories

    private suspend fun fetchAllCategoriesWithLocalUpdate(): FuncResultWithData<List<Category>> {
        val fetchResult = categoryUseCase.fetchAllCategories()
        if (fetchResult is FuncResultWithData.Success) {
            _allCategories.value = fetchResult.data
        }
        return fetchResult
    }

    fun fetchAllCategories(callback: (FuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val fetchResult = fetchAllCategoriesWithLocalUpdate()
            callback(fetchResult.toFuncStatusInfo())
        }
    }

    /******************* メール抽出の実行状況 **********************/
    suspend fun getIsGmailTokenExist(): FuncResultWithData<Boolean> {
        return mailboxExtractionRepository.getIsGmailTokenExist()
    }

    fun loadIsGmailTokenExistWithLocalUpdate(
        callback: (FuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val fetchResult = getIsGmailTokenExist()
            if (fetchResult is FuncResultWithData.Success) {
                _isGmailTokenExist.value = fetchResult.data
                callback(
                    FuncStatusInfo(
                        FuncStatus.SUCCESS,
                        "Gmail token status loaded successfully."
                    )
                )
            } else {
                callback(fetchResult.toFuncStatusInfo())
            }
        }
    }

    suspend fun getMailTypeLastExec(
        type: EmailTemplateType
    ): FuncResultWithData<MailboxExtractionLastExec> {
        return mailboxExtractionRepository.getMailTypeLastExec(type)
    }

    suspend fun getMailTypeLastExecWithLocalUpdate(
        type: EmailTemplateType
    ): FuncResultWithData<MailboxExtractionLastExec> {
        val fetchResult = getMailTypeLastExec(type)
        if (fetchResult is FuncResultWithData.Success) {
            val lastExec = fetchResult.data
            val updatedMap = _lastExecMap.value.toMutableMap()
            updatedMap[type.nodeName] = lastExec
            _lastExecMap.value = updatedMap
        }
        return fetchResult
    }

    fun loadAllMailTypeLastExec(
        callback: (FuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            var failedList = emptyList<String>()
            for (typeState in allEmailTemplateStateFlowsList) {
                val result = getMailTypeLastExecWithLocalUpdate(typeState.value.type)
                if (result !is FuncResultWithData.Success) {
                    failedList = failedList + typeState.value.type.menuName
                }
            }
            if (failedList.isEmpty()) {
                callback(
                    FuncStatusInfo(
                        FuncStatus.SUCCESS,
                        "All last execution times loaded successfully."
                    )
                )
            } else {
                callback(
                    FuncStatusInfo(
                        FuncStatus.FAILED,
                        "Failed to load last execution for: ${failedList.joinToString(", ")}"
                    )
                )
            }
        }
    }
}