
# Supabase Migration Rules


Principios:


1. No migrar sin modelo aprobado.


2. Toda tabla debe tener:

- id UUID
- created_at
- updated_at


3. Datos financieros:

- auditoría
- historial
- trazabilidad


4. Seguridad:

RLS obligatorio.


5. Migraciones:

Versionadas.


