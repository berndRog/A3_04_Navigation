package de.rogallab.mobile.di

import androidx.navigation3.runtime.NavKey
import de.rogallab.mobile.data.local.Seed
import de.rogallab.mobile.data.local.datastore.DataStore
import de.rogallab.mobile.data.local.mediastore.MediaStore
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.IMediaStore
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.usecases.photos.CaptureImageUseCase
import de.rogallab.mobile.domain.usecases.photos.SelectGalleryImageUseCase
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.navigation.INavHandler
import de.rogallab.mobile.ui.navigation.Nav3ViewModel
import de.rogallab.mobile.ui.people.PersonValidator
import de.rogallab.mobile.ui.people.PersonViewModel
import de.rogallab.mobile.ui.photos.PhotoViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val definedModules: Module = module {
    val tag = "<-definedModules"

    // Provide IO dispatcher
    single<CoroutineDispatcher>(named("ioDispatcher")) {
        Dispatchers.IO
    }

    // data modules
    logInfo(tag, "single    -> Seed")
    single<Seed> {
        Seed(
           _context = androidContext(),
           _mediaStore = get<IMediaStore>(),
        )
    }

    logInfo(tag, "single    -> DataStore")
    single {
        DataStore(
           _context = androidContext(),
           _seed = get<Seed>(),
        )
    }

    logInfo(tag, "single    -> MediaStore: IMediaStore")
    single<IMediaStore> {
        MediaStore(
           _context = androidContext(),
           _ioDispatcher = get(named("ioDispatcher"))
        )
    }


    logInfo(tag, "single    -> PersonRepository: IPersonRepository")
    single<IPersonRepository> {
        PersonRepository(_dataStore = get<DataStore>())
    }

    // domain modules
    // Photo Use Cases
    logInfo(tag, "factory   -> CapturePhotoUseCase")
    factory {
        CaptureImageUseCase(_mediaStore = get<IMediaStore>())
    }
    logInfo(tag, "factory   -> SelectImageFromGallery")
    factory {
        SelectGalleryImageUseCase(
           _mediaStore = get<IMediaStore>()
        )
    }

    // ui modules
    logInfo(tag, "single    -> PersonValidator")
    single<PersonValidator> {
        PersonValidator(_context = androidContext())
    }



    logInfo(tag, "viewModel -> Nav3ViewModel as INavHandler (with params)")
    viewModel { (startDestination: NavKey) ->  // Parameter for startDestination
        Nav3ViewModel(startDestination = startDestination)
    } bind INavHandler::class
    
    logInfo(tag, "viewModel -> PersonViewModel")
    viewModel { (navHandler: INavHandler) ->
        PersonViewModel(
           _repository = get<IPersonRepository>(),
           _navHandler = navHandler,
           _validator = get<PersonValidator>()
        )
    }
    logInfo(tag, "viewModel -> PhotoViewModel")
    viewModel { (navHandler: INavHandler) ->
        PhotoViewModel(
           _captureImageUseCase = get<CaptureImageUseCase>(),
           _selectImageFromGalleryUseCase = get<SelectGalleryImageUseCase>(),
           _navHandler = navHandler
        )
    }

}

val appModules: Module = module {

    try {
        val testedModules = definedModules
        requireNotNull(testedModules) {
            "definedModules is null"
        }
        includes(
           testedModules,
           //useCaseModules
        )
    } catch (e: Exception) {
        logInfo("<-appModules", e.message!!)
    }
}
