package de.rogallab.mobile.ui.images

import androidx.core.net.toUri
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.usecases.images.CaptureImageUseCase
import de.rogallab.mobile.domain.usecases.images.SelectGalleryImageUseCase
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ImageViewModel(
   private val _captureImageUseCase: CaptureImageUseCase,
   private val _selectImageFromGalleryUseCase: SelectGalleryImageUseCase,
   private val _navHandler: INavHandler,
) : BaseViewModel(_navHandler, TAG) {

   private val _imageUiStateFlow = MutableStateFlow(ImageUiState())
   val imagesUiStateFlow: StateFlow<ImageUiState> = _imageUiStateFlow.asStateFlow()

   // Direct functions with return values
   suspend fun selectImageFromGallery(uriString: String): String? {
      return when (val resultData = _selectImageFromGalleryUseCase(uriString)) {
         is ResultData.Success -> {
            val uriStorage = resultData.data
            _imageUiStateFlow.update { currentState ->
               currentState.copy(
                  capturedImageUri = uriStorage,
               )
            }
            uriStorage.toString() // Return uriStorage a string
         }
         is ResultData.Error -> {
            handleErrorEvent(resultData.message)
            null
         }
      }
   }

   suspend fun captureImage(uriString: String): String? {
      return when (val resultData = _captureImageUseCase(uriString,"")) {
         is ResultData.Success -> {
            val uriStorage = resultData.data
            _imageUiStateFlow.update { currentState ->
               currentState.copy(
                  capturedImageUri = uriStorage,
               )
            }
            uriStorage.toString() // Return uriStorage a string
         }
         is ResultData.Error -> {
            handleErrorEvent(resultData.message)
            null
         }
      }
   }

   fun clearCapturedUri() {
      _imageUiStateFlow.update { currentState ->
         currentState.copy(
            capturedImageUri = null,
         )
      }
   }


   companion object {
      private const val TAG = "ImageViewModel"
   }
}