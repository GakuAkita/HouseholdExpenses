package gaku.original.myapplication.utility

/**
 * Emailの@や.を置換する
 */
fun sanitizeEmail(email: String): String {
    return email.replace(".", "__dot__").replace("@", "__at__")
}

fun restoreEmail(safeEmail: String): String {
    return safeEmail.replace("__at__", "@").replace("__dot__", ".")
}