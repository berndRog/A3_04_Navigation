package de.rogallab.mobile.ui.photos.composables

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat

@Composable
fun CameraCheckPermission(
   handleErrorEvent: (String) -> Unit,
   onPermissionGranted: @Composable () -> Unit
) {
   val context = LocalContext.current
   var hasCameraPermission by remember {
      mutableStateOf(
         ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
         ) == PackageManager.PERMISSION_GRANTED
      )
   }

   val permissionLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
   ) { isGranted ->
      hasCameraPermission = isGranted
      if (!isGranted) {
         handleErrorEvent("Camera permission denied")
      }
   }

   if (hasCameraPermission) {
      onPermissionGranted()
   } else {
      Button(
         onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
         modifier = Modifier.fillMaxWidth()
      ) {
         Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Camera Permission"
         )
         Spacer(modifier = Modifier.width(8.dp))
         Text("Grant Camera Permission")
      }
   }
}