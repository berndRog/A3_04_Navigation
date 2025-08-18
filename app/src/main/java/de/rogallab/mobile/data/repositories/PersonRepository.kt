package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.IDataStore
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person

class PersonRepository(
   private val _dataStore: IDataStore
): IPersonRepository {

   override fun getAll(): ResultData<List<Person>> =
      try {
         //throw Exception("Failed to fetch all people")
         ResultData.Success(_dataStore.selectAll())
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in getAll()")
      }

   override fun getWhere(
      predicate: (Person) -> Boolean
   ): ResultData<List<Person>> =
      try {
         ResultData.Success(_dataStore.selectWhere(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in getWhere()")
      }

   override fun getById(id: String): ResultData<Person?> =
      try {
         //throw Exception("Failed to get a person by id")
         ResultData.Success(_dataStore.findById(id))
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in getById()")
      }

   override fun getBy(
      predicate: (Person) -> Boolean
   ): ResultData<Person?> =
      try {
         ResultData.Success(_dataStore.findBy(predicate))
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in getBy()")
      }

   override fun create(person: Person): ResultData<Unit> =
      try {
         _dataStore.insert(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in create()")
      }

   override fun update(person: Person): ResultData<Unit> =
      try {
         _dataStore.update(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in update()")
      }

   override fun remove(person: Person): ResultData<Unit> =
      try {
         _dataStore.delete(person)
         ResultData.Success(Unit)
      } catch (t: Throwable) {
         ResultData.Error(t.message ?: "Error in remove()")
      }
}