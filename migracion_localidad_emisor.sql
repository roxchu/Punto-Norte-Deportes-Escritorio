-- Ejecutar una vez en s32_puntonorte para mostrar la localidad en documentos impresos.
-- CABA coincide con la localidad incluida en el domicilio de la configuración actual.

SET @sql_localidad = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'emisor_config'
          AND COLUMN_NAME = 'localidad'
    ),
    'SELECT ''La columna localidad ya existe'' AS estado',
    'ALTER TABLE emisor_config ADD COLUMN localidad VARCHAR(80) NOT NULL DEFAULT ''CABA'' AFTER domicilioComercial'
);
PREPARE sentencia_localidad FROM @sql_localidad;
EXECUTE sentencia_localidad;
DEALLOCATE PREPARE sentencia_localidad;

UPDATE emisor_config SET localidad = 'CABA' WHERE idEmisor = 1 AND (localidad IS NULL OR localidad = '');
