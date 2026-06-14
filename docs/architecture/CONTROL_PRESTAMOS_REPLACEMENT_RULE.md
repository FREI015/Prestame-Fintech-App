# CONTROL PRESTAMOS - REGLA OFICIAL DE REEMPLAZO LIMPIO

Fecha de creación:
20260613-232742

## Regla
Cuando se cree una vista nueva, lógica nueva, flujo nuevo o componente nuevo que sustituya uno anterior, la versión anterior debe eliminarse por completo.

## Obligatorio
1. No dejar convivencia entre versión vieja y nueva.
2. Eliminar navegación vieja.
3. Eliminar imports viejos.
4. Eliminar referencias huérfanas.
5. Eliminar archivos muertos si ya no se usan.
6. Auditar referencias globales antes de cerrar.
7. Compilar debug.
8. Compilar release si el cambio afecta la app real.
9. Probar en teléfono si el cambio afecta UX o seguridad.
10. Documentar el cambio realizado.

## Aplicación inmediata
- AppLockScreen vieja no debe convivir con Login premium unificado.
- Si Login premium sustituye el desbloqueo viejo, el flujo viejo debe ser retirado.
