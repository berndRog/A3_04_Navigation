package de.rogallab.mobile.ui.photos

import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.usecases.photos.CaptureImageUseCase
import de.rogallab.mobile.domain.usecases.photos.SelectGalleryImageUseCase
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PhotoViewModel(
   private val _captureImageUseCase: CaptureImageUseCase,
   private val _selectImageFromGalleryUseCase: SelectGalleryImageUseCase,
   private val _navHandler: INavHandler,
) : BaseViewModel(_navHandler, TAG) {

   private val _photoUiStateFlow = MutableStateFlow(PhotoUiState())
   val photoUiStateFlow: StateFlow<PhotoUiState> = _photoUiStateFlow.asStateFlow()

   // Direct functions with return values
   suspend fun selectImageFromGallery(uriString: String): String? {
      return when (val resultData = _selectImageFromGalleryUseCase(uriString)) {
         is ResultData.Success -> resultData.data.toString()  // uriStorage
         is ResultData.Error -> {
            handleErrorEvent(resultData.message)
            null
         }
      }
   }

   suspend fun captureImage(groupName: String): String? {
      return when (val resultData = _captureImageUseCase(groupName)) {
         is ResultData.Success -> resultData.data.toString()
         is ResultData.Error -> {
            handleErrorEvent(resultData.message)
            null
         }
      }
   }

   fun clearMessages() {
      _photoUiStateFlow.update { currentState ->
         currentState.copy(
            // successMessage = null,
         )
      }
   }

   fun clearCapturedUri() {
      _photoUiStateFlow.update { currentState ->
         currentState.copy(
            capturedImageUri = null,
            pendingCaptureUri = null
         )
      }
   }


   companion object {
      private const val TAG = "PhotoViewModel"
   }
}