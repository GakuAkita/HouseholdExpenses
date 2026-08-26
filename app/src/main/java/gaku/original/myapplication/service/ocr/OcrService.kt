package gaku.original.myapplication.service.ocr

interface OcrService {
    suspend fun runOcr(imageUri: String)
}