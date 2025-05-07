package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import android.graphics.drawable.Drawable
import dev.gmarques.controledenotificacoes.domain.repository.AppRepository
import dev.gmarques.controledenotificacoes.presentation.model.InstalledApp
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 07 de maio de 2025 as 10:34.
 */
class GetInstalledAppIconUseCase @Inject constructor(private val repository: AppRepository) {

    suspend operator fun invoke(pkg: String): Drawable? {
        return repository.getDrawable(pkg)
    }
}