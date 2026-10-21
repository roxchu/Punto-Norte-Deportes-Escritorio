-- Relaciona cada producto activo del catálogo actual con un proveedor coherente.
-- Acepta nombres de proveedor con o sin el prefijo "Demo".
-- Si se vuelve a ejecutar, INSERT IGNORE evita duplicar relaciones existentes.

START TRANSACTION;

INSERT IGNORE INTO producto_proveedor (idProducto, idProveedor)
SELECT p.idProducto, pr.idProveedor
FROM (
    SELECT 'Buzo' AS producto, 'Indumentaria Norte' AS proveedor
    UNION ALL SELECT 'Guantes esqui', 'Accesorios Atléticos'
    UNION ALL SELECT 'Pantalón', 'Indumentaria Norte'
    UNION ALL SELECT 'Buzo con capucha Nike', 'Indumentaria Norte'
    UNION ALL SELECT 'Campera impermeable Adidas', 'Indumentaria Norte'
    UNION ALL SELECT 'Remera deportiva Puma', 'Indumentaria Norte'
    UNION ALL SELECT 'Short de entrenamiento Nike', 'Indumentaria Norte'
    UNION ALL SELECT 'Medias deportivas Puma', 'Medias y Textiles Deportivos'
    UNION ALL SELECT 'Pantalón jogger Nike', 'Indumentaria Norte'
    UNION ALL SELECT 'Guantes térmicos Reusch', 'Accesorios Atléticos'
    UNION ALL SELECT 'Pelota de fútbol Wilson', 'Equipamiento de Fútbol'
    UNION ALL SELECT 'Pelota de básquet Wilson', 'Equipamiento de Básquet'
    UNION ALL SELECT 'Pelota de vóley Penalty', 'Productos de Entrenamiento'
    UNION ALL SELECT 'Pelota de tenis Wilson', 'Artículos de Tenis'
    UNION ALL SELECT 'Pelota de rugby Wilson', 'Productos de Entrenamiento'
    UNION ALL SELECT 'Raqueta de tenis Wilson', 'Artículos de Tenis'
    UNION ALL SELECT 'Raqueta de pádel Wilson', 'Artículos de Tenis'
    UNION ALL SELECT 'Raqueta de bádminton Wilson', 'Productos de Entrenamiento'
    UNION ALL SELECT 'Raqueta de squash Wilson', 'Productos de Entrenamiento'
    UNION ALL SELECT 'Protector bucal Venum adulto', 'Accesorios Atléticos'
    UNION ALL SELECT 'Protector bucal Venum junior', 'Accesorios Atléticos'
    UNION ALL SELECT 'Protector bucal doble Venum', 'Accesorios Atléticos'
    UNION ALL SELECT 'Protector bucal con estuche Venum', 'Accesorios Atléticos'
    UNION ALL SELECT 'Palo de hockey Grays 36.5', 'Artículos de Hockey'
    UNION ALL SELECT 'Palo de hockey Grays 37.5', 'Artículos de Hockey'
    UNION ALL SELECT 'Palo de hockey junior Grays', 'Artículos de Hockey'
    UNION ALL SELECT 'Palo de hockey composite Grays', 'Artículos de Hockey'
    UNION ALL SELECT 'Palo de hockey indoor Grays', 'Artículos de Hockey'
    UNION ALL SELECT 'Mochila deportiva Adidas', 'Mochilas y Bolsos Deportivos'
    UNION ALL SELECT 'Gorra deportiva Nike', 'Indumentaria Norte'
) AS asignacion
JOIN productos p ON p.nombre = asignacion.producto AND p.activo = 1
JOIN proveedor pr ON pr.nombre LIKE CONCAT('%', asignacion.proveedor, '%');

COMMIT;

-- Revisión: cada producto activo debe tener al menos un proveedor asociado.
SELECT p.idProducto, p.nombre, COUNT(pp.idProveedor) AS cantidadProveedores,
       GROUP_CONCAT(pr.nombre ORDER BY pr.nombre SEPARATOR ', ') AS proveedores
FROM productos p
LEFT JOIN producto_proveedor pp ON pp.idProducto = p.idProducto
LEFT JOIN proveedor pr ON pr.idProveedor = pp.idProveedor
WHERE p.activo = 1
GROUP BY p.idProducto, p.nombre
ORDER BY p.nombre;
