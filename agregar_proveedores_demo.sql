-- Agrega diez proveedores ficticios para pruebas en s32_puntonorte.
-- Es idempotente: omite cada proveedor si ya existe por nombre o razón social.
-- No se inventan CUIT ni datos de contacto; esos campos quedan NULL para completarlos.

START TRANSACTION;

INSERT INTO proveedor (razonSocial, nombre, cuit, telefono, correo)
SELECT nuevos.razonSocial, nuevos.nombre, NULL, NULL, NULL
FROM (
    SELECT 'Demo Indumentaria Norte S.R.L.' AS razonSocial, 'Demo Indumentaria Norte' AS nombre
    UNION ALL SELECT 'Demo Calzado Deportivo S.R.L.', 'Demo Calzado Deportivo'
    UNION ALL SELECT 'Demo Accesorios Atléticos S.R.L.', 'Demo Accesorios Atléticos'
    UNION ALL SELECT 'Demo Equipamiento de Fútbol S.R.L.', 'Demo Equipamiento de Fútbol'
    UNION ALL SELECT 'Demo Equipamiento de Básquet S.R.L.', 'Demo Equipamiento de Básquet'
    UNION ALL SELECT 'Demo Artículos de Hockey S.R.L.', 'Demo Artículos de Hockey'
    UNION ALL SELECT 'Demo Artículos de Tenis S.R.L.', 'Demo Artículos de Tenis'
    UNION ALL SELECT 'Demo Medias y Textiles Deportivos S.R.L.', 'Demo Medias y Textiles Deportivos'
    UNION ALL SELECT 'Demo Mochilas y Bolsos Deportivos S.R.L.', 'Demo Mochilas y Bolsos Deportivos'
    UNION ALL SELECT 'Demo Productos de Entrenamiento S.R.L.', 'Demo Productos de Entrenamiento'
) AS nuevos
WHERE NOT EXISTS (
    SELECT 1
    FROM proveedor existente
    WHERE existente.nombre = nuevos.nombre
       OR existente.razonSocial = nuevos.razonSocial
);

COMMIT;

-- Verifica que estén disponibles los proveedores de demostración.
SELECT idProveedor, nombre, razonSocial, cuit, telefono, correo
FROM proveedor
WHERE nombre LIKE 'Demo %'
ORDER BY nombre;
