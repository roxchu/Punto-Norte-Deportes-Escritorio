-- Catálogo inicial Punto Norte Deportes: deja 30 productos en total.
-- Conserva los 3 productos del volcado y agrega 27 productos con una variante cada uno.
-- Se puede ejecutar más de una vez: evita duplicar marcas, categorías, productos,
-- variantes (por SKU) y stock (por variante/ubicación).
-- Importar con la base puntonortedeporte seleccionada.

START TRANSACTION;

-- Marcas existentes (Nike y Reusch) se conservan; se agregan las que faltan.
INSERT INTO marca (nombre)
SELECT marcas.nombre
FROM (
    SELECT 'Adidas' nombre UNION ALL SELECT 'Puma' UNION ALL SELECT 'Wilson'
    UNION ALL SELECT 'Penalty' UNION ALL SELECT 'Venum' UNION ALL SELECT 'Grays'
) marcas
LEFT JOIN marca m ON LOWER(m.nombre) = LOWER(marcas.nombre)
WHERE m.idMarca IS NULL;

-- Categorías padre.
INSERT INTO categoria (nombre, idCategoriaPadre)
SELECT padres.nombre, NULL
FROM (
    SELECT 'Ropa deportiva' nombre UNION ALL SELECT 'Accesorios deportivos'
) padres
LEFT JOIN categoria c ON LOWER(c.nombre) = LOWER(padres.nombre)
WHERE c.idCategoria IS NULL;

-- Las categorías existentes se mantienen y quedan bajo Ropa deportiva.
UPDATE categoria c
JOIN categoria ropa ON LOWER(ropa.nombre) = 'ropa deportiva'
SET c.idCategoriaPadre = ropa.idCategoria
WHERE LOWER(c.nombre) = 'ropa de abrigo' AND c.idCategoriaPadre IS NULL;

UPDATE categoria c
JOIN categoria ropa ON LOWER(ropa.nombre) = 'ropa deportiva'
SET c.idCategoriaPadre = ropa.idCategoria
WHERE LOWER(c.nombre) = 'pantalones';

-- Subcategorías de ropa y accesorios.
INSERT INTO categoria (nombre, idCategoriaPadre)
SELECT d.nombre, p.idCategoria
FROM (
    SELECT 'Remeras' nombre, 'Ropa deportiva' padre
    UNION ALL SELECT 'Buzos', 'Ropa deportiva'
    UNION ALL SELECT 'Camperas', 'Ropa deportiva'
    UNION ALL SELECT 'Shorts', 'Ropa deportiva'
    UNION ALL SELECT 'Medias', 'Ropa deportiva'
    UNION ALL SELECT 'Pelotas', 'Accesorios deportivos'
    UNION ALL SELECT 'Raquetas', 'Accesorios deportivos'
    UNION ALL SELECT 'Protectores bucales', 'Accesorios deportivos'
    UNION ALL SELECT 'Palos de hockey', 'Accesorios deportivos'
    UNION ALL SELECT 'Bolsos y mochilas', 'Accesorios deportivos'
    UNION ALL SELECT 'Gorras', 'Accesorios deportivos'
) d
JOIN categoria p ON LOWER(p.nombre) = LOWER(d.padre)
LEFT JOIN categoria existente ON LOWER(existente.nombre) = LOWER(d.nombre)
WHERE existente.idCategoria IS NULL;

-- 27 productos nuevos: 7 de indumentaria y 20 accesorios.
INSERT INTO productos (idCategoria, idMarca, nombre, descripcion, activo)
SELECT c.idCategoria, m.idMarca, d.nombre, d.descripcion, 1
FROM (
    SELECT 'Buzos' categoria, 'Nike' marca, 'Buzo con capucha Nike' nombre, 'Buzo deportivo de frisa con capucha y bolsillo frontal.' descripcion
    UNION ALL SELECT 'Camperas', 'Adidas', 'Campera impermeable Adidas', 'Campera liviana impermeable para entrenamiento y uso urbano.'
    UNION ALL SELECT 'Remeras', 'Puma', 'Remera deportiva Puma', 'Remera respirable de secado rápido para entrenamiento.'
    UNION ALL SELECT 'Shorts', 'Nike', 'Short de entrenamiento Nike', 'Short liviano para entrenamiento.'
    UNION ALL SELECT 'Medias', 'Puma', 'Medias deportivas Puma', 'Medias acolchadas para actividad deportiva.'
    UNION ALL SELECT 'Pantalones', 'Nike', 'Pantalón jogger Nike', 'Pantalón jogger deportivo con puños.'
    UNION ALL SELECT 'Guantes esqui', 'Reusch', 'Guantes térmicos Reusch', 'Guantes térmicos para actividades de invierno.'
    UNION ALL SELECT 'Pelotas', 'Wilson', 'Pelota de fútbol Wilson', 'Pelota de fútbol tamaño y peso reglamentarios.'
    UNION ALL SELECT 'Pelotas', 'Wilson', 'Pelota de básquet Wilson', 'Pelota de básquet para entrenamiento en interior y exterior.'
    UNION ALL SELECT 'Pelotas', 'Penalty', 'Pelota de vóley Penalty', 'Pelota de vóley para entrenamiento.'
    UNION ALL SELECT 'Pelotas', 'Wilson', 'Pelota de tenis Wilson', 'Tubo de pelotas de tenis presurizadas.'
    UNION ALL SELECT 'Pelotas', 'Wilson', 'Pelota de rugby Wilson', 'Pelota de rugby con superficie de buen agarre.'
    UNION ALL SELECT 'Raquetas', 'Wilson', 'Raqueta de tenis Wilson', 'Raqueta de tenis para nivel inicial e intermedio.'
    UNION ALL SELECT 'Raquetas', 'Wilson', 'Raqueta de pádel Wilson', 'Raqueta de pádel con núcleo de control.'
    UNION ALL SELECT 'Raquetas', 'Wilson', 'Raqueta de bádminton Wilson', 'Raqueta liviana de bádminton.'
    UNION ALL SELECT 'Raquetas', 'Wilson', 'Raqueta de squash Wilson', 'Raqueta de squash liviana y resistente.'
    UNION ALL SELECT 'Protectores bucales', 'Venum', 'Protector bucal Venum adulto', 'Protector bucal moldeable para deportes de contacto.'
    UNION ALL SELECT 'Protectores bucales', 'Venum', 'Protector bucal Venum junior', 'Protector bucal moldeable de tamaño junior.'
    UNION ALL SELECT 'Protectores bucales', 'Venum', 'Protector bucal doble Venum', 'Protector bucal doble capa para entrenamiento de contacto.'
    UNION ALL SELECT 'Protectores bucales', 'Venum', 'Protector bucal con estuche Venum', 'Protector bucal moldeable con estuche ventilado.'
    UNION ALL SELECT 'Palos de hockey', 'Grays', 'Palo de hockey Grays 36.5', 'Palo de hockey sobre césped de 36.5 pulgadas.'
    UNION ALL SELECT 'Palos de hockey', 'Grays', 'Palo de hockey Grays 37.5', 'Palo de hockey sobre césped de 37.5 pulgadas.'
    UNION ALL SELECT 'Palos de hockey', 'Grays', 'Palo de hockey junior Grays', 'Palo de hockey sobre césped para jugador junior.'
    UNION ALL SELECT 'Palos de hockey', 'Grays', 'Palo de hockey composite Grays', 'Palo composite para hockey sobre césped.'
    UNION ALL SELECT 'Palos de hockey', 'Grays', 'Palo de hockey indoor Grays', 'Palo liviano para hockey indoor.'
    UNION ALL SELECT 'Bolsos y mochilas', 'Adidas', 'Mochila deportiva Adidas', 'Mochila deportiva con compartimento principal y bolsillos.'
    UNION ALL SELECT 'Gorras', 'Nike', 'Gorra deportiva Nike', 'Gorra con visera y ajuste posterior.'
) d
JOIN categoria c ON LOWER(c.nombre) = LOWER(d.categoria)
JOIN marca m ON LOWER(m.nombre) = LOWER(d.marca)
LEFT JOIN productos existente ON LOWER(existente.nombre) = LOWER(d.nombre)
WHERE existente.idProducto IS NULL;

-- Una variante por producto para que el catálogo quede disponible en Ventas.
INSERT INTO producto_variantes
    (idProducto, talle, color, sku, codigoBarras, precioVenta, costoCotizacion, stockMinimo, stockMaximo, activo)
SELECT p.idProducto, d.talle, d.color, d.sku, NULL, d.precioVenta, d.costoCotizacion, 2, 0, 1
FROM (
    SELECT 'Buzo con capucha Nike' producto, 'L' talle, 'Negro' color, 'BUZCAPNI01' sku, 62000.00 precioVenta, 35000.00 costoCotizacion
    UNION ALL SELECT 'Campera impermeable Adidas', 'M', 'Azul', 'CAMIMPAD01', 98000, 57000
    UNION ALL SELECT 'Remera deportiva Puma', 'M', 'Blanco', 'REMDEPPU01', 32000, 17000
    UNION ALL SELECT 'Short de entrenamiento Nike', 'M', 'Negro', 'SHOENTNI01', 35000, 19000
    UNION ALL SELECT 'Medias deportivas Puma', 'Único', 'Blanco', 'MEDDEPPU01', 14000, 6500
    UNION ALL SELECT 'Pantalón jogger Nike', 'L', 'Gris', 'PANJOGNI01', 59000, 33000
    UNION ALL SELECT 'Guantes térmicos Reusch', 'M', 'Negro', 'GUNTEMRE01', 44000, 25000
    UNION ALL SELECT 'Pelota de fútbol Wilson', '5', 'Blanco/Negro', 'PELFUTWI01', 48000, 27000
    UNION ALL SELECT 'Pelota de básquet Wilson', '7', 'Naranja', 'PELBASWI01', 52000, 30000
    UNION ALL SELECT 'Pelota de vóley Penalty', '5', 'Blanco/Azul', 'PELVOLPE01', 39000, 22000
    UNION ALL SELECT 'Pelota de tenis Wilson', 'Único', 'Amarillo', 'PELTENWI01', 18000, 9500
    UNION ALL SELECT 'Pelota de rugby Wilson', '5', 'Marrón', 'PELRUGWI01', 46000, 26000
    UNION ALL SELECT 'Raqueta de tenis Wilson', 'Único', 'Negro/Rojo', 'RATTENWI01', 135000, 82000
    UNION ALL SELECT 'Raqueta de pádel Wilson', 'Único', 'Negro/Azul', 'RATPADWI01', 168000, 103000
    UNION ALL SELECT 'Raqueta de bádminton Wilson', 'Único', 'Negro/Amarillo', 'RATBADWI01', 79000, 45000
    UNION ALL SELECT 'Raqueta de squash Wilson', 'Único', 'Negro', 'RATSQUWI01', 112000, 68000
    UNION ALL SELECT 'Protector bucal Venum adulto', 'Adulto', 'Transparente', 'PROBUDVE01', 22000, 10500
    UNION ALL SELECT 'Protector bucal Venum junior', 'Junior', 'Azul', 'PROBUJVE01', 19000, 9000
    UNION ALL SELECT 'Protector bucal doble Venum', 'Adulto', 'Negro', 'PROBUDVE02', 29000, 14500
    UNION ALL SELECT 'Protector bucal con estuche Venum', 'Adulto', 'Rojo', 'PROBUEVE01', 25000, 12000
    UNION ALL SELECT 'Palo de hockey Grays 36.5', '36.5', 'Negro/Verde', 'PALHOGGR01', 145000, 87000
    UNION ALL SELECT 'Palo de hockey Grays 37.5', '37.5', 'Negro/Azul', 'PALHOGGR02', 155000, 93000
    UNION ALL SELECT 'Palo de hockey junior Grays', 'Junior', 'Azul', 'PALHOJGR01', 89000, 51000
    UNION ALL SELECT 'Palo de hockey composite Grays', '37.5', 'Negro/Rojo', 'PALHOGRGR01', 198000, 122000
    UNION ALL SELECT 'Palo de hockey indoor Grays', '36.5', 'Negro', 'PALHOIGR01', 128000, 76000
    UNION ALL SELECT 'Mochila deportiva Adidas', 'Único', 'Negro', 'MOCDEPAD01', 72000, 42000
    UNION ALL SELECT 'Gorra deportiva Nike', 'Único', 'Negro', 'GORDEPNI01', 27000, 13000
) d
JOIN productos p ON LOWER(p.nombre) = LOWER(d.producto)
LEFT JOIN producto_variantes v ON v.sku = d.sku
WHERE v.idVariante IS NULL;

-- Stock inicial de prueba: 5 unidades en Salón y 10 en Depósito por variante.
INSERT INTO stock (idVariante, idUbicacion, cantidad)
SELECT v.idVariante, u.idUbicacion,
       CASE WHEN LOWER(u.nombre) = 'salon' THEN 5 ELSE 10 END
FROM producto_variantes v
JOIN ubicacion u ON LOWER(u.nombre) IN ('salon', 'deposito')
WHERE v.sku IN (
    'BUZCAPNI01','CAMIMPAD01','REMDEPPU01','SHOENTNI01','MEDDEPPU01','PANJOGNI01','GUNTEMRE01',
    'PELFUTWI01','PELBASWI01','PELVOLPE01','PELTENWI01','PELRUGWI01','RATTENWI01','RATPADWI01',
    'RATBADWI01','RATSQUWI01','PROBUDVE01','PROBUJVE01','PROBUDVE02','PROBUEVE01','PALHOGGR01',
    'PALHOGGR02','PALHOJGR01','PALHOGRGR01','PALHOIGR01','MOCDEPAD01','GORDEPNI01'
)
AND NOT EXISTS (
    SELECT 1 FROM stock s WHERE s.idVariante = v.idVariante AND s.idUbicacion = u.idUbicacion
);

COMMIT;
