package eti.lucasgomes.makalu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eti.lucasgomes.makalu.navigation.Destination
import eti.lucasgomes.makalu.navigation.NavOptions
import eti.lucasgomes.makalu.navigation.Navigator
import eti.lucasgomes.makalu.navigation.PopUpToOptions
import kotlinx.coroutines.launch

class HomeViewModel(private val navigator: Navigator) : ViewModel() {

    fun goToLogin() {
        viewModelScope.launch {
            navigator.navigate(
                Destination.AuthGraph,
                NavOptions(
                    popUpTo = PopUpToOptions(
                        destination = Destination.HomeGraph,
                        inclusive = true
                    )
                )
            )
        }
    }
}