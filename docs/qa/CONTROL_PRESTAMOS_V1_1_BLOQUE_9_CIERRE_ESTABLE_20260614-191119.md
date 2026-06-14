# CONTROL PRESTAMOS - MEGA BLOQUE 9 CIERRE ESTABLE V1.1

Fecha:
20260614-191119

Rama:
feature/v1.1-premium-dashboard-security

## Estado
Cierre estable de V1.1 completado.

## Versión
- versionCode: 2
- versionName: 1.1.0-dev

## Auditorías finales
- Git limpio antes de iniciar.
- Textos prohibidos eliminados de Kotlin.
- PREMIUM_GOLD eliminado.
- Recurso login_bg_profesional verificado.
- Commits clave revisados.
- Debug final compilado.
- Release final compilado.
- APK final copiado.
- SHA256 final generado.
- Firma final verificada.
- APK final instalado en teléfono.
- Versión instalada verificada por ADB.
- App abierta con monkey.
- Git limpio al final.

## APK final estable
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_final_stable_signed_20260614-191119.apk

## SHA256
D232D8B433E5900ABE369C0E0BA57E631194DFB52E843F2AC6262B90BCD44C94

## Versión instalada
    versionCode=2 minSdk=26 targetSdk=35
    versionName=1.1.0-dev

## Commits revisados
OK - V1.1 iniciar bloques profesionales y auditar cobranza
OK - V1.1 cerrar crear prestamo tipo referencia
OK - V1.1 cerrar editar prestamo con reglas seguras
WARN - No exacto: V1.1 rediseñar crear cliente tipo referencia
OK - V1.1 cerrar editar cliente con reglas seguras
OK - V1.1 cerrar mas ajustes y preferencias
WARN - No exacto: V1.1 rediseñar reportes y exportacion profesional
OK - V1.1 corregir recurso login profesional y limpiar premium
OK - V1.1 QA integral release candidate
OK - V1.1 documentar APK release candidate final

## Firma
Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): false
Verified using v3.1 scheme (APK Signature Scheme v3.1): false
Verified using v4 scheme (APK Signature Scheme v4): false
Verified for SourceStamp: false
Number of signers: 1
Signer #1 certificate DN: CN=Control Prestamos, OU=Control Prestamos, O=Control Prestamos, L=Local, ST=Local, C=VE
Signer #1 certificate SHA-256 digest: 97aa3c38cb1a5af58d6eb72b10f58a74fbe1f590b60512d31589b4bef57fa1d2
Signer #1 certificate SHA-1 digest: db95b18d614c9140195da3d9b6113966366df98e
Signer #1 certificate MD5 digest: d0011f8fe052dc20664a03dd6bb01fd1
Signer #1 key algorithm: RSA
Signer #1 key size (bits): 2048
Signer #1 public key SHA-256 digest: 7dbf5fd8c6ef499ea0e917ac02a083368f99b89d62f2489e8a78f1d7fd2fdaf5
Signer #1 public key SHA-1 digest: ae0d14352fe2aa2bc9977bf74c3958e266d31362
Signer #1 public key MD5 digest: f3ff780bb0ee86fd821487da0e62da74

## Prueba manual final recomendada
1. Abrir app.
2. Revisar Login.
3. Revisar Inicio.
4. Revisar Clientes.
5. Crear cliente.
6. Editar cliente.
7. Revisar Préstamos.
8. Crear préstamo con cuotas.
9. Crear préstamo de pago único.
10. Editar préstamo.
11. Registrar pago.
12. Revisar Pagos.
13. Abrir Más desde top bar.
14. Abrir Reportes.
15. Confirmar que Más no aparece en bottom navigation.
16. Confirmar que no aparece lenguaje premium visible.
17. Confirmar que la app no crashea.
