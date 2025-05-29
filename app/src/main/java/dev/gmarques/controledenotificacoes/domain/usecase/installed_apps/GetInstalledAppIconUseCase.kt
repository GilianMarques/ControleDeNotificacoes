package dev.gmarques.controledenotificacoes.domain.usecase.installed_apps

import android.graphics.drawable.Drawable
import dev.gmarques.controledenotificacoes.domain.data.repository.InstalledAppRepository
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em terça-feira, 07 de maio de 2025 as 10:34.
 */
class GetInstalledAppIconUseCase @Inject constructor(private val repository: InstalledAppRepository) {

    suspend operator fun invoke(pkg: String): Drawable? {
        return repository.getDrawable(pkg)
    }
}