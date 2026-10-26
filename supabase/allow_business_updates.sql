-- Permite que los miembros actualicen los datos de su propio negocio.
-- Ejecutar una vez en Supabase SQL Editor.

GRANT UPDATE ON public.negocios TO authenticated;

DROP POLICY IF EXISTS "Actualizar negocio propio" ON public.negocios;

CREATE POLICY "Actualizar negocio propio"
ON public.negocios
AS PERMISSIVE
FOR UPDATE
TO authenticated
USING (
    EXISTS (
        SELECT 1
        FROM public.miembros_negocio m
        WHERE m.negocio_id = negocios.id
          AND m.usuario_id = auth.uid()
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1
        FROM public.miembros_negocio m
        WHERE m.negocio_id = negocios.id
          AND m.usuario_id = auth.uid()
    )
);
