# CONTROL PRESTAMOS - BLOQUE 12A FIX1 MORESCREEN COMPOSABLE

Fecha:
20260614-212816

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Reparar fallo Compose del Bloque 12A.

## Problema corregido
LogoutCard quedó insertado cerca de ReferenceCard y provocó:
- Doble @Composable.
- ReferenceCard sin @Composable.
- Error de compilación en MoreScreen.kt.

## Cambios mantenidos
- Login con iconos reasignados por contexto.
- Centro de control eliminado de Más.
- Cerrar sesión restaurado.
- Respaldo local o Google Drive visible.
- AppNavGraph mantiene conexión de cierre de sesión.

## APK
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_bloque_12a_fix1_mas_login_signed_20260614-212816.apk

## SHA256
04795ACB52A37BDFDC8384732F2934B19497AE5A31A1BA26AA707FCA906CC9BC

## Backup local
_local_archives\v1_1_bloque_12a_fix1_before_20260614-212816

## Prueba manual
- Login: Correo = sobre.
- Login: PIN = números/PIN.
- Login: Huella = huella/tacto.
- Más: no aparece Centro de control.
- Más: aparece Cerrar sesión.
- Más: aparece Respaldo local o Google Drive.
