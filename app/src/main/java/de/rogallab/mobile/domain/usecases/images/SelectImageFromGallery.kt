package de.rogallab.mobile.domain.usecases.images

import android.net.Uri
import androidx.core.net.toUri
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.ResultData

class SelectGalleryImageUseCase(
   private val _mediaStore: IMediaStore
) {
   suspend operator fun invoke(uriString: String): ResultData<Uri> {

      return try {
         if (uriString.isBlank())
            return ResultData.Error("URI string cannot be empty")

         val uriMediaStore = uriString.trim().toUri()

         // Validate URI scheme (content:// or file://)
         when (uriMediaStore.scheme) {
            "content", "file" -> {
               // Copy image to app's private storage
               _mediaStore.convertMediaStoreToAppStorage(uriMediaStore)?.let { uriStorage ->
                  return ResultData.Success(uriStorage)
               } ?: run {
                 ResultData.Error("Failed to copy image from gallery to app storage")
               }
            }
            else -> {
               ResultData.Error("Invalid URI scheme: ${uriMediaStore.scheme}. Expected 'content' or 'file'")
            }
         }
      } catch (e: Exception) {
         ResultData.Error(e.message ?: "Unknown error while selecting image from gallery")
      }
   }
}
