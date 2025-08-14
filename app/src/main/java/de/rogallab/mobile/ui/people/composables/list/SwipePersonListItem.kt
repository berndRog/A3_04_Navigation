package de.rogallab.mobile.ui.people.composables.list
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug // Make sure this utility is available
import kotlinx.coroutines.delay


/**
 * SwipePersonListItem Algorithm
 *
 * OVERVIEW:
 * This composable implements a swipeable list item with delete/undo functionality and edit navigation.
 * It uses a single state variable approach to manage swipe gestures and their corresponding actions.
 *
 * STATE MANAGEMENT:
 * - isDelete: Boolean - Single state controlling both visual animation and delete logic
 *
 * SWIPE GESTURE HANDLING:
 * SwipeToDismissBoxState.confirmValueChange() validates and confirms swipe actions:
 * 1. StartToEnd (left-to-right swipe): EDIT ACTION
 *    - Immediately calls onNavigate(person.id) for navigation to edit screen
 *    - Returns false to snap item back to original position (no dismissal)
 *    - Action fires once per swipe gesture
 *
 * 2. EndToStart (right-to-left swipe): DELETE ACTION
 *    - Sets isDelete = true (triggers both visual animation and delete sequence)
 *    - Returns false to prevent actual dismissal (we handle it manually)
 *
 * 3. Settled: Returns true to allow normal position reset
 *
 * DELETE SEQUENCE (LaunchedEffect with isDelete key):
 * When isDelete becomes true:
 * 1. Wait for visual exit animation to complete (delay(animationDuration))
 * 2. Execute onDelete() - removes person from data store
 * 3. Execute onUndo() - displays snackbar with undo option
 * 4. NOTE: isDelete state is NOT reset here - handled by undo restoration effect
 *
 * UNDO RESTORATION (LaunchedEffect with person.id key):
 * Monitors when person exists in composable but isDelete = true:
 * - This condition indicates the person was restored via undo action
 * - Adds small delay (100ms) to ensure data restoration is complete
 * - Resets isDelete = false to restore normal visual state
 * - Makes the item visible again by triggering AnimatedVisibility recomposition
 *
 * VISUAL ANIMATION:
 * - AnimatedVisibility with visible = !isDelete controls item visibility
 * - Exit animation: shrinkVertically (top-aligned) + fadeOut over animationDuration
 * - SwipeToDismissBox provides colored backgrounds and icons based on swipe direction
 *
 * KEY DESIGN PATTERNS:
 * - Single state variable: isDelete serves dual purpose (visual + logic trigger)
 * - Immediate actions: Edit navigation happens instantly on swipe confirmation
 * - Deferred actions: Delete sequence waits for animation completion
 * - Undo detection: Monitors person existence vs isDelete state mismatch
 * - Animation synchronization: Visual and data operations properly coordinated
 * - State persistence: isDelete remains true until explicit undo restoration
 *
 * ADVANTAGES:
 * - Simplified state management with single boolean
 * - Clear separation between immediate (edit) and deferred (delete) actions
 * - Proper undo handling with state restoration
 * - Smooth visual animations synchronized with data operations
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipePersonListItem(
   person: Person,
   onNavigate: (String) -> Unit,
   onDelete: () -> Unit,
   onUndo: () -> Unit,
   animationDuration: Int = 500,
   content: @Composable () -> Unit
) {
   val tag = "<-SwipePersonListItem"
   var isDelete by remember { mutableStateOf(false) }

   val state = rememberSwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      confirmValueChange = { target ->
         when (target) {
            SwipeToDismissBoxValue.StartToEnd -> {
               logDebug(tag, "Swipe to Edit for ${person.firstName+" "+person.lastName}")
               // Edit: fire once, snap back by returning false
               onNavigate(person.id)
               false
            }
            SwipeToDismissBoxValue.EndToStart -> {
               logDebug(tag, "Swipe to Delete for ${person.firstName+" "+person.lastName}")
               // Delete: start exit animation, then handle delete+undo in effect
               isDelete = true
               false
            }
            SwipeToDismissBoxValue.Settled -> true
         }
      }
   )

   // When exit animation starts, finish the delete flow.
   LaunchedEffect(isDelete) {
      if (isDelete) {
         delay(animationDuration.toLong())
         logDebug(tag, "Delete person ${person.firstName+" "+person.lastName}")
         onDelete()
         logDebug(tag, "Show undo snackbar")
         onUndo() // show snackbar with undo
      }
   }

   // For a proper undo handling
   LaunchedEffect(person.id) {
      if (isDelete) {
         logDebug(tag, "Person ${person.firstName+" "+person.lastName} was restored via undo")
         delay(100) // Allow data restoration to complete
         isDelete = false // Reset visual state when person is restored
      }
   }

   AnimatedVisibility(
      visible = !isDelete,
      exit = shrinkVertically(
         animationSpec = tween(durationMillis = animationDuration),
         shrinkTowards = Alignment.Top
      ) + fadeOut()
   ) {
      SwipeToDismissBox(
         state = state,
         backgroundContent = { SetSwipeBackground(state) },
         enableDismissFromStartToEnd = true,  // edit
         enableDismissFromEndToStart = true,  // delete
         modifier = Modifier.padding(vertical = 4.dp)
      ) {
         content()
      }
   }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SetSwipeBackground(state: SwipeToDismissBoxState) {

   // Determine the properties of the swipe
   val (colorBox, colorIcon, alignment, icon, description, scale) =
      GetSwipeProperties(state)

   Box(
      Modifier.fillMaxSize()
         .background(
            color = colorBox,
            shape = RoundedCornerShape(10.dp)
         )
         .padding(horizontal = 16.dp),
      contentAlignment = alignment
   ) {
      Icon(
         icon,
         contentDescription = description,
         modifier = Modifier.scale(scale),
         tint = colorIcon
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetSwipeProperties(
   state: SwipeToDismissBoxState
): SwipeProperties {

   // Set the color of the box
   // https://hslpicker.com
   val colorBox: Color = when (state.targetValue) {
      SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
      SwipeToDismissBoxValue.StartToEnd -> Color.hsl(120.0f,0.80f,0.30f, 1f) //Color.Green    // move to right
      // move to left  color: dark red
      SwipeToDismissBoxValue.EndToStart -> Color.hsl(0.0f,0.90f,0.40f,1f)//Color.Red      // move to left
   }

   // Set the color of the icon
   val colorIcon: Color = when (state.targetValue) {
      SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurface
      else -> Color.White
   }

   // Set the alignment of the icon
   val alignment: Alignment = when (state.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
      SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
      else -> Alignment.Center
   }

   // Set the icon
   val icon: ImageVector = when (state.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Edit   // left
      SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete // right
      else -> Icons.Outlined.Info
   }

   // Set the description
   val description: String = when (state.dismissDirection) {
      SwipeToDismissBoxValue.StartToEnd -> "Editieren"
      SwipeToDismissBoxValue.EndToStart -> "Löschen"
      else -> "Unknown Action"
   }

   // Set the scale
   val scale = if (state.targetValue == SwipeToDismissBoxValue.Settled)
      1.2f else 1.8f

   return SwipeProperties(
      colorBox, colorIcon, alignment, icon, description, scale)
}

data class SwipeProperties(
   val colorBox: Color,
   val colorIcon: Color,
   val alignment: Alignment,
   val icon: ImageVector,
   val description: String,
   val scale: Float
)



/**
 * SwipePersonListItem Algorithm:
 *
 * OVERVIEW:
 * This composable implements a swipeable list item with delete/undo functionality and edit navigation.
 * It uses a state machine approach to handle different swipe gestures and their corresponding actions.
 *
 * STATE MANAGEMENT:
 * - isRemoved: Boolean - Controls visual state (red background, AnimatedVisibility)
 * - deleteTriggered: Boolean - Triggers the delete action sequence (prevents infinite loops)
 * - hasNavigatedThisSwipe: Boolean - Prevents duplicate navigation calls during single swipe gesture
 *
 * SWIPE GESTURE HANDLING:
 * 1. SwipeToDismissBoxState.confirmValueChange() validates and confirms swipe actions:
 *    - StartToEnd (left-to-right): Edit action
 *      * Calls onNavigate(person.id) only once per swipe gesture
 *      * Sets hasNavigatedThisSwipe = true to prevent duplicates
 *    - EndToStart (right-to-left): Delete action
 *      * Sets isRemoved = true (triggers red background)
 *      * Sets deleteTriggered = true (triggers delete sequence)
 *    - Settled: Resets hasNavigatedThisSwipe when item returns to normal position
 *
 * DELETE SEQUENCE (LaunchedEffect with deleteTriggered key):
 * 1. Wait for visual animation to complete (delay(animationDuration))
 * 2. Call onDelete() - removes person from data store
 * 3. Call onUndo() - displays snackbar with undo option
 * 4. Reset deleteTriggered = false (allows future delete actions)
 * 5. Reset isRemoved = false (prepares for potential undo restoration)
 *
 * UNDO RESTORATION (LaunchedEffect with person.id, isRemoved keys):
 * - Monitors when person exists in list but isRemoved = true
 * - This indicates the person was restored via undo action
 * - Resets isRemoved = false to remove red background and show normal item
 * - Small delay ensures data restoration is complete before visual reset
 *
 * VISUAL ANIMATION:
 * - AnimatedVisibility with !isRemoved controls item visibility
 * - Exit animation: shrinkVertically + fadeOut over animationDuration
 * - SwipeToDismissBox provides background colors/icons based on swipe direction
 *
 * KEY DESIGN PATTERNS:
 * - Separation of concerns: deleteTriggered vs isRemoved prevents LaunchedEffect loops
 * - State reset patterns: hasNavigatedThisSwipe resets on settle, enabling new gestures
 * - Undo pattern: Visual state persists until explicit reset via undo detection
 * - One-shot actions: Each state change triggers actions exactly once
 */

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SwipePersonListItem2(
//   person: Person,
//   onNavigate: (String) -> Unit, // This should trigger viewModel.prepareAndNavigateToDetail
//   onDelete: () -> Unit, // delete action
//   onUndo: () -> Unit,   // Snackbar message and undo action
//   animationDuration: Int = 1000,
//   content: @Composable () -> Unit
//) {
//   val tag = "<-SwipePersonListItem"
//
//   // State to track if the item is removed
//   var isRemoved by remember { mutableStateOf(false) }
//   // Use a separate trigger for the delete action
//   var deleteTriggered by remember { mutableStateOf(false) }
//
//   // This flag ensures onNavigate is called only once per successful swipe-to-edit gesture.
//   // It's reset when the item settles back.
//   var hasNavigatedThisSwipe by remember { mutableStateOf(false) }
//
//   val state: SwipeToDismissBoxState =
//      rememberSwipeToDismissBoxState(
//         initialValue = SwipeToDismissBoxValue.Settled,
//         confirmValueChange = { targetValue: SwipeToDismissBoxValue ->
//            when (targetValue) {
//               SwipeToDismissBoxValue.StartToEnd -> { // Swipe to Edit
//                  if (!hasNavigatedThisSwipe) {
//                     logDebug(tag, "Swipe to Edit confirmed for ${person.id}. Calling onNavigate.")
//                     onNavigate(person.id)
//                     hasNavigatedThisSwipe = true // Mark that navigation has been attempted for this gesture
//                     return@rememberSwipeToDismissBoxState true // Confirm the state change
//                  }
//                  logDebug(tag,"Swipe to Edit: Navigation already attempted this swipe for ${person.id}.")
//                  return@rememberSwipeToDismissBoxState false // Don't re-confirm if already attempted
//               }
//               SwipeToDismissBoxValue.EndToStart -> { // Swipe to Delete
//                  logDebug(tag, "Swipe to Delete confirmed for ${person.id}.")
//                  isRemoved = true
//                  deleteTriggered = true // Mark that delete action has been triggered
//                  return@rememberSwipeToDismissBoxState true // Confirm the state change
//               }
//               SwipeToDismissBoxValue.Settled -> {
//                  // This case is typically handled by the swipe itself resetting,
//                  // but we manage hasNavigatedThisSwipe in a LaunchedEffect based on currentValue.
//                  return@rememberSwipeToDismissBoxState true // Allow settling
//               }
//            }
//         },
//         positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold,
//      )
//
//   // Reset hasNavigatedThisSwipe when the item settles back to its original position.
//   // This is crucial if a navigation attempt was made (hasNavigatedThisSwipe = true)
//   // but the actual navigation was prevented by the ViewModel (e.g., pre-check failed).
//   // Resetting allows a new navigation attempt on a subsequent swipe.
//   LaunchedEffect(state.currentValue) {
//      if (state.currentValue == SwipeToDismissBoxValue.Settled) {
//         if (hasNavigatedThisSwipe) {
//            logDebug(tag, "Item settled. Resetting hasNavigatedThisSwipe for ${person.id}.")
//            hasNavigatedThisSwipe = false
//         }
//      }
//   }
//
//   // Effect for handling the removal process (animation and intent)
//   LaunchedEffect(key1 = deleteTriggered) {
//      if (deleteTriggered) {
//         delay(animationDuration.toLong()) // Wait for visual animation
//
//         logDebug(tag, "Delete person ${person.firstName+" "+person.lastName}")
//         onDelete() // Trigger the delete action
//
//         // Prepare Snackbar for undoing the delete
//         logDebug(tag, "Starting undo feedback and logic.")
//         onUndo()
//
//         // Reset the deleteTriggered state to allow for future swipes
//         deleteTriggered = false
//         isRemoved = false // Reset isRemoved to allow re-adding the item if needed
//
//      }
//   }
//
//   LaunchedEffect(person.id, isRemoved) {
//      // If item is marked as removed but the person exists in the list (restored via undo)
//      if (isRemoved) {
//         logDebug(tag, "Checking if person ${person.id} was restored via undo")
//         // Small delay to ensure the data restoration is complete
//         delay(100)
//         logDebug(tag, "Person ${person.id} restored, resetting isRemoved state.")
//         isRemoved = false
//      }
//   }
//
//   AnimatedVisibility(
//      visible = !isRemoved, // Item is visible unless it's in the process of being removed
//      exit = shrinkVertically(
//         animationSpec = tween(durationMillis = animationDuration),
//         shrinkTowards = Alignment.Top
//      ) + fadeOut(),
//      modifier = Modifier // Add any specific modifiers for AnimatedVisibility if needed
//   ) {
//      SwipeToDismissBox(
//         state = state,
//         backgroundContent = { SetSwipeBackground(state) },
//         modifier = Modifier.padding(vertical = 4.dp),
//         enableDismissFromStartToEnd = true, // Allows swipe from left-to-right (e.g., Edit)
//         enableDismissFromEndToStart = true  // Allows swipe from right-to-left (e.g., Delete)
//      ) {
//         content() // This is the actual UI content of the list item (e.g., PersonCard)
//      }
//   }
//}
