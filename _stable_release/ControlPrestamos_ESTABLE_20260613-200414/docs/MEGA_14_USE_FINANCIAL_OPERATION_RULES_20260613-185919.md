# MEGA 14 - Usar FinancialOperationRules en pantallas críticas

Fecha:
20260613-185919

Objetivo:
Empezar a usar la matriz central de reglas financieras creada en MEGA 13.

Archivos modificados:
- app\src\main\java\com\controlprestamos\features\loans\presentation\CreateLoanScreen.kt
- app\src\main\java\com\controlprestamos\features\payments\presentation\CreatePaymentScreen.kt
- app\src\main\java\com\controlprestamos\features\loans\presentation\LoansByClientScreen.kt

Archivo base usado:
- app\src\main\java\com\controlprestamos\core\rules\FinancialOperationRules.kt

Backup:
_migration_backup\mega_14_use_financial_rules_before_20260613-185919

Cambios:
1. CreateLoanScreen.kt
   - Usa FinancialOperationRules.canCreateLoanForClient(client).
   - Usa FinancialOperationRules.getClientOperationMessage(client).

2. CreatePaymentScreen.kt
   - Usa FinancialOperationRules.canShowClientInOperationalLists(client).
   - Usa FinancialOperationRules.isClosedLoan(loan).
   - Usa FinancialOperationRules.getClientOperationMessage(client).
   - Usa FinancialOperationRules.getLoanOperationMessage(loan).

3. LoansByClientScreen.kt
   - Usa FinancialOperationRules.canShowClientInOperationalLists(client).
   - Usa FinancialOperationRules.isClosedLoan(loan).

Regla:
Las pantallas críticas deben ir dejando de tener reglas sueltas y empezar a consultar la matriz central.
