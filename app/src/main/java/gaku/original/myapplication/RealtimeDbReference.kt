package gaku.original.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.utility.LogException
import gaku.original.myapplication.utility.LogTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class RealtimeDbReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val database = FirebaseDatabase
        .getInstance("https://householdexpenses2-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference//users配下にそれぞれのuserIdが存在

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    //users配下の自分のuserIdのreferenceを返す
    // userId配下のexpenses
    suspend fun getUserRef(): FetchResult<DatabaseReference> {
        val funcName = ::getUserRef.name
        return try {
            withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    val userId: String = currentUserId ?: throw Exception("userId is null")

                    val ref: DatabaseReference = database.child("users").child(userId)
                    FetchResult.Success(ref)
                }
            }
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            FetchResult.Failure.Timeout()
        } catch (e: Exception) {
            LogException(className, funcName, e)
            FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "${e.message}"
            )
        }
    }

    /* getUserExpensesRefとgetUserCategoryRefで同じことをやっていたので共通化 */
    private suspend fun getUserChildrenRef(
        childrenPath: List<String>,/* たどり着きたい順に名前をいれていく */
        funcName: String
    ): FetchResult<DatabaseReference> {
        val userRefRet = getUserRef()

        if (userRefRet !is FetchResult.Success) {
            return userRefRet
        }

        val userRef = userRefRet.data

        //シーケンスみたい。ある処理が終えたら次をスタートして、、みたいな。
        return try {
            val finalRef = withTimeout(2000) {
                withContext(Dispatchers.IO) {
                    var tmpRef = userRef
                    for (childName in childrenPath) {
                        tmpRef = tmpRef.child(childName)
                            ?: throw Exception("tmpRef became null at child: $childName")
                    }
                    tmpRef
                }
            }
            val result = FetchResult.Success(finalRef)
            result
        } catch (e: TimeoutCancellationException) {
            LogTimeout(className, funcName, e)
            val result = FetchResult.Failure.Timeout("Timeout: ${e.message}")
            result
        } catch (e: Exception) {
            LogException(className, funcName, e)
            val result = FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
            result
        }
    }

//    // userId配下のexpenses
//    suspend fun getUserExpenseRef(): FetchResult<DatabaseReference> {
//        val funcName = ::getUserExpenseRef.name
//        Log.d(className, "${funcName} was called.")
//        val childrenPath = listOf("data", "expenses")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    //userId配下のcategory
//    suspend fun getUserCategoryRef(): FetchResult<DatabaseReference> {
//        val funcName = ::getUserCategoryRef.name
//        Log.d(className, "${funcName} was called")
//        val childrenPath = listOf("data", "categories")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    suspend fun getUserSettingsRef(): FetchResult<DatabaseReference> {
//        val funcName = ::getUserSettingsRef.name
//        Log.d(className, "${funcName} was called")
//        val childrenPath = listOf("settings")
//
//        val ret = getUserChildrenRef(childrenPath, funcName)
//        return ret
//    }
//
//    suspend fun getUserRepeatAddRef(): FetchResult<DatabaseReference> {
//        val funcName = ::getUserRepeatAddRef.name
//        Log.d(className, "${funcName} was called")
//        val baseRefRet = getUserSettingsRef()
//        if (baseRefRet !is FetchResult.Success) {//拡張関数を使うと、スマートキャストが効かない
//            return baseRefRet
//        }
//
//        val baseRef = baseRefRet.data
//        val newRef = baseRef.child("repeatAdd")
//        val result = FetchResult.Success(newRef)
//        return result
//    }

    /* カテゴリー割当て */
    suspend fun getCategoryAssignmentDataRef(): FetchResult<DatabaseReference> {
        val funcName = ::getCategoryAssignmentDataRef.name
        val childrenPath = listOf("category_assignment_data")

        val ret = getUserChildrenRef(childrenPath, funcName)
        return ret
    }

    /**
     * CategoryAssignmentDataのプロパティ名とノード名を一致させておく
     * そうすると、RepositoryでGetするときに一括変換できる
     */
    suspend fun getProductNameCategoryAssignmentRef(): FetchResult<DatabaseReference> {
        val baseRefRet = getCategoryAssignmentDataRef()
        if (baseRefRet !is FetchResult.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FetchResult.Success(
            baseRef.child("productName")
        )
        return result
    }

    suspend fun getStoreNameCategoryAssignmentRef(): FetchResult<DatabaseReference> {
        val baseRefRet = getCategoryAssignmentDataRef()
        if (baseRefRet !is FetchResult.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FetchResult.Success(
            baseRef.child("storeName")
        )
        return result
    }

    /* MailboxExtraction配下 */
    private suspend fun getMailboxExtractionRef(): FetchResult<DatabaseReference> {
        val funcName = ::getMailboxExtractionRef.name
        /* Log.d(className, "${funcName} was called") */
        val childrenPath = listOf("mailbox_extraction")

        val ret = getUserChildrenRef(childrenPath, funcName)//callbackは中で実行される
        return ret
    }

    private suspend fun getMailboxExtractionMailTypeSettingsRef(): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionRef()
        if (baseRefRet !is FetchResult.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        val result = FetchResult.Success(
            baseRef.child("mail_type_settings")
        )
        return result
    }

    suspend fun getMailboxExtractionMailTypeSettingSingleRef(
        type: EmailTemplateType
    ): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionMailTypeSettingsRef()
        if (baseRefRet !is FetchResult.Success) {
            return baseRefRet
        }
        val baseRef = baseRefRet.data

        val result = FetchResult.Success(
            baseRef.child(type.nodeName)
        )
        return result
    }

    /**
     * もっと柔軟にしたいけど、とりあえずはベタ打ち
     */
    suspend fun getMailboxExtractionMailTypeCategoryAssignmentRef(
        type: EmailTemplateType,
    ): FetchResult<DatabaseReference> {
        val baseRefRet = getMailboxExtractionMailTypeSettingSingleRef(type);
        if (baseRefRet !is FetchResult.Success) {
            return baseRefRet
        }

        val baseRef = baseRefRet.data
        when (type::class) {
            EmailTemplateType.RakutenPay::class -> {
                val result = FetchResult.Success(
                    baseRef.child("storeCategoryAssignments")
                )
                return result
            }

            EmailTemplateType.AmazonItem::class -> {
                val result = FetchResult.Success(
                    baseRef.child("itemCategoryAssignments")
                )
                return result
            }

            else -> {
                val result = FetchResult.Failure.GenericFailure(
                    status = SuspendFuncStatus.FAILED,
                    errorMessage = "対応していないタイプです(${type::class})"
                )
                return result
            }
        }
    }
}