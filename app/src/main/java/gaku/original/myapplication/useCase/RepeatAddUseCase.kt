package gaku.original.myapplication.useCase

import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.dataClass.getDaysInMonthByFrequency
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.utility.concatStringWithBars
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RepeatAddUseCase @Inject constructor(
    //private val repeatAddRepository: RepeatAddFirestoreRepository,
    private val expenseRepository: ExpenseRepository
) {
    //新しいRepeatAddにちゃんと値が入っているかチェックする
    fun checkNewRepeatAddValid(newRepeatAdd: RepeatAdd): String {
//        if (newRepeatAdd.expense.amount == null || newRepeatAdd.expense.amount == 0L) {
//            return "expense amount is empty or 0"
//        } else if (newRepeatAdd.expense.category == null) {
//            return "expense category is empty"
//        } else if (newRepeatAdd.frequencyInfo.frequency == null) {
//            return "frequency is empty"
//        }
//
//        val frequencyInfo = newRepeatAdd.frequencyInfo
//        val frequency = frequencyInfo.frequency
//
//        //各頻度ごとに該当するフィールドのチェックを追加
//        if (frequency == RepeatFrequency.EVERY_YEAR) {
//            if (frequencyInfo.month == null) return "month is empty"
//        }
//
//        if (frequency == RepeatFrequency.EVERY_YEAR ||
//            frequency == RepeatFrequency.EVERY_MONTH
//        ) {
//            if (frequencyInfo.day == null) return "day is empty"
//        }
//
//        if (frequency == RepeatFrequency.EVERY_WEEK) {
//            if (frequencyInfo.dayOfWeek == null) return "day of week is empty"
//        }
//
//        if (frequency == RepeatFrequency.EVERY_YEAR ||
//            frequency == RepeatFrequency.EVERY_MONTH ||
//            frequency == RepeatFrequency.EVERY_WEEK ||
//            frequency == RepeatFrequency.WEEKENDS ||
//            frequency == RepeatFrequency.WEEKDAYS ||
//            frequency == RepeatFrequency.EVERYDAY
//        ) {
//            if (frequencyInfo.hour == null) return "hour is empty"
//            if (frequencyInfo.minute == null) return "minute is empty"
//        }

        return TODO()
    }

//    suspend fun addRepeatAdd(
//        repeatAdd: RepeatAdd,
//        validCheck: Boolean = true
//    ): FuncResultWithData<RepeatAdd> {
//        if (validCheck) {
//            val msg = checkNewRepeatAddValid(repeatAdd)
//            if (msg.isNotEmpty()) return FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = msg
//            )
//        }
//        return repeatAddRepository.addRepeatAdd(repeatAdd)
//    }
//
//    suspend fun fetchAllRepeatAdd(): FuncResultWithData<List<RepeatAdd>> {
//        return repeatAddRepository.fetchAllRepeatAdd()
//    }
//
//    suspend fun updateRepeatAdd(
//        repeatAdd: RepeatAdd,
//        validCheck: Boolean = true
//    ): FuncStatusInfo {
//        if (validCheck) {
//            val msg = checkNewRepeatAddValid(repeatAdd)
//            if (msg.isNotEmpty()) return FuncStatusInfo(
//                status = FuncStatus.FAILED,
//                errorMessage = msg
//            )
//        }
//        return repeatAddRepository.updateRepeatAdd(repeatAdd)
//    }
//
//    suspend fun removeRepeatAdd(repeatAdd: RepeatAdd): FuncStatusInfo {
//        return repeatAddRepository.removeRepeatAdd(repeatAdd)
//    }

    /**
     * UI側で何％完了したかをわかるように、Flowで実行
     * flowについてはよくわかっていない。
     */
    fun addExpensesForRestOfDaysFlow(repeatAdd: RepeatAdd): Flow<Pair<Float, FuncStatusInfo?>> =
        flow {
            val id = repeatAdd.id ?: run {
                emit(
                    1f to FuncStatusInfo(
                        FuncStatus.FAILED,
                        "繰り返し追加のidの取得に失敗しました"
                    )
                )
                return@flow
            }

            /**
             * frequencyのデータから今月分の日付全部抽出して、
             * その後、今日以降のものをフィルターすればいいか
             */
            val daysList = getDaysInMonthByFrequency(TODO())
            /* 今日の日時の翌日でフィルターを掛けたい */
            val today = TODO()
            //val tomorrowMidnight = today.toLocalDate().plusDays(1).atStartOfDay()
            val tomorrowMidnight = TODO()

            val addDays = daysList.filter { !it.isBefore(tomorrowMidnight) }

            val expenseTemplate = repeatAdd.expense.copy(
                generatedType = concatStringWithBars(listOf<String>(GeneratedType.REPEAT_ADD, id))
            )

            var addedCnt: Int = 0

            for ((index, day) in addDays.withIndex()) {
                /* このdayはTimeZoneのdayだから、ISO文字列に変える必要がある */
                val isoStr = TODO()
                val expenseToAdd = expenseTemplate.copy(datetime = isoStr)

//                val stat = expenseRepository.addExpense(expenseToAdd)
//                if (stat.toFuncStatusInfo().status == FuncStatus.SUCCESS) {
//                    addedCnt++
//                }
//                emit((index + 1).toFloat() / addDays.size to null)
            }

            val finalStatus = if (addedCnt == addDays.size) {
                FuncStatusInfo(FuncStatus.SUCCESS, "すべてを追加しました")
            } else {
                FuncStatusInfo(
                    FuncStatus.FAILED,
                    "一部費用追加に失敗しました"
                )
            }

            emit(1f to finalStatus)
        }
}