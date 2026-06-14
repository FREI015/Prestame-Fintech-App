# CONTROL PRESTAMOS - MEGA BLOQUE 3 EDITAR PRESTAMO SEGURO V1.1

Fecha:
20260614-163438

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Rediseñar Editar Préstamo con reglas financieras seguras para evitar descuadres cuando ya existen pagos, cuotas abonadas o préstamos cerrados.

## Archivo modificado
.\app\src\main\java\com\controlprestamos\features\loans\presentation\EditLoanScreen.kt

## Cambios aplicados
- Se detectó la pantalla real con fun EditLoanScreen.
- Se respaldó EditLoanScreen.kt.
- Se respaldó AppNavGraph.kt.
- Se verificaron métodos existentes en repositorios antes de modificar.
- Se mantiene navegación compatible:
  - loanId
  - onNavigateBack
  - onLoanUpdated
- Se muestra resumen del préstamo.
- Se muestra cliente asociado.
- Se muestra estado.
- Se muestra total esperado actual.
- Se muestra cantidad de pagos.
- Se muestra cantidad de cuotas.
- Se detecta si el préstamo tiene historial financiero.
- Se detecta si hay cuotas abonadas o pagadas.
- Se detecta si el préstamo está pagado o cancelado.

## Regla financiera segura
Si el préstamo no tiene pagos ni cuotas abonadas:
- Se permite editar capital.
- Se permite editar interés.
- Se permite editar plazo.
- Se permite editar fecha de inicio.
- Se permite editar plan.
- Se permite editar descripción.
- Se valida con LoanFormValidator.
- Se actualiza el préstamo.
- Se reconstruyen cuotas.

Si el préstamo tiene pagos, cuotas abonadas, está pagado o cancelado:
- Se bloquean campos financieros.
- Solo se permite editar descripción.
- No se reconstruyen cuotas.
- No se toca capital, interés, plazo, fecha, plan ni totales.

## Seguridad aplicada
- No se introducen planes no soportados.
- Se usa RepaymentPlanType real:
  - INSTALLMENTS
  - SINGLE_PAYMENT
- No se usa DAILY/WEEKLY/MONTHLY.
- No se usa lenguaje premium visible.
- No se tocan repositorios.
- No se toca AppNavGraph.

## Líneas actuales
901

## Backup local
_local_archives\v1_1_bloque_3_edit_loan_safe_before_20260614-163438

## Prueba manual recomendada
1. Abrir app.
2. Crear un cliente si no existe.
3. Crear un préstamo nuevo.
4. Entrar a Detalle Préstamo.
5. Tocar Editar.
6. Confirmar que permite cambiar capital, interés, plazo, fecha y plan.
7. Guardar cambios.
8. Confirmar regreso correcto.
9. Revisar cuotas reconstruidas.
10. Registrar un pago en ese préstamo.
11. Volver a Editar Préstamo.
12. Confirmar que campos financieros aparecen protegidos.
13. Cambiar solo descripción.
14. Guardar descripción.
15. Confirmar que no se descuadran cuotas ni pagos.

## Siguiente bloque recomendado
Mega Bloque 4 - Rediseñar Crear Cliente.

## Corrección posterior por Mega Bloque 3-FIX
El primer intento del Mega Bloque 3 falló en build debug porque LocalLoanRepository.updateLoan no recibe un Loan, recibe UpdateLoanInput.

Corrección:
- Se importó UpdateLoanInput.
- Se reemplazó loan.copy enviado a updateLoan por UpdateLoanInput.
- En préstamos protegidos se usa updateLoanDescription.
- En préstamos editables se usa updateLoan(UpdateLoanInput).
- Se mantiene rebuildInstallmentsForLoan luego de actualizar condiciones financieras.

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
