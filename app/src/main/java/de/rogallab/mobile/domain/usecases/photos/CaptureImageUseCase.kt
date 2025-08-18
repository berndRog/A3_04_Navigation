package de.rogallab.mobile.domain.usecases.photos

import android.net.Uri
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.ResultData

class CaptureImageUseCase(
   private val _mediaStore: IMediaStore
) {
   suspend operator fun invoke(
      capturedImageUri: String,
      groupName: String = "Photos"
   ): ResultData<Uri> {
      return try {
         // Parse the image URI captured by the camera
         val tempUri = Uri.parse(capturedImageUri)

         // Save image to MediaStore with grouping
         val uriMediaStore = _mediaStore.saveImageToMediaStore(groupName,tempUri )
         uriMediaStore ?: return ResultData.Error("Failed to save image to MediaStore for group: $groupName")

         // Convert MediaStore URI to app's private storage
         val uriStorage = _mediaStore.convertMediaStoreToAppStorage(uriMediaStore)
         uriStorage ?: return ResultData.Error("Failed to copy image from MediaStore to app storage")

         // Return the URI of the image stored in the app's private storage
         ResultData.Success(uriStorage)

      } catch (e: Exception) {
         ResultData.Error(e.message ?: "Unknown error while capturing photo")
      }
   }
}