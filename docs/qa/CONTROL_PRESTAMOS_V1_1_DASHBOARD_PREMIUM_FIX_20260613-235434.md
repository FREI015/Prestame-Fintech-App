# CONTROL PRESTAMOS - DASHBOARD PREMIUM FIX V1.1

Fecha:
20260613-235434

Rama:
feature/v1.1-premium-dashboard-security

## Problema detectado
El bloque 30E aplicó el Dashboard premium, pero falló la compilación antes del commit.

## Errores corregidos
- Unresolved reference: BorderStroke
- Unresolved reference: dueDate

## Corrección aplicada
- Se agregó import androidx.compose.foundation.BorderStroke.
- Se reemplazó el uso inválido de nextDue?.dueDate por dueDateMillis formateado.
- Se agregó helper formatDashboardDate(millis).
- Se conserva el Dashboard premium aplicado.
- Se mantiene la regla de reemplazo limpio.

## Resultado esperado
- Build debug debe compilar.
- Commit debe realizarse.
- Release firmado debe generarse.
- APK debe instalarse en teléfono.
- El Dashboard debe mostrar la tarjeta Cartera ejecutiva y clientes activos.

## Backup local
_local_archives\v1_1_dashboard_premium_fix_before_20260613-235434
