package gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction

import EmailProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.repository.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.useCase.CategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MailboxExtractionUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isGmailConnected: Boolean = false,

    val categories: List<Category> = emptyList(),

    val rakutenPay: EmailTemplateUiState<EmailTemplateType.RakutenPay> =
        EmailTemplateUiState(
            type = EmailTemplateType.RakutenPay(
                enabled = false
            ),
            isLoading = false
        ),
    val amazonKindle: EmailTemplateUiState<EmailTemplateType.AmazonKindle> =
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonKindle(enabled = false),
            isLoading = false
        ),
    val amazonItem: EmailTemplateUiState<EmailTemplateType.AmazonItem> =
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonItem(enabled = false),
            isLoading = false
        ),
    val amazonSubscribe: EmailTemplateUiState<EmailTemplateType.AmazonSubscribe> =
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonSubscribe(enabled = false),
            isLoading = false
        ),
    val shikokuElectricPower: EmailTemplateUiState<EmailTemplateType.ShikokuElectricPower> =
        EmailTemplateUiState(
            type = EmailTemplateType.ShikokuElectricPower(
                enabled = false
            ),
            isLoading = false
        ),
    val udemy: EmailTemplateUiState<EmailTemplateType.Udemy> =
        EmailTemplateUiState(
            type = EmailTemplateType.Udemy(enabled = false),
            isLoading = false
        ),
    val rakutenCardETC: EmailTemplateUiState<EmailTemplateType.RakutenCardETC> =
        EmailTemplateUiState(
            type = EmailTemplateType.RakutenCardETC(
                enabled = false
            ),
            isLoading = false
        )
)

data class EmailTemplateUiState<T : EmailTemplateType>(
    val type: T,
    val isLoading: Boolean = false
)

sealed interface EmailTemplateType {
    val enabled: Boolean
    val emailProvider: EmailProvider/* This is not used yet. */

    data class RakutenPay(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL
    ) : EmailTemplateType

    data class AmazonKindle(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId

    data class AmazonItem(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
    ) : EmailTemplateType

    data class AmazonSubscribe(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL
    ) : EmailTemplateType

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId

    data class Udemy(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId

    data class RakutenCardETC(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId
}

fun MailboxExtractionUiState.updateType(
    type: EmailTemplateType,
    isLoading: Boolean? = null
): MailboxExtractionUiState {

    /* if isLoading is null, use as it is */
    val loadingState = if (isLoading == null) this.isLoading else isLoading

    return when (type) {
        is EmailTemplateType.RakutenPay -> this.copy(
            rakutenPay = rakutenPay.copy(type = type),
            isLoading = loadingState
        )

        is EmailTemplateType.AmazonKindle -> this.copy(
            amazonKindle = amazonKindle.copy(
                type = type,
                isLoading = loadingState
            )
        )

        is EmailTemplateType.AmazonItem -> this.copy(
            amazonItem = amazonItem.copy(
                type = type,
                isLoading = loadingState
            )
        )

        is EmailTemplateType.AmazonSubscribe -> this.copy(
            amazonSubscribe = amazonSubscribe.copy(
                type = type,
                isLoading = loadingState
            )
        )

        is EmailTemplateType.ShikokuElectricPower -> this.copy(
            shikokuElectricPower = shikokuElectricPower.copy(
                type = type,
                isLoading = loadingState
            )
        )

        is EmailTemplateType.Udemy -> this.copy(
            udemy = udemy.copy(
                type = type,
                isLoading = loadingState
            )
        )

        is EmailTemplateType.RakutenCardETC -> this.copy(
            rakutenCardETC = rakutenCardETC.copy(
                type = type,
                isLoading = loadingState
            )
        )
    }
}

class MailboxExtractionViewModel(
    private val categoryRepository: CategoryRepository,
    private val mailboxExtractionRepository: MailboxExtractionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MailboxExtractionUiState())
    val uiState: StateFlow<MailboxExtractionUiState> get() = _uiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                MailboxExtractionViewModel(
                    session.categoryRepository,
                    session.mailboxExtractionRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            val isGmailConnected = mailboxExtractionRepository.getIsGmailToken()
            _uiState.update {
                it.copy(
                    isGmailConnected = isGmailConnected
                )
            }
            if (isGmailConnected) {
                val currentSettings = mailboxExtractionRepository.getAllMailTypeSetting()
                for (setting in currentSettings) {
                    _uiState.value = _uiState.value.updateType(
                        setting
                    )
                }
            }
        }

        viewModelScope.launch {
            val categories = categoryRepository.getAllCategories()
            _uiState.update {
                it.copy(
                    categories = categories.values.toList()
                )
            }
        }
    }

    fun onEnableClick(typeState: EmailTemplateType) {
        try {

        } catch (e: Exception) {

        } finally {

        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}


data class EmailTemplateSettingState(
    val type: EmailTemplateType,/* これは変えない */
    val setting: EmailTemplateType?,/* これが実際の値 */
    var status: FuncStatusInfo = FuncStatusInfo(
        FuncStatus.SUCCESS,
        "Not loaded yet"
    )
)


@HiltViewModel
class _MailboxExtractionViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository,
    private val categoryUseCase: CategoryUseCase
) : ViewModel() {
//    val className: String = this::class.simpleName ?: "UnableToGetClassName"
//
//    private val _loading = MutableStateFlow(false)
//    val loading: StateFlow<Boolean> get() = _loading
//
//    private var initialized = false
//
//    private val _isGmailTokenExist = MutableStateFlow(false)
//    val isGmailTokenExist: StateFlow<Boolean> get() = _isGmailTokenExist
//
////    TODO("private val _lastExecMap = MutableStateFlow<Map<String, MailboxExtractionLastExec>>(emptyMap())")
////    TODO("val lastExecMap: StateFlow<Map<String, MailboxExtractionLastExec>> get() = _lastExecMap")
//
//    /**
//     * GmailのOAuth関連
//     */
//    private fun generateOAuthUrl(idToken: String): String {
//        val baseUrl = "https://accounts.google.com/o/oauth2/v2/auth"
//        val params = listOf(
//            "client_id=${BuildConfig.WEB_CLIENT_ID}",
//            "redirect_uri=${BuildConfig.REDIRECT_URI}",
//            "response_type=code",
//            "scope=email https://www.googleapis.com/auth/gmail.readonly",
//            "access_type=offline",
//            "prompt=consent",
//            "state=$idToken"
//        ).joinToString("&")
//        // OAuthのURLを生成するロジックを実装
//        // ここでは仮のURLを返す
//        return "$baseUrl?$params"
//    }
//
//    /**
//     * callback内で生成したOAuth URLを受け取り、WebViewやブラウザで開く
//     */
//    fun getOAuthUrl(callback: (FuncStatusInfo, String) -> Unit) {
//        _loading.value = true
//        viewModelScope.launch {
//            val idTokenResult = firebaseAuthRepository.getIdToken()
//            if (idTokenResult !is FuncResultWithData.Success) {
//                _loading.value = false
//                callback(idTokenResult.toFuncStatusInfo(), "")
//                return@launch
//            }
//
//            val token: String = idTokenResult.data
//            val oauthUrl = generateOAuthUrl(token)
//
//            val status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "OAuth URL generated successfully"
//            )
//            _loading.value = false
//            callback(status, oauthUrl)
//        }
//    }
//
//    /* 全部のメール設定の情報を取ってくる */
//    /* 取得に失敗したらnullにいれる */
//    /* StateFlowで全部持っておく */
//    private val _rakutenPaySettingState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.RakutenPay(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val rakutenPaySettingState: StateFlow<EmailTemplateSettingState> get() = _rakutenPaySettingState
//
//    private val _shikokuElectricPowerSettingState =
//        MutableStateFlow(
//            EmailTemplateSettingState(
//                type = EmailTemplateType.ShikokuElectricPower(),
//                setting = null,
//                status = FuncStatusInfo(
//                    FuncStatus.SUCCESS,
//                    "Not loaded yet"
//                )
//            )
//        )
//    val shikokuElectricPowerSettingState: StateFlow<EmailTemplateSettingState> get() = _shikokuElectricPowerSettingState
//
//    private val _amazonKindleSettingState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.AmazonKindle(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val amazonKindleSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonKindleSettingState
//
//    private val _amazonItemSettingState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.AmazonItem(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val amazonItemSettingState: StateFlow<EmailTemplateSettingState> get() = _amazonItemSettingState
//
//    private val _amazonSubscribeState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.AmazonSubscribe(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val amazonSubscribeState: StateFlow<EmailTemplateSettingState> = _amazonSubscribeState
//
//    private val _udemySettingState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.Udemy(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val udemySettingState: StateFlow<EmailTemplateSettingState> get() = _udemySettingState
//
//    private val _rakutenCardETCSettingState = MutableStateFlow(
//        EmailTemplateSettingState(
//            type = EmailTemplateType.RakutenCardETC(),
//            setting = null,
//            status = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "Not loaded yet"
//            )
//        )
//    )
//    val rakutenCardETCSettingState: StateFlow<EmailTemplateSettingState> get() = _rakutenCardETCSettingState
//
//    /**
//     * 手打ちはやりたくない、、、
//     */
//    init {
//        LogAkitaDebug("$className: Initializing MailboxExtractionViewModel")
//    }
//
//    /**
//     * ここに追加しておかないと、
//     * ロードもされないし、アップロードもされない
//     */
//    private val allEmailTemplateStateFlowsList: List<MutableStateFlow<EmailTemplateSettingState>> =
//        listOf(
//            _rakutenPaySettingState,
//            _shikokuElectricPowerSettingState,
//            _amazonKindleSettingState,
//            _amazonItemSettingState,
//            _amazonSubscribeState,
//            _udemySettingState,
//            _rakutenCardETCSettingState
//        )
//
//    /**
//     * 最初にすべての設定をリモートから取ってくる
//     * 初回にしかやらない、、初回で失敗した場合はnullに入るから
//     */
//
//    private var activeLoadingCount = 0
//    fun startInit() {
//        if (initialized) {
//            LogAkitaDebug("$className: Already initialized, skipping re-initialization.")
//            return
//        }
//        initialized = true
//        _loading.value = true
//
//        fun onFinishOne() {
//            activeLoadingCount--
//            LogAkitaDebug("$className: One loading task finished, remaining: $activeLoadingCount")
//            if (activeLoadingCount <= 0) {
//                _loading.value = false
//                activeLoadingCount = 0
//            }
//        }
//
//        activeLoadingCount++
////        fetchAllCategories(
////            callback = {
////                onFinishOne()
////            }
////        )
//
//        activeLoadingCount++
//        loadAllEmailTemplateTypeSetting(
//            callback = {
//                onFinishOne()
//            }
//        )
//
//        activeLoadingCount++
//        loadAllMailTypeLastExec(
//            callback = {
//                onFinishOne()
//            }
//        )
//
//        activeLoadingCount++
//        loadIsGmailTokenExistWithLocalUpdate(
//            callback = {
//                onFinishOne()
//            }
//        )
//    }
//
//    private fun loadAllEmailTemplateTypeSetting(callback: (FuncStatusInfo) -> Unit = {}) {
//        var count = 0
//        _loading.value = true
//        viewModelScope.launch {
//            for (settingState in allEmailTemplateStateFlowsList) {
//                val fetchResult =
//                    mailboxExtractionRepository.getMailTypeSetting(settingState.value.type)
//                if (fetchResult is FuncResultWithData.Success) {
//                    if (fetchResult.isEmpty) {
//                        /* まだ未設定。明示的にデフォルト値をいれる */
//                        settingState.value = settingState.value.copy(
//                            setting = settingState.value.type.defaultInstance(),
//                            status = FuncStatusInfo(
//                                FuncStatus.SUCCESS,
//                                "Default value set for ${settingState.value.type.menuName}"
//                            )
//                        )
//                    } else {
//                        settingState.value = settingState.value.copy(
//                            setting = fetchResult.data,
//                            status = FuncStatusInfo(
//                                FuncStatus.SUCCESS,
//                                "Loaded ${settingState.value.type.menuName} setting successfully."
//                            )
//                        )
//                    }
//                    count++
//                } else {
//                    /* ステータスだけ取得しておく */
//                    settingState.value = settingState.value.copy(
//                        status = fetchResult.toFuncStatusInfo()
//                    )
//                }
//            }
//
//            var statusInfo = FuncStatusInfo(
//                FuncStatus.SUCCESS,
//                "All email template settings loaded successfully."
//            )
//            if (count == allEmailTemplateStateFlowsList.size) {
//                // 全ての設定が正常にロードされた場合の処理
//                LogAkitaDebug("$className: All email template settings loaded successfully.")
//            } else {
//                // 何かしらの設定がロードできなかった場合の処理
//                LogAkitaDebug("$className: Some email template settings failed to load.")
//                statusInfo = FuncStatusInfo(
//                    FuncStatus.FAILED,
//                    "Some email template settings failed to load."
//                )
//            }
//            callback(statusInfo)
//        }
//    }
//
//    suspend fun updateEmailTemplateSetting(
//        settingState: EmailTemplateSettingState
//    ): FuncStatusInfo {
//        val setting = settingState.setting
//        if (settingState.setting == null) {
//            /* そもそもnullだったらこの関数が実行されないようにUIになっているはずだが、、 */
//            val statusInfo = FuncStatusInfo(
//                FuncStatus.FAILED,
//                "Email template setting is null, cannot update."
//            )
//            return statusInfo
//        }
//
//        return if (setting is HasCategoryId && setting.categoryId == null) {
//            /* forceをtrueにすると、まるごと設定をsetする */
//            mailboxExtractionRepository.updateMailTypeSetting(settingState.setting, force = true)
//        } else {
//            mailboxExtractionRepository.updateMailTypeSetting(settingState.setting)
//        }
//
//    }
//
//    fun updateEmailTemplateSettingWithLocalUpdate(
//        settingState: EmailTemplateSettingState,
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        var index: Int = -1
//        for (i in allEmailTemplateStateFlowsList.indices) {
//            if (allEmailTemplateStateFlowsList[i].value.type == settingState.type) {
//                index = i
//                break
//            }
//        }
//        if (index == -1) {
//            /* まあここに来ることはほぼないが、、 */
//            callback(
//                FuncStatusInfo(
//                    FuncStatus.FAILED,
//                    "Email template type not found in state flows."
//                )
//            )
//            return
//        }
//
//        viewModelScope.launch {
//            val statusInfo = updateEmailTemplateSetting(settingState)
//            if (statusInfo.status != FuncStatus.SUCCESS) {
//                /* 失敗したときは、ステータスだけ更新しておく */
//                allEmailTemplateStateFlowsList[index].value =
//                    allEmailTemplateStateFlowsList[index].value.copy(
//                        status = statusInfo
//                    )
//            } else {
//                allEmailTemplateStateFlowsList[index].value =
//                    allEmailTemplateStateFlowsList[index].value.copy(
//                        setting = settingState.setting,
//                        status = statusInfo
//                    )
//            }
//            LogAkitaDebug("$className: Updated email template setting for ${settingState.type.menuName} with status: ${statusInfo.status}")
//            callback(statusInfo)
//        }
//    }
//
//    /* ----------------------カテゴリーの扱い--------------------------- */
//    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
//    val allCategories: StateFlow<List<Category>> get() = _allCategories
//
////    private suspend fun fetchAllCategoriesWithLocalUpdate(): FuncResultWithData<List<Category>> {
////        val fetchResult = categoryUseCase.fetchAllCategories()
////        if (fetchResult is FuncResultWithData.Success) {
////            _allCategories.value = fetchResult.data
////        }
////        return fetchResult
////    }
//
////    fun fetchAllCategories(callback: (FuncStatusInfo) -> Unit = {}) {
////        viewModelScope.launch {
////            val fetchResult = fetchAllCategoriesWithLocalUpdate()
////            callback(fetchResult.toFuncStatusInfo())
////        }
////    }
//
//    /******************* メール抽出の実行状況 **********************/
//    suspend fun getIsGmailTokenExist(): FuncResultWithData<Boolean> {
//        return mailboxExtractionRepository.getIsGmailTokenExist()
//    }
//
//    fun loadIsGmailTokenExistWithLocalUpdate(
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        viewModelScope.launch {
//            val fetchResult = getIsGmailTokenExist()
//            if (fetchResult is FuncResultWithData.Success) {
//                _isGmailTokenExist.value = fetchResult.data
//                callback(
//                    FuncStatusInfo(
//                        FuncStatus.SUCCESS,
//                        "Gmail token status loaded successfully."
//                    )
//                )
//            } else {
//                callback(fetchResult.toFuncStatusInfo())
//            }
//        }
//    }
//
//    suspend fun getMailTypeLastExec(
//        type: EmailTemplateType
//    ): FuncResultWithData<MailboxExtractionLastExec> {
//        return mailboxExtractionRepository.getMailTypeLastExec(type)
//    }
//
//    suspend fun getMailTypeLastExecWithLocalUpdate(
//        type: EmailTemplateType
//    ): FuncResultWithData<MailboxExtractionLastExec> {
//        val fetchResult = getMailTypeLastExec(type)
//        if (fetchResult is FuncResultWithData.Success) {
//            val lastExec = fetchResult.data
//            val updatedMap = _lastExecMap.value.toMutableMap()
//            updatedMap[type.nodeName] = lastExec
//            _lastExecMap.value = updatedMap
//        }
//        return fetchResult
//    }
//
//    fun loadAllMailTypeLastExec(
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        viewModelScope.launch {
//            var failedList = emptyList<String>()
//            for (typeState in allEmailTemplateStateFlowsList) {
//                val result = getMailTypeLastExecWithLocalUpdate(typeState.value.type)
//                if (result !is FuncResultWithData.Success) {
//                    failedList = failedList + TODO("typeState.value.type.menuName")
//                }
//            }
//            if (failedList.isEmpty()) {
//                callback(
//                    FuncStatusInfo(
//                        FuncStatus.SUCCESS,
//                        "All last execution times loaded successfully."
//                    )
//                )
//            } else {
//                callback(
//                    FuncStatusInfo(
//                        FuncStatus.FAILED,
//                        "Failed to load last execution for: ${failedList.joinToString(", ")}"
//                    )
//                )
//            }
//        }
//    }
}