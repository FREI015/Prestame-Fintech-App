package com.controlprestamos.features.clients.presentation.form.viewmodel


sealed class ClientFormEvent {



    // =========================
    // IDENTIDAD
    // =========================


    data class NameChanged(
        val value:String
    ):ClientFormEvent()



    data class LastNameChanged(
        val value:String
    ):ClientFormEvent()



    data class DocumentChanged(
        val value:String
    ):ClientFormEvent()



    data class DocumentTypeChanged(
        val value:String
    ):ClientFormEvent()



    data class BirthDateChanged(
        val value:String
    ):ClientFormEvent()



    // =========================
    // CONTACTO
    // =========================


    data class PhoneChanged(
        val value:String
    ):ClientFormEvent()



    data class EmailChanged(
        val value:String
    ):ClientFormEvent()



    data class WhatsAppChanged(
        val value:String
    ):ClientFormEvent()



    // =========================
    // DIRECCION
    // =========================


    data class AddressChanged(
        val value:String
    ):ClientFormEvent()



    data class CityChanged(
        val value:String
    ):ClientFormEvent()



    data class StateChanged(
        val value:String
    ):ClientFormEvent()



    // =========================
    // EMPLEO
    // =========================


    data class JobChanged(
        val value:String
    ):ClientFormEvent()



    data class CompanyChanged(
        val value:String
    ):ClientFormEvent()



    data class IncomeChanged(
        val value:String
    ):ClientFormEvent()



    // =========================
    // NAVEGACION WIZARD
    // =========================


    object NextStep:ClientFormEvent()



    object PreviousStep:ClientFormEvent()



    // =========================
    // FINALIZACION
    // =========================


    object SaveClient:ClientFormEvent()



    object ClearForm:ClientFormEvent()



}
