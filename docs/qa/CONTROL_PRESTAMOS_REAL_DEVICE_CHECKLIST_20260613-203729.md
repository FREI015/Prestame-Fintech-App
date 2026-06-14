# Control Préstamos — Checklist real en teléfono

Fecha:
20260613-203729

APK probado:
C:\Users\freil\Desktop\ControlPrestamosApp_Profesional\_stable_release\ControlPrestamos_ESTABLE_20260613-200414\apks\app-debug_debug_20260613-202323.apk

Release:
C:\Users\freil\Desktop\ControlPrestamosApp_Profesional\_stable_release\ControlPrestamos_ESTABLE_20260613-200414

## Estado de instalación

- ADB disponible: True
- Estado dispositivo: device-authorized
- Estado instalación: success

---

# Prueba manual obligatoria

## Inicio

- [ ] La app abre sin cerrarse.
- [ ] Se muestra el nombre Control Préstamos.
- [ ] La navegación inferior o principal responde correctamente.
- [ ] Los colores se ven profesionales y consistentes.
- [ ] No hay textos cortados o encimados.

---

## Clientes

- [ ] Crear cliente nuevo.
- [ ] Ver cliente en lista de activos.
- [ ] Entrar al detalle del cliente.
- [ ] Intentar crear cliente duplicado.
- [ ] Confirmar que la app bloquea duplicados.
- [ ] Archivar cliente.
- [ ] Confirmar que desaparece de operación principal.
- [ ] Ver cliente en archivados.
- [ ] Reactivar cliente.
- [ ] Confirmar que vuelve a activos.

---

## Préstamos

- [ ] Crear préstamo a cliente activo.
- [ ] Confirmar que aparece en detalle del cliente.
- [ ] Confirmar que aparece en lista de préstamos.
- [ ] Confirmar cuotas generadas.
- [ ] Intentar crear préstamo a cliente archivado.
- [ ] Confirmar que la app lo bloquea.
- [ ] Editar préstamo sin pagos.
- [ ] Confirmar que permite editar campos financieros.
- [ ] Registrar pago y luego intentar editar préstamo.
- [ ] Confirmar que protege campos financieros.

---

## Pagos

- [ ] Registrar pago parcial.
- [ ] Confirmar que el pago aparece en historial.
- [ ] Confirmar que baja el saldo.
- [ ] Confirmar que afecta cuotas.
- [ ] Registrar pago que salde préstamo.
- [ ] Confirmar que el préstamo pasa a pagado.
- [ ] Intentar pagar préstamo pagado.
- [ ] Confirmar que la app lo bloquea.
- [ ] Anular pago.
- [ ] Confirmar que el pago queda en historial como anulado.
- [ ] Confirmar que el saldo se recalcula.
- [ ] Confirmar que cuotas se reconstruyen.

---

## Cuotas

- [ ] Revisar cuotas pendientes.
- [ ] Revisar cuotas parciales.
- [ ] Revisar cuotas pagadas.
- [ ] Revisar cuotas vencidas si aplica.
- [ ] Confirmar que las cuotas canceladas no cuentan como operativas.

---

## Dashboard

- [ ] Confirmar clientes activos correctos.
- [ ] Confirmar préstamos activos correctos.
- [ ] Confirmar saldos correctos.
- [ ] Confirmar que clientes archivados no contaminan operación.
- [ ] Confirmar que pagos anulados no suman.

---

## Reportes

- [ ] Abrir reportes.
- [ ] Confirmar totales operativos.
- [ ] Confirmar que pagos anulados no suman.
- [ ] Confirmar que préstamos cancelados no cuentan.
- [ ] Confirmar que clientes archivados no contaminan operación.

---

## PDF de cobranza

- [ ] Abrir detalle de cliente.
- [ ] Tocar PDF de cobranza.
- [ ] Confirmar que se genera PDF.
- [ ] Confirmar que muestra datos del cliente.
- [ ] Confirmar que muestra resumen de cartera.
- [ ] Confirmar que muestra saldo pendiente.
- [ ] Confirmar que muestra cuotas vencidas.
- [ ] Confirmar que muestra mensaje sugerido para WhatsApp.
- [ ] Compartir PDF por WhatsApp o visor disponible.

---

## PDF de recibo

- [ ] Abrir historial de pagos.
- [ ] Generar recibo de pago.
- [ ] Confirmar datos del negocio.
- [ ] Confirmar datos del cliente.
- [ ] Confirmar monto.
- [ ] Confirmar estado del pago.
- [ ] Compartir PDF.

---

## Backup y restore

- [ ] Crear backup.
- [ ] Confirmar que se genera archivo.
- [ ] Restaurar backup.
- [ ] Confirmar vista previa antes de restaurar.
- [ ] Confirmar que clientes vuelven.
- [ ] Confirmar que préstamos vuelven.
- [ ] Confirmar que pagos vuelven.
- [ ] Confirmar que cuotas se recalculan.
- [ ] Confirmar que dashboard queda consistente.

---

## Auditoría financiera

- [ ] Abrir auditoría financiera.
- [ ] Confirmar que carga sin errores.
- [ ] Revisar alertas.
- [ ] Anular un pago y volver a auditoría.
- [ ] Confirmar que no rompe balances.
- [ ] Confirmar que pagos huérfanos no aparecen en flujo normal.

---

# Resultado final manual

- [ ] APROBADO PARA USO INTERNO
- [ ] REQUIERE AJUSTES VISUALES
- [ ] REQUIERE AJUSTES FUNCIONALES
- [ ] REQUIERE AJUSTES DE BACKUP
- [ ] REQUIERE AJUSTES DE PDF

Notas:

