# CONTROL PRESTAMOS - ROADMAP PRODUCTO PROFESIONAL

Fecha:
20260614-160817

Rama:
feature/v1.1-premium-dashboard-security

## Nuevo objetivo del proyecto
Convertir Control Préstamos en una app profesional, usable para clientes reales, con flujos simples y capacidad de crecer hacia funciones avanzadas sin complicar la experiencia.

## Principio principal
La app debe ser simple para el usuario, aunque por dentro tenga lógica avanzada.

El usuario común debe poder:

1. Crear cliente.
2. Crear préstamo.
3. Ver cuotas.
4. Cobrar.
5. Ver reportes.
6. Resguardar su información.
7. Entender su negocio rápido.

## Línea visual
Mantener estilo financiero moderno:

- Fondo claro.
- Tarjetas limpias.
- Resúmenes arriba.
- Acciones claras.
- Menos texto largo.
- Más datos útiles.
- Sin lenguaje premium visible.
- Sin pantallas recargadas.

## Flujos base que deben quedar perfectos

### Flujo cliente
- Crear cliente.
- Editar cliente.
- Archivar cliente.
- Reactivar cliente.
- Ver deuda.
- Ver historial.
- Evitar duplicados.

### Flujo préstamo
- Crear préstamo.
- Ver total esperado.
- Generar cuotas.
- Editar con reglas seguras.
- Cancelar préstamo.
- Marcar como pagado.
- Ver progreso.

### Flujo cobranza
- Ver qué cobrar hoy.
- Ver vencidos.
- Registrar pago.
- Aplicar pago a cuotas.
- Actualizar préstamo.
- Mostrar historial.
- Evitar pagos inválidos.

### Flujo reportes
- Hoy.
- Semana.
- Mes.
- Rango personalizado.
- Prestado.
- Cobrado.
- Pendiente.
- Vencido.
- Clientes de riesgo.
- Cartera activa.

## Funciones profesionales futuras

### 1. Tasa BCV
Objetivo:
Permitir que la app use tasa de referencia del Banco Central para mostrar equivalencias o registrar operaciones en moneda local.

Regla:
No quemar valores manuales en pantallas.

Debe diseñarse como servicio configurable:

- Fuente de tasa.
- Fecha de actualización.
- Valor actual.
- Modo manual si no hay conexión.
- Historial de tasas si hace falta.
- Aviso visible si la tasa está desactualizada.

Bloque futuro sugerido:
Mega Bloque BCV-1 - Arquitectura de tasa BCV.

### 2. Binance / cripto
Objetivo:
Permitir préstamos o equivalencias en cripto sin complicar al usuario.

Regla:
Primero diseñar modelo y configuración antes de conectar API.

Debe contemplar:

- Moneda base.
- Activo cripto.
- Tasa de conversión.
- Fecha/hora de tasa.
- Monto original.
- Monto equivalente.
- Riesgo de volatilidad.
- Modo manual si no hay internet.
- No romper préstamos tradicionales.

Bloque futuro sugerido:
Mega Bloque CRYPTO-1 - Arquitectura cripto y Binance.

### 3. Resguardo de data con Google
Objetivo:
Permitir respaldo y restauración segura de datos.

Debe contemplar:

- Exportar backup local.
- Subir backup a Google Drive.
- Restaurar backup.
- Confirmación antes de sobrescribir datos.
- Fecha del último respaldo.
- Estado del respaldo.
- Manejo de errores.
- Privacidad del usuario.

Bloque futuro sugerido:
Mega Bloque BACKUP-1 - Diseño de respaldo Google.

### 4. Configuración profesional
Debe incluir:

- Moneda.
- Negocio.
- Seguridad.
- Respaldo.
- Tasa.
- Cripto.
- Apariencia.
- Datos de recibo.
- Exportación.

### 5. Usabilidad para clientes reales
La app debe evitar flujos complicados.

Reglas:

- No pedir demasiados datos de golpe.
- Mostrar resumen antes de guardar.
- Explicar errores de forma clara.
- No usar lenguaje técnico.
- Tener estados vacíos útiles.
- Evitar botones decorativos.
- Todo botón debe hacer algo real.

## Bloques nuevos sugeridos desde ahora

### Mega Bloque 1
Auditoría navegación de cobranza + Roadmap profesional.

### Mega Bloque 2
Rediseñar Crear Préstamo tipo referencia.

### Mega Bloque 3
Rediseñar Editar Préstamo con reglas seguras.

### Mega Bloque 4
Rediseñar Crear Cliente.

### Mega Bloque 5
Rediseñar Editar Cliente.

### Mega Bloque 6
Auditoría clientes archivados con deuda.

### Mega Bloque 7
Rediseñar Reportes.

### Mega Bloque 8
Rediseñar Más / Configuración.

### Mega Bloque 9
Auditoría moneda y preferencias.

### Mega Bloque 10
Auditoría financiera general.

### Mega Bloque 11
Auditoría vencimientos y cuotas.

### Mega Bloque 12
Arquitectura tasa BCV.

### Mega Bloque 13
Arquitectura cripto / Binance.

### Mega Bloque 14
Arquitectura backup Google.

### Mega Bloque 15
Auditoría visual global.

### Mega Bloque 16
APK final V1.1 validado.

### Mega Bloque 17
QA final V1.1.

## Nota de producto
BCV, Binance y Google Backup son módulos profesionales, pero no deben meterse sin arquitectura.

Primero se cierra el flujo base.
Luego se agregan servicios externos de forma ordenada.
