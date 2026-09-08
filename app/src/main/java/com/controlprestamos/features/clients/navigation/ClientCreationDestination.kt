package com.controlprestamos.features.clients.navigation


sealed class ClientCreationDestination {


    data object CreateClient :
        ClientCreationDestination()


    data object ClientList :
        ClientCreationDestination()


    data object ClientDetail :
        ClientCreationDestination()


}

