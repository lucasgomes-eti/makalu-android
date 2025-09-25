package eti.lucasgomes.makalu.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.navigation.Destination
import eti.lucasgomes.makalu.navigation.NavOptions
import eti.lucasgomes.makalu.navigation.Navigator
import eti.lucasgomes.makalu.navigation.PopUpToOptions
import kotlinx.coroutines.launch

class RegistrationViewModel(private val navigator: Navigator) : ViewModel() {

    fun goToHome() {
        viewModelScope.launch {
            navigator.navigate(
                Destination.Graph.Home,
                NavOptions(popUpTo = PopUpToOptions(Destination.Graph.Auth, inclusive = true))
            )
        }
    }
}