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
│   │   ├── enums/                         # Color, TipoPieza, EstadoJuego, etc.
│   │   ├── modelos/                       # Tablero, Partida, Usuario, Piezas, HistorialMovimientos
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

## Historial de movimientos (Lista doblemente enlazada)

El historial de la partida se guarda en una **lista doblemente enlazada** implementada a mano, sin usar `ArrayList` ni `LinkedList` de Java.

### Clases

| Clase | Ubicación | Responsabilidad |
|---|---|---|
| `NodoMovimiento` | `src/dominio/modelos/` | Representa **un solo movimiento** (de blancas o de negras) |
| `HistorialMovimientos` | `src/dominio/modelos/` | La lista: mantiene la `cabeza`, la `cola` y el `tamanio` |

### El nodo

Cada `NodoMovimiento` guarda los datos del movimiento y dos referencias:

| Atributo | Descripción |
|---|---|
| `origen` / `destino` | Casillas de inicio y fin (ej. `e2` → `e4`) |
| `piezaMovida` | La pieza que se movió |
| `piezaCapturada` | La pieza que se comió (o `null` si no hubo captura) |
| `color` | Color del jugador que hizo el movimiento |
| `anterior` | Referencia al nodo del movimiento previo |
| `siguiente` | Referencia al nodo del movimiento posterior |

```
            cabeza                                        cola
              │                                             │
              ▼                                             ▼
null ◄── ┌─────────┐ ◄── ┌─────────┐ ◄── ┌─────────┐
         │ e2-e4   │     │ d7-d5   │     │ e4-d5   │
         │ Blancas │     │ Negras  │     │ Blancas │
         │ Peón    │     │ Peón    │     │ Peón    │
         └─────────┘ ──► └─────────┘ ──► └─────────┘ ──► null
```

- La **cabeza** es el primer movimiento de la partida; su `anterior` es `null`.
- La **cola** es el último movimiento; su `siguiente` es `null`.

### Operaciones

**`agregar(nodo)`**: se llama cada vez que un jugador hace un movimiento válido (`Partida.realizarMovimiento`).

1. Si la lista está vacía, el nodo pasa a ser la `cabeza`.
2. Si no, se enlaza al final: `cola.siguiente = nodo` y `nodo.anterior = cola`.
3. El nodo pasa a ser la nueva `cola` y aumenta el `tamanio`.

**`eliminarUltimo()`**: se usa al **regresar un movimiento**.

1. Se toma la `cola` actual.
2. La nueva `cola` es `cola.anterior`. Gracias al enlace hacia atrás, esto se hace en un paso, sin recorrer la lista.
3. Si la nueva cola es `null`, la lista queda vacía (`cabeza = null`). Si no, se corta su enlace: `cola.siguiente = null`.
4. Disminuye el `tamanio` y se devuelve el nodo eliminado.

**Recorrido**: para mostrar el historial en pantalla, se empieza en la `cabeza` y se avanza por `siguiente` hasta llegar a `null`. Cada nodo es una fila de la tabla:

| Nº | Jugador | Pieza | Movimiento |
|---|---|---|---|
| 1 | Blancas | Peón | e2-e4 |
| 2 | Negras | Peón | d7-d5 |
| 3 | Blancas | Peón | e4-d5 |

### Regresar movimiento

Cada jugador puede **regresar su movimiento hasta 2 veces por partida** (`Partida.MAX_RETROCESOS`).

- Después de mover, aparece el botón **"Regresar movimiento (n)"** en el panel de ese jugador, donde `n` son los retrocesos que le quedan.
- Solo se puede regresar el último movimiento propio, es decir, antes de que el rival responda (la `cola` debe ser de su color).
- Al presionarlo:
  1. `HistorialMovimientos.eliminarUltimo()` quita la `cola`.
  2. `Tablero.deshacerMovimiento(nodo)` regresa la pieza a su `origen` y, si hubo captura, repone la `piezaCapturada` en el `destino`.
  3. El turno vuelve al jugador y se descuenta uno de sus retrocesos.
- En partidas **online**, la lista de movimientos se actualiza en la base de datos y el rival detecta que es más corta que la suya, así que deshace el movimiento en su tablero.
