# CONTROL PRESTAMOS - DASHBOARD PREMIUM V1.1

Fecha:
20260613-234849

Rama:
feature/v1.1-premium-dashboard-security

## Cambios aplicados
- Se reemplazó DashboardExecutiveCard por PremiumPortfolioTotalsCard.
- Se eliminó el componente viejo sustituido.
- Se agregó PremiumClientSnapshotCard.
- Se agregó resumen por cliente activo.
- Se calcula Capital colocado desde préstamos activos.
- Se calcula Utilidad pactada como cartera objetivo menos capital.
- Se calcula Cartera objetivo desde totalExpectedAmount.
- Se calcula Recaudado desde pagos activos por préstamo.
- Se calcula Saldo por recaudar desde cartera objetivo menos recaudado.
- Se calcula vencido desde cuotas OVERDUE.
- Se muestra próximo cobro por cliente.
- Se mantiene la regla de no contar clientes archivados usando FinancialOperationRules.
- Se mantiene la regla de no contar préstamos cancelados al usar préstamos activos.
- Se mantiene la regla de no contar pagos anulados usando getTotalPaidByLoan.

## Auditoría de reemplazo
DashboardExecutiveCard call count:
0

DashboardExecutiveCard function count:
0

PremiumPortfolioTotalsCard count:
2

PremiumClientSnapshotCard count:
2

## Resultado esperado
- El inicio debe verse más ejecutivo.
- Debe aparecer la tarjeta Cartera ejecutiva.
- Deben aparecer Capital colocado, Utilidad pactada, Cartera objetivo, Recaudado y Pendiente.
- Debe aparecer una lista compacta de clientes activos con saldo, vencido y próximo cobro.
- No debe existir el componente viejo reemplazado.

## Backup
_local_archives\v1_1_dashboard_premium_before_20260613-234849
