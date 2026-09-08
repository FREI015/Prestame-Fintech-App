# MEGA 17 - Aplicar FinancialOperationRules en pantallas restantes

Fecha:
20260613-192428

Objetivo:
Extender el uso de la matriz central de reglas a pantallas que todavía podían tener condiciones sueltas.

Archivos modificados:
- app\src\main\java\com\controlprestamos\features\clients\presentation\ClientDetailScreen.kt
- app\src\main\java\com\controlprestamos\features\payments\presentation\PaymentsByLoanScreen.kt
- app\src\main\java\com\controlprestamos\features\loans\presentation\EditLoanScreen.kt
- app\src\main\java\com\controlprestamos\features\audit\presentation\FinancialAuditScreen.kt

Archivo base:
- app\src\main\java\com\controlprestamos\core\rules\FinancialOperationRules.kt

Backup:
_migration_backup\mega_17_rules_remaining_screens_before_20260613-192428

QA:
- docs\qa\CONTROL_PRESTAMOS_RULES_USAGE_SCAN_20260613-192428.txt

Cambios:
1. ClientDetailScreen.kt
   - Usa FinancialOperationRules para validar cliente activo/archivado.
   - Usa mensaje centralizado del estado del cliente cuando aplica.

2. PaymentsByLoanScreen.kt
   - Usa FinancialOperationRules para pago activo/anulado.
   - Usa regla central para cancelar pago activo cuando aplica.

3. EditLoanScreen.kt
   - Usa FinancialOperationRules para validar edición financiera.
   - Usa regla central para descripción editable y préstamo cerrado.

4. FinancialAuditScreen.kt
   - Usa FinancialOperationRules para interpretar pagos activos/anulados.
   - Usa regla central para detectar préstamos cerrados cuando aplica.

Regla:
Las pantallas deben consultar reglas centrales en vez de repetir condiciones de estado.
