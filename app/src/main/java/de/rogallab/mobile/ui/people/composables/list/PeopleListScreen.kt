package de.rogallab.mobile.ui.people.composables.list

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.PersonDetail
import de.rogallab.mobile.ui.navigation.PersonInput
import de.rogallab.mobile.ui.people.PeopleIntent
import de.rogallab.mobile.ui.people.PersonIntent
import de.rogallab.mobile.ui.people.PersonViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
   viewModel: PersonViewModel = koinViewModel(),
   onNavigatePersonInput: () -> Unit = { },
   onNavigatePersonDetail: (String) -> Unit = {  }
) {
   val tag = "<-PeopleListScreen"

   logDebug(tag, "PeopleListScreen composition")

   // Observe the PeopleUiStateFlow of the ViewModel
   val peopleUiState by viewModel.peopleUiStateFlow.collectAsStateWithLifecycle()

   logDebug(tag, "PeopleUiState: ${peopleUiState.people.size}")


   LaunchedEffect(Unit) {
      // Fetch all people when the screen is launched
      viewModel.onProcessPeopleIntent(PeopleIntent.FetchPeople)
   }

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets.safeDrawing, // .safeContent .safeGestures
      modifier = Modifier.fillMaxSize(),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.peopleList)) },
            navigationIcon = {
               val activity: Activity? = LocalActivity.current
               IconButton(
                  onClick = {
                     logDebug(tag, "Menu navigation clicked -> Exit App")
                     activity?.finish()
                  }
               ) {
                  Icon(
                     imageVector = Icons.Default.Menu,
                     contentDescription = "Exit App"
                  )
               }
            },
         )
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
               logDebug(tag, "FAB clicked")
               viewModel.onProcessPersonIntent(PersonIntent.Clear) // Reset the person state
               onNavigatePersonInput() // Navigate to PersonInputScreen
            }
         ) {
            Icon(Icons.Default.Add, "Add a contact")
         }
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }
   ) { innerPadding ->

      val undoMessage = stringResource(R.string.undoDeletePerson)
      val undoActionLabel = stringResource(R.string.undoAnswer)

      LazyColumn(
         modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 20.dp) // Add vertical padding
            .fillMaxSize()) {
         items(
            items = peopleUiState.people.sortedBy { it.firstName },
            key = { it: Person -> it.id }
         ) { person ->
            logDebug(tag, "Person: ${person.firstName} ${person.lastName}")
            SwipePersonListItem(
               person = person,                        // item
               onNavigate = {                          // navigate to DetailScreen
                  onNavigatePersonDetail(person.id)
               },
               onDelete = {                            // remove item
                  logDebug(tag, "Delete person: ${person.firstName} ${person.lastName}")
                  viewModel.onProcessPersonIntent(PersonIntent.Remove(person))
               },
               onUndo = {                             // handle undo action
                  viewModel.handleUndoEvent(
                     message = undoMessage,
                     actionLabel = undoActionLabel,
                     onActionPerform = {
                        logDebug(tag, "Undo delete person: ${person.firstName} ${person.lastName}")
                        viewModel.onProcessPersonIntent(PersonIntent.Undo)
                     }
                  )
               },
            ) {
               PersonCard(
                  firstName = person.firstName,
                  lastName = person.lastName,
                  email = person.email,
                  phone = person.phone,
                  imagePath = person.imagePath,
               )
            }
         }
      }
   }

   // Error handling
   ErrorHandler(
      viewModel = viewModel,
      snackbarHostState = snackbarHostState
   )
}