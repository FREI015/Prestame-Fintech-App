# CONTROL PRESTAMOS - MEGA BLOQUE 3-FIX4 NORMALIZAR SAFELOAN V1.1

Fecha:
20260614-170127

Rama:
feature/v1.1-premium-dashboard-security

## Problema detectado
El Mega Bloque 3-FIX3 se detuvo en auditoría.

## Causa
Quedaron nombres duplicados por reemplazos repetidos:
- safesafeLoan.
- updatedsafesafeLoan.

## Corrección aplicada
- Se normalizó cualquier variante updated + safeLoan hacia updatedLoan.
- Se normalizó cualquier variante safe + safeLoan hacia safeLoan.
- Se aseguró:
  - onLoanUpdated(updatedLoan.id).
  - loanId = updatedLoan.id para reconstrucción de cuotas.
  - safeLoan.id para updateLoanDescription.
  - safeLoan.id para UpdateLoanInput.

## Seguridad mantenida
Si el préstamo tiene pagos, cuotas abonadas o está cerrado:
- Solo se edita descripción.
- No se reconstruyen cuotas.

Si el préstamo no tiene historial financiero:
- Se editan condiciones.
- Se usa UpdateLoanInput.
- Se reconstruyen cuotas.

## Líneas actuales
905

## Backup local
_local_archives\v1_1_bloque_3_fix4_normalizar_safeloan_before_20260614-170127

## Resultado esperado
- Build debug correcto.
- Commit correcto.
- Push correcto.
- Release firmado correcto.
- APK copiado.
- Firma verificada.
- APK instalado.
- App abierta.
- Git limpio.

## Siguiente bloque recomendado
Mega Bloque 4 - Rediseñar Crear Cliente.

## Corrección posterior por Mega Bloque 3-FIX5
El Mega Bloque 3-FIX4 limpió los nombres duplicados safeLoan/updatedLoan, pero el build debug falló porque updatedLoan puede ser nullable.

Corrección:
- Se reemplazó onLoanUpdated(updatedLoan.id) por onLoanUpdated(updatedLoan?.id ?: safeLoan.id).
- Se reemplazó loanId = updatedLoan.id por loanId = updatedLoan?.id ?: safeLoan.id.
- Se conserva safeLoan como préstamo original no nulo.
- Se mantiene UpdateLoanInput.
- Se mantiene updateLoanDescription en préstamos protegidos.
- Se mantiene updateLoan(updateInput) y rebuildInstallmentsForLoan en préstamos editables.
