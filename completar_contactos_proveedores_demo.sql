-- Completa CUIT, teléfono y correo de los diez proveedores demo ya insertados.
-- Los CUIT son ficticios y tienen dígito de control inválido intencionalmente.
-- Los correos usan el dominio reservado .test.

START TRANSACTION;

UPDATE proveedor AS p
JOIN (
    SELECT 'Demo Indumentaria Norte' AS nombre, '30-00000001-0' AS cuit, '11-5555-0101' AS telefono, 'indumentaria.norte@proveedores.test' AS correo
    UNION ALL SELECT 'Demo Calzado Deportivo', '30-00000002-0', '11-5555-0102', 'calzado.deportivo@proveedores.test'
    UNION ALL SELECT 'Demo Accesorios Atléticos', '30-00000003-0', '11-5555-0103', 'accesorios.atleticos@proveedores.test'
    UNION ALL SELECT 'Demo Equipamiento de Fútbol', '30-00000004-0', '11-5555-0104', 'futbol@proveedores.test'
    UNION ALL SELECT 'Demo Equipamiento de Básquet', '30-00000005-0', '11-5555-0105', 'basquet@proveedores.test'
    UNION ALL SELECT 'Demo Artículos de Hockey', '30-00000006-0', '11-5555-0106', 'hockey@proveedores.test'
    UNION ALL SELECT 'Demo Artículos de Tenis', '30-00000007-0', '11-5555-0107', 'tenis@proveedores.test'
    UNION ALL SELECT 'Demo Medias y Textiles Deportivos', '30-00000008-0', '11-5555-0108', 'textiles@proveedores.test'
    UNION ALL SELECT 'Demo Mochilas y Bolsos Deportivos', '30-00000009-0', '11-5555-0109', 'mochilas@proveedores.test'
    UNION ALL SELECT 'Demo Productos de Entrenamiento', '30-00000010-0', '11-5555-0110', 'entrenamiento@proveedores.test'
) AS datos ON datos.nombre = p.nombre
SET p.cuit = datos.cuit,
    p.telefono = datos.telefono,
    p.correo = datos.correo;

COMMIT;

-- Verifica los datos cargados.
SELECT nombre, cuit, telefono, correo
FROM proveedor
WHERE nombre LIKE 'Demo %'
ORDER BY nombre;
