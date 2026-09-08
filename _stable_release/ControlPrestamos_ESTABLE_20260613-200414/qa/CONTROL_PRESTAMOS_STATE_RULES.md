# Control Préstamos — Matriz de Reglas de Estado

## Objetivo

Definir de forma clara qué puede hacer cada entidad según su estado.

Esta matriz sirve como referencia para pantallas, reportes, PDF, auditoría, backup y pruebas manuales.

---

# 1. Cliente

## Cliente activo

Puede:

- aparecer en operación principal
- aparecer en dashboard operativo
- aparecer en reportes operativos
- recibir préstamos nuevos
- registrar pagos operativos en sus préstamos activos
- generar PDF de cobranza
- ser archivado

No debería:

- duplicarse por documento
- duplicarse por teléfono

---

## Cliente archivado

Puede:

- conservar historial
- conservar préstamos
- conservar pagos
- aparecer en historial
- aparecer en filtro Archivados
- generar PDF de cobranza
- ser reactivado

No puede:

- recibir préstamos nuevos
- registrar pagos operativos nuevos
- aparecer como cliente activo
- contar en dashboard operativo
- contar en reportes operativos
- aparecer en cobranza principal

---

# 2. Préstamo

## Préstamo activo

Puede:

- recibir pagos
- aparecer en cartera operativa si su cliente está activo
- aparecer en dashboard operativo si su cliente está activo
- aparecer en reportes operativos si su cliente está activo
- cambiar a pagado si su saldo llega a cero

No debería:

- editar campos financieros si ya tiene pagos
- perder sus cuotas al restaurar backup

---

## Préstamo pagado

Puede:

- conservar historial
- aparecer como pagado
- mantener pagos asociados
- aparecer en detalle del cliente

No puede:

- recibir pagos nuevos
- editar campos financieros

---

## Préstamo cancelado

Puede:

- conservar historial
- aparecer como cancelado
- mantener pagos históricos asociados

No puede:

- recibir pagos nuevos
- contar en reportes operativos
- contar en dashboard operativo
- generar cuotas operativas nuevas

---

# 3. Pago

## Pago activo

Puede:

- sumar al total cobrado
- reducir saldo pendiente
- aplicarse a cuotas
- aparecer en reportes financieros
- generar recibo

Puede anularse si fue registrado por error.

---

## Pago anulado

Puede:

- quedar en historial
- mostrar motivo de anulación
- mostrar fecha de anulación
- conservar recibo histórico si aplica

No puede:

- sumar al total cobrado
- reducir saldo pendiente
- contar en dashboard
- contar en reportes financieros
- aplicarse a cuotas activas

---

# 4. Cuota

## Cuota pendiente

Puede:

- recibir abonos
- pasar a parcial
- pasar a pagada
- pasar a vencida si supera fecha

---

## Cuota parcial

Puede:

- recibir más abonos
- pasar a pagada
- pasar a vencida si corresponde

---

## Cuota pagada

Puede:

- conservar historial de pago

No debe:

- recibir más saldo si ya está cubierta

---

## Cuota vencida

Puede:

- recibir pagos
- aparecer en cobranza prioritaria

---

## Cuota cancelada

Puede:

- conservar historial

No debe:

- contar como pendiente operativo

---

# 5. Reglas globales

## Operación principal

Incluye:

- clientes activos
- préstamos activos de clientes activos
- pagos activos
- cuotas pendientes/parciales/vencidas de préstamos operativos

Excluye:

- clientes archivados
- préstamos cancelados
- pagos anulados

---

## Historial

Incluye:

- clientes activos
- clientes archivados
- préstamos activos
- préstamos pagados
- préstamos cancelados
- pagos activos
- pagos anulados

---

## Backup

Debe conservar:

- clientes activos
- clientes archivados
- préstamos
- cuotas
- pagos activos
- pagos anulados
- preferencias

Después de restaurar:

- debe recargar repositorios
- debe reconstruir cuotas
- debe recalcular estado financiero

---

# 6. Regla de oro

Nada financiero se borra si tiene historial.

Se archiva, se cancela o se anula, pero no se elimina de la historia.
