-- Ejecutar una vez en s32_puntonorte.
-- Agrega la condición/plazo de pago, con Contado como valor inicial para proveedores existentes.

SET @sql_condicion_pago = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'proveedor'
          AND COLUMN_NAME = 'condicionPago'
    ),
    'SELECT ''La columna condicionPago ya existe'' AS estado',
    'ALTER TABLE proveedor ADD COLUMN condicionPago VARCHAR(80) NOT NULL DEFAULT ''Contado'' AFTER correo'
);
PREPARE sentencia_condicion_pago FROM @sql_condicion_pago;
EXECUTE sentencia_condicion_pago;
DEALLOCATE PREPARE sentencia_condicion_pago;

SELECT idProveedor, razonSocial, condicionPago
FROM proveedor
ORDER BY razonSocial;
