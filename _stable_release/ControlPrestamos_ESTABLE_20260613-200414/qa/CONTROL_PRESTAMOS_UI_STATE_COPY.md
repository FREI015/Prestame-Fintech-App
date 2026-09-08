# Control Préstamos — Guía de Textos para Estados Financieros

## Objetivo

Unificar los textos visibles de la app para que el usuario entienda claramente qué puede hacer y qué no puede hacer según el estado de cada entidad.

---

# Cliente

## Activo

Texto recomendado:

Cliente activo: puede crear préstamos y registrar pagos operativos.

Uso:

- detalle del cliente
- listas operativas
- dashboard
- reportes

---

## Archivado

Texto recomendado:

Cliente archivado: su historial se conserva, pero no participa en la operación principal. Reactívalo para crear préstamos o registrar pagos.

Uso:

- detalle del cliente
- cartera del cliente
- bloqueo de nuevo préstamo
- bloqueo de nuevo pago

Regla:

Un cliente archivado no debe borrarse. Se conserva por historial.

---

# Préstamo

## Activo

Texto recomendado:

Préstamo activo: admite pagos y seguimiento de cuotas.

Uso:

- detalle del préstamo
- cartera del cliente
- cobranza
- reportes operativos

---

## Pagado

Texto recomendado:

Préstamo pagado: ya fue saldado por completo y no admite nuevos pagos.

Uso:

- detalle del préstamo
- bloqueo de nuevo pago
- historial

---

## Cancelado

Texto recomendado:

Préstamo cancelado: conserva historial, pero no admite nuevos pagos ni cuotas operativas.

Uso:

- detalle del préstamo
- bloqueo de nuevo pago
- historial

Regla:

Un préstamo cancelado no debe contar en operación principal.

---

# Pago

## Activo

Texto recomendado:

Pago activo: suma al total cobrado y reduce el saldo pendiente.

Uso:

- historial de pagos
- recibo
- reportes
- dashboard

---

## Anulado

Texto recomendado:

Pago anulado: queda en historial, pero no suma financieramente ni reduce saldo.

Uso:

- historial de pagos
- recibo histórico
- auditoría

Regla:

Un pago anulado no desaparece, pero no cuenta financieramente.

---

# Regla general de lenguaje

Usar:

- Archivado para clientes fuera de operación.
- Cancelado para préstamos detenidos.
- Anulado para pagos registrados por error.
- Pagado para préstamos saldados.
- Activo para operación vigente.

Evitar:

- mezclar pago, abono y cobro como si fueran lo mismo.
- decir eliminado cuando realmente fue archivado, cancelado o anulado.
- ocultar historial financiero.
