package de.rogallab.mobile.domain.usecases.photos

import android.net.Uri
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.ResultData


class SaveToMediaStoreUseCase(
   private val _mediaStore: IMediaStore
) {
   suspend operator fun invoke(groupName: String? = null, filename: String? = null): ResultData<Uri> {
      return try {
         val group = groupName ?: _mediaStore.createSessionFolder()

         if (group.isBlank()) {
            return ResultData.Error("Failed to create or get valid group name")
         }

         val uri = _mediaStore.createGroupedImageUri(group, filename)

         if (uri != null) {
            ResultData.Success(uri)
         } else {
            ResultData.Error("Failed to create image URI for group: $group")
         }
      } catch (e: Exception) {
         ResultData.Error(e.message ?: "Unknown error while creating image URI")
      }
   }
}