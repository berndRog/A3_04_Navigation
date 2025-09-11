package de.rogallab.mobile.ui.people

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.as8
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.newUuid
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.PeopleList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonViewModel(
   private val _repository: IPersonRepository,
   private val _navHandler: INavHandler,
   private val _validator: PersonValidator
): BaseViewModel(_navHandler, TAG) {

   init { logDebug(TAG, "init instance=${System.identityHashCode(this)}") }


   // StateFlow for UI State People
   private val _peopleUiStateFlow: MutableStateFlow<PeopleUiState> =
      MutableStateFlow(PeopleUiState())
   val peopleUiStateFlow: StateFlow<PeopleUiState> =
      _peopleUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessPeopleIntent(intent: PeopleIntent) {
      when (intent) {
         is PeopleIntent.FetchPeople -> fetch()
      }
   }

   // StateFlow for UIState Person
   private val _personUiStateFlow: MutableStateFlow<PersonUiState> =
      MutableStateFlow(PersonUiState())
   val personUiStateFlow: StateFlow<PersonUiState> =
      _personUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessPersonIntent(intent: PersonIntent) {
      when (intent) {
         is PersonIntent.FirstNameChange -> onFirstNameChange(intent.firstName)
         is PersonIntent.LastNameChange -> onLastNameChange(intent.lastName)
         is PersonIntent.EmailChange -> onEmailChange(intent.email)
         is PersonIntent.PhoneChange -> onPhoneChange(intent.phone)

         is PersonIntent.Clear -> clearState()
         is PersonIntent.FetchById -> fetchById(intent.id)
         is PersonIntent.Create -> create()
         is PersonIntent.Update -> update()
         is PersonIntent.Remove -> remove(intent.person)
         is PersonIntent.Undo -> undoRemove()
         is PersonIntent.Restored -> Restored()

         is PersonIntent.HandleUndoEvent -> handleUndoEvent(intent.errorState)
      }
   }

   private fun onFirstNameChange(firstName: String) {
      val trimmed = firstName.trim()
      if (trimmed == _personUiStateFlow.value.person.firstName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(firstName = trimmed))
      }
   }
   private fun onLastNameChange(lastName: String) {
      val trimmed = lastName.trim()
      if (trimmed == _personUiStateFlow.value.person.lastName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(lastName = trimmed))
      }
   }
   private fun onEmailChange(email: String?) {
      val trimmed = email?.trim()
      if (trimmed == _personUiStateFlow.value.person.email) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(email = trimmed))
      }
   }
   private fun onPhoneChange(phone: String?) {
      val trimmed = phone?.trim()
      if(trimmed == _personUiStateFlow.value.person.phone) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(phone = trimmed))
      }
   }

   private fun fetchById(id: String) {
      logDebug(TAG, "fetchById() $id")
      _repository.findById(id)
         .onSuccess { person ->
            if(person != null) {
               logDebug(TAG, "fetchPersonById")
               _personUiStateFlow.update { it: PersonUiState ->
                  it.copy(person = person)  // new UiState
               }
            } else {
               handleErrorEvent(
                  message = "Person not found",
                  withDismissAction = true,
                  navKey = PeopleList, // navigate to PeopleListScreen
               )
            }
         }
         .onFailure { t ->
            handleErrorEvent(
               throwable = t,
               withDismissAction = true,
               navKey = PeopleList, // navigate to PeopleListScreen
            )
         }
   }


   private fun clearState() {
      _personUiStateFlow.update { it.copy(person = Person(id = newUuid() )) }
   }

   private fun create() {
      logDebug(TAG, "createPerson")
      _repository.create(_personUiStateFlow.value.person)
         .onSuccess { fetch() } // reread all people
         .onFailure { t -> handleErrorEvent(t) }
   }

   private fun update() {
      logDebug(TAG, "updatePerson()")
      _repository.update(_personUiStateFlow.value.person)
         .onSuccess { fetch() } // reread all people
         .onFailure { t -> handleErrorEvent(t) }
   }

   private var _removedPerson: Person? = null
   private var _removedPersonIndex: Int = -1 // Store only the index

   private fun remove(person: Person) {
      logDebug(TAG, "removePerson()")

      val currentList = _peopleUiStateFlow.value.people
      val index = currentList.indexOf(person)
      if (index == -1) return

      _removedPerson = person
      _removedPersonIndex = index

      // Immediately update UI
      val updatedList = currentList.toMutableList().also { it.removeAt(index) }
      _peopleUiStateFlow.update { it: PeopleUiState ->
         it.copy(people = updatedList)
      }

      _repository.remove(person)
         //.onSuccess { fetch() } // reread all people
         .onFailure { t -> handleErrorEvent(t) }
   }

   private fun undoRemove() {

      val personToRestore = _removedPerson ?: return
      val indexToRestore = _removedPersonIndex
      if (indexToRestore == -1) return
      logDebug(TAG, "undoRemovePerson: ${personToRestore.id}")
      val currentList = _peopleUiStateFlow.value.people.toMutableList()

      if (currentList.any { it.id == personToRestore.id }) return

      currentList.add(indexToRestore.coerceAtMost(currentList.size), personToRestore)
      _peopleUiStateFlow.update {
         it.copy(
            people = currentList,
            restoredPersonId = personToRestore.id
         )
      }
      // Add person back to repository in background
      _repository.create(personToRestore)
         .onFailure { t -> handleErrorEvent(t)
      }
      _removedPerson = null
      _removedPersonIndex    = -1
   }

   private fun Restored() {
      logDebug(TAG, "onRestored() acknowledged by UI")
      // The UI has finished scrolling, so we clear the ID
      _peopleUiStateFlow.update {
         it.copy(restoredPersonId = null)
      }
   }


   // validate all input fields after user finished input into the form
   fun validate(): Boolean {
      val person = _personUiStateFlow.value.person

      // only one erromssage can be processed at a time
      if(!validateAndLogError(_validator.validateFirstName(person.firstName)))
         return false
      if(!validateAndLogError(_validator.validateLastName(person.lastName)))
         return false
      if(!validateAndLogError(_validator.validateEmail(person.email)))
         return false
      if(!validateAndLogError(_validator.validatePhone(person.phone)))
         return false
      return true // all fields are valid
   }

   private fun validateAndLogError(validationResult: Pair<Boolean, String>): Boolean {
      val (error, message) = validationResult
      if (error) {
         handleErrorEvent(
            message = message,
            withDismissAction = true,
            onDismissed = { /* no op, Unit returned */ },
            duration = SnackbarDuration.Long,
            navKey = null, // stay on the screen
         )
         return false
      }
      return true
   }
   // endregion



   // read all people from repository
   private fun fetch() {
      logDebug(TAG, "fetch")
      _peopleUiStateFlow.update { it: PeopleUiState ->
         it.copy(isLoading = true)
      }
      _repository.getAllSortedBy { it.firstName }
         .onSuccess { people ->
            _peopleUiStateFlow.update { it: PeopleUiState ->
               val snapshot = people.toList()
               logDebug(TAG, "apply PeopleUiState: isLoading=false size=${snapshot.size}")
               it.copy(
                  isLoading = false,
                  people = snapshot //new instance of a list
               )
            }
         }
         .onFailure { t -> handleErrorEvent(t) }
   }
   // endregion

   companion object {
      private const val TAG = "<-PersonViewModel"
   }
}