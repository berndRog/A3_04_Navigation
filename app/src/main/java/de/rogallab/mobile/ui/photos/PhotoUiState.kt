package de.rogallab.mobile.ui.photos

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class PhotoUiState(
   val capturedImageUri: Uri? = null,
   val pendingCaptureUri: Uri? = null,
   val isLoading: Boolean = false
)