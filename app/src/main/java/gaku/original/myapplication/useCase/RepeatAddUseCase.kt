package gaku.original.myapplication.useCase

import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.repository.FirestoreRepository.RepeatAddFirestoreRepository
import javax.inject.Inject

class RepeatAddUseCase @Inject constructor(
    private val repeatAddRepository: RepeatAddFirestoreRepository,
    private val expenseRepository: ExpenseFirestoreRepository
) {
    suspend fun addRepeatAdd(repeatAdd: RepeatAdd): FuncResultWithData<RepeatAdd> {
        return repeatAddRepository.addRepeatAdd(repeatAdd)
    }

    suspend fun fetchAllRepeatADd(): FuncResultWithData<List<RepeatAdd>> {
        return repeatAddRepository.fetchAllRepeatAdd()
    }

    suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): SuspendFuncStatusInfo {
        return repeatAddRepository.updateRepeatAdd(repeatAdd)
    }

    suspend fun removeRepeatAdd(repeatAdd: RepeatAdd): SuspendFuncStatusInfo {
        return repeatAddRepository.removeRepeatAdd(repeatAdd)
    }

    /**
     * RepeatAddをしたあと、月末まで追加する
     */
}