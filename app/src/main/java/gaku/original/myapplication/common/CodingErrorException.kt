package gaku.original.myapplication.common

class CodingErrorException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    override val message: String
        get() = "Coding Error: ${super.message}"
}