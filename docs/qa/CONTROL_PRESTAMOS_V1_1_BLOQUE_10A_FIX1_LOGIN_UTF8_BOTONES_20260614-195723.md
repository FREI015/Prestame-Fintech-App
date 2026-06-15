# CONTROL PRESTAMOS - BLOQUE 10A FIX1 LOGIN UTF8 Y BOTONES

Fecha:
20260614-195723

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Corregir recodificacion de LoginScreen y auditar botones sin accion.

## Acciones
- LoginScreen corregido con lectura/escritura UTF8 estricta.
- Iconos normalizados sin Get-Content.
- Documentacion V6 saneada.
- Login y docs auditados sin mojibake detectado.
- Botones y callbacks vacios auditados.
- Debug correcto.
- Release correcto.

## Cambios detectados
- a_acento
- e_acento
- enie
- i_acento
- o_acento


## Docs corregidos


## Auditoria de botones sospechosos
.\app\src\main\java\com\controlprestamos\features\payments\domain\receipt\PaymentReceiptFormatter.kt:52: Método: ${payment.method.ifBlank { "No registrado" }}
.\app\src\main\java\com\controlprestamos\features\payments\presentation\CreatePaymentScreen.kt:660: text = "Método: ${method.ifBlank { "Pago" }}",
.\app\src\main\java\com\controlprestamos\features\payments\presentation\PaymentsByLoanScreen.kt:509: text = "Método: ${payment.method.ifBlank { "No registrado" }}",
.\app\src\main\java\com\controlprestamos\features\security\presentation\SecurityScreen.kt:337: enabled = false

## Auditoria de callbacks vacios
.\app\src\main\java\com\controlprestamos\features\clients\presentation\ClientsScreen.kt:120: onOpenClients = { },
.\app\src\main\java\com\controlprestamos\features\dashboard\presentation\DashboardScreen.kt:80: onNotifications = {}
.\app\src\main\java\com\controlprestamos\features\loans\presentation\LoansScreen.kt:143: onOpenLoans = { },

## APK
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_bloque_10a_fix1_signed_20260614-195723.apk

## SHA256
57642C1CB8BE7FF8951A3408F59D26CD98D8363279E047E9E92FC59F72CD776F

## Backup local
_local_archives\v1_1_bloque_10a_fix1_before_20260614-195723

## Resultado
Login saneado. Si hay lineas en botones/callbacks, corregir en Bloque 10B.
