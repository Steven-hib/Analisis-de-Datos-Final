# Sistema de Gestión Tecnológica - UCC Cali

Aplicación académica de escritorio para controlar el acceso a salas y el préstamo de computadores portátiles. Está escrita en Java con interfaz Swing, orientación a objetos y persistencia SQLite.

## Requisitos

- JDK 8 o superior.
- Maven 3.6.3 o superior, o un IDE que pueda importar proyectos Maven.
- Conexión a Internet la primera vez que Maven descargue las dependencias.

## Compilar y ejecutar

Desde esta carpeta, ejecuta:

```text
mvn clean package
java -jar target/sistema-gestion-tecnologica-1.0.0.jar
```

En Eclipse o IntelliJ, importa la carpeta como un proyecto Maven, espera la descarga de dependencias y ejecuta `vistas.Main`.

La primera ejecución crea `datos/gestion_tecnologica.db` y agrega salas, portátiles y ubicaciones iniciales de ejemplo. Los accesos también se reflejan en `datos/accesos_salas.csv`. Conserva una copia de seguridad de la carpeta `datos` para respaldar la información.

## Módulos

- **Salas:** consulta usuarios por identificación, crea invitados cuando no existen, registra sala y hora de ingreso, y permite registrar la salida. Los datos se almacenan en SQLite y se anexan al CSV de accesos.
- **Portátiles:** registra solicitudes con ubicación y celular, muestra las solicitudes bloqueadas, asigna equipos y registra su devolución.
- **Usuarios y bloqueos:** crea o actualiza usuarios, importa archivos, habilita el período académico activo y bloquea o desbloquea usuarios.
- **Inventario:** administra salas, portátiles y ubicaciones.
- **Informes:** filtra por fechas, programa e identificación; muestra detalle o conteos por fecha/programa y por documento; exporta a `.xlsx`.

## Importación de usuarios

Desde **Usuarios y bloqueos**, importa `.txt`, `.csv`, `.tsv`, `.xlsx` o `.xls`. La primera hoja de Excel se usa. El archivo debe tener diez columnas en este orden:

```text
Identificacion,Nombres,Apellidos,Tipo,Programa,Semestre,Jornada,Facultad,Bloqueado,Activo
```

`Semestre` puede quedar vacío. `Bloqueado` y `Activo` aceptan `true/false`, `1/0`, `sí/no` o `x`. El archivo [usuarios_ejemplo.csv](datos/usuarios_ejemplo.csv) contiene identificaciones ficticias para familiarizarse con la importación; reemplázalo por los datos autorizados antes de entregar.

## Estructura

```text
src/
  controladores/   flujo de ingreso a salas
  modelos/         usuario, sala, portátil, ubicación y préstamo
  servicios/       reglas de negocio y consultas
  utilidades/      SQLite, importación, CSV y Excel
  vistas/          ventana Swing y punto de entrada
datos/             base local, CSV de accesos y ejemplo de importación
documentacion/     requisitos, manuales y diagramas
```

La especificación y los manuales están en `documentacion/Especificacion-y-Manuales.md`. Los archivos Mermaid de diagramas están en `documentacion/diagramas/`.
