package com.controlprestamos.features.clients.presentation.form.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClientFormViewModel : ViewModel() {

    private val _state =
        MutableStateFlow(
            ClientFormUiState()
        )

    val state: StateFlow<ClientFormUiState> =
        _state.asStateFlow()

    fun onEvent(event: ClientFormEvent) {

        when (event) {

            is ClientFormEvent.NameChanged -> {
                updateState {
                    copy(
                        firstName = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.LastNameChanged -> {
                updateState {
                    copy(
                        lastName = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.DocumentChanged -> {
                updateState {
                    copy(
                        document = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.DocumentTypeChanged -> {
                updateState {
                    copy(
                        documentType = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.BirthDateChanged -> {
                updateState {
                    copy(
                        birthDate = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.PhoneChanged -> {
                updateState {
                    copy(
                        phone = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.EmailChanged -> {
                updateState {
                    copy(
                        email = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.WhatsAppChanged -> {
                updateState {
                    copy(
                        whatsapp = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.AddressChanged -> {
                updateState {
                    copy(
                        address = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.CityChanged -> {
                updateState {
                    copy(
                        city = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.StateChanged -> {
                updateState {
                    copy(
                        stateProvince = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.JobChanged -> {
                updateState {
                    copy(
                        occupation = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.CompanyChanged -> {
                updateState {
                    copy(
                        company = event.value,
                        errors = emptyList()
                    )
                }
            }

            is ClientFormEvent.IncomeChanged -> {
                updateState {
                    copy(
                        income = event.value,
                        errors = emptyList()
                    )
                }
            }

            ClientFormEvent.NextStep -> {

                val errors =
                    validateCurrentStep()

                if (errors.isEmpty()) {

                    updateState {
                        copy(
                            currentStep =
                                (currentStep + 1)
                                    .coerceAtMost(totalSteps),
                            errors = emptyList(),
                            canContinue = false,
                            message = null
                        )
                    }

                } else {

                    updateState {
                        copy(
                            errors = errors,
                            canContinue = false
                        )
                    }

                }
            }

            ClientFormEvent.PreviousStep -> {

                updateState {
                    copy(
                        currentStep =
                            (currentStep - 1)
                                .coerceAtLeast(1),
                        errors = emptyList(),
                        message = null
                    )
                }
            }

            ClientFormEvent.SaveClient -> {

                val errors =
                    validateCurrentStep()

                if (errors.isEmpty()) {

                    updateState {
                        copy(
                            saving = true,
                            errors = emptyList(),
                            message = "Preparando registro del cliente..."
                        )
                    }

                } else {

                    updateState {
                        copy(
                            errors = errors,
                            saving = false
                        )
                    }
                }
            }

            ClientFormEvent.ClearForm -> {

                _state.value =
                    ClientFormUiState()
            }
        }
    }

    private fun updateState(
        transform: ClientFormUiState.() -> ClientFormUiState
    ) {

        _state.value =
            _state.value.transform()
    }

    private fun validateCurrentStep(): List<String> {

        val state =
            _state.value

        val errors =
            mutableListOf<String>()

        when (state.currentStep) {

            1 -> {

                if (state.firstName.trim().isBlank()) {
                    errors.add(
                        "Nombre requerido"
                    )
                }

                if (state.lastName.trim().isBlank()) {
                    errors.add(
                        "Apellido requerido"
                    )
                }

                if (state.document.trim().isBlank()) {
                    errors.add(
                        "Documento requerido"
                    )
                }

                if (
                    state.document.isNotBlank() &&
                    state.document.trim().length < 5
                ) {
                    errors.add(
                        "Documento inválido"
                    )
                }
            }

            2 -> {
                // Validación de contacto pendiente
            }

            3 -> {
                // Validación de dirección pendiente
            }

            4 -> {
                // Validación de empleo pendiente
            }

            5 -> {
                // Validación financiera pendiente
            }

            6 -> {
                // Validación de referencias pendiente
            }

            7 -> {
                // Validación de documentos pendiente
            }

            8 -> {
                // Validación de consentimiento pendiente
            }
        }

        return errors
    }
}
