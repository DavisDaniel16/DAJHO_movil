-- Corrige los permisos necesarios para completar el registro desde la app.
-- Ejecutar una vez en Supabase SQL Editor con un rol administrador.

GRANT USAGE ON SCHEMA public TO authenticated;
GRANT SELECT, INSERT, UPDATE ON
    public.negocios,
    public.perfiles,
    public.miembros_negocio
TO authenticated;

ALTER TABLE public.negocios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.miembros_negocio ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Crear negocio autenticado" ON public.negocios;
CREATE POLICY "Crear negocio autenticado"
ON public.negocios
FOR INSERT
TO authenticated
WITH CHECK (true);

DROP POLICY IF EXISTS "Acceso propio negocios" ON public.negocios;
CREATE POLICY "Acceso propio negocios"
ON public.negocios
FOR SELECT
TO authenticated
USING (
    id IN (
        SELECT negocio_id
        FROM public.miembros_negocio
        WHERE usuario_id = auth.uid()
    )
);

DROP POLICY IF EXISTS "Acceso propio perfiles" ON public.perfiles;
CREATE POLICY "Acceso propio perfiles"
ON public.perfiles
FOR ALL
TO authenticated
USING (usuario_id = auth.uid())
WITH CHECK (usuario_id = auth.uid());

DROP POLICY IF EXISTS "Acceso propio miembros" ON public.miembros_negocio;
CREATE POLICY "Acceso propio miembros"
ON public.miembros_negocio
FOR ALL
TO authenticated
USING (usuario_id = auth.uid())
WITH CHECK (usuario_id = auth.uid());
