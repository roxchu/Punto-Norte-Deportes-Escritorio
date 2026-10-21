-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 19-10-2026 a las 21:41:49
-- Versión del servidor: 8.0.46
-- Versión de PHP: 8.5.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `s32_puntonorte`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `anulaciones`
--

CREATE TABLE `anulaciones` (
  `idAnulacion` int NOT NULL,
  `idVenta` int NOT NULL,
  `usrSolicita` int NOT NULL,
  `usrAutoriza` int NOT NULL,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `motivo` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `anulacion_compra`
--

CREATE TABLE `anulacion_compra` (
  `idAnulacion` int NOT NULL,
  `idPedido` int NOT NULL,
  `usrSolicita` int NOT NULL,
  `usrAutoriza` int NOT NULL,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `motivo` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auditoria`
--

CREATE TABLE `auditoria` (
  `idAuditoria` int NOT NULL,
  `idUsuario` int NOT NULL,
  `usrAutoriza` int DEFAULT NULL,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `accionRealizada` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `tablaAfectada` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `idRegistroAfectado` int DEFAULT NULL,
  `valorAnterior` text COLLATE utf8mb4_general_ci,
  `valorNuevo` text COLLATE utf8mb4_general_ci,
  `motivo` text COLLATE utf8mb4_general_ci,
  `detalleCambio` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `auditoria`
--

INSERT INTO `auditoria` (`idAuditoria`, `idUsuario`, `usrAutoriza`, `fechaHora`, `accionRealizada`, `tablaAfectada`, `idRegistroAfectado`, `valorAnterior`, `valorNuevo`, `motivo`, `detalleCambio`) VALUES
(1, 1, 1, '2026-09-30 13:16:58', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(2, 1, 1, '2026-09-30 13:30:36', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(3, 2, 2, '2026-09-30 14:19:05', 'Solicitud de cuenta', 'usuarios', 2, NULL, 'PENDIENTE', NULL, 'Rocio Monzon solicitó el rol Vendedor'),
(4, 1, 1, '2026-09-30 14:19:25', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(5, 1, 1, '2026-09-30 14:19:42', 'Solicitud aprobada', 'usuarios', 2, 'PENDIENTE (Vendedor)', 'ACTIVO (Vendedor)', NULL, 'Rocio Monzon'),
(6, 1, 1, '2026-09-30 14:20:37', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(7, 2, 2, '2026-09-30 14:21:18', 'Inicio de sesión', 'usuarios', 2, NULL, NULL, NULL, 'Rocio Monzon'),
(8, 1, 1, '2026-10-01 18:23:08', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(9, 1, 1, '2026-10-01 18:26:04', 'Usuario pausado', 'usuarios', 2, 'ACTIVO', 'PAUSADO', 'Usuario fuera del local', 'Rocio Monzon'),
(10, 1, 1, '2026-10-01 18:37:55', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(11, 1, 1, '2026-10-01 18:50:35', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(12, 1, 1, '2026-10-01 18:51:17', 'Alta de categoría', 'productos', 1, NULL, 'Ropa de abrigo', NULL, 'Ropa de abrigo'),
(13, 1, 1, '2026-10-01 18:51:22', 'Alta de marca', 'productos', 1, NULL, 'Nike', NULL, 'Nike'),
(14, 1, 1, '2026-10-01 18:57:15', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(15, 1, 1, '2026-10-01 18:58:07', 'Alta de producto', 'productos', 1, NULL, '1 variante', NULL, 'Buzo (BUZNG01)'),
(16, 1, 1, '2026-10-01 18:58:20', 'Edición de variante', 'productos', 1, 'sku=BUZNG01; talle=S; color=Negro; barras=-; precio=$ 10000.00; costo=$ 10000.00; mín=10; máx=0', 'sku=BUZNG01; talle=M; color=Negro; barras=-; precio=$ 10000.00; costo=$ 10000.00; mín=10; máx=0', NULL, 'BUZNG01'),
(17, 1, 1, '2026-10-01 19:24:55', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(18, 1, 1, '2026-10-01 19:25:07', 'Usuario reactivado', 'usuarios', 2, 'PAUSADO', 'ACTIVO', NULL, 'Rocio Monzon'),
(19, 1, 1, '2026-10-01 19:26:37', 'Ajuste de stock', 'stock', 1, '0', '10', 'Ingreso mercaderia', 'BUZNG01 · Deposito · +10'),
(20, 1, 1, '2026-10-01 19:26:59', 'Ajuste de stock', 'stock', 1, '10', '9', 'Rotura producto', 'BUZNG01 · Deposito · -1'),
(21, 1, 1, '2026-10-01 19:35:10', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(22, 1, 1, '2026-10-01 19:39:33', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(23, 1, 1, '2026-10-01 19:43:13', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(24, 1, 1, '2026-10-01 19:52:00', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(25, 1, 1, '2026-10-01 19:52:55', 'Alta de categoría', 'productos', 2, NULL, 'Guantes esquí', NULL, 'Guantes esquí · subcategoría de 1'),
(26, 1, 1, '2026-10-01 19:53:25', 'Alta de marca', 'productos', 2, NULL, 'Reusch', NULL, 'Reusch'),
(27, 1, 1, '2026-10-01 19:54:14', 'Alta de producto', 'productos', 2, NULL, '1 variante', NULL, 'Guantes esqui (GUNV01)'),
(28, 1, 1, '2026-10-01 19:54:51', 'Ajuste de stock', 'stock', 2, '0', '20', 'Ingreso mercaderia', 'GUNV01 · Deposito · +20'),
(29, 1, 1, '2026-10-01 19:55:52', 'Transferencia de stock', 'stock', 2, NULL, NULL, NULL, 'GUNV01 · 10 u. · Deposito → Salon'),
(30, 1, 1, '2026-10-01 19:59:25', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(31, 1, 1, '2026-10-01 19:59:53', 'Transferencia de stock', 'stock', 1, NULL, NULL, NULL, 'BUZNG01 · 5 u. · Deposito → Salon'),
(32, 1, 1, '2026-10-01 20:00:14', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(33, 2, 2, '2026-10-01 20:00:39', 'Inicio de sesión', 'usuarios', 2, NULL, NULL, NULL, 'Rocio Monzon'),
(34, 2, 2, '2026-10-01 20:07:37', 'Inicio de sesión', 'usuarios', 2, NULL, NULL, NULL, 'Rocio Monzon'),
(35, 2, 2, '2026-10-01 20:08:55', 'Cierre de sesión', 'usuarios', 2, NULL, NULL, NULL, 'Rocio Monzon'),
(36, 1, 1, '2026-10-01 20:09:13', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(37, 1, 1, '2026-10-01 20:09:25', 'Precio modificado', 'productos', 1, '$ 10000.00', '$ 20000.00', NULL, 'BUZNG01'),
(38, 1, 1, '2026-10-01 20:09:25', 'Edición de variante', 'productos', 1, 'sku=BUZNG01; talle=M; color=Negro; barras=-; precio=$ 10000.00; costo=$ 10000.00; mín=10; máx=0', 'sku=BUZNG01; talle=M; color=Negro; barras=-; precio=$ 20000.00; costo=$ 10000.00; mín=10; máx=0', NULL, 'BUZNG01'),
(39, 1, 1, '2026-10-01 20:09:47', 'Ajuste de stock', 'stock', 1, '4', '3', 'Traslado', 'BUZNG01 · Deposito · -1'),
(40, 1, 1, '2026-10-01 20:10:16', 'Transferencia de stock', 'stock', 1, NULL, NULL, NULL, 'BUZNG01 · 1 u. · Salon → Deposito'),
(41, 1, 1, '2026-10-01 20:10:33', 'Ajuste de stock', 'stock', 1, '4', '3', 'Venta', 'BUZNG01 · Salon · -1'),
(42, 1, 1, '2026-10-01 20:11:38', 'Alta de categoría', 'productos', 3, NULL, 'Pantalones', NULL, 'Pantalones · subcategoría de 1'),
(43, 1, 1, '2026-10-01 20:12:05', 'Alta de producto', 'productos', 3, NULL, '1 variante', NULL, 'Pantalón (PANNEG01)'),
(44, 1, 1, '2026-10-01 20:13:00', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(45, 3, 3, '2026-10-01 20:14:13', 'Solicitud de cuenta', 'usuarios', 3, NULL, 'PENDIENTE', NULL, 'Juan Perez solicitó el rol Vendedor'),
(46, 1, 1, '2026-10-01 20:14:27', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(47, 1, 1, '2026-10-01 20:14:47', 'Solicitud aprobada', 'usuarios', 3, 'PENDIENTE (Vendedor)', 'ACTIVO (Vendedor)', NULL, 'Juan Perez'),
(48, 1, 1, '2026-10-01 20:14:52', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(49, 3, 3, '2026-10-01 20:15:26', 'Inicio de sesión', 'usuarios', 3, NULL, NULL, NULL, 'Juan Perez'),
(50, 1, 1, '2026-10-05 18:03:45', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(51, 1, 1, '2026-10-05 18:04:20', 'Usuario pausado', 'usuarios', 2, 'ACTIVO', 'PAUSADO', 'Nada', 'Rocio Monzon'),
(52, 1, 1, '2026-10-05 19:57:14', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(53, 1, 1, '2026-10-05 19:59:47', 'Intento de sesión fallido', 'usuarios', 1, NULL, NULL, NULL, 'Código incorrecto'),
(54, 1, 1, '2026-10-05 20:00:16', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(55, 1, 1, '2026-10-05 20:02:25', 'Usuario reactivado', 'usuarios', 2, 'PAUSADO', 'ACTIVO', NULL, 'Rocio Monzon'),
(56, 1, 1, '2026-10-05 20:02:29', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(57, 3, 3, '2026-10-05 20:04:36', 'Inicio de sesión', 'usuarios', 3, NULL, NULL, NULL, 'Juan Perez'),
(58, 3, 3, '2026-10-05 20:07:41', 'Cierre de sesión', 'usuarios', 3, NULL, NULL, NULL, 'Juan Perez'),
(59, 1, 1, '2026-10-05 20:07:54', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(60, 1, 1, '2026-10-05 20:16:49', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(61, 4, 4, '2026-10-05 20:20:33', 'Solicitud de cuenta', 'usuarios', 4, NULL, 'PENDIENTE', NULL, 'Manu Carabajal solicitó el rol Vendedor'),
(62, 1, 1, '2026-10-05 20:23:51', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(63, 1, 1, '2026-10-05 20:31:54', 'Solicitud aprobada', 'usuarios', 4, 'PENDIENTE (Vendedor)', 'ACTIVO (Vendedor)', NULL, 'Manu Carabajal'),
(64, 1, 1, '2026-10-05 20:33:35', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(65, 4, 4, '2026-10-05 20:34:03', 'Inicio de sesión', 'usuarios', 4, NULL, NULL, NULL, 'Manu Carabajal'),
(66, 4, 4, '2026-10-05 21:06:00', 'Cierre de sesión', 'usuarios', 4, NULL, NULL, NULL, 'Manu Carabajal'),
(67, 1, 1, '2026-10-05 21:06:26', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(68, 1, 1, '2026-10-08 19:22:21', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(69, 1, 1, '2026-10-08 19:23:25', 'Venta registrada', 'ventas', 1, NULL, '$ 20.000,00', NULL, 'Venta #1 · 1 línea(s) · Tarjeta de débito'),
(70, 1, 1, '2026-10-08 19:25:47', 'Ajuste de stock', 'stock', 3, '0', '20', 'Ingreso mercaderia', 'PANNEG01 · Deposito · +20'),
(71, 1, 1, '2026-10-08 19:26:14', 'Transferencia de stock', 'stock', 3, NULL, NULL, NULL, 'PANNEG01 · 10 u. · Deposito → Salon'),
(72, 1, 1, '2026-10-08 20:13:49', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(73, 1, 1, '2026-10-08 20:15:37', 'Venta registrada', 'ventas', 2, NULL, '$ 188.000,00', NULL, 'Venta #2 · 2 línea(s) · Tarjeta de débito'),
(74, 1, 1, '2026-10-08 20:22:33', 'Descuento creado', 'descuentos', 1, NULL, 'Pago descuento débito · porcentuales', NULL, NULL),
(75, 1, 1, '2026-10-08 20:53:46', 'Intento de sesión fallido', 'usuarios', 1, NULL, NULL, NULL, 'Código incorrecto'),
(76, 1, 1, '2026-10-08 20:53:55', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(77, 1, 1, '2026-10-08 20:54:16', 'Descuento desactivado', 'descuentos', 1, 'Activo', 'Inactivo', NULL, NULL),
(78, 1, 1, '2026-10-08 20:54:52', 'Descuento creado', 'descuentos', 2, NULL, 'Pago con crédito · porcentuales', NULL, NULL),
(79, 1, 1, '2026-10-08 20:55:44', 'Descuento creado', 'descuentos', 3, NULL, 'Pago con efectivo · generales', NULL, NULL),
(80, 1, 1, '2026-10-08 20:56:44', 'Descuento creado', 'descuentos', 4, NULL, 'Navidad · porcentuales', NULL, NULL),
(81, 1, 1, '2026-10-08 21:00:02', 'Venta registrada', 'ventas', 3, NULL, '$ 152.990,00', NULL, 'Venta #3 · 3 línea(s) · Efectivo ($ 10,00 de descuento)'),
(82, 1, 1, '2026-10-08 21:16:19', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(83, 1, 1, '2026-10-08 21:20:09', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(84, 1, 1, '2026-10-08 21:20:53', 'Descuento creado', 'descuentos', 5, NULL, 'Descuento por producto · por producto', NULL, NULL),
(85, 1, 1, '2026-10-12 19:32:57', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(86, 1, 1, '2026-10-12 19:34:26', 'Descuento creado', 'descuentos', 6, NULL, 'Descuetnto por producto · por producto', NULL, NULL),
(87, 1, 1, '2026-10-12 19:35:22', 'Venta registrada', 'ventas', 4, NULL, '$ 183.000,00', NULL, 'Venta #4 · 3 línea(s) · Tarjeta de débito'),
(88, 1, 1, '2026-10-12 20:35:31', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(89, 1, 1, '2026-10-12 20:37:29', 'Descuento creado', 'descuentos', 7, NULL, '2x1 · especiales', NULL, NULL),
(90, 1, 1, '2026-10-12 20:39:48', 'Venta registrada', 'ventas', 5, NULL, '$ 174.995,00', NULL, 'Venta #5 · 4 línea(s) · Efectivo'),
(91, 1, 1, '2026-10-12 20:41:05', 'Descuento desactivado', 'descuentos', 3, 'Activo', 'Inactivo', NULL, NULL),
(92, 1, 1, '2026-10-12 20:42:14', 'Descuento creado', 'descuentos', 8, NULL, 'Efectivo · porcentuales', NULL, NULL),
(93, 1, 1, '2026-10-12 20:43:12', 'Descuento creado', 'descuentos', 9, NULL, 'Efectivo · porcentuales', NULL, NULL),
(94, 1, 1, '2026-10-12 20:43:25', 'Descuento desactivado', 'descuentos', 8, 'Activo', 'Inactivo', NULL, NULL),
(95, 1, 1, '2026-10-12 20:44:35', 'Venta registrada', 'ventas', 6, NULL, '$ 88.200,00', NULL, 'Venta #6 · 1 línea(s) · Efectivo (10% de descuento)'),
(96, 1, 1, '2026-10-12 20:54:22', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(97, 1, 1, '2026-10-12 21:01:52', 'Transferencia de stock', 'stock', 8, NULL, NULL, NULL, 'MEDDEPPU01 · 5 u. · Deposito → Salon'),
(98, 1, 1, '2026-10-12 21:09:56', 'Venta registrada', 'ventas', 7, NULL, '$ 30.600,00', NULL, 'Venta #7 · 2 línea(s) · Efectivo (10% de descuento)'),
(99, 1, 1, '2026-10-12 21:10:51', 'Venta registrada', 'ventas', 8, NULL, '$ 62.000,00', NULL, 'Venta #8 · 1 línea(s) · Tarjeta de débito'),
(100, 1, 1, '2026-10-14 16:11:54', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(101, 1, 1, '2026-10-14 17:54:27', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(102, 1, 1, '2026-10-14 18:04:22', 'Descuento creado', 'descuentos', 10, NULL, 'Producto Raqueta · por producto', NULL, NULL),
(103, 1, 1, '2026-10-14 18:40:51', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(104, 1, 1, '2026-10-14 18:46:41', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(105, 1, 1, '2026-10-14 19:20:36', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(106, 1, 1, '2026-10-14 19:26:10', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(107, 1, 1, '2026-10-14 20:03:19', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(108, 1, 1, '2026-10-14 20:07:18', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(109, 1, 1, '2026-10-18 23:37:51', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(110, 1, 1, '2026-10-18 23:41:13', 'Venta registrada', 'ventas', 9, NULL, '$ 112.495,50', NULL, 'Venta #9 · 2 línea(s) · Efectivo (10% de descuento)'),
(111, 1, 1, '2026-10-18 23:43:55', 'Venta registrada', 'ventas', 10, NULL, '$ 139.500,00', NULL, 'Venta #10 · 1 línea(s) · Efectivo (10% de descuento)'),
(112, 1, 1, '2026-10-18 23:47:06', 'Venta registrada', 'ventas', 11, NULL, '$ 155.000,00', NULL, 'Venta #11 · 1 línea(s) · Tarjeta de débito'),
(113, 1, 1, '2026-10-18 23:58:33', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(114, 1, 1, '2026-10-19 15:48:33', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(115, 1, 1, '2026-10-19 15:53:05', 'Cierre de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini'),
(116, 3, 3, '2026-10-19 15:55:40', 'Inicio de sesión', 'usuarios', 3, NULL, NULL, NULL, 'Juan Perez'),
(117, 3, 3, '2026-10-19 17:53:16', 'Venta registrada', 'ventas', 12, NULL, '$ 149.000,00', NULL, 'Venta #12 · 2 línea(s) · Tarjeta de débito'),
(118, 3, 3, '2026-10-19 17:59:34', 'Cierre de sesión', 'usuarios', 3, NULL, NULL, NULL, 'Juan Perez'),
(119, 1, 1, '2026-10-19 18:00:07', 'Inicio de sesión', 'usuarios', 1, NULL, NULL, NULL, 'Joaquin Maini');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria`
--

CREATE TABLE `categoria` (
  `idCategoria` int NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `idCategoriaPadre` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categoria`
--

INSERT INTO `categoria` (`idCategoria`, `nombre`, `idCategoriaPadre`) VALUES
(1, 'Ropa de abrigo', 4),
(2, 'Guantes esquí', 1),
(3, 'Pantalones', 4),
(4, 'Ropa deportiva', NULL),
(5, 'Accesorios deportivos', NULL),
(7, 'Remeras', 4),
(8, 'Buzos', 4),
(9, 'Camperas', 4),
(10, 'Shorts', 4),
(11, 'Medias', 4),
(12, 'Pelotas', 5),
(13, 'Raquetas', 5),
(14, 'Protectores bucales', 5),
(15, 'Palos de hockey', 5),
(16, 'Bolsos y mochilas', 5),
(17, 'Gorras', 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
  `idCliente` int NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `apellido` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `telefono` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `correo` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `dni` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `condicionIva` enum('CONSUMIDOR_FINAL','MONOTRIBUTISTA','RESPONSABLE_INSCRIPTO') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CONSUMIDOR_FINAL',
  `cuit` varchar(13) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `razonSocial` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `domicilio` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cliente`
--

INSERT INTO `cliente` (`idCliente`, `nombre`, `apellido`, `telefono`, `correo`, `dni`, `condicionIva`, `cuit`, `razonSocial`, `domicilio`) VALUES
(1, 'Juanito', 'Lopez', '', 'correo@gmail.com', '111111', 'CONSUMIDOR_FINAL', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `descuentos`
--

CREATE TABLE `descuentos` (
  `idDescuento` int NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `naturaleza` enum('descuento','recargo') COLLATE utf8mb4_general_ci NOT NULL,
  `tipo` enum('porcentuales','por producto','especiales','generales') COLLATE utf8mb4_general_ci NOT NULL,
  `medioPago` enum('Efectivo','Debito','Credito','Transferencia') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `valor` decimal(10,2) NOT NULL,
  `fechaInicio` date DEFAULT NULL,
  `fechaFin` date DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `cantidadCompra` int DEFAULT NULL,
  `cantidadPaga` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `descuentos`
--

INSERT INTO `descuentos` (`idDescuento`, `nombre`, `naturaleza`, `tipo`, `medioPago`, `valor`, `fechaInicio`, `fechaFin`, `activo`, `cantidadCompra`, `cantidadPaga`) VALUES
(1, 'Pago descuento débito', 'descuento', 'porcentuales', 'Debito', 10.00, NULL, NULL, 0, NULL, NULL),
(2, 'Pago con crédito', 'recargo', 'porcentuales', 'Credito', 10.00, NULL, NULL, 1, NULL, NULL),
(3, 'Pago con efectivo', 'descuento', 'generales', 'Efectivo', 10.00, NULL, NULL, 0, NULL, NULL),
(4, 'Navidad', 'descuento', 'porcentuales', NULL, 20.00, '2026-12-01', '2028-12-28', 1, NULL, NULL),
(5, 'Descuento por producto', 'descuento', 'por producto', NULL, 5.00, NULL, NULL, 1, NULL, NULL),
(6, 'Descuetnto por producto', 'descuento', 'por producto', NULL, 10000.00, NULL, NULL, 1, NULL, NULL),
(7, '2x1', 'descuento', 'especiales', NULL, 0.00, NULL, NULL, 1, 2, 1),
(8, 'Efectivo', 'descuento', 'porcentuales', 'Efectivo', 10.00, NULL, NULL, 0, NULL, NULL),
(9, 'Efectivo', 'descuento', 'porcentuales', 'Efectivo', 10.00, NULL, NULL, 1, NULL, NULL),
(10, 'Producto Raqueta', 'descuento', 'por producto', NULL, 25.00, '2026-10-13', '2026-10-29', 1, NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `descuento_producto`
--

CREATE TABLE `descuento_producto` (
  `idDescuento` int NOT NULL,
  `idProducto` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `descuento_producto`
--

INSERT INTO `descuento_producto` (`idDescuento`, `idProducto`) VALUES
(6, 2),
(7, 8),
(10, 16),
(5, 30);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_devolucion`
--

CREATE TABLE `detalle_devolucion` (
  `idDetDevolucion` int NOT NULL,
  `idDevolucion` int NOT NULL,
  `idVariante` int NOT NULL,
  `idUbicacion` int NOT NULL,
  `cantidad` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_dev_compra`
--

CREATE TABLE `detalle_dev_compra` (
  `idDetDevolucCompra` int NOT NULL,
  `idDevolucion` int NOT NULL,
  `idVariante` int NOT NULL,
  `cantidad` int NOT NULL,
  `motivo` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_ped_compra`
--

CREATE TABLE `detalle_ped_compra` (
  `idDetPedido` int NOT NULL,
  `idPedido` int NOT NULL,
  `idVariante` int NOT NULL,
  `skuProveedor` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cantidadPedida` int NOT NULL,
  `costoAcordado` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_recepcion`
--

CREATE TABLE `detalle_recepcion` (
  `idDetRecepcion` int NOT NULL,
  `idRecepcion` int NOT NULL,
  `idVariante` int NOT NULL,
  `cantidadRecibida` int NOT NULL,
  `observacion` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_ventas`
--

CREATE TABLE `detalle_ventas` (
  `idDetVenta` int NOT NULL,
  `idVenta` int NOT NULL,
  `idVariante` int NOT NULL,
  `idDescuento` int DEFAULT NULL,
  `cantidad` int NOT NULL,
  `precioHistorico` decimal(10,2) NOT NULL,
  `montoDescuentoAplicado` decimal(10,2) DEFAULT '0.00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `detalle_ventas`
--

INSERT INTO `detalle_ventas` (`idDetVenta`, `idVenta`, `idVariante`, `idDescuento`, `cantidad`, `precioHistorico`, `montoDescuentoAplicado`) VALUES
(1, 1, 1, NULL, 1, 20000.00, 0.00),
(2, 2, 1, NULL, 1, 20000.00, 0.00),
(3, 2, 17, NULL, 1, 168000.00, 0.00),
(4, 3, 30, NULL, 1, 27000.00, 0.00),
(5, 3, 8, NULL, 2, 14000.00, 0.00),
(6, 3, 5, NULL, 1, 98000.00, 0.00),
(7, 4, 2, NULL, 3, 35000.00, 0.00),
(8, 4, 10, NULL, 1, 44000.00, 0.00),
(9, 4, 3, NULL, 1, 34000.00, 0.00),
(10, 5, 4, NULL, 1, 62000.00, 0.00),
(11, 5, 29, NULL, 1, 72000.00, 0.00),
(12, 5, 30, 5, 1, 27000.00, 5.00),
(13, 5, 8, NULL, 1, 14000.00, 0.00),
(14, 6, 5, NULL, 1, 98000.00, 0.00),
(15, 7, 1, NULL, 1, 20000.00, 0.00),
(16, 7, 8, 7, 2, 14000.00, 14000.00),
(17, 8, 4, NULL, 1, 62000.00, 0.00),
(18, 9, 30, 5, 1, 27000.00, 5.00),
(19, 9, 5, NULL, 1, 98000.00, 0.00),
(20, 10, 25, NULL, 1, 155000.00, 0.00),
(21, 11, 25, NULL, 1, 155000.00, 0.00),
(22, 12, 2, 6, 1, 35000.00, 10000.00),
(23, 12, 4, NULL, 2, 62000.00, 0.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones`
--

CREATE TABLE `devoluciones` (
  `idDevolucion` int NOT NULL,
  `idVenta` int NOT NULL,
  `usrSolicita` int NOT NULL,
  `usrAutoriza` int NOT NULL,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `tipoResolucion` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `motivo` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones_compra`
--

CREATE TABLE `devoluciones_compra` (
  `idDevolucion` int NOT NULL,
  `idPedido` int NOT NULL,
  `usrSolicita` int NOT NULL,
  `usrAutoriza` int NOT NULL,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `tipoResolucion` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `motivo` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `emisor_config`
--

CREATE TABLE `emisor_config` (
  `idEmisor` tinyint NOT NULL DEFAULT '1',
  `razonSocial` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `cuit` varchar(13) COLLATE utf8mb4_general_ci NOT NULL,
  `condicionIva` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Responsable Inscripto',
  `domicilioComercial` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `ingresosBrutos` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `inicioActividades` date DEFAULT NULL,
  `puntoVenta` smallint UNSIGNED NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `emisor_config`
--

INSERT INTO `emisor_config` (`idEmisor`, `razonSocial`, `cuit`, `condicionIva`, `domicilioComercial`, `ingresosBrutos`, `inicioActividades`, `puntoVenta`) VALUES
(1, 'Punto Norte Deportes', '30-00000000-0', 'Responsable Inscripto', 'Av. Corrientes 1234, Piso 3, Oficina \"B\", CABA (Código Postal: C1043AAZ)', '000-000000-0', '2015-01-01', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `marca`
--

CREATE TABLE `marca` (
  `idMarca` int NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `marca`
--

INSERT INTO `marca` (`idMarca`, `nombre`) VALUES
(1, 'Nike'),
(2, 'Reusch'),
(3, 'Adidas'),
(4, 'Puma'),
(5, 'Wilson'),
(6, 'Penalty'),
(7, 'Venum'),
(8, 'Grays');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedidos_compra`
--

CREATE TABLE `pedidos_compra` (
  `idPedido` int NOT NULL,
  `idProveedor` int NOT NULL,
  `idUsuario` int NOT NULL,
  `fechaEmision` date NOT NULL,
  `estado` enum('Pendiente','Parcial','Completado','Cancelado') COLLATE utf8mb4_general_ci DEFAULT 'Pendiente',
  `linkSeguimiento` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `idProducto` int NOT NULL,
  `idCategoria` int NOT NULL,
  `idMarca` int NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_general_ci,
  `activo` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`idProducto`, `idCategoria`, `idMarca`, `nombre`, `descripcion`, `activo`) VALUES
(1, 1, 1, 'Buzo', 'Buzo negro de frisa', 1),
(2, 2, 2, 'Guantes esqui', 'Guantes de esqui hidrofobicos y reforzados', 1),
(3, 3, 1, 'Pantalón', 'Pantalon de joggin', 1),
(4, 8, 1, 'Buzo con capucha Nike', 'Buzo deportivo de frisa con capucha y bolsillo frontal.', 1),
(5, 9, 3, 'Campera impermeable Adidas', 'Campera liviana impermeable para entrenamiento y uso urbano.', 1),
(6, 7, 4, 'Remera deportiva Puma', 'Remera respirable de secado rápido para entrenamiento.', 1),
(7, 10, 1, 'Short de entrenamiento Nike', 'Short liviano para entrenamiento.', 1),
(8, 11, 4, 'Medias deportivas Puma', 'Medias acolchadas para actividad deportiva.', 1),
(9, 3, 1, 'Pantalón jogger Nike', 'Pantalón jogger deportivo con puños.', 1),
(10, 2, 2, 'Guantes térmicos Reusch', 'Guantes térmicos para actividades de invierno.', 1),
(11, 12, 5, 'Pelota de fútbol Wilson', 'Pelota de fútbol tamaño y peso reglamentarios.', 1),
(12, 12, 5, 'Pelota de básquet Wilson', 'Pelota de básquet para entrenamiento en interior y exterior.', 1),
(13, 12, 6, 'Pelota de vóley Penalty', 'Pelota de vóley para entrenamiento.', 1),
(14, 12, 5, 'Pelota de tenis Wilson', 'Tubo de pelotas de tenis presurizadas.', 1),
(15, 12, 5, 'Pelota de rugby Wilson', 'Pelota de rugby con superficie de buen agarre.', 1),
(16, 13, 5, 'Raqueta de tenis Wilson', 'Raqueta de tenis para nivel inicial e intermedio.', 1),
(17, 13, 5, 'Raqueta de pádel Wilson', 'Raqueta de pádel con núcleo de control.', 1),
(18, 13, 5, 'Raqueta de bádminton Wilson', 'Raqueta liviana de bádminton.', 1),
(19, 13, 5, 'Raqueta de squash Wilson', 'Raqueta de squash liviana y resistente.', 1),
(20, 14, 7, 'Protector bucal Venum adulto', 'Protector bucal moldeable para deportes de contacto.', 1),
(21, 14, 7, 'Protector bucal Venum junior', 'Protector bucal moldeable de tamaño junior.', 1),
(22, 14, 7, 'Protector bucal doble Venum', 'Protector bucal doble capa para entrenamiento de contacto.', 1),
(23, 14, 7, 'Protector bucal con estuche Venum', 'Protector bucal moldeable con estuche ventilado.', 1),
(24, 15, 8, 'Palo de hockey Grays 36.5', 'Palo de hockey sobre césped de 36.5 pulgadas.', 1),
(25, 15, 8, 'Palo de hockey Grays 37.5', 'Palo de hockey sobre césped de 37.5 pulgadas.', 1),
(26, 15, 8, 'Palo de hockey junior Grays', 'Palo de hockey sobre césped para jugador junior.', 1),
(27, 15, 8, 'Palo de hockey composite Grays', 'Palo composite para hockey sobre césped.', 1),
(28, 15, 8, 'Palo de hockey indoor Grays', 'Palo liviano para hockey indoor.', 1),
(29, 16, 3, 'Mochila deportiva Adidas', 'Mochila deportiva con compartimento principal y bolsillos.', 1),
(30, 17, 1, 'Gorra deportiva Nike', 'Gorra con visera y ajuste posterior.', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producto_proveedor`
--

CREATE TABLE `producto_proveedor` (
  `idProducto` int NOT NULL,
  `idProveedor` int NOT NULL,
  `costoUnitario` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producto_variantes`
--

CREATE TABLE `producto_variantes` (
  `idVariante` int NOT NULL,
  `idProducto` int NOT NULL,
  `talle` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `color` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `sku` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `codigoBarras` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precioVenta` decimal(10,2) NOT NULL,
  `costoRef` decimal(10,2) DEFAULT NULL,
  `stockMinimo` int DEFAULT '0',
  `stockMaximo` int DEFAULT '0',
  `activo` tinyint(1) NOT NULL DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `producto_variantes`
--

INSERT INTO `producto_variantes` (`idVariante`, `idProducto`, `talle`, `color`, `sku`, `codigoBarras`, `precioVenta`, `costoRef`, `stockMinimo`, `stockMaximo`, `activo`) VALUES
(1, 1, 'M', 'Negro', 'BUZNG01', NULL, 20000.00, 10000.00, 10, 0, 1),
(2, 2, '1', 'Negro y violeta', 'GUNV01', NULL, 35000.00, 40000.00, 5, 0, 1),
(3, 3, '1', 'Negro', 'PANNEG01', NULL, 34000.00, 40000.00, 10, 0, 1),
(4, 4, 'L', 'Negro', 'BUZCAPNI01', NULL, 62000.00, 35000.00, 2, 0, 1),
(5, 5, 'M', 'Azul', 'CAMIMPAD01', NULL, 98000.00, 57000.00, 2, 0, 1),
(6, 6, 'M', 'Blanco', 'REMDEPPU01', NULL, 32000.00, 17000.00, 2, 0, 1),
(7, 7, 'M', 'Negro', 'SHOENTNI01', NULL, 35000.00, 19000.00, 2, 0, 1),
(8, 8, 'Único', 'Blanco', 'MEDDEPPU01', NULL, 14000.00, 6500.00, 2, 0, 1),
(9, 9, 'L', 'Gris', 'PANJOGNI01', NULL, 59000.00, 33000.00, 2, 0, 1),
(10, 10, 'M', 'Negro', 'GUNTEMRE01', NULL, 44000.00, 25000.00, 2, 0, 1),
(11, 11, '5', 'Blanco/Negro', 'PELFUTWI01', NULL, 48000.00, 27000.00, 2, 0, 1),
(12, 12, '7', 'Naranja', 'PELBASWI01', NULL, 52000.00, 30000.00, 2, 0, 1),
(13, 13, '5', 'Blanco/Azul', 'PELVOLPE01', NULL, 39000.00, 22000.00, 2, 0, 1),
(14, 14, 'Único', 'Amarillo', 'PELTENWI01', NULL, 18000.00, 9500.00, 2, 0, 1),
(15, 15, '5', 'Marrón', 'PELRUGWI01', NULL, 46000.00, 26000.00, 2, 0, 1),
(16, 16, 'Único', 'Negro/Rojo', 'RATTENWI01', NULL, 135000.00, 82000.00, 2, 0, 1),
(17, 17, 'Único', 'Negro/Azul', 'RATPADWI01', NULL, 168000.00, 103000.00, 2, 0, 1),
(18, 18, 'Único', 'Negro/Amarillo', 'RATBADWI01', NULL, 79000.00, 45000.00, 2, 0, 1),
(19, 19, 'Único', 'Negro', 'RATSQUWI01', NULL, 112000.00, 68000.00, 2, 0, 1),
(20, 20, 'Adulto', 'Transparente', 'PROBUDVE01', NULL, 22000.00, 10500.00, 2, 0, 1),
(21, 21, 'Junior', 'Azul', 'PROBUJVE01', NULL, 19000.00, 9000.00, 2, 0, 1),
(22, 22, 'Adulto', 'Negro', 'PROBUDVE02', NULL, 29000.00, 14500.00, 2, 0, 1),
(23, 23, 'Adulto', 'Rojo', 'PROBUEVE01', NULL, 25000.00, 12000.00, 2, 0, 1),
(24, 24, '36.5', 'Negro/Verde', 'PALHOGGR01', NULL, 145000.00, 87000.00, 2, 0, 1),
(25, 25, '37.5', 'Negro/Azul', 'PALHOGGR02', NULL, 155000.00, 93000.00, 2, 0, 1),
(26, 26, 'Junior', 'Azul', 'PALHOJGR01', NULL, 89000.00, 51000.00, 2, 0, 1),
(27, 27, '37.5', 'Negro/Rojo', 'PALHOGRGR01', NULL, 198000.00, 122000.00, 2, 0, 1),
(28, 28, '36.5', 'Negro', 'PALHOIGR01', NULL, 128000.00, 76000.00, 2, 0, 1),
(29, 29, 'Único', 'Negro', 'MOCDEPAD01', NULL, 72000.00, 42000.00, 2, 0, 1),
(30, 30, 'Único', 'Negro', 'GORDEPNI01', NULL, 27000.00, 13000.00, 2, 0, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `proveedor`
--

CREATE TABLE `proveedor` (
  `idProveedor` int NOT NULL,
  `razonSocial` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `cuit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telefono` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `correo` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `recepciones_compra`
--

CREATE TABLE `recepciones_compra` (
  `idRecepcion` int NOT NULL,
  `idPedido` int NOT NULL,
  `idUbicacion` int NOT NULL,
  `idUsuarioRecepciona` int NOT NULL,
  `numComprobante` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fechaRecepcion` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `roles`
--

CREATE TABLE `roles` (
  `idRol` int NOT NULL,
  `nombreRol` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `roles`
--

INSERT INTO `roles` (`idRol`, `nombreRol`, `descripcion`) VALUES
(1, 'Vendedor', 'Registra ventas y consulta productos y stock'),
(2, 'Depósito', 'Recibe mercadería y registra movimientos de stock'),
(3, 'Encargado', 'Supervisa ventas y controla stock'),
(4, 'Supervisor', 'Autoriza operaciones sensibles y gestiona usuarios'),
(5, 'Gerente', 'Acceso general, reportes e historial');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `stock`
--

CREATE TABLE `stock` (
  `idStock` int NOT NULL,
  `idVariante` int NOT NULL,
  `idUbicacion` int NOT NULL,
  `cantidad` int NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `stock`
--

INSERT INTO `stock` (`idStock`, `idVariante`, `idUbicacion`, `cantidad`) VALUES
(1, 1, 2, 4),
(2, 2, 2, 10),
(3, 2, 1, 6),
(4, 1, 1, 0),
(5, 3, 2, 10),
(6, 3, 1, 9),
(7, 4, 1, 1),
(8, 4, 2, 10),
(9, 5, 1, 2),
(10, 5, 2, 10),
(11, 6, 1, 5),
(12, 6, 2, 10),
(13, 7, 1, 5),
(14, 7, 2, 10),
(15, 8, 1, 5),
(16, 8, 2, 5),
(17, 9, 1, 5),
(18, 9, 2, 10),
(19, 10, 1, 4),
(20, 10, 2, 10),
(21, 11, 1, 5),
(22, 11, 2, 10),
(23, 12, 1, 5),
(24, 12, 2, 10),
(25, 13, 1, 5),
(26, 13, 2, 10),
(27, 14, 1, 5),
(28, 14, 2, 10),
(29, 15, 1, 5),
(30, 15, 2, 10),
(31, 16, 1, 5),
(32, 16, 2, 10),
(33, 17, 1, 4),
(34, 17, 2, 10),
(35, 18, 1, 5),
(36, 18, 2, 10),
(37, 19, 1, 5),
(38, 19, 2, 10),
(39, 20, 1, 5),
(40, 20, 2, 10),
(41, 21, 1, 5),
(42, 21, 2, 10),
(43, 22, 1, 5),
(44, 22, 2, 10),
(45, 23, 1, 5),
(46, 23, 2, 10),
(47, 24, 1, 5),
(48, 24, 2, 10),
(49, 25, 1, 3),
(50, 25, 2, 10),
(51, 26, 1, 5),
(52, 26, 2, 10),
(53, 27, 1, 5),
(54, 27, 2, 10),
(55, 28, 1, 5),
(56, 28, 2, 10),
(57, 29, 1, 4),
(58, 29, 2, 10),
(59, 30, 1, 2),
(60, 30, 2, 10);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ubicacion`
--

CREATE TABLE `ubicacion` (
  `idUbicacion` int NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `ubicacion`
--

INSERT INTO `ubicacion` (`idUbicacion`, `nombre`) VALUES
(1, 'Salon'),
(2, 'Deposito');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `idUsuario` int NOT NULL,
  `idRol` int NOT NULL,
  `nombre` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `apellido` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `dni` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telefono` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `correo` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fechaIngreso` date DEFAULT NULL,
  `puestoUsername` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `passwordHash` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `activo` enum('PENDIENTE','ACTIVO','PAUSADO','DESACTIVADO') COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`idUsuario`, `idRol`, `nombre`, `apellido`, `dni`, `telefono`, `correo`, `fechaIngreso`, `puestoUsername`, `username`, `passwordHash`, `activo`) VALUES
(1, 4, 'Joaquin', 'Maini', '40111222', '', 'supervisor@puntonorte.com', '2026-09-30', 'Supervisor', 'supervisor@puntonorte.com', '9c6c4878ecd919da3e62480eb8bb67cea2bcaaf144edbef27b1c7528bc1e6d31', 'ACTIVO'),
(2, 1, 'Rocio', 'Monzon', '11223344', '1166669606', 'belenrociomonzon11@gmail.com', '2026-09-30', 'Vendedor', 'belenrociomonzon11@gmail.com', '6831637e37caa6016d8eeecfe2311b06728be274e69dbb0ac7a272d668f1bbdd', 'ACTIVO'),
(3, 1, 'Juan', 'Perez', '1122333', '113334444', 'rocio.monzon.t1vl@gmail.com', '2026-10-01', 'Vendedor', 'rocio.monzon.t1vl@gmail.com', '6a9354b8b23b2d5cd299cd937068b4f5c9d40ea4e27efa7b2bd7547357baaef4', 'ACTIVO'),
(4, 1, 'Manu', 'Carabajal', '1111111', '1112222222', 'manuelcarabajal944@gmail.com', '2026-10-05', 'Vendedor', 'manuelcarabajal944@gmail.com', 'b537623dd5b4a0715a5235add7654a66c247909357a7754b760eef8d40c376dc', 'ACTIVO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ventas`
--

CREATE TABLE `ventas` (
  `idVenta` int NOT NULL,
  `idCliente` int DEFAULT NULL,
  `idUsuario` int NOT NULL,
  `idUbicacion` int NOT NULL,
  `idDescuento` int DEFAULT NULL,
  `medioPago` enum('Efectivo','Debito','Credito','Transferencia') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Efectivo',
  `montoAjustePago` decimal(10,2) DEFAULT '0.00',
  `estado` enum('Esperando','Pagada','Entregada','Anulada','Devuelta') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Esperando',
  `montoRecibido` decimal(10,2) DEFAULT NULL,
  `vuelto` decimal(10,2) DEFAULT NULL,
  `observaciones` text COLLATE utf8mb4_general_ci,
  `fechaHora` datetime DEFAULT CURRENT_TIMESTAMP,
  `subtotal` decimal(10,2) NOT NULL,
  `descuentoTotal` decimal(10,2) DEFAULT '0.00',
  `montoTotal` decimal(10,2) NOT NULL,
  `tipoComprobante` enum('A','B') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `estadoFactura` enum('BORRADOR','EMITIDA','ANULADA') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `puntoVenta` smallint UNSIGNED DEFAULT NULL,
  `numeroComprobante` int UNSIGNED DEFAULT NULL,
  `condicionIvaReceptor` enum('CONSUMIDOR_FINAL','MONOTRIBUTISTA','RESPONSABLE_INSCRIPTO') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receptorNombre` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receptorTipoDoc` enum('SIN_IDENTIFICAR','DNI','CUIL','CDI','CUIT','PASAPORTE') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receptorNumeroDoc` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receptorDomicilio` varchar(150) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `montoNeto` decimal(12,2) DEFAULT NULL,
  `montoIva` decimal(12,2) DEFAULT NULL,
  `cae` varchar(14) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `vencimientoCae` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `ventas`
--

INSERT INTO `ventas` (`idVenta`, `idCliente`, `idUsuario`, `idUbicacion`, `idDescuento`, `medioPago`, `montoAjustePago`, `estado`, `montoRecibido`, `vuelto`, `observaciones`, `fechaHora`, `subtotal`, `descuentoTotal`, `montoTotal`, `tipoComprobante`, `estadoFactura`, `puntoVenta`, `numeroComprobante`, `condicionIvaReceptor`, `receptorNombre`, `receptorTipoDoc`, `receptorNumeroDoc`, `receptorDomicilio`, `montoNeto`, `montoIva`, `cae`, `vencimientoCae`) VALUES
(1, NULL, 1, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-08 19:23:25', 20000.00, 0.00, 20000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, NULL, 1, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-08 20:15:37', 188000.00, 0.00, 188000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, NULL, 1, 1, 3, 'Efectivo', -10.00, 'Pagada', 160000.00, 7010.00, NULL, '2026-10-08 21:00:02', 153000.00, 0.00, 152990.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, NULL, 1, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-12 19:35:22', 183000.00, 0.00, 183000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, NULL, 1, 1, NULL, 'Efectivo', 0.00, 'Pagada', 180000.00, 5005.00, NULL, '2026-10-12 20:39:47', 175000.00, 5.00, 174995.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, NULL, 1, 1, 9, 'Efectivo', -9800.00, 'Pagada', 90000.00, 1800.00, NULL, '2026-10-12 20:44:35', 98000.00, 0.00, 88200.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(7, NULL, 1, 1, 9, 'Efectivo', -3400.00, 'Pagada', 31000.00, 400.00, NULL, '2026-10-12 21:09:56', 48000.00, 14000.00, 30600.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(8, 1, 1, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-12 21:10:51', 62000.00, 0.00, 62000.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(9, NULL, 1, 1, 9, 'Efectivo', -12499.50, 'Pagada', 113000.00, 504.50, NULL, '2026-10-18 23:41:08', 125000.00, 5.00, 112495.50, 'B', 'EMITIDA', 1, 1, 'CONSUMIDOR_FINAL', 'A CONSUMIDOR FINAL', 'SIN_IDENTIFICAR', NULL, NULL, 92971.49, 19524.01, '99990001000001', '2026-10-28'),
(10, NULL, 1, 1, 9, 'Efectivo', -15500.00, 'Pagada', 140000.00, 500.00, NULL, '2026-10-18 23:43:51', 155000.00, 0.00, 139500.00, 'B', 'EMITIDA', 1, 2, 'MONOTRIBUTISTA', 'MONOTRIBUTISTA (SIN IDENTIFICAR)', 'SIN_IDENTIFICAR', NULL, NULL, 115289.26, 24210.74, '99990001000002', '2026-10-28'),
(11, NULL, 1, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-18 23:47:02', 155000.00, 0.00, 155000.00, 'A', 'EMITIDA', 1, 1, 'RESPONSABLE_INSCRIPTO', 'Juancito panes', 'CUIT', '23172033059', 'Una callesita', 128099.17, 26900.83, '99990001000001', '2026-10-28'),
(12, NULL, 3, 1, NULL, 'Debito', 0.00, 'Pagada', NULL, NULL, NULL, '2026-10-19 17:53:06', 159000.00, 10000.00, 149000.00, 'B', 'EMITIDA', 1, 3, 'CONSUMIDOR_FINAL', 'A CONSUMIDOR FINAL', 'SIN_IDENTIFICAR', NULL, NULL, 123140.50, 25859.50, '99996001000003', '2026-10-29');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `venta_descuento`
--

CREATE TABLE `venta_descuento` (
  `idVenta` int NOT NULL,
  `idDescuento` int NOT NULL,
  `montoAplicado` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `anulaciones`
--
ALTER TABLE `anulaciones`
  ADD PRIMARY KEY (`idAnulacion`),
  ADD KEY `fk_anulacion_venta` (`idVenta`),
  ADD KEY `fk_anulacion_solicita` (`usrSolicita`),
  ADD KEY `fk_anulacion_autoriza` (`usrAutoriza`);

--
-- Indices de la tabla `anulacion_compra`
--
ALTER TABLE `anulacion_compra`
  ADD PRIMARY KEY (`idAnulacion`),
  ADD KEY `fk_anulcompra_pedido` (`idPedido`),
  ADD KEY `fk_anulcompra_solicita` (`usrSolicita`),
  ADD KEY `fk_anulcompra_autoriza` (`usrAutoriza`);

--
-- Indices de la tabla `auditoria`
--
ALTER TABLE `auditoria`
  ADD PRIMARY KEY (`idAuditoria`),
  ADD KEY `fk_auditoria_usuario` (`idUsuario`),
  ADD KEY `fk_auditoria_autoriza` (`usrAutoriza`);

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`idCategoria`),
  ADD KEY `fk_categoria_padre` (`idCategoriaPadre`);

--
-- Indices de la tabla `cliente`
--
ALTER TABLE `cliente`
  ADD PRIMARY KEY (`idCliente`),
  ADD UNIQUE KEY `uq_cliente_cuit` (`cuit`);

--
-- Indices de la tabla `descuentos`
--
ALTER TABLE `descuentos`
  ADD PRIMARY KEY (`idDescuento`);

--
-- Indices de la tabla `descuento_producto`
--
ALTER TABLE `descuento_producto`
  ADD PRIMARY KEY (`idDescuento`,`idProducto`),
  ADD KEY `idx_descuento_producto_producto` (`idProducto`);

--
-- Indices de la tabla `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  ADD PRIMARY KEY (`idDetDevolucion`),
  ADD KEY `fk_detdevol_devolucion` (`idDevolucion`),
  ADD KEY `fk_detdevol_variante` (`idVariante`),
  ADD KEY `fk_detdevol_ubicacion` (`idUbicacion`);

--
-- Indices de la tabla `detalle_dev_compra`
--
ALTER TABLE `detalle_dev_compra`
  ADD PRIMARY KEY (`idDetDevolucCompra`),
  ADD KEY `fk_detdevolcompra_devolucion` (`idDevolucion`),
  ADD KEY `fk_detdevolcompra_variante` (`idVariante`);

--
-- Indices de la tabla `detalle_ped_compra`
--
ALTER TABLE `detalle_ped_compra`
  ADD PRIMARY KEY (`idDetPedido`),
  ADD KEY `fk_detped_pedido` (`idPedido`),
  ADD KEY `fk_detped_variante` (`idVariante`);

--
-- Indices de la tabla `detalle_recepcion`
--
ALTER TABLE `detalle_recepcion`
  ADD PRIMARY KEY (`idDetRecepcion`),
  ADD KEY `fk_detrecep_recepcion` (`idRecepcion`),
  ADD KEY `fk_detrecep_variante` (`idVariante`);

--
-- Indices de la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  ADD PRIMARY KEY (`idDetVenta`),
  ADD KEY `fk_detventa_venta` (`idVenta`),
  ADD KEY `fk_detventa_variante` (`idVariante`),
  ADD KEY `fk_detventa_descuento` (`idDescuento`);

--
-- Indices de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD PRIMARY KEY (`idDevolucion`),
  ADD KEY `fk_devolucion_venta` (`idVenta`),
  ADD KEY `fk_devolucion_solicita` (`usrSolicita`),
  ADD KEY `fk_devolucion_autoriza` (`usrAutoriza`);

--
-- Indices de la tabla `devoluciones_compra`
--
ALTER TABLE `devoluciones_compra`
  ADD PRIMARY KEY (`idDevolucion`),
  ADD KEY `fk_devolcompra_pedido` (`idPedido`),
  ADD KEY `fk_devolcompra_solicita` (`usrSolicita`),
  ADD KEY `fk_devolcompra_autoriza` (`usrAutoriza`);

--
-- Indices de la tabla `emisor_config`
--
ALTER TABLE `emisor_config`
  ADD PRIMARY KEY (`idEmisor`);

--
-- Indices de la tabla `marca`
--
ALTER TABLE `marca`
  ADD PRIMARY KEY (`idMarca`);

--
-- Indices de la tabla `pedidos_compra`
--
ALTER TABLE `pedidos_compra`
  ADD PRIMARY KEY (`idPedido`),
  ADD KEY `fk_pedido_proveedor` (`idProveedor`),
  ADD KEY `fk_pedido_usuario` (`idUsuario`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`idProducto`),
  ADD KEY `fk_productos_categoria` (`idCategoria`),
  ADD KEY `fk_productos_marca` (`idMarca`);

--
-- Indices de la tabla `producto_proveedor`
--
ALTER TABLE `producto_proveedor`
  ADD PRIMARY KEY (`idProducto`,`idProveedor`),
  ADD KEY `fk_pp_proveedor` (`idProveedor`);

--
-- Indices de la tabla `producto_variantes`
--
ALTER TABLE `producto_variantes`
  ADD PRIMARY KEY (`idVariante`),
  ADD KEY `fk_variantes_producto` (`idProducto`);

--
-- Indices de la tabla `proveedor`
--
ALTER TABLE `proveedor`
  ADD PRIMARY KEY (`idProveedor`);

--
-- Indices de la tabla `recepciones_compra`
--
ALTER TABLE `recepciones_compra`
  ADD PRIMARY KEY (`idRecepcion`),
  ADD KEY `fk_recepcion_pedido` (`idPedido`),
  ADD KEY `fk_recepcion_ubicacion` (`idUbicacion`),
  ADD KEY `fk_recepcion_usuario` (`idUsuarioRecepciona`);

--
-- Indices de la tabla `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`idRol`);

--
-- Indices de la tabla `stock`
--
ALTER TABLE `stock`
  ADD PRIMARY KEY (`idStock`),
  ADD UNIQUE KEY `idVariante` (`idVariante`,`idUbicacion`),
  ADD KEY `fk_stock_ubicacion` (`idUbicacion`);

--
-- Indices de la tabla `ubicacion`
--
ALTER TABLE `ubicacion`
  ADD PRIMARY KEY (`idUbicacion`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`idUsuario`),
  ADD KEY `fk_usuarios_rol` (`idRol`);

--
-- Indices de la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD PRIMARY KEY (`idVenta`),
  ADD UNIQUE KEY `uq_comprobante` (`tipoComprobante`,`puntoVenta`,`numeroComprobante`),
  ADD KEY `fk_ventas_cliente` (`idCliente`),
  ADD KEY `fk_ventas_usuario` (`idUsuario`),
  ADD KEY `fk_ventas_ubicacion` (`idUbicacion`),
  ADD KEY `fk_ventas_descuento` (`idDescuento`);

--
-- Indices de la tabla `venta_descuento`
--
ALTER TABLE `venta_descuento`
  ADD PRIMARY KEY (`idVenta`,`idDescuento`),
  ADD KEY `idx_venta_descuento_descuento` (`idDescuento`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `anulaciones`
--
ALTER TABLE `anulaciones`
  MODIFY `idAnulacion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `anulacion_compra`
--
ALTER TABLE `anulacion_compra`
  MODIFY `idAnulacion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `auditoria`
--
ALTER TABLE `auditoria`
  MODIFY `idAuditoria` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=120;

--
-- AUTO_INCREMENT de la tabla `categoria`
--
ALTER TABLE `categoria`
  MODIFY `idCategoria` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT de la tabla `cliente`
--
ALTER TABLE `cliente`
  MODIFY `idCliente` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `descuentos`
--
ALTER TABLE `descuentos`
  MODIFY `idDescuento` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  MODIFY `idDetDevolucion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_dev_compra`
--
ALTER TABLE `detalle_dev_compra`
  MODIFY `idDetDevolucCompra` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_ped_compra`
--
ALTER TABLE `detalle_ped_compra`
  MODIFY `idDetPedido` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_recepcion`
--
ALTER TABLE `detalle_recepcion`
  MODIFY `idDetRecepcion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  MODIFY `idDetVenta` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  MODIFY `idDevolucion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `devoluciones_compra`
--
ALTER TABLE `devoluciones_compra`
  MODIFY `idDevolucion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `marca`
--
ALTER TABLE `marca`
  MODIFY `idMarca` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `pedidos_compra`
--
ALTER TABLE `pedidos_compra`
  MODIFY `idPedido` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `idProducto` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT de la tabla `producto_variantes`
--
ALTER TABLE `producto_variantes`
  MODIFY `idVariante` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT de la tabla `proveedor`
--
ALTER TABLE `proveedor`
  MODIFY `idProveedor` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `recepciones_compra`
--
ALTER TABLE `recepciones_compra`
  MODIFY `idRecepcion` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `roles`
--
ALTER TABLE `roles`
  MODIFY `idRol` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `stock`
--
ALTER TABLE `stock`
  MODIFY `idStock` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- AUTO_INCREMENT de la tabla `ubicacion`
--
ALTER TABLE `ubicacion`
  MODIFY `idUbicacion` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `idUsuario` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `ventas`
--
ALTER TABLE `ventas`
  MODIFY `idVenta` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `anulaciones`
--
ALTER TABLE `anulaciones`
  ADD CONSTRAINT `fk_anulacion_autoriza` FOREIGN KEY (`usrAutoriza`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_anulacion_solicita` FOREIGN KEY (`usrSolicita`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_anulacion_venta` FOREIGN KEY (`idVenta`) REFERENCES `ventas` (`idVenta`);

--
-- Filtros para la tabla `anulacion_compra`
--
ALTER TABLE `anulacion_compra`
  ADD CONSTRAINT `fk_anulcompra_autoriza` FOREIGN KEY (`usrAutoriza`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_anulcompra_pedido` FOREIGN KEY (`idPedido`) REFERENCES `pedidos_compra` (`idPedido`),
  ADD CONSTRAINT `fk_anulcompra_solicita` FOREIGN KEY (`usrSolicita`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `auditoria`
--
ALTER TABLE `auditoria`
  ADD CONSTRAINT `fk_auditoria_autoriza` FOREIGN KEY (`usrAutoriza`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_auditoria_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `categoria`
--
ALTER TABLE `categoria`
  ADD CONSTRAINT `fk_categoria_padre` FOREIGN KEY (`idCategoriaPadre`) REFERENCES `categoria` (`idCategoria`);

--
-- Filtros para la tabla `descuento_producto`
--
ALTER TABLE `descuento_producto`
  ADD CONSTRAINT `fk_descuento_producto_descuento` FOREIGN KEY (`idDescuento`) REFERENCES `descuentos` (`idDescuento`),
  ADD CONSTRAINT `fk_descuento_producto_producto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`);

--
-- Filtros para la tabla `detalle_devolucion`
--
ALTER TABLE `detalle_devolucion`
  ADD CONSTRAINT `fk_detdevol_devolucion` FOREIGN KEY (`idDevolucion`) REFERENCES `devoluciones` (`idDevolucion`),
  ADD CONSTRAINT `fk_detdevol_ubicacion` FOREIGN KEY (`idUbicacion`) REFERENCES `ubicacion` (`idUbicacion`),
  ADD CONSTRAINT `fk_detdevol_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`);

--
-- Filtros para la tabla `detalle_dev_compra`
--
ALTER TABLE `detalle_dev_compra`
  ADD CONSTRAINT `fk_detdevolcompra_devolucion` FOREIGN KEY (`idDevolucion`) REFERENCES `devoluciones_compra` (`idDevolucion`),
  ADD CONSTRAINT `fk_detdevolcompra_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`);

--
-- Filtros para la tabla `detalle_ped_compra`
--
ALTER TABLE `detalle_ped_compra`
  ADD CONSTRAINT `fk_detped_pedido` FOREIGN KEY (`idPedido`) REFERENCES `pedidos_compra` (`idPedido`),
  ADD CONSTRAINT `fk_detped_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`);

--
-- Filtros para la tabla `detalle_recepcion`
--
ALTER TABLE `detalle_recepcion`
  ADD CONSTRAINT `fk_detrecep_recepcion` FOREIGN KEY (`idRecepcion`) REFERENCES `recepciones_compra` (`idRecepcion`),
  ADD CONSTRAINT `fk_detrecep_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`);

--
-- Filtros para la tabla `detalle_ventas`
--
ALTER TABLE `detalle_ventas`
  ADD CONSTRAINT `fk_detventa_descuento` FOREIGN KEY (`idDescuento`) REFERENCES `descuentos` (`idDescuento`),
  ADD CONSTRAINT `fk_detventa_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`),
  ADD CONSTRAINT `fk_detventa_venta` FOREIGN KEY (`idVenta`) REFERENCES `ventas` (`idVenta`);

--
-- Filtros para la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD CONSTRAINT `fk_devolucion_autoriza` FOREIGN KEY (`usrAutoriza`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_devolucion_solicita` FOREIGN KEY (`usrSolicita`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_devolucion_venta` FOREIGN KEY (`idVenta`) REFERENCES `ventas` (`idVenta`);

--
-- Filtros para la tabla `devoluciones_compra`
--
ALTER TABLE `devoluciones_compra`
  ADD CONSTRAINT `fk_devolcompra_autoriza` FOREIGN KEY (`usrAutoriza`) REFERENCES `usuarios` (`idUsuario`),
  ADD CONSTRAINT `fk_devolcompra_pedido` FOREIGN KEY (`idPedido`) REFERENCES `pedidos_compra` (`idPedido`),
  ADD CONSTRAINT `fk_devolcompra_solicita` FOREIGN KEY (`usrSolicita`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `pedidos_compra`
--
ALTER TABLE `pedidos_compra`
  ADD CONSTRAINT `fk_pedido_proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedor` (`idProveedor`),
  ADD CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `fk_productos_categoria` FOREIGN KEY (`idCategoria`) REFERENCES `categoria` (`idCategoria`),
  ADD CONSTRAINT `fk_productos_marca` FOREIGN KEY (`idMarca`) REFERENCES `marca` (`idMarca`);

--
-- Filtros para la tabla `producto_proveedor`
--
ALTER TABLE `producto_proveedor`
  ADD CONSTRAINT `fk_pp_producto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`),
  ADD CONSTRAINT `fk_pp_proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `proveedor` (`idProveedor`);

--
-- Filtros para la tabla `producto_variantes`
--
ALTER TABLE `producto_variantes`
  ADD CONSTRAINT `fk_variantes_producto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`);

--
-- Filtros para la tabla `recepciones_compra`
--
ALTER TABLE `recepciones_compra`
  ADD CONSTRAINT `fk_recepcion_pedido` FOREIGN KEY (`idPedido`) REFERENCES `pedidos_compra` (`idPedido`),
  ADD CONSTRAINT `fk_recepcion_ubicacion` FOREIGN KEY (`idUbicacion`) REFERENCES `ubicacion` (`idUbicacion`),
  ADD CONSTRAINT `fk_recepcion_usuario` FOREIGN KEY (`idUsuarioRecepciona`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `stock`
--
ALTER TABLE `stock`
  ADD CONSTRAINT `fk_stock_ubicacion` FOREIGN KEY (`idUbicacion`) REFERENCES `ubicacion` (`idUbicacion`),
  ADD CONSTRAINT `fk_stock_variante` FOREIGN KEY (`idVariante`) REFERENCES `producto_variantes` (`idVariante`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `fk_usuarios_rol` FOREIGN KEY (`idRol`) REFERENCES `roles` (`idRol`);

--
-- Filtros para la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD CONSTRAINT `fk_ventas_cliente` FOREIGN KEY (`idCliente`) REFERENCES `cliente` (`idCliente`),
  ADD CONSTRAINT `fk_ventas_descuento` FOREIGN KEY (`idDescuento`) REFERENCES `descuentos` (`idDescuento`),
  ADD CONSTRAINT `fk_ventas_ubicacion` FOREIGN KEY (`idUbicacion`) REFERENCES `ubicacion` (`idUbicacion`),
  ADD CONSTRAINT `fk_ventas_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`);

--
-- Filtros para la tabla `venta_descuento`
--
ALTER TABLE `venta_descuento`
  ADD CONSTRAINT `fk_venta_descuento_descuento` FOREIGN KEY (`idDescuento`) REFERENCES `descuentos` (`idDescuento`),
  ADD CONSTRAINT `fk_venta_descuento_venta` FOREIGN KEY (`idVenta`) REFERENCES `ventas` (`idVenta`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
