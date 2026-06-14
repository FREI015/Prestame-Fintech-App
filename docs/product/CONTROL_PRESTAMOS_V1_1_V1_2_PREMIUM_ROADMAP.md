# Control Préstamos — Roadmap Premium V1.1 / V1.2

Documento oficial de alcance para evolución profesional de la app Control Préstamos.

Este roadmap parte de la versión estable:

- `v1.0.0-estable`
- `v1.0.0-release-firmado`

Y continúa sobre la rama:

- `feature/v1.1-premium-dashboard-security`

---

# 1. Estado actual confirmado

## 1.1 Respaldo y release

Estado:

- Proyecto limpio respaldado en GitHub.
- Repositorio local limpio.
- Rama principal `main` estable.
- Tag `v1.0.0-estable` creado.
- Tag `v1.0.0-release-firmado` creado.
- APK release firmado generado.
- APK release firmado verificado con `apksigner`.
- APK release firmado instalado en teléfono real.

## 1.2 V1.1 iniciada

Primer bloque aplicado:

- Rama `feature/v1.1-premium-dashboard-security`.
- Corrección de bloqueo lógico.
- Login premium inicial.
- Versión actualizada a `1.1.0-dev`.
- Build debug validado.

---

# 2. Objetivo general V1.1

Convertir Control Préstamos en una app más premium, más segura y más profesional, manteniendo la lógica sana:

- Sin duplicidades.
- Sin conexiones huérfanas.
- Sin archivos basura.
- Sin secretos en Git.
- Sin romper la versión estable.
- Todo conectado a datos reales.
- Todo validado con build antes de commit.

---

# 3. Seguridad y Login Premium

## 3.1 Problema detectado

Cuando la app queda inactiva y el usuario vuelve o retrocede, no debe permitir regresar al módulo central si la sesión debe estar bloqueada.

## 3.2 Regla esperada

Si la app requiere desbloqueo:

- Debe volver al Login.
- Debe limpiar el backstack.
- No debe permitir regresar al Dashboard usando atrás.
- Debe ofrecer métodos de desbloqueo integrados:
  - Correo/contraseña.
  - PIN.
  - Huella / biometría.

## 3.3 Pendiente

Completar login con Google real.

El botón de Google no debe ser decorativo. Debe usar flujo oficial:

- Sign in with Google.
- Credential Manager cuando aplique.
- Cuenta asociada al usuario local.
- Preparar cumplimiento de Google Play.

---

# 4. Cumplimiento Google / Play Store

Si la app crea cuentas o permite autenticación con Google, debe contemplar:

- Política de privacidad.
- Declaración de datos usados.
- Eliminación de cuenta.
- Eliminación de datos asociados.
- Manejo claro de backup.
- Pantalla de ayuda/legal.
- Explicación clara del uso de Google Drive si se integra.

## 4.1 Regla interna

Toda explicación legal o técnica larga debe estar en Ayuda.

La app no debe llenar pantallas operativas con comentarios innecesarios.

---

# 5. Dashboard Premium

## 5.1 Objetivo

Mejorar el inicio para que se sienta como panel ejecutivo real.

## 5.2 Tarjeta principal superior

Reemplazar o reorganizar métricas para que la primera lectura sea financiera:

- Capital colocado.
- Utilidad pactada.
- Total a recaudar.
- Cobrado.
- Pendiente por recaudar.
- Vencido.
- Cobrado hoy.
- Cobrado semana.
- Cobrado mes.
- Promedio diario.

## 5.3 Nombres profesionales sugeridos

- Capital colocado: dinero principal prestado.
- Utilidad pactada: ganancia o interés esperado.
- Cartera objetivo: capital + utilidad pactada.
- Recaudado: pagos efectivos activos.
- Saldo por recaudar: total objetivo menos pagos efectivos.
- Mora / vencido: cuotas vencidas no cubiertas.

## 5.4 Salud de cartera

Debe verse más premium:

- Barra de progreso.
- Indicadores visuales.
- Porcentaje recaudado.
- Porcentaje pendiente.
- Riesgo por mora.
- Estado textual:
  - Saludable.
  - En observación.
  - Riesgo moderado.
  - Riesgo alto.

## 5.5 Alertas premium

Las alertas deben verse más claras y profesionales:

- Cobros de hoy.
- Cobros vencidos.
- Clientes con mora.
- Pagos recientes.
- Préstamos próximos a cerrar.
- Clientes archivados no deben contaminar alertas operativas.

---

# 6. Lista premium de clientes en Inicio

Después de alertas debe aparecer una lista compacta de clientes activos.

Cada tarjeta debe mostrar:

- Foto/avatar del cliente.
- Nombre.
- Estado breve.
- Capital prestado.
- Porcentaje o utilidad pactada.
- Total a recaudar.
- Monto cobrado.
- Pendiente.
- Próximo cobro.
- Estado de cuota:
  - Al día.
  - Vence hoy.
  - Vencido.
  - Pagado.
  - Archivado, solo si se muestra en historial.

La foto debe visualizarse en todos los lugares donde el cliente aparezca:

- Clientes.
- Detalle del cliente.
- Préstamos del cliente.
- Pagos del cliente.
- Dashboard.
- Reportes/PDF.
- Cobranza.

Si no hay foto, no usar signo de interrogación. Usar icono profesional de cliente/persona.

---

# 7. Estadísticas de colocación y cobranza

Reincorporar visualmente indicadores como:

- Colocación últimos 7 días.
- Cobranza últimos 7 días.
- Movimientos diarios.
- Días con préstamos creados.
- Días con pagos recibidos.
- Tendencia simple.

Debe conectarse a datos reales:

- Préstamos activos.
- Pagos activos.
- Cuotas vencidas.
- Clientes activos.

No debe contar:

- Préstamos cancelados.
- Pagos anulados.
- Clientes archivados en operación.

---

# 8. Preferencias visuales premium

Agregar en Preferencias:

## 8.1 Escala visual

Permitir modificar tamaño visual de la app:

- Compacta.
- Normal.
- Cómoda.
- Grande.

Debe afectar:

- Espaciado.
- Tamaño de tarjetas.
- Densidad de listas.
- Algunos tamaños de texto.

## 8.2 Temas cromáticos

Agregar temas profesionales:

### Azul ejecutivo

Base actual:

- Azul oscuro.
- Turquesa.
- Blanco.
- Grises financieros.

### Verde financiero

Enfoque:

- Verde sobrio.
- Verde éxito.
- Fondo claro profesional.
- Uso moderado para no parecer informal.

### Dorado premium

Enfoque:

- Azul oscuro + dorado.
- Dorado solo como acento.
- Ideal para reportes y panel ejecutivo.

Regla:

- Los temas no deben romper contraste.
- Deben verse profesionales.
- Deben aplicarse globalmente desde preferencias.

---

# 9. Monedas, tasas y conversión

## 9.1 Monedas base

Agregar soporte real para:

- Bolívares venezolanos.
- Dólar estadounidense.
- Euro.
- Cripto referencial, especialmente USDT cuando aplique.

## 9.2 Tasas

El usuario final puede prestar con distintas tasas.

La app debe permitir:

- Tasa manual.
- Tasa oficial BCV como referencia.
- Tasa EUR/BCV como referencia.
- Tasa cripto referencial, por ejemplo Binance, cuando aplique.
- Fecha y fuente de la tasa.
- Historial de tasas usadas en cada operación.

## 9.3 Regla importante

Cada préstamo debe guardar la tasa usada al momento de crearlo.

No se debe recalcular un préstamo viejo automáticamente solo porque cambió la tasa.

## 9.4 Arquitectura recomendada

Crear proveedores de tasa:

- `ExchangeRateProvider`
- `ManualRateProvider`
- `BcvRateProvider`
- `BinanceRateProvider`

Cada proveedor debe devolver:

- moneda origen
- moneda destino
- tasa
- fuente
- fecha/hora
- estado: actualizado, manual, error, cache

Si no hay internet o falla la API:

- usar última tasa guardada
- permitir tasa manual
- mostrar advertencia clara

---

# 10. Reportes y PDF premium

## 10.1 Problema

Los reportes actuales se sienten poco profesionales.

## 10.2 Objetivo

Rediseñar exportaciones con branding premium:

- Logo/nombre de la app.
- Nombre del negocio del usuario.
- Datos del prestamista.
- Colores del tema elegido.
- Fecha de generación.
- Resumen ejecutivo.
- Tablas limpias.
- Totales claros.
- Pie con nota legal/configurable.

## 10.3 Tipos de PDF requeridos

- PDF general de préstamos.
- PDF general de cobranza.
- PDF por cliente.
- PDF de recibo de pago.
- PDF de estado de cuenta del cliente.
- PDF de cartera.
- PDF de auditoría.
- PDF de carta de compromiso / contrato.

## 10.4 Exportación textual

Además del PDF, mejorar mensajes de texto:

- Mensaje WhatsApp de cobro.
- Mensaje de resumen de deuda.
- Mensaje de recibo.
- Mensaje de recordatorio.
- Mensaje de vencimiento.
- Mensaje de acuerdo/carta de compromiso.

Estos textos deben ser configurables desde preferencias/plantillas.

---

# 11. Datos de cobro del prestamista

Crear sección de datos de cobro.

Debe permitir registrar métodos nacionales e internacionales.

## 11.1 Venezuela

Campos sugeridos:

- Banco.
- Titular.
- Cédula/RIF.
- Teléfono pago móvil.
- Número de cuenta.
- Tipo de cuenta.
- Correo.
- Notas.

## 11.2 Internacional

Campos sugeridos:

- Zelle.
- PayPal.
- Binance Pay.
- USDT wallet.
- Wise.
- Cuenta bancaria internacional.
- Datos adicionales.

## 11.3 Regla

Si el método de pago es muy específico, dejar opción:

- "Consultar datos directamente con el proveedor/prestamista."

---

# 12. Cartas de compromiso / contratos

## 12.1 Objetivo

Permitir generar una carta de compromiso asociada a un préstamo.

Debe incluir:

- Datos del prestamista.
- Datos del cliente.
- Monto prestado.
- Interés/utilidad pactada.
- Total a pagar.
- Fechas.
- Frecuencia de pago.
- Mora/penalidad si aplica.
- Métodos de pago.
- Condiciones.
- Aceptación.

## 12.2 Aceptación digital

Idea deseada:

- Generar link.
- El cliente abre el link.
- Lee términos.
- Acepta.
- La app guarda evidencia.

## 12.3 Evidencia mínima

Guardar:

- fecha/hora
- nombre del aceptante
- documento
- teléfono/correo
- IP si aplica
- versión del contrato
- PDF generado
- hash del documento
- estado de aceptación

## 12.4 Advertencia legal

La app puede ayudar a generar y guardar aceptación, pero la validez legal específica depende del país, redacción, identificación, firma, consentimiento y normativa aplicable.

Antes de venderlo como contrato legalmente vinculante, debe revisarlo un abogado.

---

# 13. Google Drive Backup

## 13.1 Objetivo

Agregar respaldo en Google Drive.

Opciones:

- Backup manual a Drive.
- Restaurar desde Drive.
- Último backup visible.
- Historial de respaldos.
- Validación antes de restaurar.

## 13.2 Seguridad

No guardar contraseñas planas.

No subir keystore.

No subir secretos.

Cifrar backup antes de enviarlo a Drive si se implementa nube real.

---

# 14. Ayuda funcional

La sección Ayuda debe convertirse en un módulo útil.

Debe incluir:

- Cómo crear cliente.
- Cómo crear préstamo.
- Cómo registrar pago.
- Diferencia entre pago, abono y cobro.
- Cómo anular pagos.
- Cómo archivar clientes.
- Cómo generar PDF.
- Cómo hacer backup.
- Cómo restaurar backup.
- Cómo configurar moneda.
- Cómo funcionan las tasas.
- Cómo funciona la seguridad.
- Privacidad.
- Eliminación de cuenta/datos.
- Uso de Google.
- Límites legales de contratos y cartas de compromiso.

---

# 15. Auditoría

La auditoría debe ser limpia y profesional.

Quitar comentarios innecesarios.

Mantener:

- Acción.
- Fecha.
- Usuario.
- Entidad afectada.
- Estado anterior.
- Estado nuevo.
- Motivo.
- Resultado.

Las explicaciones largas deben moverse a Ayuda.

---

# 16. Orden de implementación recomendado

## Fase 1 — V1.1 Premium local

1. Seguridad/login premium.
2. Dashboard premium.
3. Tarjetas de cliente premium.
4. Alertas de cobro.
5. Preferencias visuales.
6. Monedas manuales.
7. Reportes/PDF premium.
8. Ayuda funcional.
9. Auditoría limpia.

## Fase 2 — V1.2 Conectividad

1. Login Google real.
2. Privacidad/eliminación de cuenta.
3. Google Drive backup.
4. Tasas BCV/EUR.
5. Binance/cripto referencial.
6. Carta de compromiso.
7. Aceptación por link.
8. Respaldo de contrato en Drive.

---

# 17. Reglas de salud técnica

Cada bloque debe cumplir:

- Crear backup local antes de tocar archivos.
- Trabajar en rama.
- Compilar debug.
- No subir secretos.
- No subir APK.
- Commit pequeño y claro.
- Push de rama.
- Reporte QA.
- No tocar `main` hasta validar.
- No mezclar módulos grandes sin necesidad.

---

# 18. Próximo bloque sugerido

`MEGA BLOQUE 30C - Dashboard premium conectado a cartera real`

Debe implementar:

- Capital colocado.
- Utilidad pactada.
- Cartera objetivo.
- Recaudado.
- Saldo por recaudar.
- Salud de cartera.
- Alertas premium.
- Lista compacta de clientes con foto/avatar.
- Próximo cobro.
- Colocación/cobranza últimos 7 días.

