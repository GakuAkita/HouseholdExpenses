package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider
import kotlinx.coroutines.delay

class FakeEmailConnectionRepository: EmailConnectionRepository {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        return false
    }

    override suspend fun connect(provider: EmailProvider): EmailConnectionAction {
        delay(2000)
        return EmailConnectionAction.Connected
    }
}