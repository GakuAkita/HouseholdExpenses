package gaku.original.myapplication

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData

/**
 * ほとんどGPTに作ってもらったからわからん。
 */
object CredentialManagerHelper {
    suspend fun getGoogleIdToken(context: Context): FuncResultWithData<String> {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false) // falseで全アカウント表示
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                FuncResultWithData.Success(data = googleCredential.idToken)
            } else FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "Unexpected credential type: ${credential?.type}"
            )
        } catch (e: GetCredentialException) {
            Log.e("CredentialManager", "Credential error: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}