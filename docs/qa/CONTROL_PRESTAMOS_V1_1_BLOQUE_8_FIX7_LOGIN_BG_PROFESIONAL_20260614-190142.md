# CONTROL PRESTAMOS - BLOQUE 8-FIX7 LOGIN BG PROFESIONAL V1.1

Fecha:
20260614-190142

Rama:
feature/v1.1-premium-dashboard-security

## Problema
Después de limpiar lenguaje premium, LoginScreen quedó apuntando a un recurso inexistente:

login_bg_profesional

## Corrección aplicada
- Se renombró el recurso login_bg_premium a login_bg_profesional cuando existía.
- Se ajustó LoginScreen para apuntar a un recurso existente.
- Se renombró PREMIUM_GOLD a PROFESSIONAL_GOLD.
- Se verificó ausencia de Premium/premium en Kotlin.
- Se compiló debug correctamente.

## Archivos Kotlin tocados
app/src/main/java/com/controlprestamos/features/auth/presentation/LoginScreen.kt
app/src/main/java/com/controlprestamos/features/preferences/data/LocalPreferencesRepository.kt
app/src/main/java/com/controlprestamos/features/preferences/presentation/PreferencesScreen.kt

## Recursos renombrados
C:\Users\freil\Desktop\ControlPrestamosApp_Profesional\app\src\main\res\drawable-nodpi\login_bg_profesional.jpg
