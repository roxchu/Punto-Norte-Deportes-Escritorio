-- Ejecutar una vez (se puede volver a ejecutar sin error) en s32_puntonorte.
-- El precio por proveedor es opcional y de solo consulta durante la creación del pedido.

SET @sql_agregar_precio = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'producto_proveedor'
          AND COLUMN_NAME = 'costoUnitario'
    ),
    'SELECT ''La columna costoUnitario ya existe'' AS estado',
    'ALTER TABLE producto_proveedor ADD COLUMN costoUnitario DECIMAL(10,2) NULL DEFAULT NULL'
);
PREPARE sentencia_precio FROM @sql_agregar_precio;
EXECUTE sentencia_precio;
DEALLOCATE PREPARE sentencia_precio;

SET @sql_costo_pedido = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'detalle_ped_compra'
          AND COLUMN_NAME = 'costoAcordado'
          AND IS_NULLABLE = 'YES'
    ),
    'SELECT ''costoAcordado ya admite NULL'' AS estado',
    'ALTER TABLE detalle_ped_compra MODIFY COLUMN costoAcordado DECIMAL(10,2) NULL DEFAULT NULL'
);
PREPARE sentencia_costo_pedido FROM @sql_costo_pedido;
EXECUTE sentencia_costo_pedido;
DEALLOCATE PREPARE sentencia_costo_pedido;

-- Los productos asociados sin precio registrado se muestran como "sin informar".
SELECT p.nombre AS producto, pr.nombre AS proveedor, pp.costoUnitario
FROM producto_proveedor pp
JOIN productos p ON p.idProducto = pp.idProducto
JOIN proveedor pr ON pr.idProveedor = pp.idProveedor
ORDER BY pr.nombre, p.nombre;
