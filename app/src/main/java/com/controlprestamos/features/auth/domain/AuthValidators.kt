package com.controlprestamos.features.auth.domain

object AuthValidators {

    fun validateEmail(email: String): String? {
        val normalizedEmail = email.trim()

        return when {
            normalizedEmail.isBlank() -> "Ingresa tu correo electrónico."
            !normalizedEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> {
                "Ingresa un correo electrónico válido."
            }
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Ingresa tu contraseña."
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            password.none { it.isLetter() } -> "La contraseña debe incluir al menos una letra."
            password.none { it.isDigit() } -> "La contraseña debe incluir al menos un número."
            else -> null
        }
    }

    fun validateRegister(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            firstName.trim().isBlank() -> "Ingresa tu nombre."
            lastName.trim().isBlank() -> "Ingresa tu apellido."
            validateEmail(email) != null -> validateEmail(email)
            validatePassword(password) != null -> validatePassword(password)
            password != confirmPassword -> "Las contraseñas no coinciden."
            else -> null
        }
    }

    fun validateLogin(
        email: String,
        password: String
    ): String? {
        return validateEmail(email) ?: when {
            password.isBlank() -> "Ingresa tu contraseña."
            else -> null
        }
    }

    fun validatePin(pin: String): String? {
        return when {
            pin.isBlank() -> "Ingresa tu PIN."
            pin.length !in 4..6 -> "El PIN debe tener entre 4 y 6 números."
            pin.any { !it.isDigit() } -> "El PIN solo debe contener números."
            else -> null
        }
    }
}
