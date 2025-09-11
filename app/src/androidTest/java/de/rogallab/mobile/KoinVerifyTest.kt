package de.rogallab.mobile

import de.rogallab.mobile.di.appModules
import org.junit.Test
import org.koin.test.verify.verify

class KoinVerifyTest {


   @Test
   fun verifyKoinModules() {
      // This test is used to verify that all Koin modules are correctly defined
      // and can be loaded without any issues.
      // It will fail if there are any issues with the Koin setup.
      appModules.verify()
   }
}