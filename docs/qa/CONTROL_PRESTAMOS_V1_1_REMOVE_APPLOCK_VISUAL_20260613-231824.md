# CONTROL PRESTAMOS - REMOVE APPLOCK VISUAL V1.1

Fecha:
20260613-231824

Rama:
feature/v1.1-premium-dashboard-security

## Problema
La app seguia mostrando la pantalla vieja AppLockScreen.

## Fix aplicado
- AppNavGraph ya no importa AppLockScreen.
- AppNavGraph ya no navega directamente a AppRoute.AppLock.
- El startDestination bloqueado apunta a Login.
- La ruta AppRoute.AppLock queda solo como compatibilidad, pero renderiza LoginScreen premium.
- LoginScreen mantiene fondo premium login_bg_premium.
- Fondo ajustado para mayor visibilidad.

## Resultado esperado
- No debe aparecer pantalla vieja de desbloqueo.
- Debe aparecer Login premium.
- Deben verse juntos correo, PIN y huella cuando aplique.
- Debe verse el fondo opaco premium.

## Backup local
_local_archives\v1_1_remove_applock_visual_before_20260613-231824
