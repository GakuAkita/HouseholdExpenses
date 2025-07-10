package gaku.original.myapplication.data.Repository.FirestoreRepository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
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
        email: String,
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName: String = ::addUserInitialData.name
        LogClassFuncCalled(className, funcName)

        val userRef = firestoreReference.getUserDocRef()
        if (userRef == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空でユーザーDocを取得できませんでした。"
            )
            callback(statusInfo)
            return statusInfo
        }


        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "ユーザーIDが空です。"
            )
            callback(statusInfo)
            return statusInfo
        }
        val newMap = mapOf("email" to email, "id" to uid)

        val statusInfo = setDataToFirestore(newMap, reference = userRef, callback = callback)
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
            callback = callback
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
        callback: (SuspendFuncStatusInfo) -> Unit
    ): SuspendFuncStatusInfo {
        val funcName: String = ::setUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val statusInfo = SuspendFuncStatusInfo(
                SuspendFuncStatus.FAILED,
                "UserPreferencesドキュメントが参照できませんでした"
            )
            callback(statusInfo)
            return statusInfo
        }

        return setDataToFirestore(userPreferences.toMap(), reference = ref, callback = callback)
    }

    /**
     * タイムゾーンを設定する。
     * これはdata classでsetするのではなくて、zoneId単体でセットする
     */
    suspend fun setUserTimeZone(
        zoneId: String = "Asia/Tokyo",
        callback: (SuspendFuncStatusInfo) -> Unit
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
        val statusInfo = mergeDataToFirestore(newMap, reference = ref, callback = {
            if (it.status == SuspendFuncStatus.SUCCESS) {
                /**
                 * ここでやっているのは、Repositoryを他のViewModelに注入したときに
                 * 毎回ApptimeZone.update..って書く必要があるからa
                 */
                AppTimeZone.updateStrZoneId(zoneId)
                callback(it)
            } else {
                callback(it)
            }
        })
        return statusInfo
    }

    /**
     * UserPreferencesを全部取ってきて、
     * その中からタイムゾーンだけ取り出す
     */
    suspend fun getUserTimeZone(
        callback: (SuspendFuncStatusInfo) -> Unit
    ): FetchResult<String> {
        val fetchResult = fetchUserPreferences()
        if (fetchResult !is FetchResult.Success) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "UserPreferencesの取得に失敗しました"
            )

            callback(result.toSuspendFuncStatusInfo())
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
        val result = FetchResult.Success(
            userPreferences.timeZone
        )
        callback(result.toSuspendFuncStatusInfo())
        return result
    }

    /**
     * UserPreferencesを全部取ってくる。特定の項目だけではなく。
     */
    suspend fun fetchUserPreferences(
        timeout: Long = 3000,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ): FetchResult<UserPreferences> {
        val funcName = ::fetchUserPreferences.name
        LogClassFuncCalled(className, funcName)

        val ref = firestoreReference.getUserPreferencesDocRef()
        if (ref == null) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "UserPreferencesドキュメントが参照できませんでした"
            )
            callback(result.toSuspendFuncStatusInfo())
            return result
        }

        return try {
            withTimeout(timeout) {
                val snapshot = ref.get().await()
                if (snapshot.exists()) {
                    val preferences = snapshot.toObject(UserPreferences::class.java)
                    if (preferences != null) {
                        val result = FetchResult.Success(
                            preferences
                        )
                        callback(result.toSuspendFuncStatusInfo())
                        result
                    } else {
                        val result = FetchResult.Failure.GenericFailure(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "UserPreferencesデータの変換に失敗しました"
                        )
                        callback(result.toSuspendFuncStatusInfo())
                        result
                    }
                } else {
                    val result = FetchResult.Failure.GenericFailure(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "UserPreferencesドキュメントが存在しません"
                    )

                    callback(result.toSuspendFuncStatusInfo())
                    result
                }
            }
        } catch (e: TimeoutCancellationException) {
            val result = FetchResult.Failure.Timeout()
            callback(result.toSuspendFuncStatusInfo())
            result
        } catch (e: Exception) {
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "不明なエラー"
            )
            callback(result.toSuspendFuncStatusInfo())
            result
        }
    }
}