-- ====================================================================
-- ESQUEMA DE BASE DE DATOS POSTGRESQL PARA SUPABASE & DAJHO (FASE 13)
-- Políticas de Seguridad RLS, IDs UUID v4, Multi-Negocio e Idempotencia
-- ====================================================================

-- Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. BUSINESSSES
CREATE TABLE IF NOT EXISTS public.businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    ruc_or_cedula TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    address TEXT DEFAULT '',
    rimpe_type TEXT DEFAULT 'RIMPE_POPULAR',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 2. USER PROFILES
CREATE TABLE IF NOT EXISTS public.profiles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT DEFAULT '',
    role TEXT DEFAULT 'PROPIETARIO',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 3. BUSINESS MEMBERS
CREATE TABLE IF NOT EXISTS public.business_members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(user_id) ON DELETE CASCADE,
    role TEXT DEFAULT 'PROPIETARIO',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(business_id, user_id)
);

-- 4. PRODUCTS
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    sku TEXT NOT NULL,
    name TEXT NOT NULL,
    brand TEXT DEFAULT 'DAJHO',
    category TEXT DEFAULT 'General',
    cost_cents BIGINT NOT NULL DEFAULT 0,
    sale_price_cents BIGINT NOT NULL DEFAULT 0,
    image_uri TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL,
    UNIQUE(business_id, sku)
);

-- 5. PRODUCT VARIANTS
CREATE TABLE IF NOT EXISTS public.product_variants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    size TEXT DEFAULT 'Única',
    color TEXT DEFAULT 'Único',
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 6. CLIENTS
CREATE TABLE IF NOT EXISTS public.clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT DEFAULT '',
    info TEXT DEFAULT '',
    debt_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 7. SUPPLIERS
CREATE TABLE IF NOT EXISTS public.suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT DEFAULT '',
    info TEXT DEFAULT '',
    debt_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 8. SALES
CREATE TABLE IF NOT EXISTS public.sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(user_id) ON DELETE SET NULL,
    sale_number TEXT NOT NULL,
    client_id UUID REFERENCES public.clients(id) ON DELETE SET NULL,
    payment_method TEXT NOT NULL,
    transfer_reference TEXT DEFAULT NULL,
    total_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 9. SALE ITEMS
CREATE TABLE IF NOT EXISTS public.sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID REFERENCES public.sales(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES public.product_variants(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1,
    unit_price_cents BIGINT NOT NULL DEFAULT 0,
    subtotal_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. PAYMENTS
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID REFERENCES public.sales(id) ON DELETE CASCADE,
    method TEXT NOT NULL,
    amount_cents BIGINT NOT NULL DEFAULT 0,
    reference TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. DEBT PAYMENTS
CREATE TABLE IF NOT EXISTS public.debt_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    client_id UUID REFERENCES public.clients(id) ON DELETE CASCADE,
    supplier_id UUID REFERENCES public.suppliers(id) ON DELETE CASCADE,
    amount_cents BIGINT NOT NULL DEFAULT 0,
    payment_method TEXT DEFAULT 'CASH',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 12. EXPENSES
CREATE TABLE IF NOT EXISTS public.expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    category TEXT DEFAULT 'Gasto Operativo',
    concept TEXT NOT NULL,
    amount_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ DEFAULT NULL
);

-- 13. CASH SESSIONS
CREATE TABLE IF NOT EXISTS public.cash_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID REFERENCES public.businesses(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(user_id) ON DELETE SET NULL,
    initial_cash_cents BIGINT NOT NULL DEFAULT 0,
    expected_cash_cents BIGINT NOT NULL DEFAULT 0,
    actual_cash_cents BIGINT DEFAULT NULL,
    is_open BOOLEAN DEFAULT TRUE,
    opened_at TIMESTAMPTZ DEFAULT NOW(),
    closed_at TIMESTAMPTZ DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 14. CASH MOVEMENTS
CREATE TABLE IF NOT EXISTS public.cash_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID REFERENCES public.cash_sessions(id) ON DELETE CASCADE,
    concept TEXT NOT NULL,
    amount_cents BIGINT NOT NULL DEFAULT 0,
    movement_type TEXT NOT NULL,
    related_sale_id UUID REFERENCES public.sales(id) ON DELETE SET NULL,
    related_client_id UUID REFERENCES public.clients(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 15. SYNC AUDIT LOG
CREATE TABLE IF NOT EXISTS public.sync_operations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_name TEXT NOT NULL,
    entity_id UUID NOT NULL,
    operation TEXT NOT NULL,
    payload_json TEXT DEFAULT '',
    attempts INT DEFAULT 0,
    last_error TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- HABILITAR ROW LEVEL SECURITY (RLS)
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.suppliers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cash_sessions ENABLE ROW LEVEL SECURITY;

-- POLITICAS DE SEGURIDAD RLS
CREATE POLICY "Acceso multi-negocio productos" ON public.products
    FOR ALL USING (
        business_id IN (
            SELECT business_id FROM public.business_members WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Acceso multi-negocio clientes" ON public.clients
    FOR ALL USING (
        business_id IN (
            SELECT business_id FROM public.business_members WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Acceso multi-negocio ventas" ON public.sales
    FOR ALL USING (
        business_id IN (
            SELECT business_id FROM public.business_members WHERE user_id = auth.uid()
        )
    );
