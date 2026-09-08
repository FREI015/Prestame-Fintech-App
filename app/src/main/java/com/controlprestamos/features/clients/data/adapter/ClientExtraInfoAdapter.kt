package com.controlprestamos.features.clients.data.adapter

import com.controlprestamos.features.clients.domain.profile.ClientProfessionalProfile


object ClientExtraInfoAdapter {


    fun convert(
        clientId:String
    ):ClientProfessionalProfile {


        return ClientProfessionalProfile(
            clientId = clientId
        )

    }

}

