package com.golde.cowries.Injection.Component

import com.golde.cowries.Injection.Module.NetworkModule
import com.golde.cowries.UI.ViewModels.ConversionViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 */
@Singleton
@Component(modules = [(NetworkModule::class)])
interface ViewModelInjector {
    /**
     * Injects required dependencies into the specified ConversionViewModel.
     * @param ConversionViewModel ConversionViewModel in which to inject the dependencies
     */
    fun inject(ConversionViewModel: ConversionViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector

        fun networkModule(networkModule: NetworkModule): Builder
    }
}