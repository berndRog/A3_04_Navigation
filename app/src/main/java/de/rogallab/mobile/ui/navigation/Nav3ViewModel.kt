package de.rogallab.mobile.ui.navigation // Assuming package from other files
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import de.rogallab.mobile.domain.utilities.logDebug

// ViewModel for managing a single navigation back stack.
// Suitable for simple navigation flows with one top-Levele
class Nav3ViewModel(
   val startDestination: NavKey = PeopleList
) : ViewModel(), INavHandler {

   private val _tag: String = "<-Nav3ViewModel"

   // Internal mutable backstack. Initialized with the startDestination.
   private val _navBackStack: SnapshotStateList<NavKey> = mutableStateListOf(startDestination)

   // Public read-only access to backstack
   val backStack: List<NavKey>
      get() = _navBackStack

   // Interface INavHandler implementation
   // ------------------------------------
   // Pushes a new destination [NavKey] onto the backstack.
   override fun push(destination: NavKey) {
      logDebug(_tag, "push: $destination")
      _navBackStack.add(destination)
   }

   // Pops the current top destination from the backstack.
   override fun pop() {
      if (_navBackStack.size > 1) { // Prevent removing the root/start destination
         val removedKey = _navBackStack.removeLastOrNull()
         logDebug(_tag, "pop: $removedKey")
      } else {
         logDebug(_tag, "pop: Attempted to remove root destination. No action taken.")
      }
   }

   // Pops the current backstack and replaces it with a new stack containing only the [rootDestination].
   override fun popToRootAndNavigate(rootDestination: NavKey) {
      logDebug(_tag, "popToRootAndNavigate to: $rootDestination")
      replaceStack(rootDestination)
   }

   // Internal methods
   // ------------------------------------
   // Replaces the entire backstack with a new set of destinations [NavKey].
   // If the provided keys array is empty, the stack will be reset to the startDestination.
   private fun replaceStack(vararg keys: NavKey) {
      logDebug(_tag, "replaceStack with ${keys.size} keys: ${keys.joinToString()}")
      _navBackStack.clear()
      if (keys.isNotEmpty()) {
         _navBackStack.addAll(keys)
      } else {
         _navBackStack.add(startDestination) // Ensure stack is never truly empty
         logDebug(_tag, "replaceStack: Provided keys were empty, reset to $startDestination")
      }
   }
}