# CONTROL PRESTAMOS - MEGA BLOQUE 3-FIX UPDATE LOAN INPUT V1.1

Fecha:
20260614-164326

Rama:
feature/v1.1-premium-dashboard-security

## Problema detectado
El Mega Bloque 3 rediseñó EditLoanScreen.kt, pero falló en build debug.

## Error real
EditLoanScreen.kt estaba enviando un Loan a LocalLoanRepository.updateLoan.

Errores:
- Type mismatch: inferred type is Loan but UpdateLoanInput was expected
- Type mismatch: inferred type is Loan but UpdateLoanInput was expected

## Causa
LocalLoanRepository.updateLoan trabaja con UpdateLoanInput, no con loan.copy() enviado directamente.

## Corrección aplicada
- Se agregó import de UpdateLoanInput.
- Para préstamos protegidos:
  - Se usa LocalLoanRepository.updateLoanDescription.
  - Solo se actualiza descripción.
  - No se reconstruyen cuotas.
- Para préstamos editables:
  - Se crea UpdateLoanInput.
  - Se llama LocalLoanRepository.updateLoan(updateInput).
  - Se reconstruyen cuotas con rebuildInstallmentsForLoan.
  - Se navega usando updatedLoan.id.

## Seguridad mantenida
- Si hay pagos, cuotas abonadas, préstamo pagado o cancelado:
  - Solo descripción.
- Si no hay historial financiero:
  - Se permite editar condiciones.
  - Se recalculan cuotas.

## Líneas actuales
901

## Backup local
_local_archives\v1_1_bloque_3_fix_update_loan_input_before_20260614-164326

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

## Corrección posterior por Mega Bloque 3-FIX4
El Mega Bloque 3-FIX3 se detuvo en auditoría porque quedaron nombres duplicados por reemplazos repetidos:
- safesafeLoan
- updatedsafesafeLoan

Corrección:
- Se normalizó safeLoan.
- Se normalizó updatedLoan.
- Se aseguró onLoanUpdated(updatedLoan.id).
- Se aseguró rebuildInstallmentsForLoan con loanId = updatedLoan.id.
- Se mantuvo safeLoan.id para updateLoanDescription y UpdateLoanInput.

## Corrección posterior por Mega Bloque 3-FIX5
El Mega Bloque 3-FIX4 limpió los nombres duplicados safeLoan/updatedLoan, pero el build debug falló porque updatedLoan puede ser nullable.

Corrección:
- Se reemplazó onLoanUpdated(updatedLoan.id) por onLoanUpdated(updatedLoan?.id ?: safeLoan.id).
- Se reemplazó loanId = updatedLoan.id por loanId = updatedLoan?.id ?: safeLoan.id.
- Se conserva safeLoan como préstamo original no nulo.
- Se mantiene UpdateLoanInput.
- Se mantiene updateLoanDescription en préstamos protegidos.
- Se mantiene updateLoan(updateInput) y rebuildInstallmentsForLoan en préstamos editables.
