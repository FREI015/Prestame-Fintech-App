# Control Préstamos — Mapa de Navegación

## Dashboard

Entrada principal de operación.

Debe mostrar:

- clientes activos
- cartera operativa
- pagos activos
- cuotas pendientes, parciales o vencidas

No debe contar:

- clientes archivados
- préstamos cancelados
- pagos anulados

---

## Clientes

Flujo esperado:

Clientes
→ Crear cliente

Clientes
→ Detalle del cliente

Detalle del cliente
→ Editar cliente

Detalle del cliente
→ Crear préstamo

Detalle del cliente
→ Cartera del cliente

Detalle del cliente
→ PDF de cobranza

Detalle del cliente
→ Archivar cliente

Detalle del cliente
→ Reactivar cliente

---

## Préstamos

Flujo esperado:

Préstamos
→ Detalle del préstamo

Cliente
→ Crear préstamo

Cartera del cliente
→ Detalle del préstamo

Cartera del cliente
→ Registrar pago de un préstamo activo

---

## Pagos

Flujo esperado:

Pagos
→ Registrar pago

Detalle de préstamo
→ Historial de pagos del préstamo

Historial de pagos
→ Anular pago

Al anular un pago:

- el pago queda en historial
- no suma financieramente
- se recalculan cuotas
- se recalcula estado del préstamo

---

## Reportes

Debe mostrar datos operativos.

Debe excluir:

- clientes archivados
- préstamos cancelados
- pagos anulados

---

## Backup

Debe permitir:

- crear copia
- inspeccionar copia
- restaurar copia
- reconstruir estado financiero después de restaurar

---

## Auditoría financiera

Debe revisar:

- pagos huérfanos
- préstamos sin cuotas
- préstamos con saldo inconsistente
- pagos anulados
- cuotas descuadradas

---

# Reglas generales

1. Cliente archivado no crea préstamos.
2. Cliente archivado no registra pagos.
3. Préstamo pagado no recibe pagos.
4. Préstamo cancelado no recibe pagos.
5. Pago anulado queda en historial pero no suma.
6. Historial no se borra.
7. Operación principal solo muestra cartera activa.
