# CONTROL PRESTAMOS - 30N REGISTRAR PAGO TIPO REFERENCIA V1.1

Fecha:
20260614-155234

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Rediseñar Registrar Pago como acción financiera clara, segura y profesional.

## Archivo modificado
app\src\main\java\com\controlprestamos\features\payments\presentation\CreatePaymentScreen.kt

## Cambios aplicados
- Se mantuvo la firma compatible:
  - loanId
  - onNavigateBack
  - onPaymentCreated
- Se mantuvo el flujo actual basado en loanId.
- Se mantuvo CreatePaymentInput.
- Se mantuvo PaymentFormValidator.
- Se mantuvo fecha real de pago con paymentDateMillis.
- Se usa moneda desde preferencias.
- Se muestra resumen del cobro.
- Se muestra cliente.
- Se muestra préstamo.
- Se muestra estado.
- Se muestra total esperado.
- Se muestra cobrado.
- Se muestra saldo pendiente.
- Se muestra próxima cuota.
- Se agregaron montos sugeridos:
  - cuota próxima
  - saldo completo
- Se mejoró formulario:
  - monto a registrar
  - método de pago
  - referencia
  - fecha real
  - notas
- Se agregó resumen previo:
  - monto
  - método
  - fecha
  - pendiente después del pago
- Se bloquean pagos si:
  - préstamo no existe
  - cliente no existe
  - cliente está archivado
  - préstamo está cancelado
  - préstamo está pagado/saldado
- Al registrar pago:
  - se crea el pago
  - se reconstruyen cuotas con rebuildInstallmentsForLoan
  - se llama onPaymentCreated

## Líneas actuales de CreatePaymentScreen.kt
753

## Backup local
_local_archives\v1_1_register_payment_reference_redesign_before_20260614-155234

## Pruebas manuales recomendadas
1. Abrir Pagos.
2. Tocar una cuota.
3. Confirmar que abre Registrar pago.
4. Ver cliente, préstamo, saldo y próxima cuota.
5. Usar monto sugerido de cuota.
6. Cambiar método de pago.
7. Cambiar fecha real de pago.
8. Registrar pago.
9. Confirmar regreso correcto.
10. Revisar Pagos / Historial.
11. Revisar Detalle préstamo.
