package com.controlprestamos.features.clients.navigation


import com.controlprestamos.features.clients.config.ClientFeatureFlags


enum class ClientFlow {

    OLD_FLOW,

    NEW_FLOW

}



object ClientFlowRouter {


    fun resolve():

    ClientFlow {


        return if(
            ClientFeatureFlags.CLIENT_FORM_V2_ENABLED
        ){

            ClientFlow.NEW_FLOW

        }else{


            ClientFlow.OLD_FLOW

        }


    }


}

