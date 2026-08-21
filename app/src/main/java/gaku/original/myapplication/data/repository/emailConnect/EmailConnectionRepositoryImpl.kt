package gaku.original.myapplication.data.repository.emailConnect

import EmailProvider


class EmailConnectionRepositoryImpl: EmailConnectionRepository {
    override suspend fun isConnected(provider: EmailProvider): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun connect(provider: EmailProvider) {
        TODO("Not yet implemented")
    }

}