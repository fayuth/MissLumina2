# Miss Lumina - Tienda virtual de maquillaje

Proyecto academico completo desarrollado con **Java 21**, **Spring Boot**, **Vaadin**, **JPA/Hibernate** y **PostgreSQL**. Implementa el flujo de compra de una tienda de maquillaje, desde el registro del cliente hasta la administracion de productos, inventario, pedidos y reportes.

---

## 1. Funcionalidades

### Tienda
- Portada con categorias y productos destacados.
- Registro de clientes con validacion y contrasena cifrada con SHA-256.
- Inicio y cierre de sesion con estado guardado en la sesion Vaadin.
- Catalogo con busqueda por texto, categoria, marca y tono.
- Detalle de producto con precio, descuento, stock y selector de cantidad.
- Carrito de compras en memoria por sesion.
- Confirmacion de pedido con metodo de pago, subtotal, IVA del 19% y total.
- Generacion de **UUID unico** y fecha/hora para cada transaccion.
- Historial de pedidos del cliente con detalle y estado.

### Administracion
- Panel con indicadores de ventas, pedidos, clientes, productos y stock bajo.
- CRUD de productos.
- Ajuste de inventario y deteccion de stock bajo.
- Gestion de pedidos y cambio de estado con transiciones validas.
- CRUD de categorias, marcas y tonos.
- Reportes por rango de fechas: productos mas vendidos, ventas por categoria, pedidos por estado y ventas por dia.
- API REST opcional para consultas.

---

## 2. Tecnologias

| Componente | Version / uso |
|---|---|
| Java | 21 (compilado y probado con JDK 25) |
| Spring Boot | 4.1.1 |
| Vaadin | 25.3.2, tema Lumo por defecto |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL |
| Build | Maven + Maven Wrapper |
| Logs | SLF4J + Logback (`logs/miss-lumina.log`) |

No se usa Swing ni SQLite.

---

## 3. Estructura del proyecto

```text
MissLumina(Proyecto)
├── database/
│   └── miss_lumina_db.sql          Script DDL + DML + consultas de ejemplo
├── docs/
│   ├── diagrama-clases.puml        Diagrama UML editable
│   ├── diagrama-clases.svg         Diagrama UML renderizado
│   └── guia-sustentacion.md        Explicacion para la defensa
├── src/main/java/com/misslumina/
│   ├── MissLuminaApplication.java  Unico main del proyecto
│   ├── config/                     Constantes y datos iniciales
│   ├── controller/                 API REST opcional y DTO
│   ├── exception/                  Excepciones y manejador global
│   ├── model/                      Entidades y clases del dominio
│   ├── repository/                 Repositorios Spring Data JPA
│   ├── service/                    Reglas de negocio y transacciones
│   ├── util/                       Formatos, validaciones y hash
│   └── view/                       Vistas Vaadin, layout y componentes
├── src/main/resources/
│   ├── application.properties
│   └── META-INF/resources/styles.css
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## 4. Modelo de datos

Tablas principales:

- `usuarios`
- `categorias`
- `marcas`
- `tonos`
- `productos`
- `pedidos`
- `detalle_pedido`

Relaciones:

- Un usuario tiene muchos pedidos.
- Un pedido tiene muchos detalles.
- Un producto aparece en muchos detalles.
- `productos.categoria`, `productos.marca` y `productos.tono` son llaves foraneas hacia las tablas maestras por nombre, con `ON UPDATE CASCADE`.
- `pedidos.id_transaccion` es `UUID NOT NULL UNIQUE`.

El script `database/miss_lumina_db.sql` incluye:

- `CREATE DATABASE`
- `CREATE TABLE`
- `ALTER TABLE` para llaves foraneas, checks e indices
- `INSERT` de datos de demostracion
- `SELECT`, `UPDATE` y `DELETE` de ejemplo

---

## 5. Requisitos previos

1. **JDK 21 o superior** instalado.
2. **PostgreSQL 14 o superior** en ejecucion.
3. Puerto `5432` disponible (o cambiar la configuracion).
4. Maven no es obligatorio: el proyecto incluye `mvnw.cmd` para Windows.

Verificar Java:

```powershell
java -version
```

---

## 6. Crear la base de datos

### Opcion A: psql

Desde la raiz del proyecto:

```powershell
psql -U postgres -f database/miss_lumina_db.sql
```

El script crea la base `miss_lumina_db`, las tablas y los datos de prueba.

> Si la base ya existe, comente temporalmente la linea `CREATE DATABASE` o elimine la base antes de volver a ejecutar el script.

### Opcion B: pgAdmin

1. Crear una base llamada `miss_lumina_db`.
2. Abrir el Query Tool sobre esa base.
3. Ejecutar todo el contenido del script **desde la seccion de tablas**, omitiendo `CREATE DATABASE` y `\connect`.

### Configuracion de conexion

`src/main/resources/application.properties` usa por defecto:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/miss_lumina_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Se puede cambiar con variables de entorno:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/miss_lumina_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_clave"
```

Tambien se puede editar directamente el archivo `application.properties`.

---

## 7. Ejecutar la aplicacion

En Windows:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

O con Maven instalado:

```powershell
mvn clean package
mvn spring-boot:run
```

Abrir en el navegador:

```text
http://localhost:8080
```

La primera ejecucion puede descargar dependencias de Maven y recursos del frontend de Vaadin. Se requiere conexion a Internet en ese primer arranque.

Si la base esta vacia, `DataInitializer` crea automaticamente los datos de demostracion. Si ya se ejecuto el script SQL, no duplica la informacion.

---

## 8. Cuentas de prueba

| Rol | Correo | Contrasena |
|---|---|---|
| Administrador | `admin@misslumina.com` | `Admin123*` |
| Cliente | `cliente@misslumina.com` | `Cliente123*` |

Las contrasenas se guardan como hash SHA-256, no como texto plano.

---

## 9. Rutas principales

| Ruta | Vista |
|---|---|
| `/` o `/inicio` | Portada |
| `/login` | Inicio de sesion |
| `/registro` | Registro de cliente |
| `/catalogo` | Catalogo |
| `/producto/{id}` | Detalle de producto |
| `/carrito` | Carrito |
| `/checkout` | Confirmar pedido |
| `/historial` | Historial del cliente |
| `/admin` | Panel administrativo |
| `/admin/productos` | Gestion de productos |
| `/admin/inventario` | Inventario |
| `/admin/pedidos` | Gestion de pedidos |
| `/admin/catalogos` | Categorias, marcas y tonos |
| `/admin/reportes` | Reportes |

---

## 10. Flujo transaccional del pedido

El metodo principal es `PedidoService.registrarPedido(...)` y se ejecuta dentro de `@Transactional`:

1. Valida usuario autenticado, carrito no vacio y metodo de pago.
2. Relee el usuario desde PostgreSQL.
3. Crea el pedido y genera un `UUID` con `UUID.randomUUID()`.
4. Verifica que el UUID no exista.
5. Por cada producto, lo relee con **bloqueo pesimista** (`PESSIMISTIC_WRITE`).
6. Valida stock disponible.
7. Crea los detalles y calcula subtotales.
8. Calcula subtotal, IVA y total.
9. Guarda el pedido y sus detalles.
10. Descuenta el stock y hace `flush`.
11. Si algo falla, Spring revierte toda la transaccion.

Al cancelar un pedido, el inventario se repone dentro de la misma transaccion.

---

## 11. API REST opcional

```text
GET /api/productos
GET /api/productos/{id}
GET /api/productos/destacados
GET /api/productos?texto=labial&categoria=Labiales
GET /api/pedidos
GET /api/pedidos/{id}
GET /api/pedidos/usuario/{usuarioId}
GET /api/reportes/resumen
GET /api/reportes/resumen?desde=2026-01-01&hasta=2026-12-31
GET /api/reportes/productos
GET /api/reportes/categorias
```

Los errores de negocio se transforman en respuestas JSON con codigos HTTP adecuados desde `GlobalExceptionHandler`.

---

## 12. Reglas de negocio destacadas

- Precio mayor que cero.
- Stock no negativo.
- Stock minimo configurable: `Producto.STOCK_MINIMO = 5`.
- Maximo de 20 unidades del mismo producto por carrito.
- IVA del 19%.
- Correo unico por usuario.
- No se puede eliminar un producto con pedidos asociados.
- No se puede eliminar una categoria, marca o tono con productos asociados.
- Transiciones de estado permitidas:
  - `PENDIENTE -> CONFIRMADO`
  - `PENDIENTE -> CANCELADO`
  - `CONFIRMADO -> ENVIADO`
  - `CONFIRMADO -> CANCELADO`
  - `ENVIADO -> ENTREGADO`

---

## 13. Verificacion realizada

La version entregada fue verificada con los siguientes resultados:

- `.\mvnw.cmd -DskipTests compile`: compilacion correcta.
- `.\mvnw.cmd -DskipTests package`: empaquetado correcto, incluido el frontend de Vaadin en modo produccion.
- Script `database/miss_lumina_db.sql` ejecutado correctamente en PostgreSQL 17.6: 2 usuarios, 15 productos, 3 pedidos y 4 detalles.
- Aplicacion iniciada contra PostgreSQL y respuesta HTTP 200 en `http://localhost:8080/`.
- API REST probada: `/api/productos`, `/api/productos/destacados`, `/api/productos/{id}`, `/api/pedidos`, `/api/pedidos/{id}`, `/api/pedidos/usuario/{id}`, reportes y error 404 controlado con JSON.
- Flujo de compra probado desde el navegador: login de cliente, agregar al carrito, checkout, UUID generado y stock descontado de 16 a 15.
- Panel administrativo probado: dashboard, productos, inventario, categorias/marcas/tonos, pedidos y reportes.
- Cambio de estado probado: pedido `PENDIENTE` a `CONFIRMADO` persistido en PostgreSQL.
- Cancelacion probada: pedido `CONFIRMADO` a `CANCELADO` con reposicion automatica del stock de 15 a 16.
- Base vacia probada: Hibernate creo las tablas y `DataInitializer` cargo los datos de demostracion sin ejecutar el script SQL.

La verificacion de ejecucion se hizo con PostgreSQL 17.6 en un puerto alterno
(`55432`) y con JDK 25 compilando el target Java 21. En el entorno normal del
usuario se debe usar PostgreSQL en el puerto `5432` o configurar `DB_URL`.

Comando recomendado para una verificacion completa:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Si PostgreSQL no esta disponible, la aplicacion registra el error en
`logs/miss-lumina.log` y no carga los datos iniciales.

---

## 14. Documentacion adicional

- `docs/diagrama-clases.puml`: diagrama UML editable.
- `docs/diagrama-clases.svg`: diagrama listo para incluir en el informe.
- `docs/guia-sustentacion.md`: explicacion de POO, base de datos, transacciones y preguntas frecuentes de la defensa.
