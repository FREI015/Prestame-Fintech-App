# CONTROL PRESTAMOS - BLOQUE 11B FIX3 MAS PREFERENCIAS AYUDA

Fecha:
20260614-210324

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Corregir firma de LocalPreferencesRepository.savePreferences y cerrar limpieza de Mas, Preferencias y Ayuda.

## Corrección principal
- PreferencesScreen ahora usa:
  LocalPreferencesRepository.savePreferences(context, preferences = AppPreferences(...))

## Cambios incluidos de 11B
- Mas simplificado.
- Accesos rapidos duplicados eliminados.
- Tarjeta de estado/version eliminada.
- Exportar informacion duplicado eliminado de Mas.
- Preferencias reducidas a datos funcionales.
- Ayuda y Acerca de completados.

## APK
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_bloque_11b_fix3_mas_preferencias_signed_20260614-210324.apk

## SHA256
E029AD372AC565F64C964829E61B2345D93D81CA0BF5178C180ED9D1148E8892

## Backup local
_local_archives\v1_1_bloque_11b_fix3_before_20260614-210324

## Prueba manual
- Abrir Mas.
- Abrir Preferencias.
- Guardar nombre del negocio.
- Cambiar simbolo de moneda.
- Cambiar formato de fecha.
- Abrir Acerca de/Ayuda.
