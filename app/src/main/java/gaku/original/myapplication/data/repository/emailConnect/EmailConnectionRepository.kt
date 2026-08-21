package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider

/* not login.  */
interface EmailConnectionRepository {
    suspend fun isConnected(provider:EmailProvider):Boolean

    suspend fun connect(provider:EmailProvider)
}