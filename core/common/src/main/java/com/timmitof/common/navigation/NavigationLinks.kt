package com.timmitof.common.navigation

import kotlinx.serialization.Serializable

sealed class NavigationLinks {

    @Serializable
    data object Home: NavigationLinks()
}