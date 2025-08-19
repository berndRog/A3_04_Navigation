package de.rogallab.mobile.ui.images.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectImageFromGallery(
   onImageSelected: (String) -> Unit // Pass URI string to ViewModel
) {
   val selectImageFromGalleryLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
   ) { uri: Uri? ->
      uri?.let { selectedUri ->
         onImageSelected(selectedUri.toString()) // Just pass the URI string to ViewModel
      }
   }

   Button(
      onClick = {
         selectImageFromGalleryLauncher.launch("image/*")
      },
      modifier = Modifier.fillMaxWidth()
   ) {
      Icon(
         imageVector = Icons.Default.PhotoLibrary,
         contentDescription = "Select from Gallery"
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("Select from Gallery")
   }
}