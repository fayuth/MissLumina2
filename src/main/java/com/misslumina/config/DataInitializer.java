package com.misslumina.config;

import com.misslumina.model.Categoria;
import com.misslumina.model.Marca;
import com.misslumina.model.Pedido;
import com.misslumina.model.Producto;
import com.misslumina.model.Tono;
import com.misslumina.model.Usuario;
import com.misslumina.repository.CategoriaRepository;
import com.misslumina.repository.MarcaRepository;
import com.misslumina.repository.PedidoRepository;
import com.misslumina.repository.ProductoRepository;
import com.misslumina.repository.TonoRepository;
import com.misslumina.repository.UsuarioRepository;
import com.misslumina.service.ProductoService;
import com.misslumina.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String CORREO_ADMIN = "admin@misslumina.com";
    private static final String CLAVE_ADMIN = "Admin123*";
    private static final String CORREO_CLIENTE = "cliente@misslumina.com";
    private static final String CLAVE_CLIENTE = "Cliente123*";

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final TonoRepository tonoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           MarcaRepository marcaRepository,
                           TonoRepository tonoRepository,
                           ProductoRepository productoRepository,
                           PedidoRepository pedidoRepository,
                           ProductoService productoService) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.tonoRepository = tonoRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
    }

    @Override
    public void run(String... args) {
        try {
            sembrarUsuarios();
            sembrarCatalogos();
            sembrarProductos();
            sembrarPedidosDemo();
            log.info("Datos de demostracion verificados. Admin: {} / {} | Cliente: {} / {}",
                    CORREO_ADMIN, CLAVE_ADMIN, CORREO_CLIENTE, CLAVE_CLIENTE);
        } catch (Exception e) {
            log.error("No se pudieron cargar los datos iniciales. "
                    + "Verifique la conexion con PostgreSQL.", e);
        }
    }

    private void sembrarUsuarios() {
        if (usuarioRepository.findByCorreoIgnoreCase(CORREO_ADMIN).isEmpty()) {
            Usuario admin = new Usuario("Administradora Miss Lumina", CORREO_ADMIN,
                    PasswordUtil.hashear(CLAVE_ADMIN), "3001234567");
            admin.setRol(Usuario.ROL_ADMINISTRADOR);
            admin.setFechaRegistro(LocalDate.now().minusMonths(6));
            usuarioRepository.save(admin);
            log.info("Usuario administrador creado: {}", CORREO_ADMIN);
        }
        if (usuarioRepository.findByCorreoIgnoreCase(CORREO_CLIENTE).isEmpty()) {
            Usuario cliente = new Usuario("Valentina Gomez", CORREO_CLIENTE,
                    PasswordUtil.hashear(CLAVE_CLIENTE), "3109876543");
            cliente.setRol(Usuario.ROL_CLIENTE);
            cliente.setFechaRegistro(LocalDate.now().minusMonths(2));
            usuarioRepository.save(cliente);
            log.info("Usuario cliente creado: {}", CORREO_CLIENTE);
        }
    }

    private void sembrarCatalogos() {
        crearCategoria("Labiales", "Labiales mate, cremosos y liquidos.");
        crearCategoria("Bases", "Bases de maquillaje para todo tipo de piel.");
        crearCategoria("Sombras", "Sombras individuales y paletas.");
        crearCategoria("Rubores", "Rubores en polvo y cremosos.");
        crearCategoria("Delineadores", "Delineadores de ojos y labios.");
        crearCategoria("Mascaras de pestanas", "Mascaras para volumen y longitud.");
        crearCategoria("Polvos", "Polvos compactos y traslucidos.");

        crearMarca("Maybelline", "Estados Unidos");
        crearMarca("L'Oreal", "Francia");
        crearMarca("NARS", "Estados Unidos");
        crearMarca("MAC", "Canada");
        crearMarca("Vogue", "Colombia");
        crearMarca("Yanbal", "Peru");

        crearTono("Rojo Pasion", "#B3122E");
        crearTono("220 Natural", "#D8A47F");
        crearTono("Nude", "#C89F8A");
        crearTono("Orgasm", "#E58C8C");
        crearTono("Negro Intenso", "#111111");
        crearTono("Negro", "#1B1B1B");
        crearTono("02 Coy", "#B76E79");
        crearTono("W3 Golden Beige", "#D2A679");
        crearTono("Cafe", "#6B4A37");
        crearTono("Coral", "#E2725B");
        crearTono("Marron", "#5A3A2E");
        crearTono("Velvet Teddy", "#A45A52");
        crearTono("Natural", "#E3C3A3");
        crearTono("Rosa Romantico", "#D98A9A");
    }

    private void sembrarProductos() {
        if (productoRepository.count() > 0) {
            return;
        }
        productoService.guardar(new Producto(
                "Labial Mate Luminoso", "Labial de larga duracion con acabado mate y confort.",
                32900, 39900, 25, "Maybelline", "Labiales", "Rojo Pasion"));
        productoService.guardar(new Producto(
                "Base Fit Me", "Base liquida de cobertura media, ideal para piel mixta.",
                54900, 62900, 18, "Maybelline", "Bases", "220 Natural"));
        productoService.guardar(new Producto(
                "Paleta de Sombras Nude", "Doce tonos nude con acabados mate y satinado.",
                129900, 149900, 10, "NARS", "Sombras", "Nude"));
        productoService.guardar(new Producto(
                "Rubor en Polvo Orgasm", "Rubor iconico con destellos dorados.",
                119900, 0, 7, "NARS", "Rubores", "Orgasm"));
        productoService.guardar(new Producto(
                "Delineador Tattoo", "Delineador liquido de punta fina y alta duracion.",
                27900, 0, 30, "Maybelline", "Delineadores", "Negro Intenso"));
        productoService.guardar(new Producto(
                "Mascara Lash Sensational", "Mascara que separa y da volumen a las pestanas.",
                45900, 52900, 22, "Maybelline", "Mascaras de pestanas", "Negro"));
        productoService.guardar(new Producto(
                "Labial Vinyl Ink", "Labial liquido con acabado vinilo y 16 horas de color.",
                41900, 0, 15, "L'Oreal", "Labiales", "02 Coy"));
        productoService.guardar(new Producto(
                "Base True Match", "Base con tecnologia de match perfecto para cada tono.",
                59900, 0, 12, "L'Oreal", "Bases", "W3 Golden Beige"));
        productoService.guardar(new Producto(
                "Sombra Individual Nude", "Sombra de alta pigmentacion y facil difuminado.",
                18900, 0, 40, "Vogue", "Sombras", "Cafe"));
        productoService.guardar(new Producto(
                "Rubor Cremoso Coral", "Rubor cremoso que se funde con la piel.",
                34900, 39900, 3, "Yanbal", "Rubores", "Coral"));
        productoService.guardar(new Producto(
                "Delineador Retractil", "Delineador de ojos retractil con difuminador.",
                89900, 0, 6, "MAC", "Delineadores", "Marron"));
        productoService.guardar(new Producto(
                "Labial Cremoso Velvet Teddy", "Labial cremoso de acabado aterciopelado.",
                109900, 0, 4, "MAC", "Labiales", "Velvet Teddy"));
        productoService.guardar(new Producto(
                "Mascara Volume Express", "Mascara para volumen extremo con cepillo curvo.",
                38900, 0, 0, "L'Oreal", "Mascaras de pestanas", "Negro"));
        productoService.guardar(new Producto(
                "Polvo Compacto Natural", "Polvo compacto que controla el brillo durante el dia.",
                29900, 0, 20, "Vogue", "Polvos", "Natural"));
        productoService.guardar(new Producto(
                "Labial Liquido Rosa", "Labial liquido de acabado mate y tono rosa romantico.",
                37900, 42900, 16, "Yanbal", "Labiales", "Rosa Romantico"));
    }

    private void sembrarPedidosDemo() {
        if (pedidoRepository.count() > 0) {
            return;
        }
        Usuario cliente = usuarioRepository.findByCorreoIgnoreCase(CORREO_CLIENTE).orElse(null);
        List<Producto> productos = productoRepository.findAll();
        if (cliente == null || productos.size() < 4) {
            return;
        }

        Producto labial = buscarProducto(productos, "Labial Mate Luminoso");
        Producto base = buscarProducto(productos, "Base Fit Me");
        Producto paleta = buscarProducto(productos, "Paleta de Sombras Nude");
        Producto delineador = buscarProducto(productos, "Delineador Tattoo");

        Pedido pedido1 = new Pedido(cliente, "Tarjeta de credito");
        pedido1.setIdTransaccion(UUID.randomUUID());
        pedido1.setEstado(Pedido.ESTADO_ENTREGADO);
        pedido1.setFechaPedido(LocalDate.now().minusDays(10));
        pedido1.setFechaHoraPedido(LocalDateTime.now().minusDays(10).withHour(14).withMinute(30));
        pedido1.agregarDetalle(labial, 2);
        pedido1.agregarDetalle(base, 1);
        pedido1.recalcularTotal();
        pedidoRepository.save(pedido1);

        Pedido pedido2 = new Pedido(cliente, "PSE");
        pedido2.setIdTransaccion(UUID.randomUUID());
        pedido2.setEstado(Pedido.ESTADO_CONFIRMADO);
        pedido2.setFechaPedido(LocalDate.now().minusDays(3));
        pedido2.setFechaHoraPedido(LocalDateTime.now().minusDays(3).withHour(10).withMinute(15));
        pedido2.agregarDetalle(paleta, 1);
        pedido2.recalcularTotal();
        pedidoRepository.save(pedido2);

        Pedido pedido3 = new Pedido(cliente, "Nequi");
        pedido3.setIdTransaccion(UUID.randomUUID());
        pedido3.setEstado(Pedido.ESTADO_PENDIENTE);
        pedido3.setFechaPedido(LocalDate.now().minusDays(1));
        pedido3.setFechaHoraPedido(LocalDateTime.now().minusDays(1).withHour(18).withMinute(5));
        pedido3.agregarDetalle(delineador, 3);
        pedido3.recalcularTotal();
        pedidoRepository.save(pedido3);

        log.info("Pedidos de demostracion creados: 3");
    }

    private Producto buscarProducto(List<Producto> productos, String nombre) {
        return productos.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(productos.get(0));
    }

    private void crearCategoria(String nombre, String descripcion) {
        if (categoriaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            categoriaRepository.save(new Categoria(nombre, descripcion));
        }
    }

    private void crearMarca(String nombre, String pais) {
        if (marcaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            marcaRepository.save(new Marca(nombre, pais));
        }
    }

    private void crearTono(String nombre, String hex) {
        if (tonoRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            tonoRepository.save(new Tono(nombre, hex));
        }
    }
}
