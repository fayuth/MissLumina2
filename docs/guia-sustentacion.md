# Guia de sustentacion - Miss Lumina

Esta guia resume que se construyo, como se relaciona con los requisitos de
Programacion Orientada a Objetos, como funciona la base de datos y como se
explica el flujo transaccional de un pedido durante la defensa del proyecto.

---

## 1. Resumen del proyecto

Miss Lumina es una tienda virtual de maquillaje desarrollada con:

- Java 21 (el proyecto compila y se ejecuta con JDK 21 o superior).
- Spring Boot 4.1.1.
- Vaadin 25.3.2 con el tema Lumo por defecto.
- Spring Data JPA / Hibernate.
- PostgreSQL.
- Maven y Maven Wrapper.
- API REST opcional para consultas.

La aplicacion permite registrarse, iniciar sesion, consultar el catalogo con
imagenes y descripciones, agregar productos al carrito, registrar pedidos con
UUID unico, consultar el historial y administrar productos, inventario, pedidos,
categorias, marcas, tonos y reportes.

No se utiliza Swing ni SQLite.

---

## 2. Arquitectura por capas

El paquete base es `com.misslumina` y se organiza asi:

```text
com.misslumina
├── MissLuminaApplication        Unico metodo main
├── config                       Constantes y carga de datos iniciales
├── controller                   API REST opcional y DTO
├── exception                    Excepciones de negocio y manejador global
├── model                        Entidades y clases del dominio
├── repository                   Interfaces Spring Data JPA
├── service                      Reglas de negocio y transacciones
├── util                         Formatos, validaciones y hash
└── view                         Vistas Vaadin, layout y componentes
```

Flujo general:

```text
Vista Vaadin / Controlador REST
            |
            v
        Servicio
            |
            v
       Repositorio JPA
            |
            v
        PostgreSQL
```

La vista no ejecuta SQL directamente. Toda regla de negocio pasa por un
servicio y toda persistencia pasa por un repositorio.

---

## 3. Clases obligatorias y clases opcionales

### Clases obligatorias

| Clase | Responsabilidad |
|---|---|
| `Usuario` | Datos del cliente o administrador, rol, telefono y fecha de registro. |
| `Producto` | Datos del maquillaje, descripcion, imagen, precio, stock, marca, categoria, tono y calculos. |
| `Pedido` | Cabecera de la transaccion, UUID, fecha, estado, total y usuario. |
| `DetallePedido` | Linea del pedido: producto, cantidad, precio unitario y subtotal. |

### Clases opcionales implementadas

| Clase | Uso |
|---|---|
| `Carrito` | Carrito en memoria por sesion. |
| `ItemCarrito` | Producto y cantidad dentro del carrito. |
| `Categoria` | Tabla maestra de categorias. |
| `Marca` | Tabla maestra de marcas. |
| `Tono` | Tabla maestra de tonos. |

### Imagenes y descripcion de producto

Cada producto tiene una descripcion y una referencia interna de imagen en
`productos.imagen_url`. Las imagenes de demostracion son SVG locales ubicados
en `src/main/resources/META-INF/resources/images/productos/`. Si no hay imagen
definida, se usa `producto-default.svg`.

La descripcion se muestra en las tarjetas, en el catalogo y en el detalle del
producto. El administrador puede editar la descripcion desde `/admin/productos`
y subir una imagen con el selector de archivos o arrastrandola sobre el area de
carga. No se escriben rutas manualmente.

El archivo se valida en el servidor (PNG, JPG, WEBP o GIF, maximo 5 MB) y se
guarda en `uploads/productos/`. La aplicacion lo publica desde
`/uploads/productos/...`. La carpeta se crea automaticamente al iniciar y puede
cambiarse con la variable `UPLOADS_DIR`.

---

## 4. Evidencia de Programacion Orientada a Objetos

### 4.1 Atributos privados y encapsulamiento

Las entidades declaran sus atributos como `private` y exponen getters y
setters publicos. Ejemplo conceptual de `Producto`:

```java
private Long id;
private String nombre;
private double precio;
private int stock;

public double getPrecio() {
    return precio;
}

public void setPrecio(double precio) {
    this.precio = precio;
}
```

### 4.2 Constructores

Cada entidad tiene constructor por defecto, necesario para JPA, y constructor
con parametros para crear objetos de dominio. Ejemplos:

```java
public Producto() {
}

public Producto(String nombre, String descripcion, double precio,
                double precioAnterior, int stock, String marca,
                String categoria, String tono, String imagenUrl) {
    // asignacion y validacion
}
```

### 4.3 Metodos `toString()`

Las clases principales sobrescriben `toString()` para mostrar informacion
legible en logs y depuracion.

### 4.4 Procedimientos y funciones

Se usan ambos tipos de metodos:

- Procedimientos `void`: `descontarStock`, `reponerStock`, `registrarPedido`,
  `cambiarEstado`, `vaciar`, `agregarProducto`.
- Funciones con retorno: `calcularSubtotal`, `calcularTotal`,
  `calcularDescuentoUnitario`, `hayStock`, `esStockBajo`,
  `calcularUnidadesVendidas`, `generarIdTransaccion`.

### 4.5 Constantes `final`

Ejemplos:

```java
public static final double IVA = 0.19;
public static final int STOCK_MINIMO = 5;
public static final int MAX_UNIDADES_POR_PRODUCTO = 20;
```

### 4.6 Tipos de datos usados

- Primitivos: `int`, `double`, `boolean`, `long`.
- `String`: nombres, correos, descripciones, rutas de imagen, estados, marcas y tonos.
- `LocalDate`: fecha del pedido y fecha de registro.
- `LocalDateTime`: fecha y hora exacta del pedido.
- `UUID`: identificador unico de la transaccion.
- `List`: detalles del pedido y items del carrito.

### 4.7 Modificadores de acceso

- `private`: atributos de entidades.
- `public`: API de las clases, getters, setters y metodos de servicio.
- `protected`: metodos auxiliares de las vistas, por ejemplo titulos y
  validacion de sesion en `BaseView`.
- `static`: constantes y utilidades.
- `final`: constantes y clases de utilidades que no deben heredarse.

---

## 5. Modelo de datos

### 5.1 Tablas

| Tabla | Contenido |
|---|---|
| `usuarios` | Clientes y administradores. |
| `categorias` | Categorias del catalogo. |
| `marcas` | Marcas de maquillaje. |
| `tonos` | Tonos disponibles. |
| `productos` | Productos, precio, stock y referencias por nombre. |
| `pedidos` | Cabecera del pedido y UUID de transaccion. |
| `detalle_pedido` | Productos y cantidades de cada pedido. |

### 5.2 Relaciones

```text
usuarios 1 ---- N pedidos
pedidos  1 ---- N detalle_pedido
productos 1 ---- N detalle_pedido
categorias 1 ---- N productos
marcas 1 ---- N productos
tonos 1 ---- N productos
```

`productos.categoria`, `productos.marca` y `productos.tono` son llaves foraneas
hacia las tablas maestras por nombre. Esto permite administrar los catalogos
sin duplicar informacion y mantiene la integridad referencial en PostgreSQL.

### 5.3 Restricciones principales

- Llaves primarias en todas las tablas.
- Llaves foraneas con `ON UPDATE CASCADE` y `ON DELETE RESTRICT`.
- `pedidos.id_transaccion UUID NOT NULL UNIQUE`.
- `usuarios.correo UNIQUE`.
- `CHECK` para precios mayores que cero, stock no negativo y cantidades
  positivas.
- `NOT NULL` en campos obligatorios.
- Indices para busquedas por categoria, marca, tono, usuario y fecha.

### 5.4 Script SQL

El archivo `database/miss_lumina_db.sql` contiene:

- `CREATE DATABASE`.
- `CREATE TABLE`.
- `ALTER TABLE` para llaves foraneas, checks e indices.
- `INSERT` de datos de demostracion.
- Ejemplos de `SELECT`, `UPDATE` y `DELETE`.

El script fue probado con PostgreSQL 17.6. Si se ejecuta desde pgAdmin, se debe
crear primero la base `miss_lumina_db` y ejecutar desde la seccion de tablas,
omitiendo `CREATE DATABASE` y `\connect`, porque son instrucciones de `psql`.

---

## 6. Flujo transaccional del pedido

El metodo central es `PedidoService.registrarPedido(...)`, anotado con
`@Transactional`.

### Paso a paso

1. **Validar sesion:** el usuario debe existir y tener id.
2. **Validar carrito:** no puede estar vacio.
3. **Validar metodo de pago:** debe estar seleccionado.
4. **Releer usuario:** se obtiene el usuario administrado por JPA.
5. **Crear pedido:** se construye `Pedido` con usuario y metodo de pago.
6. **Generar UUID:** `UUID.randomUUID()` crea el identificador de transaccion.
7. **Verificar duplicado:** se consulta si el UUID ya existe.
8. **Bloquear productos:** cada producto se relee con
   `PESSIMISTIC_WRITE`, evitando vender la misma ultima unidad dos veces.
9. **Validar stock:** se compara cantidad solicitada contra stock real.
10. **Crear detalles:** se calcula precio unitario y subtotal.
11. **Calcular totales:** subtotal, IVA del 19% y total.
12. **Guardar:** se persisten pedido y detalles con JPA.
13. **Descontar stock:** se actualiza cada producto y se hace `flush`.
14. **Commit:** Spring confirma la transaccion si todo termina bien.
15. **Rollback:** cualquier excepcion revierte pedido, detalles y stock.

### Por que se usa una transaccion

Si el pedido se guarda pero falla el descuento de inventario, la base quedaria
inconsistente. `@Transactional` garantiza que ambas operaciones se confirmen o
se rechacen juntas.

### Cancelacion

Si un pedido se cancela, `PedidoService.cambiarEstado(...)` repone el stock de
cada detalle dentro de la misma transaccion. Las transiciones permitidas son:

```text
PENDIENTE  -> CONFIRMADO
PENDIENTE  -> CANCELADO
CONFIRMADO -> ENVIADO
CONFIRMADO -> CANCELADO
ENVIADO    -> ENTREGADO
```

---

## 7. Manejo de excepciones y logging

Se implementaron excepciones propias:

| Excepcion | Caso |
|---|---|
| `ProductoNoEncontradoException` | El producto no existe. |
| `StockInsuficienteException` | No hay unidades suficientes. |
| `DatosInvalidosException` | Datos incompletos o incorrectos. |
| `CredencialesInvalidasException` | Correo o contrasena incorrectos. |
| `PedidoDuplicadoException` | UUID repetido o error de integridad. |
| `ErrorBaseDatosException` | Error de conexion o acceso a datos. |
| `TransaccionException` | Error al confirmar una transaccion. |
| `MissLuminaException` | Excepcion base del dominio. |

`GlobalExceptionHandler` transforma las excepciones de la API REST en JSON con
codigos HTTP adecuados. Los servicios registran eventos importantes con SLF4J:
inicio de sesion, producto guardado, pedido registrado, cambio de estado y
errores de integridad.

---

## 8. Vistas Vaadin

| Ruta | Vista |
|---|---|
| `/` o `/inicio` | Portada |
| `/login` | Inicio de sesion |
| `/registro` | Registro de cliente |
| `/catalogo` | Catalogo con filtros |
| `/producto/{id}` | Detalle del producto |
| `/carrito` | Carrito |
| `/checkout` | Confirmacion del pedido |
| `/historial` | Historial del cliente |
| `/admin` | Panel administrativo |
| `/admin/productos` | CRUD de productos |
| `/admin/inventario` | Inventario |
| `/admin/pedidos` | Gestion de pedidos |
| `/admin/catalogos` | Categorias, marcas y tonos |
| `/admin/reportes` | Reportes |

Componentes Vaadin usados: `Grid`, `Button`, `TextField`, `NumberField`,
`TextArea`, `ComboBox`, `DatePicker`, `Checkbox`, `Dialog`, `FormLayout`,
`Notification`, `HorizontalLayout`, `VerticalLayout`, `FlexLayout`,
`RouterLink` y `AppLayout`.

La interfaz es responsive mediante CSS propio en
`src/main/resources/META-INF/resources/styles.css` con el tema Lumo por defecto.

---

## 9. Datos de prueba

### Usuarios

| Rol | Correo | Contrasena |
|---|---|---|
| Administrador | `fayuthr4@gmail.com` | `SingOfRegret@23` |
| Cliente | `fayuthr1@gmail.com` | `SingOfRegret@23` |

Las contrasenas se almacenan como hash SHA-256, no en texto plano.

### Datos cargados

- 2 usuarios.
- 7 categorias.
- 6 marcas.
- 14 tonos.
- 15 productos.
- 3 pedidos de demostracion.
- 4 detalles de pedido.

Los datos se cargan con el script SQL o, si la base esta vacia, con
`DataInitializer` al iniciar la aplicacion.

---

## 10. Como ejecutar y verificar

### Requisitos

- JDK 21 o superior.
- PostgreSQL 14 o superior.
- Puerto 5432 disponible, o variables de entorno configuradas.

### Base de datos

Opcion `psql`:

```powershell
psql -U postgres -f database/miss_lumina_db.sql
```

Opcion pgAdmin:

1. Crear la base `miss_lumina_db`.
2. Abrir Query Tool sobre esa base.
3. Ejecutar el script desde la seccion de tablas.

### Aplicacion

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080`.

### Variables de entorno

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/miss_lumina_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_clave"
```

---

## 11. Verificacion realizada

La version entregada fue verificada con:

- `.\mvnw.cmd -DskipTests compile`: compilacion correcta.
- `.\mvnw.cmd -DskipTests package`: empaquetado correcto, incluido el frontend
  de Vaadin en modo produccion.
- Script SQL ejecutado en PostgreSQL 17.6: 2 usuarios, 15 productos,
  3 pedidos y 4 detalles.
- Arranque de la aplicacion contra PostgreSQL y respuesta HTTP 200 en `/`.
- API REST probada: productos, productos destacados, pedidos, pedidos por
  usuario, reportes y error 404 controlado.
- Flujo de compra probado: login de cliente, agregar al carrito, checkout,
  UUID generado y stock descontado de 16 a 15.
- Panel administrativo probado: dashboard, productos, inventario, catalogos,
  pedidos y reportes.
- Cambio de estado probado: pedido `PENDIENTE` a `CONFIRMADO` persistido en
  PostgreSQL.
- Cancelacion probada: pedido `CONFIRMADO` a `CANCELADO` con reposicion de
  stock de 15 a 16.
- Base vacia probada: Hibernate creo las tablas y `DataInitializer` cargo los
  datos de demostracion sin ejecutar el script SQL.

---

## 12. Preguntas frecuentes de la defensa

### Por que usaron Vaadin y no Swing?

Porque el requisito pide una aplicacion web. Vaadin permite construir la
interfaz en Java, con componentes como `Grid`, `Dialog` y `FormLayout`, y
ejecutarla en el navegador. Swing es de escritorio y no cumple el enfoque web.

### Por que PostgreSQL y no SQLite?

PostgreSQL es un motor cliente-servidor, permite transacciones, bloqueos,
llaves foraneas, UUID y tipos `NUMERIC`, que son necesarios para el flujo de
pedidos. SQLite es un archivo local y no representa el escenario solicitado.

### Donde esta el UUID del pedido?

En `Pedido.idTransaccion`, mapeado a `pedidos.id_transaccion` como
`UUID NOT NULL UNIQUE`. Se genera con `UUID.randomUUID()` antes de guardar.

### Como evitan vender la misma ultima unidad dos veces?

Cada producto se relee con bloqueo pesimista:

```java
productoRepository.buscarPorIdParaActualizar(id)
```

Eso ejecuta un `SELECT ... FOR UPDATE` y serializa el acceso al stock.

### Que pasa si falla el descuento de stock?

Toda la operacion esta dentro de `@Transactional`. Si falla el guardado, el
descuento o una validacion, Spring hace rollback del pedido, los detalles y el
inventario.

### Como se calcula el total?

Cada detalle calcula `precioUnitario * cantidad`. Luego se suman los subtotales,
se aplica IVA del 19% y se redondea a dos decimales para coincidir con
`NUMERIC(12,2)`.

### Por que hay tablas de categorias, marcas y tonos si el producto guarda el nombre?

Porque el producto conserva los nombres como atributos simples, tal como pide
el modelo academico, y PostgreSQL valida que esos nombres existan en las tablas
maestras mediante llaves foraneas. Asi se evita escribir categorias o marcas
invalidas.

### Que pasa si se elimina una categoria con productos?

La llave foranea usa `ON DELETE RESTRICT`. PostgreSQL rechaza la operacion y el
servicio muestra un mensaje de error. Para retirar una categoria se debe
reasignar o eliminar primero sus productos.

### Como se manejan las contrasenas?

Se convierten a SHA-256 con `PasswordUtil` antes de guardarlas. En la base no
se almacena la contrasena original.

### Que pasa si el correo ya esta registrado?

`UsuarioService` valida el correo antes de guardar y la tabla tiene una
restriccion `UNIQUE`. Se lanza `DatosInvalidosException` con un mensaje claro.

### Que pasa si un producto no tiene stock?

El carrito y el checkout validan stock. Si no hay unidades, se lanza
`StockInsuficienteException` y no se crea el pedido.

### Como se administra el inventario?

Desde `/admin/inventario` se puede ajustar el stock. El sistema marca stock bajo
cuando las unidades son menores o iguales a `Producto.STOCK_MINIMO` (5).

### Que reportes existen?

Productos mas vendidos, ventas por categoria, pedidos por estado y ventas por
dia. Se pueden filtrar por rango de fechas y tambien se exponen por API REST.

### Que se puede mejorar?

- Pasarela de pago real.
- Envio de correos de confirmacion.
- Redimensionado automatico de las imagenes subidas.
- Recuperacion de contrasena.
- Pruebas automatizadas con JUnit y Testcontainers.
- Roles y permisos mas granulares.
- Historial de auditoria de cambios de estado.

---

## 13. Relacion con los requisitos de la asignatura

| Requisito | Donde se cumple |
|---|---|
| Java 17+ | `pom.xml`, Java release 21. |
| POO | `model`, servicios y utilidades. |
| Spring Boot | `MissLuminaApplication`, dependencias y servicios. |
| Vaadin | `view`, `MainLayout`, componentes. |
| PostgreSQL | `application.properties`, script SQL, entidades JPA. |
| Maven | `pom.xml`, `mvnw`, `mvnw.cmd`. |
| JPA/JDBC | Repositorios Spring Data JPA y driver PostgreSQL. |
| Sin Swing ni SQLite | No hay dependencias ni codigo de esos tecnologias. |
| UUID unico | `Pedido.idTransaccion` y `pedidos.id_transaccion`. |
| Calculos | `Producto`, `Pedido`, `DetallePedido`, `Carrito`. |
| Excepciones | Paquete `exception` y `GlobalExceptionHandler`. |
| Logging | SLF4J en servicios y `logs/miss-lumina.log`. |
| Interfaz responsive | Tema Lumo + `styles.css`. |
| Panel admin | Vistas `/admin/**`. |
| Reportes | `ReporteService` y `ReportesView`. |

---

## 14. Cierre sugerido para la exposicion

> Miss Lumina es una aplicacion web desarrollada en Java con Spring Boot,
> Vaadin y PostgreSQL. El sistema aplica programacion orientada a objetos en
> sus entidades y servicios, usa una arquitectura por capas y garantiza la
> integridad de cada compra mediante una transaccion que valida stock, genera
> un UUID unico, guarda el pedido y descuenta el inventario. La aplicacion
> incluye tienda para clientes, panel administrativo, reportes y una API REST
> de consulta, y fue compilada, empaquetada y probada contra PostgreSQL.
