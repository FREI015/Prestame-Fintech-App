package com.controlprestamos.features.clients.navigation


import androidx.compose.runtime.Composable

import com.controlprestamos.features.clients.presentation.creation.ClientCreationEntryPoint



@Composable
fun ClientNavigationCoordinator(

    destination: ClientCreationDestination,

    onNavigateBack: () -> Unit,

    onClientCreated: (String) -> Unit

){


    when(destination){


        ClientCreationDestination.CreateClient -> {


            ClientCreationEntryPoint(

                onNavigateBack = onNavigateBack,

                onClientCreated = onClientCreated

            )


        }



        ClientCreationDestination.ClientList -> {


        }



        ClientCreationDestination.ClientDetail -> {


        }



    }


}

