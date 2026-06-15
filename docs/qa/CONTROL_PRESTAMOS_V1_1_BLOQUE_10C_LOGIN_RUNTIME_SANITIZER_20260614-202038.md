# CONTROL PRESTAMOS - BLOQUE 10C LOGIN RUNTIME SANITIZER

Fecha:
20260614-202038

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Eliminar texto corrupto visible en Login/desbloqueo, incluyendo texto guardado previamente en preferencias.

## Correcciones
- LoginScreen saneado con textos visibles exactos.
- LocalPreferencesRepository ahora limpia businessName guardado en SharedPreferences.
- Si businessName estaba guardado como mojibake, se reescribe limpio automáticamente.
- APK release compilada.
- APK instalada en telefono.
- App abierta despues de instalar.

## APK
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_bloque_10c_login_runtime_signed_20260614-202038.apk

## SHA256
EB08182E81EE042E635D3B33E6072B47AD36CED4A21B1FC8F30172BC48734E11

## Version instalada
    versionCode=2 minSdk=26 targetSdk=35
    versionName=1.1.0-dev

## Backup local
_local_archives\v1_1_bloque_10c_login_runtime_before_20260614-202038

## Prueba manual requerida
Abrir Login/desbloqueo en el telefono y verificar:
- Control Préstamos
- Método de acceso
- Correo electrónico
- Contraseña
- Iniciar sesión
- Elige cómo quieres desbloquear tu sesión
- Ingresa tu PIN de seguridad para volver a la aplicación
- Biometría
- Rápido
