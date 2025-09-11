package de.rogallab.mobile.ui

//import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.navigation.PeopleList
import de.rogallab.mobile.ui.navigation.composables.AppNavigation
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : BaseActivity(TAG) {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      // Activity-scoped ViewModels, DI by Koin
      val navViewModel: Nav3ViewModel =
         getViewModel { parametersOf(PeopleList) }
      val personViewModel: PersonViewModel =
         getViewModel { parametersOf(navViewModel as INavHandler) }
      logDebug(TAG, "navViewModel=${System.identityHashCode(navViewModel)}")
      logDebug(TAG, "peopleViewModel=${System.identityHashCode(personViewModel)}")

      enableEdgeToEdge()

      setContent {
         AppTheme {
            AppNavigation(
               // startDestination = PeopleList
               personViewModel = personViewModel,
               navViewModel = navViewModel
            )
         }
      }

   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}
