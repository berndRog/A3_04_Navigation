package de.rogallab.mobile.ui.people.composables.input_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.people.PersonIntent
import de.rogallab.mobile.ui.people.PersonUiState
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.images.ImageViewModel
import de.rogallab.mobile.ui.images.composables.SelectAndShowImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonInputScreen(
   viewModel: PersonViewModel = koinViewModel(),
   imageViewModel: ImageViewModel = koinViewModel(),
   onNavigateReverse: () -> Unit = {},
   validator: PersonValidator = koinInject(),
) {
   // Observe the PersonUIStateFlow of the viewmodel
   val personUiState: PersonUiState by viewModel.personUiStateFlow.collectAsStateWithLifecycle()

   // Add coroutine scope for composable
   val coroutineScope = rememberCoroutineScope()

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets.safeDrawing, // .safeContent .safeGestures,
      topBar = {
         TopAppBar(
            title = { Text(text = stringResource(R.string.personInput)) },
            navigationIcon = {
               IconButton(
                  onClick = {
                     if(viewModel.validate()) {
                        viewModel.onProcessPersonIntent(PersonIntent.Create)
                        onNavigateReverse()
                     }
                  }
               ) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      },
      modifier = Modifier.fillMaxSize()
   ) { innerPadding ->

      Column(
         modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
      ) {
         InputName(
            name = personUiState.person.firstName,
            onNameChange = {
               viewModel.onProcessPersonIntent(PersonIntent.FirstNameChange(it))
            },
            label = stringResource(R.string.firstName),
            validateName = validator::validateFirstName,
         )
         InputName(
            name = personUiState.person.lastName,
            onNameChange = {
               viewModel.onProcessPersonIntent(PersonIntent.LastNameChange(it))
            },
            label = stringResource(R.string.lastName),
            validateName = validator::validateLastName,
         )
         InputEmail(
            email = personUiState.person.email ?: "",
            onEmailChange = {
               viewModel.onProcessPersonIntent(PersonIntent.EmailChange(it))
            },
            validateEmail = validator::validateEmail
         )
         InputPhone(
            phone = personUiState.person.phone ?: "",
            onPhoneChange = {
               viewModel.onProcessPersonIntent(PersonIntent.PhoneChange(it))
            },
            validatePhone = validator::validatePhone
         )

         SelectAndShowImage(
            localImage = personUiState.person.imagePath,
            remoteImage = null,
            handleErrorEvent = { message ->
               viewModel.handleErrorEvent(message)
            },
            onGalleryImage = { uriString ->
               coroutineScope.launch {
                  imageViewModel.selectImageFromGallery(uriString)?.let { uriStringStorage ->
                     viewModel.onProcessPersonIntent(
                        PersonIntent.ImagePathChange(uriStringStorage))
                  }
               }
            },
            onCameraImage = { groupName ->
               coroutineScope.launch {
                  imageViewModel.captureImage(groupName)?.let { uriStringStorage ->
                     viewModel.onProcessPersonIntent(PersonIntent.ImagePathChange(uriStringStorage))
                  }
               }
            }
            //imageLoader = koinInject(),
         )
      }
   }

   ErrorHandler(
      viewModel = viewModel,
      snackbarHostState = snackbarHostState,
   )
}