package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.navigation.PeopleList
import de.rogallab.mobile.ui.navigation.PersonDetail
import de.rogallab.mobile.ui.navigation.PersonInput
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.people.composables.input_detail.PersonDetailScreen
import de.rogallab.mobile.ui.people.composables.input_detail.PersonInputScreen
import de.rogallab.mobile.ui.people.composables.list.PeopleListScreen

@Composable
fun AppNavigation(
   navViewModel: Nav3ViewModel,
   personViewModel: PersonViewModel,
   animationDuration: Int = 1000
) {
   val tag = "<-NavigationRoot"

   // Use the navViewModel's backStack to manage navigation state
   val backStack = navViewModel.backStack

   NavDisplay(
      backStack = backStack,
      onBack = {
         logDebug(tag, "onBack() - Backstack size: ${backStack.size}")
         navViewModel.pop()
      },
      entryDecorators = listOf(
         rememberSavedStateNavEntryDecorator(),
         rememberViewModelStoreNavEntryDecorator(),
         rememberSceneSetupNavEntryDecorator()
      ),
      // Standard Android navigation animations:
      // transitionSpec:    New screen slides in from the right ({ it }),
      //                    old slides out to the left ({ -it }).
      // popTransitionSpec: New screen slides in from the left ({ -it }),
      //                    old slides out to the right ({ it }).
      transitionSpec = {
         slideInHorizontally(
            animationSpec = tween(animationDuration)
         ){ it } togetherWith
         slideOutHorizontally(
            animationSpec = tween(animationDuration)
         ){ -it }
      },
      popTransitionSpec = {
         slideInHorizontally(
            animationSpec = tween(animationDuration)
         ){ -it } togetherWith
            slideOutHorizontally(
               animationSpec = tween(animationDuration)
            ){ it }
      },
      //
      predictivePopTransitionSpec = {
         slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(animationDuration)
         ) togetherWith
            fadeOut(animationSpec = tween(animationDuration*3/2 ))
      },

      entryProvider = entryProvider {
         entry<PeopleList> { key ->
            PeopleListScreen(
               viewModel = personViewModel,
               onNavigatePersonInput = {
                  navViewModel.push(PersonInput)
               },
               onNavigatePersonDetail = { personId ->
                  navViewModel.push(PersonDetail(personId))
               }
            )
         }
         entry<PersonInput> {
            PersonInputScreen(
               viewModel = personViewModel,
               onNavigateReverse =  navViewModel::pop
            )
         }
         entry<PersonDetail> { key ->
            PersonDetailScreen(
               id = key.id,
               viewModel = personViewModel,
               onNavigateReverse = navViewModel::pop
            )
         }
      },
   )
}