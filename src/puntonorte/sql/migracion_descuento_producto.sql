-- Relaciona descuentos de tipo 'por producto' con el producto al que corresponden.
-- Ejecutar una vez sobre la base puntonortedeporte antes de usar esa opción en la app.

CREATE TABLE IF NOT EXISTS descuento_producto (
    idDescuento INT NOT NULL,
    idProducto INT NOT NULL,
    PRIMARY KEY (idDescuento, idProducto),
    KEY idx_descuento_producto_producto (idProducto),
    CONSTRAINT fk_descuento_producto_descuento
        FOREIGN KEY (idDescuento) REFERENCES descuentos (idDescuento),
    CONSTRAINT fk_descuento_producto_producto
        FOREIGN KEY (idProducto) REFERENCES productos (idProducto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
