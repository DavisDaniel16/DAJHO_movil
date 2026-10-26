-- ====================================================================
-- SCRIPT DEFINITIVO Y CORREGIDO DE MIGRACIÓN SQL PARA SUPABASE (DAJHO)
-- Evita recursión infinita en políticas RLS mediante funciones SECURITY DEFINER
-- ====================================================================

-- 1. Asegurar extensión UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Tabla de Administradores del Sistema
CREATE TABLE IF NOT EXISTS public.administradores_sistema (
    usuario_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    activo BOOLEAN NOT NULL DEFAULT true,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.administradores_sistema ENABLE ROW LEVEL SECURITY;

-- Insertar administrador global inicial (dajho1617@gmail.com)
INSERT INTO public.administradores_sistema (usuario_id, activo)
SELECT id, true FROM auth.users WHERE email = 'dajho1617@gmail.com'
ON CONFLICT (usuario_id) DO UPDATE SET activo = true;

-- 3. Tabla de Auditoría
CREATE TABLE IF NOT EXISTS public.registros_auditoria (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE SET NULL,
    accion TEXT NOT NULL,
    entidad TEXT NOT NULL,
    entidad_id TEXT,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.registros_auditoria ENABLE ROW LEVEL SECURITY;

-- 4. FUNCIONES SECURITY DEFINER (EVITAN RECURSIÓN INFINITA EN RLS)
CREATE OR REPLACE FUNCTION public.fn_is_system_admin(p_usuario_id UUID)
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.administradores_sistema
        WHERE usuario_id = p_usuario_id AND activo = true
    ) OR EXISTS (
        SELECT 1 FROM auth.users
        WHERE id = p_usuario_id AND email = 'dajho1617@gmail.com'
    );
END;
$$;

CREATE OR REPLACE FUNCTION public.fn_is_business_member(p_negocio_id UUID, p_usuario_id UUID)
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.miembros_negocio
        WHERE negocio_id = p_negocio_id AND usuario_id = p_usuario_id
    );
END;
$$;

CREATE OR REPLACE FUNCTION public.fn_is_business_owner(p_negocio_id UUID, p_usuario_id UUID)
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.miembros_negocio
        WHERE negocio_id = p_negocio_id AND usuario_id = p_usuario_id AND rol = 'PROPIETARIO'
    );
END;
$$;

REVOKE EXECUTE ON FUNCTION public.fn_is_system_admin(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.fn_is_system_admin(UUID) TO authenticated;

REVOKE EXECUTE ON FUNCTION public.fn_is_business_member(UUID, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.fn_is_business_member(UUID, UUID) TO authenticated;

REVOKE EXECUTE ON FUNCTION public.fn_is_business_owner(UUID, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.fn_is_business_owner(UUID, UUID) TO authenticated;

-- 5. ELIMINAR POLÍTICAS ANTERIORES
DROP POLICY IF EXISTS "Admin system select policy" ON public.administradores_sistema;
DROP POLICY IF EXISTS "Auditoria select policy" ON public.registros_auditoria;
DROP POLICY IF EXISTS "Auditoria insert policy" ON public.registros_auditoria;
DROP POLICY IF EXISTS "Negocios select policy" ON public.negocios;
DROP POLICY IF EXISTS "Negocios insert policy" ON public.negocios;
DROP POLICY IF EXISTS "Negocios update policy" ON public.negocios;
DROP POLICY IF EXISTS "Perfiles select policy" ON public.perfiles;
DROP POLICY IF EXISTS "Perfiles update policy" ON public.perfiles;
DROP POLICY IF EXISTS "Perfiles insert policy" ON public.perfiles;
DROP POLICY IF EXISTS "Miembros select policy" ON public.miembros_negocio;
DROP POLICY IF EXISTS "Miembros insert update policy" ON public.miembros_negocio;
DROP POLICY IF EXISTS "Productos select policy" ON public.productos;
DROP POLICY IF EXISTS "Productos modify policy" ON public.productos;
DROP POLICY IF EXISTS "Clientes select policy" ON public.clientes;
DROP POLICY IF EXISTS "Proveedores select policy" ON public.proveedores;
DROP POLICY IF EXISTS "Ventas select policy" ON public.ventas;

-- 6. CREAR POLÍTICAS RLS LIMPIAS (SIN RECURSIÓN)
CREATE POLICY "Admin system select policy"
ON public.administradores_sistema FOR SELECT TO authenticated
USING (usuario_id = auth.uid() OR public.fn_is_system_admin(auth.uid()));

CREATE POLICY "Auditoria select policy"
ON public.registros_auditoria FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_owner(negocio_id, auth.uid()));

CREATE POLICY "Auditoria insert policy"
ON public.registros_auditoria FOR INSERT TO authenticated
WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Negocios select policy"
ON public.negocios FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(id, auth.uid()));

CREATE POLICY "Negocios insert policy"
ON public.negocios FOR INSERT TO authenticated
WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Negocios update policy"
ON public.negocios FOR UPDATE TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_owner(id, auth.uid()))
WITH CHECK (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_owner(id, auth.uid()));

CREATE POLICY "Perfiles select policy"
ON public.perfiles FOR SELECT TO authenticated
USING (usuario_id = auth.uid() OR public.fn_is_system_admin(auth.uid()));

CREATE POLICY "Perfiles update policy"
ON public.perfiles FOR UPDATE TO authenticated
USING (usuario_id = auth.uid())
WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Perfiles insert policy"
ON public.perfiles FOR INSERT TO authenticated
WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Miembros select policy"
ON public.miembros_negocio FOR SELECT TO authenticated
USING (usuario_id = auth.uid() OR public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(negocio_id, auth.uid()));

CREATE POLICY "Miembros insert update policy"
ON public.miembros_negocio FOR ALL TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_owner(negocio_id, auth.uid()))
WITH CHECK (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_owner(negocio_id, auth.uid()));

CREATE POLICY "Productos select policy"
ON public.productos FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(negocio_id, auth.uid()));

CREATE POLICY "Clientes select policy"
ON public.clientes FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(negocio_id, auth.uid()));

CREATE POLICY "Proveedores select policy"
ON public.proveedores FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(negocio_id, auth.uid()));

CREATE POLICY "Ventas select policy"
ON public.ventas FOR SELECT TO authenticated
USING (public.fn_is_system_admin(auth.uid()) OR public.fn_is_business_member(negocio_id, auth.uid()));

-- 7. CONCESIÓN DE PERMISOS
GRANT USAGE ON SCHEMA public TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;
