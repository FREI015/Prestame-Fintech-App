# CONTROL PRESTAMOS - FIX DETALLE PRESTAMO TIPO REFERENCIA V1.1

Fecha:
20260614-120719

Rama:
feature/v1.1-premium-dashboard-security

## Problema detectado
El bloque 30L rediseñó LoanDetailScreen.kt, pero falló el build debug antes del commit.

## Error real
AppNavGraph.kt todavía llama a LoanDetailScreen con:

onOpenPayments

## Causa
El rediseño de LoanDetailScreen.kt reemplazó la firma anterior y eliminó ese parámetro usado por la navegación.

## Corrección aplicada
- Se restauró el parámetro onOpenPayments en LoanDetailScreen.
- Se dejó como alias compatible de onOpenPaymentsByLoan.
- Se conectó el botón Pagos al alias compatible.
- Se conserva el rediseño visual del Detalle de Préstamo.
- Se conserva el header, resumen financiero, plan de cuotas, pagos recientes y barras de progreso.

## Resultado esperado
- Build debug correcto.
- Commit correcto.
- Push correcto.
- Release firmado correcto.
- APK instalado en teléfono.

## Líneas actuales de LoanDetailScreen.kt
1010

## Backup local
_local_archives\v1_1_loan_detail_reference_redesign_fix_before_20260614-120719
