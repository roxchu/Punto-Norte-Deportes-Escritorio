-- Extiende descuentos para promociones por cantidad y descuentos generales facturados.
-- Ejecutar una vez antes de abrir la gestión de descuentos y registrar ventas con estas reglas.

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

ALTER TABLE descuentos
    ADD COLUMN IF NOT EXISTS cantidadCompra INT NULL,
    ADD COLUMN IF NOT EXISTS cantidadPaga INT NULL;

CREATE TABLE IF NOT EXISTS venta_descuento (
    idVenta INT NOT NULL,
    idDescuento INT NOT NULL,
    montoAplicado DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (idVenta, idDescuento),
    KEY idx_venta_descuento_descuento (idDescuento),
    CONSTRAINT fk_venta_descuento_venta
        FOREIGN KEY (idVenta) REFERENCES ventas (idVenta),
    CONSTRAINT fk_venta_descuento_descuento
        FOREIGN KEY (idDescuento) REFERENCES descuentos (idDescuento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
