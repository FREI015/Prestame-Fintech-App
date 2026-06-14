# Control Préstamos — Checklist Técnico de Pruebas

## Objetivo

Validar que los flujos principales de la app funcionan después de las migraciones de clientes, préstamos, pagos, PDF, backup, reportes y auditoría.

---

# 1. Clientes

## Cliente activo

Resultado esperado:

- Se puede crear.
- Se puede editar.
- No permite duplicar documento.
- No permite duplicar teléfono.
- Aparece en Activos.
- Aparece en Todos.

Checklist:

- [ ] Crear cliente activo.
- [ ] Intentar duplicar documento.
- [ ] Intentar duplicar teléfono.
- [ ] Editar datos.
- [ ] Confirmar que aparece en Activos.
- [ ] Confirmar que aparece en Todos.

---

## Cliente archivado

Resultado esperado:

- Se puede archivar.
- Se puede reactivar.
- Conserva historial.
- No aparece como cliente operativo.
- No cuenta en dashboard operativo.
- No cuenta en reportes operativos.
- No permite crear préstamos nuevos.
- No permite pagos operativos nuevos.

Checklist:

- [ ] Archivar cliente.
- [ ] Confirmar que aparece en Archivados.
- [ ] Confirmar que no aparece en Activos.
- [ ] Intentar crear préstamo.
- [ ] Confirmar bloqueo.
- [ ] Intentar registrar pago operativo.
- [ ] Confirmar bloqueo.
- [ ] Generar PDF de cobranza.
- [ ] Reactivar cliente.
- [ ] Confirmar que vuelve a Activos.

---

# 2. Préstamos

Resultado esperado:

- Se crean solo para clientes activos.
- Calculan monto esperado.
- Generan cuotas.
- Respetan fecha de inicio.
- Se bloquea edición financiera cuando ya tiene movimientos.
- Se permite editar nota/descripción en modo protegido.

Checklist:

- [ ] Crear préstamo por cuotas.
- [ ] Crear préstamo de pago único.
- [ ] Verificar total esperado.
- [ ] Verificar cuotas generadas.
- [ ] Registrar pago.
- [ ] Intentar editar monto/interés/plazo.
- [ ] Confirmar modo protegido.
- [ ] Editar solo nota/descripción.

---

# 3. Pagos

Resultado esperado:

- Se registran solo en préstamos operativos.
- Se aplican a cuotas en orden.
- Actualizan saldo.
- Se pueden anular.
- El pago anulado queda en historial.
- El pago anulado no suma financieramente.

Checklist:

- [ ] Registrar pago parcial.
- [ ] Revisar cuotas parcialmente pagadas.
- [ ] Registrar pago total.
- [ ] Confirmar préstamo pagado.
- [ ] Anular pago.
- [ ] Confirmar historial.
- [ ] Confirmar saldo recalculado.
- [ ] Confirmar cuotas recalculadas.

---

# 4. PDF y recibos

Resultado esperado:

- Recibo de pago usa moneda configurada.
- Recibo de pago muestra cliente, préstamo, monto, método y referencia.
- PDF de cobranza por cliente muestra resumen personalizado.
- PDF de cobranza mantiene historial aunque el cliente esté archivado.

Checklist:

- [ ] Generar recibo de pago.
- [ ] Compartir recibo PDF.
- [ ] Generar PDF de cobranza de cliente activo.
- [ ] Archivar cliente.
- [ ] Generar PDF de cobranza de cliente archivado.
- [ ] Verificar moneda.
- [ ] Verificar nombre del negocio.

---

# 5. Dashboard

Resultado esperado:

- Solo cuenta clientes activos.
- Solo cuenta préstamos de clientes activos.
- No suma pagos anulados.
- No cuenta clientes archivados en operación principal.

Checklist:

- [ ] Revisar clientes activos.
- [ ] Revisar total prestado.
- [ ] Revisar total cobrado.
- [ ] Revisar saldo pendiente.
- [ ] Archivar cliente con deuda.
- [ ] Confirmar que dashboard operativo cambia.
- [ ] Reactivar cliente.
- [ ] Confirmar que dashboard vuelve a incluirlo.

---

# 6. Reportes

Resultado esperado:

- Reporte operativo excluye archivados.
- Muestra clientes activos.
- Usa moneda configurada.
- No suma anulados.

Checklist:

- [ ] Abrir reportes.
- [ ] Confirmar texto "Clientes activos".
- [ ] Confirmar cartera operativa.
- [ ] Comparar con dashboard.
- [ ] Generar reporte/PDF si aplica.

---

# 7. Backup y restauración

Resultado esperado:

- Backup incluye clientes, extras, préstamos, cuotas, pagos y preferencias.
- Restauración tiene vista previa.
- Restauración recarga repositorios.
- Restauración reconstruye cuotas.
- Conserva anulados y archivados.

Checklist:

- [ ] Crear datos de prueba.
- [ ] Crear cliente activo.
- [ ] Crear cliente archivado.
- [ ] Crear préstamo.
- [ ] Registrar pago.
- [ ] Anular pago.
- [ ] Crear backup.
- [ ] Restaurar backup.
- [ ] Confirmar vista previa.
- [ ] Confirmar datos restaurados.
- [ ] Confirmar auditoría.

---

# 8. Auditoría financiera

Resultado esperado:

- Revisa saldos.
- Revisa cuotas.
- Revisa pagos huérfanos.
- Revisa préstamos sin cuotas.
- Mantiene vista global del historial.

Checklist:

- [ ] Abrir auditoría financiera.
- [ ] Revisar alertas.
- [ ] Revisar préstamos.
- [ ] Revisar pagos.
- [ ] Revisar cuotas.
- [ ] Confirmar que archivados no desaparecen del historial.

---

# Resultado final esperado

Cliente activo:
- operativo
- financiable
- cobrable
- visible en dashboard/reportes

Cliente archivado:
- histórico
- consultable
- reactivable
- excluido de operación principal

Pago activo:
- suma financieramente

Pago anulado:
- queda en historial
- no suma financieramente

Préstamo cancelado:
- queda en historial
- no participa en operación activa

Backup:
- conserva todo
- restaura todo
- reconstruye estado financiero
