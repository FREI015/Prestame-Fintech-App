# MEGA 15 - Usar FinancialOperationRules en Dashboard y Reportes

Fecha:
20260613-190712

Objetivo:
Centralizar reglas de conteo operativo en dashboard y reportes.

Archivos modificados:
- app\src\main\java\com\controlprestamos\features\dashboard\presentation\DashboardScreen.kt
- app\src\main\java\com\controlprestamos\features\reports\presentation\ReportsScreen.kt

Archivo base usado:
- app\src\main\java\com\controlprestamos\core\rules\FinancialOperationRules.kt

Backup:
_migration_backup\mega_15_rules_dashboard_reports_before_20260613-190712

Cambios:
1. DashboardScreen.kt
   - Usa FinancialOperationRules.canShowClientInOperationalLists(client).
   - Usa FinancialOperationRules.shouldCountLoanInOperationalReports(client, loan).
   - Usa FinancialOperationRules.shouldCountPaymentFinancially(payment).
   - Mantiene cuotas operativas filtradas por préstamos operativos.

2. ReportsScreen.kt
   - Usa FinancialOperationRules.canShowClientInOperationalLists(client).
   - Usa FinancialOperationRules.shouldCountLoanInOperationalReports(client, loan).
   - Usa FinancialOperationRules.shouldCountPaymentFinancially(payment).
   - Mantiene pagos anulados fuera de sumas financieras.

Reglas:
- Cliente archivado queda fuera de operación principal.
- Préstamo cancelado queda fuera de dashboard/reportes operativos.
- Pago anulado no suma financieramente.
- El historial sigue intacto.
