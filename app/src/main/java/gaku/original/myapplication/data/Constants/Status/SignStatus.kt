package gaku.original.myapplication.data.Constants.Status

enum class SignInResult {
    SUCCESS,            // サインアップ成功
    USER_ID_NULL,       // サインアップ成功したがUIDがnull
    SIGN_IN_FAILED      // サインアップ失敗
}

enum class SingUpResult {
    SUCCESS,            // サインアップ成功
    USER_ID_NULL,       // サインアップ成功したがUIDがnull
    SIGN_UP_FAILED      // サインアップ失敗
}

enum class SingOutResult {
    SUCCESS,            // サインアウト成功
    SIGN_OUT_FAILED     // サインアウト失敗
}