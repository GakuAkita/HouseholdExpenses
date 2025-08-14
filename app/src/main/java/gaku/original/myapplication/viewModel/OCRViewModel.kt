package gaku.original.myapplication.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.d("OCRViewModel", "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageUri()
    }

    private val _ocrResult = MutableStateFlow("")
    val ocrResult: StateFlow<String> get() = _ocrResult

    private val _ocrUri = MutableStateFlow<Uri?>(null)
    val ocrUri: StateFlow<Uri?> = _ocrUri.asStateFlow()

    init {
        viewModelScope.launch {
            /**
             *  sharedViewModelのuriに更新があったときに検知できるように画面が生きている間は監視しておく
             *  */
            sharedImageViewModel.sharedImageUri.collect { uri ->
                _ocrUri.value = uri
            }
        }
    }

    fun getImageUri(): Uri? {
        return sharedImageViewModel.sharedImageUri.value
    }

    fun runOcr(context: Context) {
        val imageUri = getImageUri()
        if (imageUri != null) {
            runOcrSub(context, imageUri)
        }
    }

    /** OCR を実行するメソッド */
    fun runOcrSub(context: Context, imageUri: Uri, callback: () -> Unit = {}) {
        viewModelScope.launch {
            val bitmap = loadBitmapFromUri(context, imageUri)
            if (bitmap == null) {
                LogAkitaDebug("bitmap is null")
                _ocrResult.value = ""
                return@launch
            }

            val image = InputImage.fromBitmap(bitmap, 0)

            val recognizer =
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    _ocrResult.value = visionText.text
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    _ocrResult.value = ""
                }
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }

}