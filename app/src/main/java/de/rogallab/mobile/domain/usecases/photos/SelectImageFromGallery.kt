package de.rogallab.mobile.domain.usecases.photos

import android.net.Uri
import androidx.core.net.toUri
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.ResultData

class SelectGalleryImageUseCase(
   private val _mediaStore: IMediaStore
) {
   suspend operator fun invoke(uriString: String): ResultData<Uri> {

      return try {

         // Check if uriString is empty or blank
         if (uriString.isBlank()) {
            return ResultData.Error("URI string cannot be empty")
         }

         val sourceUri = uriString.trim().toUri()

         // Validate URI scheme (content:// or file://)
         when (sourceUri.scheme) {
            "content", "file" -> {
               // Copy image to app's private storage
               val destinationUri = _mediaStore.convertMediaStoreToAppStorage(sourceUri)

               if (destinationUri != null) {
                  ResultData.Success(destinationUri)
               } else {
                  ResultData.Error("Failed to copy image from gallery to app storage")
               }
            }
            else -> {
               ResultData.Error("Invalid URI scheme: ${sourceUri.scheme}. Expected 'content' or 'file'")
            }
         }
      } catch (e: Exception) {
         ResultData.Error(e.message ?: "Unknown error while selecting image from gallery")
      }
   }
}
