-- Códigos de acceso de las cuentas de prueba sin buzón utilizable.
-- No usar en producción. El código en texto claro es 123456; la base conserva su hash.
UPDATE usuarios
SET passwordHash = SHA2(CONCAT(LOWER(TRIM(correo)), ':123456'), 256)
WHERE LOWER(TRIM(correo)) IN (
  'gerente.prueba@puntonorte.test',
  'nahuellatella@gmail.com',
  'supervisor@puntonorte.com'
);
