# MEGA 13 - Matriz central de reglas financieras

Fecha:
20260613-185549

Objetivo:
Crear una referencia central para reglas financieras y operativas de la app.

Archivos creados/modificados:
- app\src\main\java\com\controlprestamos\core\rules\FinancialOperationRules.kt
- docs\qa\CONTROL_PRESTAMOS_STATE_RULES.md

Backup:
_migration_backup\mega_13_financial_rules_before_20260613-185549

Cambios:
1. FinancialOperationRules.kt
   - Define reglas para clientes activos/archivados.
   - Define reglas para préstamos activos/pagados/cancelados.
   - Define reglas para pagos activos/anulados.
   - Define reglas para dashboard/reportes operativos.
   - Define mensajes estándar de estado.

2. CONTROL_PRESTAMOS_STATE_RULES.md
   - Documenta matriz funcional de estados.
   - Sirve como guía para QA y futuras pantallas.

Nota:
Este bloque no cambia pantallas todavía.
Deja una base central para que los próximos bloques puedan reutilizar reglas en vez de repetir condiciones.
