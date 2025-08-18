package de.rogallab.mobile.ui.photos

sealed class PhotoIntent {
   data class CameraImage(val groupName: String, val onSuccess: ((String) -> Unit)? = null) : PhotoIntent()
   data class GalleryImage(val uriString: String, val onSuccess: ((String) -> Unit)? = null) : PhotoIntent()
   data class PermissionDenied(val permanently: Boolean) : PhotoIntent()
   object ClearMessages : PhotoIntent()
   object ClearCapturedUri : PhotoIntent()
}