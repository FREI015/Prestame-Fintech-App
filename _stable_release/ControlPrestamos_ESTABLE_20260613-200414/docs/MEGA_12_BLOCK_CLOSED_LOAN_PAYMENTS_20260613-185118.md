# MEGA 12 - Bloquear pagos a préstamos cerrados

Fecha:
20260613-185118

Objetivo:
Evitar pagos nuevos sobre préstamos que ya no deben recibir movimientos operativos.

Archivo modificado:
- app\src\main\java\com\controlprestamos\features\payments\presentation\CreatePaymentScreen.kt

Backup:
_migration_backup\mega_12_block_closed_loan_payments_before_20260613-185118

Cambios:
- Si el préstamo está CANCELLED, se bloquea registro de pago.
- Si el préstamo está PAID, se bloquea registro de pago.
- Se muestra pantalla informativa con botón Volver.
- Se conserva historial financiero.
- Se protege incluso si el usuario llega por ruta directa a CreatePayment.

Reglas:
- Préstamo activo puede recibir pagos.
- Préstamo pagado no recibe pagos nuevos.
- Préstamo cancelado no recibe pagos nuevos.
- Historial no se borra.
