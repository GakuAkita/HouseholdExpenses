package gaku.original.myapplication.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun getImageUri(): Uri? {
        return sharedImageViewModel.sharedImageUri
    }

    fun runOcr(context: Context) {
        val imageUri = getImageUri()
        if (imageUri != null) {
            runOcrSub(context, imageUri)
        }
    }

    /** OCR を実行するメソッド */
    fun runOcrSub(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            val bitmap = loadBitmapFromUri(context, imageUri)
            val image = InputImage.fromBitmap(bitmap, 0)

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source)
    }

}