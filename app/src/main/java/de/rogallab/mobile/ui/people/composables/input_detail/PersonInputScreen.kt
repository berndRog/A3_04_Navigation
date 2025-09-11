package de.rogallab.mobile.ui.people.composables.input_detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.people.PersonIntent
import de.rogallab.mobile.ui.people.PersonUiState
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonInputScreen(
   viewModel: PersonViewModel,
   onNavigateReverse: () -> Unit = {},
) {
   // Observe the PersonUIStateFlow of the viewmodel
   val personUiState: PersonUiState by viewModel.personUiStateFlow.collectAsStateWithLifecycle()

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

      PersonContent(
         personUiState = personUiState,
         validator = koinInject<PersonValidator>(),
         onFirstNameChange = {
            viewModel.onProcessPersonIntent(PersonIntent.FirstNameChange(it))
         },
         onLastNameChange = {
            viewModel.onProcessPersonIntent(PersonIntent.LastNameChange(it))
         },
         onEmailChange = {
            viewModel.onProcessPersonIntent(PersonIntent.EmailChange(it))
         },
         onPhoneChange = {
            viewModel.onProcessPersonIntent(PersonIntent.PhoneChange(it))
         },
         innerPadding = innerPadding,
      )
   }

   ErrorHandler(
      viewModel = viewModel,
      snackbarHostState = snackbarHostState,
   )
}