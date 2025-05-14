package gaku.original.myapplication.data

import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus

data class ErrorInfo(
    val isError: Boolean,
    val errorMessage: String
)

data class SuspendFuncStatusInfo(
    val status: SuspendFuncStatus,
    val errorMessage: String,
)

data class ExpenseFetchResult(
    val status: SuspendFuncStatus,
    val errorMessage: String,
    val data: List<Expense> = emptyList()
)