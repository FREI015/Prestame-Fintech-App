package com.controlprestamos.features.clients.domain.repository

import com.controlprestamos.features.clients.domain.model.Client
import com.controlprestamos.features.clients.domain.model.ClientStatus
import com.controlprestamos.features.clients.domain.model.CreateClientInput
import com.controlprestamos.features.clients.domain.model.UpdateClientInput

interface ClientRepository {
    fun getClients(): List<Client>

    fun getClientById(clientId: String): Client?

    fun createClient(input: CreateClientInput): Client

    fun updateClient(input: UpdateClientInput): Boolean

    fun updateClientStatus(
        clientId: String,
        status: ClientStatus
    ): Boolean
}

