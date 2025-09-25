package eti.lucasgomes.makalu.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.shared.navigation.Destination
import eti.lucasgomes.makalu.shared.navigation.NavOptions
import eti.lucasgomes.makalu.shared.navigation.Navigator
import eti.lucasgomes.makalu.shared.navigation.PopUpToOptions
import kotlinx.coroutines.launch

class HomeViewModel(private val navigator: Navigator) : ViewModel() {

    fun goToLogin() {
        viewModelScope.launch {
            navigator.navigate(
                Destination.Graph.Auth,
                NavOptions(
                    popUpTo = PopUpToOptions(
                        destination = Destination.Graph.Home,
                        inclusive = true
                    )
                )
            )
        }
    }
}