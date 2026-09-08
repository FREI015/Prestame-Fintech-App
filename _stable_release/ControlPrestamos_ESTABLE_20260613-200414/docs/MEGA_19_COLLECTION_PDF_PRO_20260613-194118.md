# MEGA 19 - PDF de cobranza profesional

Fecha:
20260613-194118

Objetivo:
Mejorar el PDF de cobranza por cliente para que sea más profesional, claro y útil para enviar por WhatsApp.

Archivo principal modificado:
- app\src\main\java\com\controlprestamos\features\clients\domain\collection\ClientCollectionPdfGenerator.kt

Archivos relacionados revisados:
- app\src\main\java\com\controlprestamos\features\clients\presentation\ClientDetailScreen.kt
- app\src\main\java\com\controlprestamos\core\documents\PdfDocumentUtils.kt
- app\src\main\java\com\controlprestamos\core\documents\PdfShareUtils.kt
- app\src\main\java\com\controlprestamos\core\rules\FinancialOperationRules.kt

Backup:
_migration_backup\mega_19_collection_pdf_before_20260613-194118

QA:
- docs\qa\CONTROL_PRESTAMOS_COLLECTION_PDF_REVIEW.md
- docs\qa\CONTROL_PRESTAMOS_COLLECTION_PDF_SCAN_20260613-194118.txt

Cambios:
1. ClientCollectionPdfGenerator.kt
   - Encabezado profesional con fondo oscuro.
   - Datos del cliente.
   - Resumen de cartera.
   - Total a cobrar.
   - Total pagado.
   - Saldo pendiente.
   - Saldo vencido.
   - Préstamos activos.
   - Cuotas vencidas.
   - Detalle por préstamo.
   - Cuotas vencidas y próximas cuotas pendientes.
   - Mensaje sugerido para WhatsApp.
   - Uso de FinancialOperationRules para etiquetas de estado.

2. QA
   - Guía de revisión manual del PDF.
   - Scan de estructura del PDF.

Regla:
Este bloque no cambia persistencia, pagos, cuotas ni navegación. Solo mejora el PDF generado.
