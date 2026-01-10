package eu.aaltra.kmp.mdnspoc.screens.detail

import androidx.lifecycle.ViewModel
import eu.aaltra.kmp.mdnspoc.data.MuseumObject
import eu.aaltra.kmp.mdnspoc.data.MuseumRepository
import kotlinx.coroutines.flow.Flow

class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {
    fun getObject(objectId: Int): Flow<MuseumObject?> =
        museumRepository.getObjectById(objectId)
}
