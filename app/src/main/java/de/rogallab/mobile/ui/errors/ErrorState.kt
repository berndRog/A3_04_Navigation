package de.rogallab.mobile.ui.errors

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
data class ErrorState(
   // Snackbar parameters
   // message:           this is the primary text shown in the snackbar
   val message: String = "Error unknown", // default message if none is provided
   // actionLabel:       Text for an optional action button on the Snackbar. If clicked showSnackbar()
   //                    returns SnackbarResult.ActionPerformed and the onActionPerform() is called,
   //                    which can be "do nothing"
   val actionLabel: String? = null,
   val onActionPerform: () -> Unit = { }, // do nothing
   // withDismissAction: if true, shows a dismiss icon X on the Snackbar. If clicked, or if the snackbar
   //                    times out or is swiped away, showSnackbar() returns SnackbarResult.Dismissed,
   //                    and onDismissed() is called which can be "do nothing".
   val withDismissAction: Boolean = true,
   val onDismissed: () -> Unit = { }, // do nothing
   // duration:          Defines how long the Snackbar will be visible.
   // duration of the snackbars visibility
   val duration: SnackbarDuration = SnackbarDuration.Long,

   // delayed navigation, default do nothing
   val navKey: NavKey? = null, // optional NavKey for navigation
   val onDelayedNavigation: (NavKey?) -> Unit = {  }
)