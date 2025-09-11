package de.rogallab.mobile.ui.base

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class BaseViewModel(
   private val _navHandler: INavHandler,
   private val _tag: String = "<-BaseViewModel"
): ViewModel() {

   // CoroutineExceptionHandler to handle uncaught exceptions in coroutines
   protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
      logError(_tag, "CoroutineExceptionHandler: ${throwable.localizedMessage}")
      handleErrorEvent(throwable)
   }

   // Channel + Flow for Error handling
   // private val _errorChannel: Channel<ErrorState?> = Channel<ErrorState?>(Channel.BUFFERED)
   // val errorFlow: Flow<ErrorState?> = _errorChannel.receiveAsFlow()

   // MutableSharedFlow with replay = 1 ensures that the last emitted error is replayed to new collectors,
   // allowing the error to be shown immediately when a new observer collects the flow (navigation case).
   private val _errorFlow = MutableSharedFlow<ErrorState?>(replay = 1) // Changed
   val errorFlow: Flow<ErrorState?> = _errorFlow.asSharedFlow() // Changed

   // handle throwable, i.e. an error event
   fun handleErrorEvent(
      throwable: Throwable? = null,
      message: String? = null,
      actionLabel: String? = null,       // no actionLabel by default
      onActionPerform: () -> Unit = {},  // do nothing by default
      withDismissAction: Boolean = true, // show dismiss action
      onDismissed: () -> Unit = {},      // do nothing by default
      duration: SnackbarDuration = SnackbarDuration.Long,
      // delayed navigation
      navKey: NavKey? = null           // no navigation by default
   ) {
      val errorMessage =  throwable?.message ?: message ?: "Unknown error"

      val errorState = ErrorState(
         message = errorMessage,
         actionLabel = actionLabel,
         onActionPerform = onActionPerform,
         withDismissAction = withDismissAction,
         onDismissed = onDismissed,
         duration = duration,
         navKey = navKey,
         onDelayedNavigation = { key ->
            // Only navigate after dismissal
            if (key != null) {
               logDebug(_tag, "Navigating to $key after error dismissal")
               _navHandler.popToRootAndNavigate(key)
            }
         }
      )
      viewModelScope.launch {
         logError(_tag, errorMessage)
         // _errorChannel.send(errorState)
         _errorFlow.emit(errorState)
      }
   }

   // handle undo event
   fun handleUndoEvent( errorState: ErrorState) {
      viewModelScope.launch {
         // _errorChannel.send(errorState)
         _errorFlow.emit(errorState)
      }
   }

   fun clearErrorState() {
      viewModelScope.launch {
         //_errorChannel.send(null)  // Emit null to clear the error state
         _errorFlow.emit(null)  // Emit null to clear the error state
      }
   }
}