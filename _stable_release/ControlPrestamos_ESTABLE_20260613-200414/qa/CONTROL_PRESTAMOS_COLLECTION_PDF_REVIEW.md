# Control Préstamos — Revisión QA del PDF de Cobranza

## Objetivo

Verificar que el PDF de cobranza por cliente sea claro, profesional y útil para enviar por WhatsApp.

---

# Debe mostrar

- Nombre del negocio.
- Título: Estado de cobranza del cliente.
- Datos del cliente.
- Estado del cliente.
- Total a cobrar.
- Total pagado.
- Saldo pendiente.
- Saldo vencido.
- Cantidad de préstamos activos.
- Cantidad de cuotas vencidas.
- Detalle de préstamos.
- Cuotas vencidas.
- Próximas cuotas pendientes.
- Mensaje sugerido para WhatsApp.
- Lema: Gestiona. Controla. Haz crecer tu negocio.

---

# Reglas

## Cliente archivado

Puede generar PDF porque conserva historial.

No debe participar en operación principal.

---

## Préstamo cancelado

No debe contar como préstamo operativo cobrable.

Puede seguir existiendo en historial.

---

## Pago anulado

No debe sumar al total pagado.

Debe quedar en historial de pagos, pero no como dinero cobrado.

---

# Prueba manual recomendada

1. Crear un cliente activo.
2. Crear un préstamo.
3. Registrar un pago parcial.
4. Generar PDF de cobranza.
5. Verificar saldo pendiente.
6. Marcar cuotas vencidas si aplica.
7. Generar PDF otra vez.
8. Anular un pago.
9. Generar PDF otra vez.
10. Confirmar que el saldo volvió a calcularse correctamente.
11. Archivar el cliente.
12. Confirmar que todavía permite generar PDF de cobranza desde el detalle.
