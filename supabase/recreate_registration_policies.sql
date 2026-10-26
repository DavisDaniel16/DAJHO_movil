-- Ejecutar en Supabase SQL Editor con permisos de administrador.
-- Elimina políticas antiguas o restrictivas y recrea las de registro como
-- políticas permisivas explícitas.

ALTER TABLE public.negocios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.miembros_negocio ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Acceso propio negocios" ON public.negocios;
DROP POLICY IF EXISTS "Crear negocio autenticado" ON public.negocios;
DROP POLICY IF EXISTS "Acceso propio perfiles" ON public.perfiles;
DROP POLICY IF EXISTS "Acceso propio miembros" ON public.miembros_negocio;

CREATE POLICY "Crear negocio autenticado"
ON public.negocios
AS PERMISSIVE
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Leer negocios propios"
ON public.negocios
AS PERMISSIVE
FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1
        FROM public.miembros_negocio m
        WHERE m.negocio_id = negocios.id
          AND m.usuario_id = auth.uid()
    )
);

CREATE POLICY "Gestionar perfil propio"
ON public.perfiles
AS PERMISSIVE
FOR ALL
TO authenticated
USING (usuario_id = auth.uid())
WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Gestionar membresía propia"
ON public.miembros_negocio
AS PERMISSIVE
FOR ALL
TO authenticated
USING (usuario_id = auth.uid())
WITH CHECK (usuario_id = auth.uid());

GRANT USAGE ON SCHEMA public TO authenticated;
GRANT SELECT, INSERT, UPDATE
ON public.negocios, public.perfiles, public.miembros_negocio
TO authenticated;
