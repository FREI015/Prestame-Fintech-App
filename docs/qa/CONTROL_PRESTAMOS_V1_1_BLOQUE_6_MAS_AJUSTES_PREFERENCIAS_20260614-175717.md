# CONTROL PRESTAMOS - MEGA BLOQUE 6 MAS AJUSTES PREFERENCIAS V1.1

Fecha:
20260614-175717

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Rediseñar la pantalla Más / Ajustes / Preferencias como centro profesional de configuración, herramientas y accesos rápidos.

## Archivo modificado
.\app\src\main\java\com\controlprestamos\features\more\presentation\MoreScreen.kt

## Función modificada
MoreScreen

## Cambios aplicados
- Se detectó la pantalla real de Más/Ajustes.
- Se respaldó la pantalla modificada.
- Se respaldó AppNavGraph.kt.
- Se mantuvo navegación compatible con callbacks opcionales.
- Se agregó encabezado profesional "Centro de control".
- Se agregó sección de accesos rápidos:
  - Clientes.
  - Préstamos.
  - Pagos.
  - Reportes.
- Se agregó sección Preferencias:
  - Preferencias generales.
  - Datos del negocio.
  - Moneda y formato.
  - Apariencia.
- Se agregó sección Datos y seguridad:
  - Respaldo local.
  - Exportar información.
  - Importar o restaurar.
  - Seguridad.
- Se agregó sección Soporte:
  - Acerca de.
  - Ayuda.
- Se agregó tarjeta de estado actual:
  - App local.
  - V1.1.
  - versión 1.1.0-dev.
- No se agregó lenguaje premium visible.
- No se tocó AppNavGraph.
- No se tocó bottom navigation.

## Reglas visuales respetadas
- Fondo claro.
- Tarjetas blancas / superficie suave.
- Bordes sutiles.
- Acento turquesa.
- Texto profesional.
- "Más" queda como acceso superior, no como item de bottom navigation.

## Líneas actuales
472

## Backup local
_local_archives\v1_1_bloque_6_more_settings_before_20260614-175717

## Prueba manual recomendada
1. Abrir app.
2. Tocar Más desde el top bar.
3. Confirmar que abre pantalla Más/Ajustes.
4. Confirmar que no aparece Más en bottom navigation.
5. Revisar sección Accesos rápidos.
6. Revisar sección Preferencias.
7. Revisar sección Datos y seguridad.
8. Revisar sección Soporte.
9. Probar botón Volver.
10. Confirmar que la app no se cierra ni pierde navegación.

## Siguiente bloque recomendado
Mega Bloque 7 - Reportes / Exportación / Pulido profesional.

## Corrección posterior por Mega Bloque 6-FIX
El Mega Bloque 6 falló en build debug porque AppNavGraph todavía llamaba a MoreScreen con parámetros de compatibilidad:

- onOpenFinancialAudit.
- onOpenHelp.
- onLogout.

Corrección:
- Se agregaron los callbacks como alias compatibles en MoreScreen.
- No se tocó AppNavGraph.
- No se tocó bottom navigation.
- Se mantuvo la pantalla Más como acceso superior.
