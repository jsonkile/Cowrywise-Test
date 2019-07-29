package com.golde.cowrywise.UI.ViewModels

import androidx.lifecycle.ViewModel
import com.golde.cowrywise.Injection.Component.DaggerViewModelInjector
import com.golde.cowrywise.Injection.Component.ViewModelInjector
import com.golde.cowrywise.Injection.Module.NetworkModule


abstract class BaseViewModel: ViewModel(){
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule)
        .build()

    init {
        inject()
    }

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is ConversionViewModel -> injector.inject(this)
        }
    }
}