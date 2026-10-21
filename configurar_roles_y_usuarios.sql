-- Punto Norte Deportes: asegurar los cinco roles y una cuenta activa por rol.
-- Ejecutar sobre s32_puntonorte. No modifica contraseñas de usuarios existentes.
-- La cuenta Gerente de prueba se crea solo si no existe y usa el código inicial 123456.
-- Cambiar/regenerar ese código antes de usar la cuenta fuera de pruebas.

START TRANSACTION;

-- Agrega solo los roles que todavía no existan (comparación sin distinguir mayúsculas/tildes
-- según la collation utf8mb4_general_ci usada por la base).
INSERT INTO roles (nombreRol, descripcion)
SELECT faltante.nombreRol, faltante.descripcion
FROM (
    SELECT 'Vendedor' AS nombreRol, 'Registra ventas y consulta productos y stock' AS descripcion
    UNION ALL SELECT 'Depósito', 'Recibe mercadería y registra movimientos de stock'
    UNION ALL SELECT 'Encargado', 'Supervisa ventas y controla stock'
    UNION ALL SELECT 'Supervisor', 'Autoriza operaciones sensibles y gestiona usuarios'
    UNION ALL SELECT 'Gerente', 'Acceso general, reportes e historial'
) AS faltante
WHERE NOT EXISTS (
    SELECT 1 FROM roles existente WHERE existente.nombreRol = faltante.nombreRol
);

-- Reparte los usuarios del archivo adjunto entre los roles operativos y conserva
-- a Joaquin Maini como Supervisor. Los códigos de acceso de estas cuentas no cambian.
UPDATE usuarios
SET idRol = (SELECT idRol FROM roles WHERE nombreRol = 'Vendedor' LIMIT 1),
    puestoUsername = 'Vendedor'
WHERE correo = 'belenrociamonzon11@gmail.com' AND activo <> 'DESACTIVADO';

UPDATE usuarios
SET idRol = (SELECT idRol FROM roles WHERE nombreRol = 'Encargado' LIMIT 1),
    puestoUsername = 'Encargado'
WHERE correo = 'rocio.monzon.t1vl@gmail.com' AND activo <> 'DESACTIVADO';

UPDATE usuarios
SET idRol = (SELECT idRol FROM roles WHERE nombreRol = 'Depósito' LIMIT 1),
    puestoUsername = 'Depósito'
WHERE correo = 'manuelcarabajal944@gmail.com' AND activo <> 'DESACTIVADO';

UPDATE usuarios
SET idRol = (SELECT idRol FROM roles WHERE nombreRol = 'Supervisor' LIMIT 1),
    puestoUsername = 'Supervisor'
WHERE correo = 'supervisor@puntonorte.com' AND activo <> 'DESACTIVADO';

-- La entrevista/archivo solo contiene cuatro usuarios. Crea una cuenta identificada
-- explícitamente como de prueba para completar el rol Gerente sin reutilizar a otra persona.
INSERT INTO usuarios
    (idRol, nombre, apellido, dni, telefono, correo, fechaIngreso,
     puestoUsername, username, passwordHash, activo)
SELECT
    (SELECT idRol FROM roles WHERE nombreRol = 'Gerente' LIMIT 1),
    'Gerente', 'De Prueba', NULL, NULL, 'gerente.prueba@puntonorte.test', CURRENT_DATE,
    'Gerente', 'gerente.prueba@puntonorte.test',
    SHA2('gerente.prueba@puntonorte.test:123456', 256), 'ACTIVO'
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios
    WHERE correo = 'gerente.prueba@puntonorte.test'
       OR username = 'gerente.prueba@puntonorte.test'
);

-- Si la cuenta de prueba ya existía, asígnala igualmente a Gerente.
UPDATE usuarios
SET idRol = (SELECT idRol FROM roles WHERE nombreRol = 'Gerente' LIMIT 1),
    puestoUsername = 'Gerente'
WHERE correo = 'gerente.prueba@puntonorte.test';

COMMIT;

-- Verificación: cada rol debería mostrar al menos una cuenta ACTIVA.
SELECT r.nombreRol AS rol, COUNT(u.idUsuario) AS usuariosActivos
FROM roles r
LEFT JOIN usuarios u ON u.idRol = r.idRol AND u.activo = 'ACTIVO'
WHERE r.nombreRol IN ('Vendedor', 'Depósito', 'Encargado', 'Supervisor', 'Gerente')
GROUP BY r.idRol, r.nombreRol
ORDER BY r.idRol;

-- Ver qué cuenta quedó asignada a cada rol.
SELECT u.nombre, u.apellido, u.correo, r.nombreRol AS rol, u.activo
FROM usuarios u
JOIN roles r ON r.idRol = u.idRol
WHERE u.correo IN (
    'supervisor@puntonorte.com',
    'belenrociamonzon11@gmail.com',
    'rocio.monzon.t1vl@gmail.com',
    'manuelcarabajal944@gmail.com',
    'gerente.prueba@puntonorte.test'
)
ORDER BY r.idRol;
