package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider

class FakeEmailConnectionRepository: EmailConnectionRepository {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        return true
    }

    override suspend fun connect(provider: EmailProvider) {
        return
    }
}