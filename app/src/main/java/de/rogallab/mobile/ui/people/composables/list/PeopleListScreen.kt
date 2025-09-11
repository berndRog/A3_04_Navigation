package de.rogallab.mobile.ui.people.composables.list

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.people.PeopleIntent
import de.rogallab.mobile.ui.people.PersonIntent
import de.rogallab.mobile.ui.people.PersonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
   viewModel: PersonViewModel,
   onNavigatePersonInput: () -> Unit = {},
   onNavigatePersonDetail: (String) -> Unit = {}
) {
   val tag = "<-PeopleListScreen"
   logDebug(tag, "PeopleListScreen composition")

   val lifecycle = (LocalActivity.current as? ComponentActivity)?.lifecycle
      ?: LocalLifecycleOwner.current.lifecycle
   val peopleUiState by viewModel.peopleUiStateFlow.collectAsStateWithLifecycle(
      lifecycle = lifecycle,
      minActiveState = Lifecycle.State.STARTED
   )
   LaunchedEffect(peopleUiState.isLoading, peopleUiState.people.size) {
      logDebug(tag, "PeopleUiState: isLoading=${peopleUiState.isLoading} size=${peopleUiState.people.size}")
   }

   LaunchedEffect(Unit) {
      logDebug(tag, "Fetching people")
      viewModel.onProcessPeopleIntent(PeopleIntent.FetchPeople)
   }

   val listState = rememberLazyListState()
   val snackbarHostState = remember { SnackbarHostState() }

   // Scroll to the restored item only if it's not already visible
   LaunchedEffect(peopleUiState.restoredPersonId) {
      peopleUiState.restoredPersonId?.let { restoredId ->
         val index = peopleUiState.people.indexOfFirst { it.id == restoredId }
         if (index != -1) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isVisible = visibleItems.any { it.index == index }
            if (!isVisible) {
               listState.animateScrollToItem(index)
            }
         }
         // Acknowledge that the scroll is done
         viewModel.onProcessPersonIntent(PersonIntent.Restored)
      }
   }


   Scaffold(
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets.safeDrawing,
      modifier = Modifier.fillMaxSize(),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.peopleList)) },
            navigationIcon = {
               val activity: Activity? = LocalActivity.current
               IconButton(onClick = {
                  logDebug(tag, "Menu navigation clicked -> Exit App")
                  activity?.finish()
               }) {
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
               viewModel.onProcessPersonIntent(PersonIntent.Clear)
               onNavigatePersonInput()
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

      if (peopleUiState.isLoading) {
         logDebug(tag, "Loading indicator")
         Box(
            modifier = Modifier
               .fillMaxSize()
               .padding(innerPadding),
            contentAlignment = Alignment.Center
         ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
         }
      } else {
         logDebug(tag, "Show Lazy Column")

         val undoMessage = stringResource(R.string.undoDeletePerson)
         val undoActionLabel = stringResource(R.string.undoAnswer)
         val people = peopleUiState.people

         LazyColumn(
            state = listState,
            modifier = Modifier
               .padding(innerPadding)
               .padding(horizontal = 20.dp)
               .fillMaxSize()
         ) {
            items(
               items = people,
               key = { person -> person.id }
            ) { person ->
               logDebug(tag, "Lazy Column, size:${people.size} - Person: ${person.firstName}")

               SwipePersonListItem(
                  person = person,
                  onNavigate = { onNavigatePersonDetail(person.id) },
                  onDelete = {
                     viewModel.onProcessPersonIntent(PersonIntent.Remove(person))
                  },
                  onUndo = {
                     val errorState = ErrorState(
                        message = undoMessage,
                        actionLabel = undoActionLabel,
                        onActionPerform = {
                           viewModel.onProcessPersonIntent(PersonIntent.Undo)
                        },
                        withDismissAction = false
                     )
                     viewModel.onProcessPersonIntent(PersonIntent.HandleUndoEvent(errorState))
                  }
               ) {
                  PersonCard(
                     firstName = person.firstName,
                     lastName = person.lastName,
                     email = person.email,
                     phone = person.phone,
                     imagePath = person.imagePath
                  )
               }
            }
         }
      }
   }

   ErrorHandler(
      viewModel = viewModel,
      snackbarHostState = snackbarHostState
   )
}
