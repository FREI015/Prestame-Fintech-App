# CONTROL PRESTAMOS - INICIO CARTERA Y CLIENTES ACTIVOS V1.1

Fecha:
20260614-014355

Rama:
feature/v1.1-premium-dashboard-security

## Cambios aplicados
- Se quitaron accesos rápidos del inicio.
- Se quitó el botón derecho de la barra superior.
- El saludo ahora cambia según la hora del dispositivo:
  - Buenos días.
  - Buenas tardes.
  - Buenas noches.
- Se agregó tarjeta superior de cartera activa:
  - Ganancia porcentual de préstamos activos.
  - Total a recaudar de préstamos activos.
  - Capital reinvertido del periodo.
- Se reemplazó la parte inferior por clientes con préstamos activos.
- Cada cliente muestra:
  - Monto prestado.
  - Total a recoger.
  - Porcentaje cobrado.
  - Barra de progreso.

## Lógica financiera
Ganancia porcentual:
(total a recaudar - capital prestado) / capital prestado

Total a recaudar:
suma de totalExpectedAmount de préstamos activos

Reinvertido del periodo:
suma del capital prestado en préstamos activos creados/iniciados durante el mes actual

Campo usado para fecha de reinversión:
createdAtMillis

Expresión Kotlin usada:
loan.createdAtMillis

## Resultado esperado
- Inicio más limpio.
- Sin accesos rápidos abajo.
- Sin botón derecho innecesario arriba.
- Más enfoque en cartera activa.
- Lista inferior útil con clientes y progreso de cobro.

## Líneas actuales de DashboardScreen.kt
800

## Backup local
_local_archives\v1_1_dashboard_portfolio_clients_before_20260614-014355
