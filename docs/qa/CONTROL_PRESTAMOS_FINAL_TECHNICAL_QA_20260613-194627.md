# Control Préstamos — QA Técnico Final Automatizado

Fecha: 20260613-194627

## Resumen

- OK: 54
- WARN: 0
- FAIL: 2
- Total checks: 56

## Interpretación

Se detectaron fallos críticos. Revisar la sección de FAIL antes de considerar estable la versión.

No se detectaron advertencias relevantes.

## Checks por área

### Reglas centrales

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Existe FinancialOperationRules | $safeFile | $safePattern | L10 |
| OK | Regla crear préstamo por cliente | $safeFile | $safePattern | L12 |
| OK | Regla registrar pago por préstamo | $safeFile | $safePattern | L16 |
| OK | Regla reporte operativo | $safeFile | $safePattern | L71 |
| OK | Regla pago financiero | $safeFile | $safePattern | L81 |
| OK | Regla préstamo cerrado | $safeFile | $safePattern | L85 |
| OK | Etiqueta cliente | $safeFile | $safePattern | L90 |
| OK | Etiqueta préstamo | $safeFile | $safePattern | L98 |
| OK | Etiqueta pago | $safeFile | $safePattern | L107 |

### Clientes

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Anti duplicado clientes | $safeFile | $safePattern | L57, L89, L132 |
| OK | Archivo seguro cliente | $safeFile | $safePattern | L172 |
| OK | Reactivar cliente | $safeFile | $safePattern | L185 |
| OK | Filtro archivados en lista | $safeFile | $safePattern | L46, L67, L71, L72, L77 |
| OK | Detalle usa reglas centrales | $safeFile | $safePattern | L34, L178, L191, L217 |
| OK | PDF de cobranza en detalle | $safeFile | $safePattern | L199, L212 |

### Préstamos

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Actualizar estado por balance | $safeFile | $safePattern | L179 |
| OK | Protección edición financiera | $safeFile | $safePattern | L108, L112 |
| OK | Crear préstamo bloquea archivados | $safeFile | $safePattern | L119 |
| OK | Editar préstamo usa reglas | $safeFile | $safePattern | L38, L152 |
| OK | Cartera por cliente usa reglas | $safeFile | $safePattern | L32, L56, L273 |
| OK | Cartera bloquea préstamo cerrado | $safeFile | $safePattern | L273 |

### Pagos

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Historial total de pagos | $safeFile | $safePattern | L45 |
| OK | Historial por préstamo | $safeFile | $safePattern | L55 |
| OK | Anular pago | $safeFile | $safePattern | L117 |
| OK | Crear pago usa reglas | $safeFile | $safePattern | L35, L152, L155, L168, L171 |
| OK | Crear pago bloquea cerrados | $safeFile | $safePattern | L168, L183 |
| OK | Historial por préstamo usa reglas | $safeFile | $safePattern | L40, L94, L95, L463, L626 |
| FAIL | Centro pagos filtra operación | $safeFile | $safePattern | - |

### Cuotas

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Reconstrucción cuotas por préstamo | $safeFile | $safePattern | L325 |
| OK | Aplicar pago a cuotas | $safeFile | $safePattern | L125, L187, L194, L364 |
| OK | Estados vencidos | $safeFile | $safePattern | L31, L38, L43, L52, L60 |

### Dashboard y reportes

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Dashboard usa reglas | $safeFile | $safePattern | L32, L738, L746, L758 |
| OK | Dashboard reporte operativo | $safeFile | $safePattern | L746 |
| OK | Reports usa reglas | $safeFile | $safePattern | L34, L689, L697, L709 |
| OK | Reports pagos activos | $safeFile | $safePattern | L709 |

### Backup

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Inspect backup | $safeFile | $safePattern | L149, L293 |
| OK | Restore backup | $safeFile | $safePattern | L289 |
| FAIL | Schema version | $safeFile | $safePattern | - |
| OK | Recargar repositorios | $safeFile | $safePattern | L364, L365, L366, L367, L368 |
| OK | Preview restore pantalla | $safeFile | $safePattern | L117 |

### Auditoría

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Pantalla auditoría existe | $safeFile | $safePattern | L39 |
| OK | Detecta pagos huérfanos | $safeFile | $safePattern | L52, L58, L59, L121, L122 |
| OK | Usa reglas financieras | $safeFile | $safePattern | L30, L512, L513 |

### PDF cobranza

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Generator cobranza existe | $safeFile | $safePattern | L22 |
| OK | Resumen cartera | $safeFile | $safePattern | L133 |
| OK | Mensaje WhatsApp | $safeFile | $safePattern | L269 |
| OK | Usa reglas en PDF | $safeFile | $safePattern | L10, L385, L549 |

### PDF recibo

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | PDF recibo existe | $safeFile | $safePattern | L13 |
| OK | Formatter recibo existe | $safeFile | $safePattern | L12 |

### Preferencias

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | Nombre negocio | $safeFile | $safePattern | L6, L21, L33 |
| OK | Símbolo moneda | $safeFile | $safePattern | L7, L22, L34 |
| OK | Formato fecha | $safeFile | $safePattern | L8, L23, L35 |

### Navegación

| Estado | Check | Archivo | Patrón | Líneas |
|---|---|---|---|---|
| OK | AppNavGraph existe | $safeFile | $safePattern | L41 |
| OK | Ruta auditoría | $safeFile | $safePattern | L16 |
| OK | Ruta pago | $safeFile | $safePattern | L59 |
| OK | Ruta cartera cliente | $safeFile | $safePattern | L27 |

## Recomendación

Corregir primero los checks FAIL antes de continuar con pruebas finales.
