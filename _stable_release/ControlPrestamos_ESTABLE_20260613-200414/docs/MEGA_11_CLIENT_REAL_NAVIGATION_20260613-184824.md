# MEGA 11 - Navegación real desde detalle del cliente

Fecha:
20260613-184824

Objetivo:
Conectar acciones del detalle del cliente con flujos reales.

Archivos modificados:
- app\src\main\java\com\controlprestamos\core\navigation\AppNavGraph.kt
- app\src\main\java\com\controlprestamos\features\loans\presentation\LoansByClientScreen.kt
- app\src\main\java\com\controlprestamos\features\clients\presentation\ClientDetailScreen.kt

Backup:
_migration_backup\mega_11_client_navigation_before_20260613-184824

Cambios:
1. AppNavGraph.kt
   - ClientDetailScreen ahora recibe onCreatePayment.
   - Registrar pago desde cliente navega a la cartera/préstamos del cliente.
   - ClientDetailScreen también conecta clicks de préstamo a LoanDetail.
   - LoansByClientScreen ahora conecta onCreatePayment hacia CreatePayment.

2. LoansByClientScreen.kt
   - Agrega onCreatePayment por préstamo.
   - Agrega botón Registrar pago en cada préstamo con saldo pendiente.
   - Oculta registro de pago si el cliente está archivado.
   - Muestra aviso si el cliente está archivado.

Regla:
Registrar pago desde cliente requiere seleccionar un préstamo específico.
Por eso el flujo correcto es:
Detalle cliente -> Registrar pago -> Cartera del cliente -> Registrar pago en préstamo.
