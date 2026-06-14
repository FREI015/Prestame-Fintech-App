# CONTROL PRESTAMOS - AUDITORIA LOGIN V1.1

Fecha:
20260613-225003

Rama:
feature/v1.1-premium-dashboard-security

## Estado validado
- El flujo de bloqueo vuelve al Login.
- Se limpia el backstack y se evita regresar al modulo central con atras.
- Se permite acceso por correo/contrasena.
- Se permite acceso por PIN si existe configurado.
- Se permite desbloqueo biometrico si el dispositivo lo soporta y esta habilitado.
- Contrasena y PIN usan ocultacion visual.
- El desbloqueo correcto marca la sesion como desbloqueada.
- La imagen local del usuario se integra como fondo opaco del Login.

## Endurecimiento pendiente
- Login real con Google.
- Eliminacion de cuenta/datos.
- Seccion de ayuda/politicas/privacidad enfocada a Google Play.
- Endurecimiento adicional por intentos fallidos si se decide implementar.
- Revision futura de almacenamiento sensible para endurecimiento adicional.

## Recurso agregado
- app/src/main/res/drawable-nodpi/login_bg_premium.jpg

## Backup local
- _local_archives\v1_1_login_background_before_20260613-225003
