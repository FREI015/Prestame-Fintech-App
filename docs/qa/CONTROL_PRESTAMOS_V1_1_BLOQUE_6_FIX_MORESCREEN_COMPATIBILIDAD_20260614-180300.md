# CONTROL PRESTAMOS - MEGA BLOQUE 6-FIX MORESCREEN COMPATIBILIDAD V1.1

Fecha:
20260614-180300

Rama:
feature/v1.1-premium-dashboard-security

## Problema detectado
El Mega Bloque 6 falló en build debug.

## Error real
AppNavGraph llamaba a MoreScreen con parámetros que la nueva firma no aceptaba:

- onOpenFinancialAudit.
- onOpenHelp.
- onLogout.

## Causa
La pantalla MoreScreen fue reescrita y se eliminaron callbacks usados por AppNavGraph.

## Corrección aplicada
- Se agregó onOpenFinancialAudit como alias compatible.
- Se agregó onOpenHelp como alias compatible.
- Se agregó onLogout como alias compatible.
- Se agregó helper interno para mantener compatibilidad sin expresiones sueltas.
- No se tocó AppNavGraph.
- No se tocó bottom navigation.

## Líneas actuales
485

## Backup local
_local_archives\v1_1_bloque_6_fix_morescreen_compat_before_20260614-180300

## Resultado esperado
- Build debug correcto.
- Commit correcto.
- Push correcto.
- Release firmado correcto.
- APK copiado.
- Firma verificada.
- APK instalado.
- App abierta.
- Git limpio.

## Siguiente bloque recomendado
Mega Bloque 7 - Reportes / Exportación / Pulido profesional.
