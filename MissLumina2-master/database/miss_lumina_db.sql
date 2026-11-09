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

INSERT INTO categorias (nombre, descripcion) VALUES
    ('Labiales', 'Labiales mate, cremosos y liquidos.'),
    ('Bases', 'Bases de maquillaje para todo tipo de piel.'),
    ('Sombras', 'Sombras individuales y paletas.'),
    ('Rubores', 'Rubores en polvo y cremosos.'),
    ('Delineadores', 'Delineadores de ojos y labios.'),
    ('Mascaras de pestanas', 'Mascaras para volumen y longitud.'),
    ('Polvos', 'Polvos compactos y traslucidos.');

INSERT INTO marcas (nombre, pais_origen) VALUES
    ('Maybelline', 'Estados Unidos'),
    ('L''Oreal', 'Francia'),
    ('NARS', 'Estados Unidos'),
    ('MAC', 'Canada'),
    ('Vogue', 'Colombia'),
    ('Yanbal', 'Peru');

INSERT INTO tonos (nombre, codigo_hex) VALUES
    ('Rojo Pasion', '#B3122E'),
    ('220 Natural', '#D8A47F'),
    ('Nude', '#C89F8A'),
    ('Orgasm', '#E58C8C'),
    ('Negro Intenso', '#111111'),
    ('Negro', '#1B1B1B'),
    ('02 Coy', '#B76E79'),
    ('W3 Golden Beige', '#D2A679'),
    ('Cafe', '#6B4A37'),
    ('Coral', '#E2725B'),
    ('Marron', '#5A3A2E'),
    ('Velvet Teddy', '#A45A52'),
    ('Natural', '#E3C3A3'),
    ('Rosa Romantico', '#D98A9A');

INSERT INTO usuarios (nombre, correo, contrasena, rol, telefono, fecha_registro) VALUES
    ('Administradora Miss Lumina', 'admin@misslumina.com',
     '0a5bc3e342432f1bad92ffd51b785343ec72906cdba6a26131060b008e786656',
     'ADMINISTRADOR', '3001234567', CURRENT_DATE - INTERVAL '6 months'),
    ('Valentina Gomez', 'cliente@misslumina.com',
     'ff3317ed92af000942897760a1cdad0920e8f3fdb42f7d29a4e0c4843598a30c',
     'CLIENTE', '3109876543', CURRENT_DATE - INTERVAL '2 months');

INSERT INTO productos
    (nombre, descripcion, precio, precio_anterior, stock, marca, categoria, tono, disponible)
VALUES
    ('Labial Mate Luminoso',
     'Labial de larga duracion con acabado mate y confort.',
     32900, 39900, 25, 'Maybelline', 'Labiales', 'Rojo Pasion', TRUE),
    ('Base Fit Me',
     'Base liquida de cobertura media, ideal para piel mixta.',
     54900, 62900, 18, 'Maybelline', 'Bases', '220 Natural', TRUE),
    ('Paleta de Sombras Nude',
     'Doce tonos nude con acabados mate y satinado.',
     129900, 149900, 10, 'NARS', 'Sombras', 'Nude', TRUE),
    ('Rubor en Polvo Orgasm',
     'Rubor iconico con destellos dorados.',
     119900, 0, 7, 'NARS', 'Rubores', 'Orgasm', TRUE),
    ('Delineador Tattoo',
     'Delineador liquido de punta fina y alta duracion.',
     27900, 0, 30, 'Maybelline', 'Delineadores', 'Negro Intenso', TRUE),
    ('Mascara Lash Sensational',
     'Mascara que separa y da volumen a las pestanas.',
     45900, 52900, 22, 'Maybelline', 'Mascaras de pestanas', 'Negro', TRUE),
    ('Labial Vinyl Ink',
     'Labial liquido con acabado vinilo y 16 horas de color.',
     41900, 0, 15, 'L''Oreal', 'Labiales', '02 Coy', TRUE),
    ('Base True Match',
     'Base con tecnologia de match perfecto para cada tono.',
     59900, 0, 12, 'L''Oreal', 'Bases', 'W3 Golden Beige', TRUE),
    ('Sombra Individual Nude',
     'Sombra de alta pigmentacion y facil difuminado.',
     18900, 0, 40, 'Vogue', 'Sombras', 'Cafe', TRUE),
    ('Rubor Cremoso Coral',
     'Rubor cremoso que se funde con la piel.',
     34900, 39900, 3, 'Yanbal', 'Rubores', 'Coral', TRUE),
    ('Delineador Retractil',
     'Delineador de ojos retractil con difuminador.',
     89900, 0, 6, 'MAC', 'Delineadores', 'Marron', TRUE),
    ('Labial Cremoso Velvet Teddy',
     'Labial cremoso de acabado aterciopelado.',
     109900, 0, 4, 'MAC', 'Labiales', 'Velvet Teddy', TRUE),
    ('Mascara Volume Express',
     'Mascara para volumen extremo con cepillo curvo.',
     38900, 0, 0, 'L''Oreal', 'Mascaras de pestanas', 'Negro', FALSE),
    ('Polvo Compacto Natural',
     'Polvo compacto que controla el brillo durante el dia.',
     29900, 0, 20, 'Vogue', 'Polvos', 'Natural', TRUE),
    ('Labial Liquido Rosa',
     'Labial liquido de acabado mate y tono rosa romantico.',
     37900, 42900, 16, 'Yanbal', 'Labiales', 'Rosa Romantico', TRUE);

INSERT INTO pedidos
    (id_transaccion, fecha_pedido, fecha_hora_pedido, estado, total, metodo_pago, usuario_id)
VALUES
    ('11111111-1111-4111-8111-111111111111',
     CURRENT_DATE - 10, (CURRENT_DATE - 10) + TIME '14:30:00',
     'ENTREGADO', 143633.00, 'Tarjeta de credito',
     (SELECT id FROM usuarios WHERE correo = 'cliente@misslumina.com')),
    ('22222222-2222-4222-8222-222222222222',
     CURRENT_DATE - 3, (CURRENT_DATE - 3) + TIME '10:15:00',
     'CONFIRMADO', 154581.00, 'PSE',
     (SELECT id FROM usuarios WHERE correo = 'cliente@misslumina.com')),
    ('33333333-3333-4333-8333-333333333333',
     CURRENT_DATE - 1, (CURRENT_DATE - 1) + TIME '18:05:00',
     'PENDIENTE', 99603.00, 'Nequi',
     (SELECT id FROM usuarios WHERE correo = 'cliente@misslumina.com'));

INSERT INTO detalle_pedido
    (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES
    ((SELECT id FROM pedidos WHERE id_transaccion = '11111111-1111-4111-8111-111111111111'),
     (SELECT id FROM productos WHERE nombre = 'Labial Mate Luminoso'),
     2, 32900.00, 65800.00),
    ((SELECT id FROM pedidos WHERE id_transaccion = '11111111-1111-4111-8111-111111111111'),
     (SELECT id FROM productos WHERE nombre = 'Base Fit Me'),
     1, 54900.00, 54900.00),
    ((SELECT id FROM pedidos WHERE id_transaccion = '22222222-2222-4222-8222-222222222222'),
     (SELECT id FROM productos WHERE nombre = 'Paleta de Sombras Nude'),
     1, 129900.00, 129900.00),
    ((SELECT id FROM pedidos WHERE id_transaccion = '33333333-3333-4333-8333-333333333333'),
     (SELECT id FROM productos WHERE nombre = 'Delineador Tattoo'),
     3, 27900.00, 83700.00);

INSERT INTO productos
    (nombre, descripcion, precio, precio_anterior, stock, marca, categoria, tono, disponible)
VALUES
    ('Producto temporal de prueba', 'Registro usado para demostrar UPDATE y DELETE.',
     10000, 0, 5, 'Vogue', 'Polvos', 'Natural', TRUE);

SELECT p.id, p.nombre, p.categoria, p.marca, p.tono, p.precio, p.stock,
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

UPDATE productos
SET precio = 31900.00, stock = 28
WHERE nombre = 'Labial Mate Luminoso';

DELETE FROM productos
WHERE nombre = 'Producto temporal de prueba';

SELECT COUNT(*) AS total_productos FROM productos;
SELECT COUNT(*) AS total_pedidos FROM pedidos;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
