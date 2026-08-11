package gaku.original.myapplication.common

sealed interface AppResult<out T, out E : AppError> {
    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    data class Success<T>(val value: T) : AppResult<T, Nothing>
    data class Failure<E : AppError>(val error: E) : AppResult<Nothing, E>
}
// I originally thought out T was necessary. Only T is OK,but IDE warned me.
// If I don't put out T, I can't return AppResult<Expense,InputError> for example.

interface AppError {
    val message: String
}