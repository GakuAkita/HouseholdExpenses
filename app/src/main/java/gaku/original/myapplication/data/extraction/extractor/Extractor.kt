package gaku.original.myapplication.data.extraction.extractor

import android.net.Uri

interface Extractor {
    suspend fun extract(image: Uri)
}