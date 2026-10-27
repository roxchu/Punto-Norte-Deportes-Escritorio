-- Ejecutar una sola vez sobre la base s32_puntonorte.
-- Las solicitudes no afectan el stock hasta que Supervisor o Gerente las aprueba.
CREATE TABLE IF NOT EXISTS solicitudes_operativas (
  idSolicitud INT NOT NULL AUTO_INCREMENT,
  tipo ENUM('ANULACION_VENTA','RECEPCION_COMPRA') NOT NULL,
  idVenta INT DEFAULT NULL,
  idPedido INT DEFAULT NULL,
  idUbicacion INT DEFAULT NULL,
  numComprobante VARCHAR(50) DEFAULT NULL,
  motivo TEXT NOT NULL,
  idUsuarioSolicita INT NOT NULL,
  estado ENUM('Pendiente','Aprobada','Rechazada') NOT NULL DEFAULT 'Pendiente',
  idUsuarioResuelve INT DEFAULT NULL,
  fechaSolicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fechaResolucion DATETIME DEFAULT NULL,
  motivoResolucion TEXT DEFAULT NULL,
  PRIMARY KEY (idSolicitud),
  KEY idx_solicitud_pendiente (estado, fechaSolicitud),
  KEY idx_solicitud_venta (idVenta, estado),
  KEY idx_solicitud_pedido (idPedido, estado),
  CONSTRAINT fk_sol_usuario_solicita FOREIGN KEY (idUsuarioSolicita) REFERENCES usuarios (idUsuario),
  CONSTRAINT fk_sol_usuario_resuelve FOREIGN KEY (idUsuarioResuelve) REFERENCES usuarios (idUsuario),
  CONSTRAINT fk_sol_venta FOREIGN KEY (idVenta) REFERENCES ventas (idVenta),
  CONSTRAINT fk_sol_pedido FOREIGN KEY (idPedido) REFERENCES pedidos_compra (idPedido),
  CONSTRAINT fk_sol_ubicacion FOREIGN KEY (idUbicacion) REFERENCES ubicacion (idUbicacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS solicitud_operativa_detalle (
  idDetalle INT NOT NULL AUTO_INCREMENT,
  idSolicitud INT NOT NULL,
  idVariante INT NOT NULL,
  cantidad INT NOT NULL,
  observacion TEXT DEFAULT NULL,
  PRIMARY KEY (idDetalle),
  KEY idx_solicitud_detalle (idSolicitud),
  CONSTRAINT fk_sol_det_solicitud FOREIGN KEY (idSolicitud) REFERENCES solicitudes_operativas (idSolicitud),
  CONSTRAINT fk_sol_det_variante FOREIGN KEY (idVariante) REFERENCES producto_variantes (idVariante),
  CONSTRAINT chk_sol_det_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS avisos_stock_bajo (
  idAviso INT NOT NULL AUTO_INCREMENT,
  idUsuarioSolicita INT NOT NULL,
  detalle TEXT NOT NULL,
  estado ENUM('Pendiente','Atendido','Restablecido') NOT NULL DEFAULT 'Pendiente',
  idUsuarioAtiende INT DEFAULT NULL,
  fechaAviso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fechaAtencion DATETIME DEFAULT NULL,
  alertaActiva TINYINT GENERATED ALWAYS AS (CASE WHEN estado IN ('Pendiente','Atendido') THEN 1 ELSE NULL END) STORED,
  PRIMARY KEY (idAviso),
  UNIQUE KEY uq_aviso_stock_unico_activo (alertaActiva),
  CONSTRAINT fk_aviso_stock_solicita FOREIGN KEY (idUsuarioSolicita) REFERENCES usuarios (idUsuario),
  CONSTRAINT fk_aviso_stock_atiende FOREIGN KEY (idUsuarioAtiende) REFERENCES usuarios (idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
