# CONTROL PRESTAMOS - MEGA BLOQUE 2 CREAR PRESTAMO TIPO REFERENCIA V1.1

Fecha:
20260614-161537

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Rediseñar Crear Préstamo como formulario financiero claro, profesional y usable para clientes reales.

## Archivo modificado
.\app\src\main\java\com\controlprestamos\features\loans\presentation\CreateLoanScreen.kt

## Cambios aplicados
- Se detectó la pantalla real con fun CreateLoanScreen.
- Se respaldó CreateLoanScreen.kt.
- Se respaldó AppNavGraph.kt.
- Se mantuvo navegación compatible con:
  - clientId
  - preselectedClientId
  - onNavigateBack
  - onLoanCreated
  - onOpenClients
- Se agregó selección de cliente activo.
- Se respeta cliente precargado si viene desde Detalle Cliente.
- Se muestra advertencia si el cliente ya tiene préstamos activos.
- Se agregaron condiciones:
  - capital
  - interés
  - plazo en días
- Se agregó fecha de inicio visible.
- Se agregó tipo de plan:
  - diario
  - semanal
  - mensual
- Se agregó descripción.
- Se agregó resumen antes de guardar:
  - capital
  - interés
  - total esperado
  - estimado diario
  - fecha de inicio
  - plan
- Se usa moneda desde preferencias.
- Se mantiene CreateLoanInput.
- Se mantiene LoanFormValidator.
- Se crea préstamo con LocalLoanRepository.
- Se generan cuotas con LocalInstallmentRepository.
- Se eliminan textos largos y lenguaje premium visible.

## Líneas actuales
746

## Backup local
_local_archives\v1_1_bloque_2_create_loan_reference_before_20260614-161537

## Prueba manual recomendada
1. Abrir app.
2. Ir a Clientes.
3. Entrar a un cliente activo.
4. Tocar Nuevo préstamo.
5. Confirmar que el cliente aparece seleccionado.
6. Colocar capital.
7. Colocar interés.
8. Colocar plazo.
9. Cambiar fecha de inicio.
10. Cambiar plan.
11. Ver resumen antes de guardar.
12. Crear préstamo.
13. Confirmar regreso correcto.
14. Confirmar préstamo en lista.
15. Entrar al préstamo.
16. Ver cuotas generadas.
17. Ir a Pagos y verificar que aparezcan cuotas futuras si aplica.

## Siguiente bloque recomendado
Mega Bloque 3 - Rediseñar Editar Préstamo con reglas seguras.

## Corrección posterior por Mega Bloque 2-FIX
El primer intento del Mega Bloque 2 falló en build debug porque CreateLoanInput esperaba RepaymentPlanType y la pantalla estaba enviando String.

Corrección:
- Se restauró uso de RepaymentPlanType.
- Se reemplazaron planes no soportados por el modelo actual.
- Se usan opciones reales:
  - Cuotas.
  - Pago único.
- Se restauró onLoanCreated(createdLoan.id) para navegación compatible.
