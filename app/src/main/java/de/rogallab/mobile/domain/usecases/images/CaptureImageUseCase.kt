package de.rogallab.mobile.domain.usecases.images

import android.net.Uri
import androidx.core.net.toUri
import de.rogallab.mobile.MainApplication.Companion.MEDIA_STORE_GROUP_NAME
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug

class CaptureImageUseCase(
   private val _mediaStore: IMediaStore
) {
   suspend operator fun invoke(
      capturedImageUriString: String,
      groupName: String
   ): ResultData<Uri> {
      return try {
         val tag = "<-CaptureImageUC"

         // Parse the image URI captured by the camera
         val sourceUri = capturedImageUriString.toUri()

         val actualGroupName = groupName.ifBlank { MEDIA_STORE_GROUP_NAME }
         val uriMediaStore = _mediaStore.saveImageToMediaStore(groupName,sourceUri )
         uriMediaStore ?: return ResultData.Error(
            "Failed to save image to MediaStore for group: $actualGroupName")

         // Convert MediaStore URI to app's private storage
         val uriStorage = _mediaStore.convertMediaStoreToAppStorage(uriMediaStore)
         logDebug(tag,"uriStorage: $uriStorage")
         uriStorage ?: return ResultData.Error("Failed to copy image from MediaStore to app storage")

         // Return the URI of the image stored in the app's private storage
         ResultData.Success(uriStorage)

      } catch (e: Exception) {
         ResultData.Error(e.message ?: "Unknown error while capturing photo")
      }
   }
}