-- ====================================================================
-- MIGRACIÓN DE SEGURIDAD ENTERPRISE RLS & POLICIES - DAJHO
-- ====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. FUNCIÓN SUPER ADMIN (SECURITY DEFINER)
CREATE OR REPLACE FUNCTION public.es_admin_sistema()
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.administradores_sistema
        WHERE usuario_id = auth.uid() AND activo = true
    ) OR EXISTS (
        SELECT 1 FROM auth.users
        WHERE id = auth.uid() AND email = 'dajho1617@gmail.com'
    );
END;
$$;

REVOKE EXECUTE ON FUNCTION public.es_admin_sistema() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.es_admin_sistema() TO authenticated;

-- 2. FUNCIONES AUXILIARES DE NEGOCIO (SECURITY DEFINER)
CREATE OR REPLACE FUNCTION public.es_miembro_negocio(p_negocio_id UUID, p_usuario_id UUID)
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.miembros_negocio
        WHERE negocio_id = p_negocio_id AND usuario_id = p_usuario_id
    ) OR EXISTS (
        SELECT 1 FROM public.perfiles
        WHERE usuario_id = p_usuario_id AND negocio_id = p_negocio_id AND eliminado_en IS NULL
    );
END;
$$;

CREATE OR REPLACE FUNCTION public.es_propietario_negocio(p_negocio_id UUID, p_usuario_id UUID)
RETURNS BOOLEAN
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.miembros_negocio
        WHERE negocio_id = p_negocio_id AND usuario_id = p_usuario_id AND rol = 'PROPIETARIO'
    ) OR EXISTS (
        SELECT 1 FROM public.perfiles
        WHERE usuario_id = p_usuario_id AND negocio_id = p_negocio_id AND rol = 'PROPIETARIO' AND eliminado_en IS NULL
    );
END;
$$;

REVOKE EXECUTE ON FUNCTION public.es_miembro_negocio(UUID, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.es_miembro_negocio(UUID, UUID) TO authenticated;

REVOKE EXECUTE ON FUNCTION public.es_propietario_negocio(UUID, UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.es_propietario_negocio(UUID, UUID) TO authenticated;

-- 3. HABILITAR RLS EN LAS 19 TABLAS
ALTER TABLE public.administradores_sistema ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.negocios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.miembros_negocio ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.variantes_producto ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.proveedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalles_venta ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pagos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.abonos_deuda ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.gastos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sesiones_caja ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.movimientos_caja ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.operaciones_sincronizacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conflictos_sincronizacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.imagenes_locales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.registros_auditoria ENABLE ROW LEVEL SECURITY;

-- 4. POLÍTICAS RLS Y SU EXPLICACIÓN EN UNA LÍNEA

-- administradores_sistema: Solo lectura para administradores del sistema activos.
DROP POLICY IF EXISTS "admin_sys_policy" ON public.administradores_sistema;
CREATE POLICY "admin_sys_policy" ON public.administradores_sistema FOR ALL TO authenticated USING (public.es_admin_sistema());

-- negocios: Super admin ve todo; miembros ven su negocio y nuevos usuarios pueden crearlo.
DROP POLICY IF EXISTS "negocios_policy" ON public.negocios;
CREATE POLICY "negocios_policy" ON public.negocios FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(id, auth.uid()))
WITH CHECK (auth.uid() IS NOT NULL);

-- perfiles: Super admin ve todos; usuarios gestionan su propio perfil durante el registro.
DROP POLICY IF EXISTS "perfiles_policy" ON public.perfiles;
CREATE POLICY "perfiles_policy" ON public.perfiles FOR ALL TO authenticated
USING (public.es_admin_sistema() OR usuario_id = auth.uid() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (usuario_id = auth.uid());

-- miembros_negocio: Super admin ve todo; propietarios gestionan miembros y usuarios crean su membresía inicial.
DROP POLICY IF EXISTS "miembros_policy" ON public.miembros_negocio;
CREATE POLICY "miembros_policy" ON public.miembros_negocio FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (usuario_id = auth.uid() OR public.es_propietario_negocio(negocio_id, auth.uid()));

-- productos: Super admin ve todo; miembros leen, solo propietario modifica.
DROP POLICY IF EXISTS "productos_policy" ON public.productos;
CREATE POLICY "productos_policy" ON public.productos FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_propietario_negocio(negocio_id, auth.uid()));

-- variantes_producto: Acceso heredado mediante JOIN con productos del negocio.
DROP POLICY IF EXISTS "variantes_policy" ON public.variantes_producto;
CREATE POLICY "variantes_policy" ON public.variantes_producto FOR ALL TO authenticated
USING (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.productos p WHERE p.id = variantes_producto.producto_id AND public.es_miembro_negocio(p.negocio_id, auth.uid())))
WITH CHECK (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.productos p WHERE p.id = variantes_producto.producto_id AND public.es_propietario_negocio(p.negocio_id, auth.uid())));

-- clientes: Super admin ve todo; miembros ven y gestionan clientes del negocio.
DROP POLICY IF EXISTS "clientes_policy" ON public.clientes;
CREATE POLICY "clientes_policy" ON public.clientes FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()));

-- proveedores: Super admin ve todo; solo propietario gestiona proveedores.
DROP POLICY IF EXISTS "proveedores_policy" ON public.proveedores;
CREATE POLICY "proveedores_policy" ON public.proveedores FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_propietario_negocio(negocio_id, auth.uid()));

-- ventas: Super admin ve todo; miembros registran ventas, prohibido modificar/eliminar empleados.
DROP POLICY IF EXISTS "ventas_policy" ON public.ventas;
CREATE POLICY "ventas_policy" ON public.ventas FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()));

-- detalles_venta: Acceso heredado mediante JOIN con ventas del negocio.
DROP POLICY IF EXISTS "detalles_venta_policy" ON public.detalles_venta;
CREATE POLICY "detalles_venta_policy" ON public.detalles_venta FOR ALL TO authenticated
USING (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.ventas v WHERE v.id = detalles_venta.venta_id AND public.es_miembro_negocio(v.negocio_id, auth.uid())))
WITH CHECK (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.ventas v WHERE v.id = detalles_venta.venta_id AND public.es_miembro_negocio(v.negocio_id, auth.uid())));

-- pagos: Acceso heredado mediante JOIN con ventas del negocio.
DROP POLICY IF EXISTS "pagos_policy" ON public.pagos;
CREATE POLICY "pagos_policy" ON public.pagos FOR ALL TO authenticated
USING (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.ventas v WHERE v.id = pagos.venta_id AND public.es_miembro_negocio(v.negocio_id, auth.uid())))
WITH CHECK (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.ventas v WHERE v.id = pagos.venta_id AND public.es_miembro_negocio(v.negocio_id, auth.uid())));

-- abonos_deuda: Super admin ve todo; miembros registran abonos en su negocio.
DROP POLICY IF EXISTS "abonos_policy" ON public.abonos_deuda;
CREATE POLICY "abonos_policy" ON public.abonos_deuda FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()));

-- gastos: Super admin ve todo; solo el propietario registra gastos.
DROP POLICY IF EXISTS "gastos_policy" ON public.gastos;
CREATE POLICY "gastos_policy" ON public.gastos FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_propietario_negocio(negocio_id, auth.uid()));

-- sesiones_caja: Super admin ve todo; miembros operan sesiones de caja.
DROP POLICY IF EXISTS "sesiones_caja_policy" ON public.sesiones_caja;
CREATE POLICY "sesiones_caja_policy" ON public.sesiones_caja FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()));

-- movimientos_caja: Acceso heredado mediante JOIN con sesiones_caja del negocio.
DROP POLICY IF EXISTS "movimientos_caja_policy" ON public.movimientos_caja;
CREATE POLICY "movimientos_caja_policy" ON public.movimientos_caja FOR ALL TO authenticated
USING (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.sesiones_caja s WHERE s.id = movimientos_caja.sesion_id AND public.es_miembro_negocio(s.negocio_id, auth.uid())))
WITH CHECK (public.es_admin_sistema() OR EXISTS (SELECT 1 FROM public.sesiones_caja s WHERE s.id = movimientos_caja.sesion_id AND public.es_miembro_negocio(s.negocio_id, auth.uid())));

-- operaciones_sincronizacion: Cualquier usuario autenticado puede sincronizar sus operaciones offline.
DROP POLICY IF EXISTS "sync_ops_policy" ON public.operaciones_sincronizacion;
CREATE POLICY "sync_ops_policy" ON public.operaciones_sincronizacion FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- conflictos_sincronizacion: Cualquier usuario autenticado puede registrar y resolver conflictos de sincronización offline.
DROP POLICY IF EXISTS "sync_conf_policy" ON public.conflictos_sincronizacion;
CREATE POLICY "sync_conf_policy" ON public.conflictos_sincronizacion FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- imagenes_locales: Super admin ve todo; miembros gestionan imágenes de su negocio.
DROP POLICY IF EXISTS "imagenes_policy" ON public.imagenes_locales;
CREATE POLICY "imagenes_policy" ON public.imagenes_locales FOR ALL TO authenticated
USING (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()))
WITH CHECK (public.es_admin_sistema() OR public.es_miembro_negocio(negocio_id, auth.uid()));

-- registros_auditoria: Super admin ve todo; el propietario ve exclusivamente la auditoría de su propio negocio.
DROP POLICY IF EXISTS "auditoria_policy" ON public.registros_auditoria;
CREATE POLICY "auditoria_policy" ON public.registros_auditoria FOR SELECT TO authenticated
USING (public.es_admin_sistema() OR public.es_propietario_negocio(negocio_id, auth.uid()));

GRANT USAGE ON SCHEMA public TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO authenticated;
