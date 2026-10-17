# ♟️ Chess Java

Juego de ajedrez desarrollado en Java con interfaz gráfica Swing y persistencia en MySQL.

## Requisitos previos

Asegúrate de tener instalado lo siguiente antes de continuar:

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java JDK | 17+ | `java -version` |
| Docker + Docker Compose | Cualquier versión reciente | `docker -v` |

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/gafc2001/chess-java.git    
cd chess-java
```

---

## 2. Levantar la base de datos con Docker

El proyecto incluye un `docker-compose.yml` que levanta una instancia de **MySQL 8.0** automáticamente.

```bash
docker-compose up -d
```

Esto crea:
- Un contenedor llamado `chess_db`
- La base de datos `chess_game` en el puerto **3308** del host
- Las credenciales configuradas por defecto:

| Campo    | Valor  |
|----------|--------|
| Host     | `localhost` |
| Puerto   | `3308` |
| Base de datos | `chess_game` |
| Usuario root | `root` |
| Contraseña root | *(ver `DB.java`)* |

> **Nota:** El puerto expuesto es el **3308** (no el 3306 estándar) para evitar conflictos con instalaciones locales de MySQL.

---

## 3. Inicializar la base de datos

Ejecuta el script de migración para crear las tablas e insertar el usuario administrador inicial:

```bash
docker exec -i chess_db mysql -uroot -p<PASSWORD> chess_game < resources/migraciones/bd.sql
```

O bien conéctate directamente al contenedor:

```bash
docker exec -it chess_db mysql -uroot -p
```

Y ejecuta manualmente el contenido de `resources/migraciones/bd.sql`.

### Usuario administrador por defecto

| Campo | Valor |
|---|---|
| Nombre de usuario | `gfarfan` |
| Contraseña | `123456` |

---

## 4. Compilar el proyecto

Desde la raíz del proyecto, ejecuta el siguiente comando para compilar **todos** los archivos fuente de forma recursiva:

**Windows (PowerShell):**
```powershell
javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out $(Get-ChildItem -Path src -Recurse -Filter *.java | % FullName)
```

**Linux / macOS (Bash):**
```bash
find src -name "*.java" > sources.txt
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d out @sources.txt
```

Los archivos `.class` compilados se guardan en la carpeta `out/`.

---

## 5. Ejecutar la aplicación

**Windows (PowerShell):**
```powershell
java -cp "out;lib\mysql-connector-j-8.3.0.jar" Main
```

**Linux / macOS (Bash):**
```bash
java -cp "out:lib/mysql-connector-j-8.3.0.jar" Main
```

Se abrirá la ventana de inicio de sesión del juego.

---

## Estructura del proyecto

```
chess-java/
├── src/
│   ├── Main.java                          # Punto de entrada
│   ├── aplicacion/
│   │   ├── Sesion.java                    # Sesión del usuario (Singleton)
│   │   └── casosuso/
│   │       ├── GestionarJuego.java        # Lógica del juego
│   │       ├── IniciarSesion.java         # Caso de uso: login
│   │       └── RegistrarUsuario.java      # Caso de uso: registro
│   ├── dominio/
│   │   ├── enums/                         # Color, TipoPieza, EstadoJuego, EstadoMovimiento, etc.
│   │   ├── estructuras/                   # TADLista, ListaDoblementeEnlazada
│   │   ├── modelos/                       # Tablero, Partida, Usuario, Piezas, Movimiento, HistorialMovimientos
│   │   └── puertos/                       # Interfaces / contratos
│   ├── infraestructura/
│   │   ├── mapper/                        # UsuarioMapper (ResultSet → Modelo)
│   │   └── persistencia/                  # DB (Singleton), RepositorioUsuarioMySQL
│   └── presentacion/
│       ├── controladores/                 # ControladorLogin
│       └── vistas/                        # Vistas Swing (Login, Juego, Ranking, etc.)
├── lib/
│   └── mysql-connector-j-8.3.0.jar       # Driver JDBC de MySQL
├── resources/
│   ├── assets/                            # Imágenes y recursos gráficos
│   └── migraciones/
│       └── bd.sql                         # Script de creación de tablas
├── docker-compose.yml                     # Configuración del contenedor MySQL
└── out/                                   # Clases compiladas (generado al compilar)
```

---

## Historial de movimientos (TDA Lista doblemente enlazada)

El historial de cada partida se guarda en un **Tipo de Dato Abstracto (TDA)** implementado a mano como **lista doblemente enlazada**, sin usar `ArrayList` ni `LinkedList` de Java.

> 📄 Documentación técnica completa (especificación, invariantes, lógica de punteros, complejidad y pruebas): [docs/TDA_LISTA_DOBLEMENTE_ENLAZADA.md](docs/TDA_LISTA_DOBLEMENTE_ENLAZADA.md)

### Clases

| Clase | Ubicación | Responsabilidad |
|---|---|---|
| `TADLista<T>` | `src/dominio/estructuras/` | Especificación del TDA: crear, consultar, editar, eliminar y recorrer |
| `ListaDoblementeEnlazada<T>` | `src/dominio/estructuras/` | Implementación: `cabeza`, `cola`, `tamanio` y un `Nodo` **privado** con punteros `anterior`/`siguiente` |
| `Movimiento` | `src/dominio/modelos/` | El dato: una jugada inmutable (origen, destino, pieza movida, pieza capturada, estado) |
| `HistorialMovimientos` | `src/dominio/modelos/` | TDA del dominio sobre `TADLista<Movimiento>` |

```
            cabeza                                        cola
              │                                             │
              ▼                                             ▼
null ◄── ┌─────────┐ ◄── ┌─────────┐ ◄── ┌─────────┐
         │ e2-e4   │     │ d7-d5   │     │ e4xd5   │
         │ Blancas │     │ Negras  │     │ Blancas │
         └─────────┘ ──► └─────────┘ ──► └─────────┘ ──► null
```

### Cada acción del juego es una acción de la lista

| Acción | Método | Operación del TDA | Costo |
|---|---|---|---|
| Jugar | `historial.registrar(m)` | crear: `insertarAlFinal` | O(1) |
| Anotar jaque (`+`) o mate (`#`) | `historial.marcarUltimo(estado)` | editar: `modificarUltimo` | O(1) |
| Regresar movimiento | `historial.deshacerUltimo()` | eliminar: `eliminarUltimo` | O(1) |
| Mostrar la tabla / sincronizar online | `for (Movimiento m : historial)` | recorrer: iterador | O(n) |

La tabla muestra la notación completa (`e2-e4`, `e4xd5`, `d1-h5+`, `h5xf7#`). En MySQL se sigue guardando el formato `e2-e4,d7-d5,...` para las partidas online.

### Regresar movimiento

Cada jugador puede **regresar su movimiento hasta 2 veces por partida** (`Partida.MAX_RETROCESOS`).

- Después de mover, aparece el botón **"Regresar movimiento (n)"** en el panel de ese jugador, donde `n` son los retrocesos que le quedan.
- Solo se puede regresar el último movimiento propio, antes de que el rival responda (la última jugada del historial debe ser de su color).
- Al presionarlo:
  1. `HistorialMovimientos.deshacerUltimo()` quita la `cola` de la lista en O(1).
  2. `Tablero.deshacerMovimiento(movimiento)` regresa la pieza a su `origen` y, si hubo captura, repone la `piezaCapturada` en el `destino`.
  3. El turno vuelve al jugador y se descuenta uno de sus retrocesos.
- En partidas **online**, la lista de movimientos se actualiza en la base de datos; el rival detecta que es más corta que la suya y deshace el movimiento en su tablero.

### Pruebas

```powershell
javac -encoding UTF-8 -cp out -d out-test test\dominio\estructuras\PruebasListaDoblementeEnlazada.java
java -cp "out;out-test" dominio.estructuras.PruebasListaDoblementeEnlazada
```
