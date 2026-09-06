package gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction

import EmailProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionAction
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class MailboxExtractionUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isGmailConnected: Boolean = false,
    val isWaitingForAuth: Boolean = false,

    val categories: List<Category> = emptyList(),

    val emailTemplateTypeList: List<EmailTemplateUiState<EmailTemplateType>> = listOf(
        EmailTemplateUiState(
            type = EmailTemplateType.RakutenPay(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonKindle(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonItem(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.AmazonSubscribe(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.ShikokuElectricPower(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.Udemy(
                enabled = false
            ),
            isLoading = false
        ),
        EmailTemplateUiState(
            type = EmailTemplateType.RakutenCardETC(
                enabled = false
            ),
            isLoading = false
        )
    ),
)

fun MailboxExtractionUiState.updateEmailTemplate(
    newState: EmailTemplateUiState<EmailTemplateType>
): MailboxExtractionUiState {
    return copy(
        emailTemplateTypeList = emailTemplateTypeList.map { state ->
            if (state.type::class == newState.type::class) {
                newState
            } else {
                state
            }
        }
    )
}

data class EmailTemplateUiState<T : EmailTemplateType>(
    val type: T,
    val isLoading: Boolean = false
)

sealed interface EmailTemplateType {
    val enabled: Boolean
    val emailProvider: EmailProvider/* This is not used yet. */

    fun updateEnabled(enabled: Boolean): EmailTemplateType

    data class RakutenPay(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL
    ) : EmailTemplateType {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
    }

    data class AmazonKindle(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId<AmazonKindle> {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
        override fun updateCategoryId(newCategoryId: String?): AmazonKindle =
            copy(categoryId = newCategoryId)
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
    ) : EmailTemplateType {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
    }

    data class AmazonSubscribe(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL
    ) : EmailTemplateType {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId<ShikokuElectricPower> {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
        override fun updateCategoryId(newCategoryId: String?): ShikokuElectricPower =
            copy(categoryId = newCategoryId)
    }

    data class Udemy(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId<Udemy> {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
        override fun updateCategoryId(newCategoryId: String?): Udemy =
            copy(categoryId = newCategoryId)
    }

    data class RakutenCardETC(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null
    ) : EmailTemplateType, HasCategoryId<RakutenCardETC> {
        override fun updateEnabled(enabled: Boolean): EmailTemplateType = copy(enabled = enabled)
        override fun updateCategoryId(newCategoryId: String?): RakutenCardETC =
            copy(categoryId = newCategoryId)
    }
}

sealed interface MailboxExtractionUiEffect {
    data class OpenUrl(
        val url: String
    ) : MailboxExtractionUiEffect
}

class MailboxExtractionViewModel(
    private val categoryRepository: CategoryRepository,
    private val mailboxExtractionRepository: MailboxExtractionRepository,
    private val emailConnectionRepository: EmailConnectionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MailboxExtractionUiState())
    val uiState: StateFlow<MailboxExtractionUiState> get() = _uiState

    //https://stackoverflow.com/questions/66162586/the-main-difference-between-sharedflow-and-stateflow
    // https://qiita.com/void_takazu/items/64acfdc96170f8df49f0#32-sharedflow%E3%81%AE%E8%BF%BD%E5%8A%A0%E3%81%A8%E3%82%AB%E3%83%97%E3%82%BB%E3%83%AB%E5%8C%96
    private val _eventFlow = MutableSharedFlow<MailboxExtractionUiEffect>()
    val eventFlow = _eventFlow.asSharedFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                MailboxExtractionViewModel(
                    session.categoryRepository,
                    session.mailboxExtractionRepository,
                    session.emailConnectionRepository
                )
            }
        }
    }

    private suspend fun initializeGmailTemplates() {
        val isGmailConnected = emailConnectionRepository.isConnected(EmailProvider.GMAIL)
        _uiState.update {
            it.copy(
                isGmailConnected = isGmailConnected
            )
        }
        if (isGmailConnected) {
            fetchEmailTemplaSettings()
        }
    }

    private suspend fun fetchEmailTemplaSettings() {
        val currentSettings = mailboxExtractionRepository.getAllMailTypeSetting()
        for (setting in currentSettings) {
            _uiState.update {
                _uiState.value.updateEmailTemplate(
                    EmailTemplateUiState(
                        setting
                    )
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                initializeGmailTemplates()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isLoading = false
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

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onSwitchClick(typeState: EmailTemplateUiState<EmailTemplateType>) {
        viewModelScope.launch {
            try {
                val loadingState = typeState.copy(
                    isLoading = true
                )
                _uiState.update {
                    it.updateEmailTemplate(loadingState)
                }

                /* enabled is reversed */
                val newType = typeState.type.updateEnabled(!typeState.type.enabled)
                mailboxExtractionRepository.saveMailTypeSetting(newType)

                _uiState.update {
                    it.updateEmailTemplate(
                        typeState.copy(
                            type = newType,/* already updated */
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.updateEmailTemplate(
                        typeState.copy(
                            isLoading = false
                        )
                    ).copy(
                        message = e.message
                    )
                }
            }
        }
    }

    fun onCategorySelect(typeState: EmailTemplateUiState<EmailTemplateType>, categoryId: String?) {
        viewModelScope.launch {
            try {
                val loadingState = typeState.copy(isLoading = true)
                _uiState.update {
                    it.updateEmailTemplate(loadingState)
                }

                val newType = typeState.type as HasCategoryId<*>
                val newTypeWithCategoryId =
                    newType.updateCategoryId(categoryId) as EmailTemplateType
                mailboxExtractionRepository.saveMailTypeSetting(newTypeWithCategoryId)
                _uiState.update {
                    it.updateEmailTemplate(
                        newState = typeState.copy(
                            type = newTypeWithCategoryId,
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.updateEmailTemplate(
                        typeState.copy(
                            isLoading = false
                        )
                    ).copy(
                        message = e.message
                    )
                }
            }
        }
    }

    /* I want to abstract this process in case that the user can connect to outlook or other mail services. */
    /* I have no idea how to do that, so I just only implement for Gmail. */
    fun onGmailConnectClick() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                when (val action = emailConnectionRepository.connect(EmailProvider.GMAIL)) {
                    is EmailConnectionAction.Connected -> {
                        fetchEmailTemplaSettings()
                        _uiState.update {
                            it.copy(
                                message = "Gmail Connected!",
                                isGmailConnected = true,
                                isLoading = false
                            )
                        }
                    }

                    /* With using DeepLink, I might be able to get the result of the user operation after opening the url. */
                    is EmailConnectionAction.OpenUrl -> {
                        val url = action.url
                        _uiState.update {
                            it.copy(
                                isWaitingForAuth = true,
                                isLoading = false
                            )
                        }
                        _eventFlow.emit(
                            MailboxExtractionUiEffect.OpenUrl(url)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /* When launches oauth url and goes back to this app, this app needs to check if token is saved.*/
    fun onResume() {
        Timber.d("Triggered OnResume. ${_uiState.value.isWaitingForAuth}")
        if (!_uiState.value.isWaitingForAuth) return

        Timber.d("Checking if token is saved. OnResume")
        viewModelScope.launch {
            initializeGmailTemplates()
            _uiState.update {
                it.copy(
                    isWaitingForAuth = false
                )
            }
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