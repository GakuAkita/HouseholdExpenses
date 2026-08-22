package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider

/* not login.  */
interface EmailConnectionRepository {
    suspend fun isConnected(provider:EmailProvider):Boolean

    suspend fun connect(provider:EmailProvider): EmailConnectionAction
}


/* Return what UI needs to do. */
sealed interface EmailConnectionAction{
    data object Connected: EmailConnectionAction

    data class OpenUrl(
        val url:String
    ): EmailConnectionAction

     //data object StartMicrosoftAuth:EmailConnectionAction
}