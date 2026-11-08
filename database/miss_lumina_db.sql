CREATE DATABASE miss_lumina_db
    WITH ENCODING 'UTF8'
    TEMPLATE template0;

\connect miss_lumina_db

DROP TABLE IF EXISTS detalle_pedido CASCADE;
DROP TABLE IF EXISTS pedidos CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS tonos CASCADE;
DROP TABLE IF EXISTS marcas CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    correo          VARCHAR(150) NOT NULL,
    contrasena      VARCHAR(128) NOT NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE',
    telefono        VARCHAR(30),
    fecha_registro  DATE         NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT uk_usuarios_correo UNIQUE (correo)
);

CREATE TABLE categorias (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    descripcion  VARCHAR(255),
    activa       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_categorias_nombre UNIQUE (nombre)
);

CREATE TABLE marcas (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(80) NOT NULL,
    pais_origen   VARCHAR(80),
    activa        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_marcas_nombre UNIQUE (nombre)
);

CREATE TABLE tonos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL,
    codigo_hex  VARCHAR(7),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_tonos_nombre UNIQUE (nombre)
);

CREATE TABLE productos (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(150) NOT NULL,
    descripcion      TEXT,
    imagen_url       VARCHAR(255),
    precio           NUMERIC(12,2) NOT NULL,
    precio_anterior  NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock            INTEGER NOT NULL DEFAULT 0,
    marca            VARCHAR(80) NOT NULL,
    categoria        VARCHAR(80) NOT NULL,
    tono             VARCHAR(80),
    disponible       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_productos_precio CHECK (precio > 0),
    CONSTRAINT chk_productos_precio_anterior CHECK (precio_anterior >= 0),
    CONSTRAINT chk_productos_stock CHECK (stock >= 0)
);

CREATE TABLE pedidos (
    id                 BIGSERIAL PRIMARY KEY,
    id_transaccion     UUID NOT NULL,
    fecha_pedido       DATE NOT NULL,
    fecha_hora_pedido  TIMESTAMP NOT NULL,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    total              NUMERIC(12,2) NOT NULL DEFAULT 0,
    metodo_pago        VARCHAR(40) NOT NULL,
    usuario_id         BIGINT NOT NULL,
    CONSTRAINT uk_pedidos_id_transaccion UNIQUE (id_transaccion),
    CONSTRAINT chk_pedidos_estado CHECK (
        estado IN ('PENDIENTE', 'CONFIRMADO', 'ENVIADO', 'ENTREGADO', 'CANCELADO')
    ),
    CONSTRAINT chk_pedidos_total CHECK (total >= 0)
);

CREATE TABLE detalle_pedido (
    id               BIGSERIAL PRIMARY KEY,
    pedido_id        BIGINT NOT NULL,
    producto_id      BIGINT NOT NULL,
    cantidad         INTEGER NOT NULL,
    precio_unitario  NUMERIC(12,2) NOT NULL,
    subtotal         NUMERIC(12,2) NOT NULL,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_subtotal CHECK (subtotal >= 0)
);

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_rol CHECK (rol IN ('CLIENTE', 'ADMINISTRADOR'));

ALTER TABLE productos
    ADD CONSTRAINT fk_productos_categoria
        FOREIGN KEY (categoria) REFERENCES categorias (nombre)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    ADD CONSTRAINT fk_productos_marca
        FOREIGN KEY (marca) REFERENCES marcas (nombre)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    ADD CONSTRAINT fk_productos_tono
        FOREIGN KEY (tono) REFERENCES tonos (nombre)
        ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE pedidos
    ADD CONSTRAINT fk_pedidos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;

ALTER TABLE detalle_pedido
    ADD CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    ADD CONSTRAINT fk_detalle_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;

CREATE INDEX idx_productos_categoria ON productos (categoria);
CREATE INDEX idx_productos_marca ON productos (marca);
CREATE INDEX idx_productos_tono ON productos (tono);
CREATE INDEX idx_pedidos_usuario ON pedidos (usuario_id);
CREATE INDEX idx_pedidos_fecha ON pedidos (fecha_pedido);
CREATE INDEX idx_detalle_pedido ON detalle_pedido (pedido_id);
CREATE INDEX idx_detalle_producto ON detalle_pedido (producto_id);


INSERT INTO usuarios (nombre, correo, contrasena, rol, telefono, fecha_registro) VALUES
    ('Administradora Miss Lumina', 'fayuthr4@gmail.com',
     'acf7790402d727d0202543933a7286d208f75788934902b278ed6ebfe55795a8',
     'ADMINISTRADOR', '3005053089', CURRENT_DATE - INTERVAL '6 months'),

SELECT p.id, p.nombre, p.descripcion, p.imagen_url, p.categoria, p.marca, p.tono, p.precio, p.stock,
       (p.stock * p.precio) AS valor_inventario
FROM productos p
ORDER BY p.nombre;

SELECT p.id, p.id_transaccion, p.fecha_pedido, p.estado, p.total,
       u.nombre AS cliente,
       COALESCE(SUM(d.cantidad), 0) AS unidades
FROM pedidos p
JOIN usuarios u ON u.id = p.usuario_id
LEFT JOIN detalle_pedido d ON d.pedido_id = p.id
GROUP BY p.id, p.id_transaccion, p.fecha_pedido, p.estado, p.total, u.nombre
ORDER BY p.fecha_hora_pedido DESC;

SELECT nombre, marca, stock
FROM productos
WHERE stock <= 5
ORDER BY stock ASC;

SELECT p.id_transaccion, pr.nombre AS producto, d.cantidad,
       d.precio_unitario, d.subtotal
FROM pedidos p
JOIN detalle_pedido d ON d.pedido_id = p.id
JOIN productos pr ON pr.id = d.producto_id
WHERE p.id_transaccion = '11111111-1111-4111-8111-111111111111';

SELECT COUNT(*) AS total_productos FROM productos;
SELECT COUNT(*) AS total_pedidos FROM pedidos;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
