-- ====================================================================
-- ESQUEMA PROFESIONAL ENTERPRISE DE BASE DE DATOS POSTGRESQL (SUPABASE & DAJHO)
-- Incluye: Stored Procedures Atomicos, Rollback, CHECK Constraints,
-- Triggers de Auditoría, Mínimo Privilegio y Seguridad RLS
-- ====================================================================

-- 1. LIMPIEZA DE OBJETOS PREVIOS (DROP CASCADE)
DROP VIEW IF EXISTS public.vista_ventas_diarias CASCADE;
DROP VIEW IF EXISTS public.vista_arqueo_resumen CASCADE;
DROP VIEW IF EXISTS public.vista_arqueo_caja CASCADE;

DROP FUNCTION IF EXISTS public.fn_procesar_venta_atomica CASCADE;
DROP FUNCTION IF EXISTS public.fn_procesar_abono_deuda CASCADE;
DROP FUNCTION IF EXISTS public.fn_actualizar_marca_tiempo CASCADE;

DROP TABLE IF EXISTS public.conflictos_sincronizacion CASCADE;
DROP TABLE IF EXISTS public.imagenes_locales CASCADE;
DROP TABLE IF EXISTS public.operaciones_sincronizacion CASCADE;
DROP TABLE IF EXISTS public.movimientos_caja CASCADE;
DROP TABLE IF EXISTS public.sesiones_caja CASCADE;
DROP TABLE IF EXISTS public.gastos CASCADE;
DROP TABLE IF EXISTS public.abonos_deuda CASCADE;
DROP TABLE IF EXISTS public.pagos CASCADE;
DROP TABLE IF EXISTS public.detalles_venta CASCADE;
DROP TABLE IF EXISTS public.ventas CASCADE;
DROP TABLE IF EXISTS public.proveedores CASCADE;
DROP TABLE IF EXISTS public.clientes CASCADE;
DROP TABLE IF EXISTS public.variantes_producto CASCADE;
DROP TABLE IF EXISTS public.productos CASCADE;
DROP TABLE IF EXISTS public.miembros_negocio CASCADE;
DROP TABLE IF EXISTS public.perfiles CASCADE;
DROP TABLE IF EXISTS public.negocios CASCADE;

-- Limpieza de versiones previas en inglés
DROP TABLE IF EXISTS public.sync_operations CASCADE;
DROP TABLE IF EXISTS public.cash_movements CASCADE;
DROP TABLE IF EXISTS public.cash_sessions CASCADE;
DROP TABLE IF EXISTS public.expenses CASCADE;
DROP TABLE IF EXISTS public.debt_payments CASCADE;
DROP TABLE IF EXISTS public.payments CASCADE;
DROP TABLE IF EXISTS public.sale_items CASCADE;
DROP TABLE IF EXISTS public.sales CASCADE;
DROP TABLE IF EXISTS public.suppliers CASCADE;
DROP TABLE IF EXISTS public.clients CASCADE;
DROP TABLE IF EXISTS public.product_variants CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.business_members CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;
DROP TABLE IF EXISTS public.businesses CASCADE;

-- Habilitar Extensión UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ====================================================================
-- 2. ESTRUCTURA DE TABLAS CON RESTRICCIONES (CHECK CONSTRAINTS)
-- ====================================================================

-- 2.1 NEGOCIOS
CREATE TABLE public.negocios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre TEXT NOT NULL CHECK (char_length(trim(nombre)) > 0),
    ruc_o_cedula TEXT DEFAULT '',
    telefono TEXT DEFAULT '',
    direccion TEXT DEFAULT '',
    tipo_rimpe TEXT DEFAULT 'RIMPE_POPULAR' CHECK (tipo_rimpe IN ('RIMPE_POPULAR', 'RIMPE_EMPRENDEDOR', 'GENERAL')),
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.2 PERFILES DE USUARIO
CREATE TABLE public.perfiles (
    usuario_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    nombre_completo TEXT NOT NULL CHECK (char_length(trim(nombre_completo)) > 0),
    correo TEXT UNIQUE NOT NULL CHECK (correo ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$'),
    telefono TEXT DEFAULT '',
    rol TEXT DEFAULT 'PROPIETARIO' CHECK (rol IN ('PROPIETARIO', 'CAJERO', 'VENDEDOR')),
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.3 MIEMBROS DE NEGOCIO
CREATE TABLE public.miembros_negocio (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    usuario_id UUID REFERENCES public.perfiles(usuario_id) ON DELETE CASCADE,
    rol TEXT DEFAULT 'PROPIETARIO',
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(negocio_id, usuario_id)
);

-- 2.4 PRODUCTOS
CREATE TABLE public.productos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    sku TEXT NOT NULL,
    nombre TEXT NOT NULL CHECK (char_length(trim(nombre)) > 0),
    marca TEXT DEFAULT 'DAJHO',
    categoria TEXT DEFAULT 'General',
    costo_centavos BIGINT NOT NULL CHECK (costo_centavos >= 0),
    precio_venta_centavos BIGINT NOT NULL CHECK (precio_venta_centavos >= costo_centavos),
    uri_imagen TEXT DEFAULT NULL,
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL,
    UNIQUE(negocio_id, sku)
);

-- 2.5 VARIANTES DE PRODUCTO
CREATE TABLE public.variantes_producto (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    producto_id UUID REFERENCES public.productos(id) ON DELETE CASCADE,
    talla TEXT DEFAULT 'Única',
    color TEXT DEFAULT 'Único',
    stock INT NOT NULL CHECK (stock >= 0),
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.6 CLIENTES
CREATE TABLE public.clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL CHECK (char_length(trim(nombre)) > 0),
    telefono TEXT DEFAULT '',
    info TEXT DEFAULT '',
    deuda_centavos BIGINT NOT NULL DEFAULT 0 CHECK (deuda_centavos >= 0),
    tipo TEXT DEFAULT 'CLIENTE',
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.7 PROVEEDORES
CREATE TABLE public.proveedores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL CHECK (char_length(trim(nombre)) > 0),
    telefono TEXT DEFAULT '',
    info TEXT DEFAULT '',
    deuda_centavos BIGINT NOT NULL DEFAULT 0 CHECK (deuda_centavos >= 0),
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.8 VENTAS (INMUTABLE)
CREATE TABLE public.ventas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    usuario_id UUID REFERENCES public.perfiles(usuario_id) ON DELETE SET NULL,
    numero_venta TEXT NOT NULL,
    cliente_id UUID REFERENCES public.clientes(id) ON DELETE SET NULL,
    metodo_pago TEXT NOT NULL CHECK (metodo_pago IN ('CASH', 'TRANSFER', 'CREDIT')),
    referencia_transferencia TEXT DEFAULT NULL,
    total_centavos BIGINT NOT NULL CHECK (total_centavos >= 0),
    marca_tiempo BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.9 DETALLES DE VENTA
CREATE TABLE public.detalles_venta (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venta_id UUID REFERENCES public.ventas(id) ON DELETE CASCADE,
    producto_id UUID REFERENCES public.productos(id) ON DELETE CASCADE,
    variante_id UUID REFERENCES public.variantes_producto(id) ON DELETE CASCADE,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario_centavos BIGINT NOT NULL CHECK (precio_unitario_centavos >= 0),
    subtotal_centavos BIGINT NOT NULL CHECK (subtotal_centavos >= 0),
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.10 PAGOS
CREATE TABLE public.pagos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    venta_id UUID REFERENCES public.ventas(id) ON DELETE CASCADE,
    metodo TEXT NOT NULL CHECK (metodo IN ('CASH', 'TRANSFER', 'CREDIT')),
    monto_centavos BIGINT NOT NULL CHECK (monto_centavos >= 0),
    referencia TEXT DEFAULT NULL,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.11 ABONOS DE DEUDA (INMUTABLE - RN-08)
CREATE TABLE public.abonos_deuda (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    cliente_id UUID REFERENCES public.clientes(id) ON DELETE CASCADE,
    proveedor_id UUID REFERENCES public.proveedores(id) ON DELETE CASCADE,
    monto_centavos BIGINT NOT NULL CHECK (monto_centavos > 0),
    metodo_pago TEXT DEFAULT 'CASH',
    marca_tiempo BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.12 GASTOS
CREATE TABLE public.gastos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    categoria TEXT DEFAULT 'Gasto Operativo',
    concepto TEXT NOT NULL CHECK (char_length(trim(concepto)) > 0),
    monto_centavos BIGINT NOT NULL CHECK (monto_centavos > 0),
    marca_tiempo BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW(),
    eliminado_en TIMESTAMPTZ DEFAULT NULL
);

-- 2.13 SESIONES DE CAJA
CREATE TABLE public.sesiones_caja (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    usuario_id UUID REFERENCES public.perfiles(usuario_id) ON DELETE SET NULL,
    efectivo_inicial_centavos BIGINT NOT NULL CHECK (efectivo_inicial_centavos >= 0),
    efectivo_esperado_centavos BIGINT NOT NULL CHECK (efectivo_esperado_centavos >= 0),
    efectivo_contado_centavos BIGINT DEFAULT NULL,
    esta_abierta BOOLEAN DEFAULT TRUE,
    abierta_en BIGINT NOT NULL DEFAULT 0,
    cerrada_en BIGINT DEFAULT NULL,
    creado_en TIMESTAMPTZ DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.14 MOVIMIENTOS DE CAJA
CREATE TABLE public.movimientos_caja (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sesion_id UUID REFERENCES public.sesiones_caja(id) ON DELETE CASCADE,
    concepto TEXT NOT NULL,
    monto_centavos BIGINT NOT NULL CHECK (monto_centavos >= 0),
    tipo_movimiento TEXT NOT NULL CHECK (tipo_movimiento IN ('INGRESO', 'EGRESO')),
    venta_relacionada_id UUID REFERENCES public.ventas(id) ON DELETE SET NULL,
    cliente_relacionado_id UUID REFERENCES public.clientes(id) ON DELETE SET NULL,
    marca_tiempo BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.15 OPERACIONES DE SINCRONIZACIÓN
CREATE TABLE public.operaciones_sincronizacion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_entidad TEXT NOT NULL,
    entidad_id UUID NOT NULL,
    operacion TEXT NOT NULL CHECK (operacion IN ('INSERT', 'UPDATE', 'DELETE')),
    payload_json TEXT DEFAULT '',
    intentos INT DEFAULT 0,
    ultimo_error TEXT DEFAULT NULL,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.16 IMÁGENES LOCALES PENDIENTES DE SUBIDA
CREATE TABLE public.imagenes_locales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    negocio_id UUID REFERENCES public.negocios(id) ON DELETE CASCADE,
    ruta_archivo_local TEXT NOT NULL,
    ruta_storage TEXT NOT NULL,
    tipo_entidad TEXT NOT NULL,
    entidad_id UUID NOT NULL,
    estado_sincronizacion TEXT NOT NULL DEFAULT 'PENDING_INSERT'
        CHECK (estado_sincronizacion IN ('PENDING_INSERT', 'PENDING_UPDATE', 'PENDING_DELETE', 'SYNCED')),
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- 2.17 CONFLICTOS DE SINCRONIZACIÓN
CREATE TABLE public.conflictos_sincronizacion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo_conflicto TEXT NOT NULL,
    nombre_entidad TEXT NOT NULL,
    entidad_id UUID NOT NULL,
    payload_local_json TEXT DEFAULT '',
    payload_remoto_json TEXT DEFAULT '',
    resuelto BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMPTZ DEFAULT NOW()
);

-- ====================================================================
-- 3. PROCEDIMIENTOS ALMACENADOS ATÓMICOS (STORED PROCEDURES / RPC)
-- ====================================================================

-- 3.1 PROCEDIMIENTO ATÓMICO DE VENTA COMPLETA
CREATE OR REPLACE FUNCTION public.fn_procesar_venta_atomica(
    p_venta_id UUID,
    p_negocio_id UUID,
    p_usuario_id UUID,
    p_numero_venta TEXT,
    p_cliente_id UUID,
    p_metodo_pago TEXT,
    p_referencia TEXT,
    p_total_centavos BIGINT,
    p_variante_id UUID,
    p_cantidad INT
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_stock_actual INT;
BEGIN
    -- Validar stock de variante
    SELECT stock INTO v_stock_actual FROM public.variantes_producto WHERE id = p_variante_id FOR UPDATE;
    IF v_stock_actual IS NULL OR v_stock_actual < p_cantidad THEN
        RAISE EXCEPTION 'Stock insuficiente para procesar la venta en la variante %', p_variante_id;
    END IF;

    -- 1. Insertar Venta
    INSERT INTO public.ventas (id, negocio_id, usuario_id, numero_venta, cliente_id, metodo_pago, referencia_transferencia, total_centavos, marca_tiempo)
    VALUES (p_venta_id, p_negocio_id, p_usuario_id, p_numero_venta, p_cliente_id, p_metodo_pago, p_referencia, p_total_centavos, extract(epoch from now())::BIGINT * 1000);

    -- 2. Descontar Stock
    UPDATE public.variantes_producto SET stock = stock - p_cantidad, actualizado_en = NOW() WHERE id = p_variante_id;

    -- 3. Incrementar Deuda si es crédito
    IF p_metodo_pago = 'CREDIT' AND p_cliente_id IS NOT NULL THEN
        UPDATE public.clientes SET deuda_centavos = deuda_centavos + p_total_centavos, actualizado_en = NOW() WHERE id = p_cliente_id;
    END IF;

    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        -- ROLLBACK AUTOMÁTICO ANTE CUALQUIER FALLO
        RAISE EXCEPTION 'Error en transacción atómica de venta: %', SQLERRM;
        RETURN FALSE;
END;
$$;

-- 3.2 PROCEDIMIENTO ATÓMICO DE ABONO A DEUDA (RN-08)
CREATE OR REPLACE FUNCTION public.fn_procesar_abono_deuda(
    p_abono_id UUID,
    p_negocio_id UUID,
    p_cliente_id UUID,
    p_monto_centavos BIGINT,
    p_metodo_pago TEXT
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_deuda_actual BIGINT;
BEGIN
    SELECT deuda_centavos INTO v_deuda_actual FROM public.clientes WHERE id = p_cliente_id FOR UPDATE;

    IF v_deuda_actual IS NULL THEN
        RAISE EXCEPTION 'El cliente no existe';
    END IF;

    IF p_monto_centavos > v_deuda_actual THEN
        RAISE EXCEPTION 'Regla RN-08: El abono (% céntimos) no puede superar la deuda actual (% céntimos)', p_monto_centavos, v_deuda_actual;
    END IF;

    -- 1. Insertar Abono Inmutable
    INSERT INTO public.abonos_deuda (id, negocio_id, cliente_id, monto_centavos, metodo_pago, marca_tiempo)
    VALUES (p_abono_id, p_negocio_id, p_cliente_id, p_monto_centavos, p_metodo_pago, extract(epoch from now())::BIGINT * 1000);

    -- 2. Descontar Deuda
    UPDATE public.clientes SET deuda_centavos = GREATEST(0, deuda_centavos - p_monto_centavos), actualizado_en = NOW() WHERE id = p_cliente_id;

    RETURN TRUE;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al procesar abono: %', SQLERRM;
        RETURN FALSE;
END;
$$;

-- ====================================================================
-- 4. TRIGGERS DE ACTUALIZACIÓN AUTOMÁTICA DE TIMESTAMP
-- ====================================================================

CREATE OR REPLACE FUNCTION public.fn_actualizar_marca_tiempo()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_actualizado_productos BEFORE UPDATE ON public.productos FOR EACH ROW EXECUTE FUNCTION public.fn_actualizar_marca_tiempo();
CREATE TRIGGER tg_actualizado_clientes BEFORE UPDATE ON public.clientes FOR EACH ROW EXECUTE FUNCTION public.fn_actualizar_marca_tiempo();
CREATE TRIGGER tg_actualizado_ventas BEFORE UPDATE ON public.ventas FOR EACH ROW EXECUTE FUNCTION public.fn_actualizar_marca_tiempo();

-- ====================================================================
-- 5. SEGURIDAD Y PRINCIPIO DE MÍNIMO PRIVILEGIO (GRANTS & RLS)
-- ====================================================================

-- Habilitar RLS
ALTER TABLE public.negocios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.proveedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.gastos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sesiones_caja ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.miembros_negocio ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.variantes_producto ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalles_venta ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pagos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.abonos_deuda ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.movimientos_caja ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.imagenes_locales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conflictos_sincronizacion ENABLE ROW LEVEL SECURITY;

-- Asignación de Privilegios
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO authenticated;
REVOKE DELETE ON public.ventas, public.pagos, public.abonos_deuda FROM anon, authenticated;

INSERT INTO storage.buckets (id, name, public)
VALUES ('product-images', 'product-images', false)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "Leer imágenes del negocio" ON storage.objects;
DROP POLICY IF EXISTS "Subir imágenes autenticado" ON storage.objects;
DROP POLICY IF EXISTS "Actualizar imágenes autenticado" ON storage.objects;

CREATE POLICY "Leer imágenes del negocio" ON storage.objects
    FOR SELECT TO authenticated
    USING (bucket_id = 'product-images');

CREATE POLICY "Subir imágenes autenticado" ON storage.objects
    FOR INSERT TO authenticated
    WITH CHECK (bucket_id = 'product-images');

CREATE POLICY "Actualizar imágenes autenticado" ON storage.objects
    FOR UPDATE TO authenticated
    USING (bucket_id = 'product-images')
    WITH CHECK (bucket_id = 'product-images');

-- POLÍTICAS RLS EN ESPAÑOL
CREATE POLICY "Acceso multi-negocio productos" ON public.productos
    FOR ALL USING (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    ) WITH CHECK (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    );

CREATE POLICY "Acceso multi-negocio clientes" ON public.clientes
    FOR ALL USING (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    ) WITH CHECK (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    );

CREATE POLICY "Acceso multi-negocio ventas" ON public.ventas
    FOR ALL USING (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    ) WITH CHECK (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    );

CREATE POLICY "Acceso propio negocios" ON public.negocios
    FOR ALL USING (id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()))
    WITH CHECK (id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()));

CREATE POLICY "Crear negocio autenticado" ON public.negocios
    FOR INSERT TO authenticated
    WITH CHECK (true);

CREATE POLICY "Acceso propio perfiles" ON public.perfiles
    FOR ALL USING (usuario_id = auth.uid())
    WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Acceso propio miembros" ON public.miembros_negocio
    FOR ALL USING (usuario_id = auth.uid())
    WITH CHECK (usuario_id = auth.uid());

CREATE POLICY "Acceso multi-negocio proveedores" ON public.proveedores
    FOR ALL USING (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()))
    WITH CHECK (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()));

CREATE POLICY "Acceso multi-negocio variantes" ON public.variantes_producto
    FOR ALL USING (producto_id IN (SELECT id FROM public.productos))
    WITH CHECK (producto_id IN (SELECT id FROM public.productos));

CREATE POLICY "Acceso multi-negocio detalles" ON public.detalles_venta
    FOR ALL USING (venta_id IN (SELECT id FROM public.ventas))
    WITH CHECK (venta_id IN (SELECT id FROM public.ventas));

CREATE POLICY "Acceso multi-negocio pagos" ON public.pagos
    FOR ALL USING (venta_id IN (SELECT id FROM public.ventas))
    WITH CHECK (venta_id IN (SELECT id FROM public.ventas));

CREATE POLICY "Acceso multi-negocio abonos" ON public.abonos_deuda
    FOR ALL USING (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()))
    WITH CHECK (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()));

CREATE POLICY "Acceso multi-negocio gastos" ON public.gastos
    FOR ALL USING (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()))
    WITH CHECK (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()));

CREATE POLICY "Acceso multi-negocio caja" ON public.sesiones_caja
    FOR ALL USING (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()))
    WITH CHECK (negocio_id IN (SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()));

CREATE POLICY "Acceso multi-negocio movimientos" ON public.movimientos_caja
    FOR ALL USING (sesion_id IN (SELECT id FROM public.sesiones_caja))
    WITH CHECK (sesion_id IN (SELECT id FROM public.sesiones_caja));

CREATE POLICY "Acceso multi-negocio imágenes" ON public.imagenes_locales
    FOR ALL USING (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    ) WITH CHECK (
        negocio_id IN (
            SELECT negocio_id FROM public.miembros_negocio WHERE usuario_id = auth.uid()
        )
    );

CREATE POLICY "Acceso multi-negocio conflictos" ON public.conflictos_sincronizacion
    FOR ALL USING (
        EXISTS (
            SELECT 1
            FROM public.miembros_negocio m
            WHERE m.usuario_id = auth.uid()
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1
            FROM public.miembros_negocio m
            WHERE m.usuario_id = auth.uid()
        )
    );
