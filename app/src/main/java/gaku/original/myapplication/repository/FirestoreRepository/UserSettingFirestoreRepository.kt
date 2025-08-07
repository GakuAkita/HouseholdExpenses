package gaku.original.myapplication.repository.FirestoreRepository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.mergeDataToFirestore
import gaku.original.myapplication.data.RealtimeDBrepository.RepositoryUtil.setDataToFirestore
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.UserPreferences
import gaku.original.myapplication.data.dataClass.getDefaultUserPreferences
import gaku.original.myapplication.data.dataClass.toMap
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class UserSettingsFirestoreRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,/* まあ、FirestoreReference内で持っているけどこっちでも持っている */
    private val firestoreReference: FirestoreReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    val timeZoneKey: String = "TimeZone"

//    fun getUserSettingsColRef(): CollectionReference? {
//        return firestoreReference.getUserSettingsColRef()
//    }
//
//    fun getUserDocRef(): DocumentReference? {
//        return firestoreReference.getUserDocRef()
//    }

    /* emailの追加もここでやってしまう。 */
    //SignUp後にやる操作
    /* ん～functionsのほうが安全だな、、と思う。 */
    suspend fun addUserInitialData(
        email: String
    ): SuspendFuncStatusInfo {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)

        val userRef = firestoreReference.getUserDocRef()
        if (userRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空でユーザーDocを取得できませんでした。"
            )
            return statusInfo
        }


        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空です。"
            )
            return statusInfo
        }
        val newMap = mapOf("email" to email, "id" to uid)

        val statusInfo = setDataToFirestore(newMap, reference = userRef)
        if (statusInfo.status != SuspendFuncStatus.SUCCESS) {
            /* 失敗しても先にすすむ */
            Log.d(
                className,
                " $funcName: ユーザーデータの追加に失敗しました: ${statusInfo.errorMessage}"
            )
        }

        // タイムゾーンも続けてセット
        val userPrefStatus = setUserPreferences(
            getDefaultUserPreferences(),
        )

        // 両方成功したらSUCCESS返す（失敗していたら上でcallbackされてる）
        return if (userPrefStatus.status == SuspendFuncStatus.SUCCESS) {
            SuspendFuncStatusInfo(SuspendFuncStatus.SUCCESS, "")
        } else {
            userPrefStatus
        }
    }

    suspend fun setUserPreferences(
        userPreferences: UserPreferences,
    ): SuspendFuncStatusInfo {
        val funcName: String = ::setUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "UserPreferencesドキュメントが参照できませんでした"
            )
            return statusInfo
        }

        return setDataToFirestore(userPreferences.toMap(), reference = ref)
    }

    /**
     * タイムゾーンを設定する。
     * これはdata classでsetするのではなくて、zoneId単体でセットする
     */
    suspend fun setUserTimeZone(
        zoneId: String = "Asia/Tokyo"
    ): SuspendFuncStatusInfo {
        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "Userドキュメントが参照できませんでした"
            )
            return statusInfo
        }

        val newMap = mapOf(UserPreferences.FIELD_TIME_ZONE to zoneId)
        val statusInfo = mergeDataToFirestore(newMap, reference = ref)
        return statusInfo
    }

    /**
     * UserPreferencesを全部取ってきて、
     * その中からタイムゾーンだけ取り出す
     */
    suspend fun getUserTimeZone(
    ): FuncResultWithData<String> {
        val fetchResult = fetchUserPreferences()
        if (fetchResult !is FuncResultWithData.Success) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "UserPreferencesの取得に失敗しました"
            )
            return result
        }

        /* 取得はできたけどnullだったら、、 */
        val userPreferences = fetchResult.data

        /**
         * ここでセットするのは、
         * SharedViewModelにも注入されるから。
         * 毎回ApptimeZone.update..って書くのはだるい。
         * 責務の分離的には本当はよくないけど
         */
        AppTimeZone.updateStrZoneId(userPreferences.timeZone)
        val result = FuncResultWithData.Success(
            userPreferences.timeZone
        )
        return result
    }

    /**
     * UserPreferencesを全部取ってくる。特定の項目だけではなく。
     */
    suspend fun fetchUserPreferences(
        timeout: Long = 3000
    ): FuncResultWithData<UserPreferences> {
        val funcName = ::fetchUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "UserPreferencesドキュメントが参照できませんでした"
            )
            return result
        }

        return try {
            withTimeout(timeout) {
                val snapshot = ref.get().await()
                if (snapshot.exists()) {
                    val preferences = snapshot.toObject(UserPreferences::class.java)
                    if (preferences != null) {
                        val result = FuncResultWithData.Success(
                            preferences
                        )
                        result
                    } else {
                        val result = FuncResultWithData.Failure.GenericFailure(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "UserPreferencesデータの変換に失敗しました"
                        )
                        result
                    }
                } else {
                    val result = FuncResultWithData.Failure.GenericFailure(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "UserPreferencesドキュメントが存在しません"
                    )
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            val result = FuncResultWithData.Failure.Timeout()
            result
        } catch (e: Exception) {
            val result = FuncResultWithData.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
            result
        }
    }
}