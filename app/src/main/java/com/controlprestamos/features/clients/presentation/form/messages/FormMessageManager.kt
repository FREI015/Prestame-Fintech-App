package com.controlprestamos.features.clients.presentation.form.messages


class FormMessageManager {


private val queue =
mutableListOf<FormMessage>()


fun add(message:FormMessage){

queue.add(message)

}


fun clear(){

queue.clear()

}


fun messages():List<FormMessage>{

return queue

}


}

