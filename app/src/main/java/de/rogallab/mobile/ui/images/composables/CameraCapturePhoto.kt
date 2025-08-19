package de.rogallab.mobile.ui.images.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun CameraCapturePhoto(
   onImageCaptured: (String) -> Unit, // UriString
   onErrorEvent: (String?) -> Unit = {}
) {
   val context = LocalContext.current
   var imageUri by remember { mutableStateOf<Uri?>(null) }

   val cameraLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.TakePicture()
   ) { success ->
      if (success) {
         imageUri?.let { uri ->
            onImageCaptured(uri.toString())
         }
      } else {
         onErrorEvent("Camera capture failed or was cancelled")
      }
   }

   Button(
      onClick = {
         // Create a temporary file for the camera image
         val photoFile = File.createTempFile(
            "photo_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
         )
         imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
         )
         imageUri?.let { uri ->
            cameraLauncher.launch(uri)
         }
      },
      modifier = Modifier.fillMaxWidth()
   ) {
      Icon(
         imageVector = Icons.Default.PhotoCamera,
         contentDescription = "Take Photo"
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("Take Photo")
   }
}