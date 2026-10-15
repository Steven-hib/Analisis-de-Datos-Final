# Sistema de Gestión Tecnológica de Salas y Portátiles

**Universidad Cooperativa de Colombia, sede Cali**  
**Documento de requisitos, análisis y manuales**  
Versión 1.0 — 15 de octubre de 2026

> Este borrador organiza los requisitos y la operación de la aplicación implementada. Para la entrega física, aplicar portada, márgenes, numeración, tablas y demás presentación solicitada por la versión 6 de la norma ICONTEC indicada en la consigna, y completar los datos académicos que correspondan.

## 1. Introducción

### 1.1 Propósito

Especificar el funcionamiento del Sistema de Gestión Tecnológica para registrar el acceso a salas, administrar las solicitudes y devoluciones de portátiles, conservar la información en una base de datos y generar informes consultables y exportables.

### 1.2 Alcance

La aplicación de escritorio permite registrar usuarios, importar sus datos desde archivos, habilitarlos para el período académico activo, registrar ingresos y salidas de salas, gestionar préstamos de portátiles, señalar usuarios bloqueados y consultar reportes. El sistema está pensado para el uso local del personal y los usuarios de la sede Cali.

### 1.3 Definiciones, acrónimos y abreviaturas

| Término | Definición |
|---|---|
| Usuario | Estudiante, docente, funcionario, invitado o auxiliar de TI identificado por documento. |
| Invitado | Persona que no aparece en el archivo institucional y que se registra para el acceso a una sala. |
| Acceso | Evento que registra el ingreso de una persona a una sala, con hora de entrada y, cuando se registra, hora de salida. |
| Solicitud | Petición de préstamo de portátil pendiente de gestión por el auxiliar. |
| Período activo | Período académico para el cual se habilita la cuenta de usuario. |
| XLSX | Formato de libro de Microsoft Excel. |
| SQLite | Motor relacional local utilizado para almacenar la información del sistema. |

### 1.4 Referencias

- Consigna académica **Proyecto Final — Análisis de Sistemas**, compartida por el estudiante.
- Apache POI, formato Office Open XML para archivos `.xlsx`.
- Xerial SQLite JDBC, controlador JDBC para la base local SQLite.
- Norma ICONTEC de presentación de trabajos escritos, versión mínima 6 de 2008, según la consigna.

### 1.5 Resumen

El documento presenta la descripción general, los requisitos funcionales y de calidad, los actores, los casos de uso, la arquitectura, los riesgos y los manuales de usuario y programador. El código fuente está en `src/`; el esquema se crea automáticamente al iniciar la aplicación.

## 2. Descripción general

### 2.1 Perspectiva del producto

Es una aplicación local de escritorio desarrollada en Java Swing. SQLite almacena usuarios, salas, equipos, ubicaciones, períodos, accesos y préstamos. Los registros de acceso también se anexan a `datos/accesos_salas.csv` como archivo plano. La aplicación no requiere conexión a un servidor institucional para operar después de instalar sus dependencias.

### 2.2 Funcionalidad del producto

1. Importar usuarios desde TXT, CSV, TSV y Excel.
2. Crear o actualizar usuarios desde la interfaz.
3. Buscar un usuario por documento y mostrar sus datos.
4. Registrar invitados para el módulo de salas.
5. Registrar accesos y salidas de sala con fecha y hora.
6. Recibir solicitudes de préstamo de portátiles con ubicación y celular.
7. Alertar al auxiliar cuando la persona solicitante está bloqueada.
8. Asignar un portátil disponible y registrar el auxiliar y la fecha de entrega.
9. Registrar la devolución y volver a marcar el equipo como disponible.
10. Consultar, filtrar y exportar informes de accesos y préstamos.
11. Administrar salas, portátiles, ubicaciones y bloqueos.

### 2.3 Características de los usuarios

- **Estudiante:** consulta sus datos, registra un acceso y solicita un portátil.
- **Docente:** registra acceso indicando curso y facultad; puede solicitar portátiles.
- **Funcionario:** puede solicitar un portátil y utilizar las salas según las reglas institucionales.
- **Invitado:** se registra con identificación, nombre y programa o motivo para ingresar a una sala.
- **Auxiliar de TI:** revisa solicitudes, ve alertas de bloqueo, asigna equipos y registra devoluciones.
- **Administrador:** mantiene usuarios, inventario y ubicaciones; importa datos y consulta informes.

La aplicación no implementa autenticación por contraseña. Los perfiles anteriores representan responsabilidades operativas, no cuentas con permisos separados.

### 2.4 Restricciones

- Java 8 o superior; Maven 3.6.3 o superior para compilar el proyecto.
- Interfaz de escritorio Swing.
- Base SQLite local en la carpeta `datos`.
- Los archivos importados deben seguir el orden de columnas documentado en el README.
- La primera ejecución necesita conexión a Internet para resolver dependencias Maven si no están descargadas.
- La carpeta `datos` contiene información operacional y debe respaldarse.

### 2.5 Suposiciones y dependencias

- Los registros importados son proporcionados y autorizados por la institución.
- El auxiliar usa la misma aplicación y escribe su nombre al asignar un portátil.
- La disponibilidad del equipo depende de sus préstamos activos registrados.
- Un usuario bloqueado puede generar una solicitud marcada como alerta, pero no se le puede asignar un equipo mientras siga bloqueado.
- La aplicación utiliza Apache POI para leer libros Excel y generar archivos `.xlsx`, y Xerial SQLite JDBC para la base local.

### 2.6 Evolución previsible del sistema

Como futuras mejoras se contemplan autenticación por roles, instalación institucional centralizada, sincronización con sistemas académicos, auditoría de cambios, administración de períodos desde la interfaz y creación de copias de seguridad desde la aplicación.

## 3. Requisitos específicos

### 3.1 Requisitos comunes de las interfaces

La aplicación presenta módulos separados mediante pestañas, mensajes de confirmación y errores, tablas de consulta y formularios etiquetados. Los campos obligatorios se validan antes de guardar.

#### 3.1.1 Interfaces de usuario

- **Salas:** documento, datos de invitado cuando aplique, sala obligatoria, equipo opcional, curso y facultad para docentes; tabla de ingresos abiertos y acción de salida.
- **Portátiles:** documento, celular, ubicación, lista de solicitudes y controles de asignación y devolución.
- **Usuarios y bloqueos:** formulario de usuario, importación y tabla de estado de bloqueo y habilitación.
- **Inventario:** formularios y tablas de salas, portátiles y ubicaciones.
- **Informes:** fechas inicial/final, programa, documento, tipo de reporte y exportación XLSX.

### 3.2 Requisitos funcionales

| Código | Requisito |
|---|---|
| RF-01 | El sistema permitirá crear y actualizar usuarios por identificación. |
| RF-02 | El sistema importará usuarios desde archivos de texto delimitado y libros Excel. |
| RF-03 | El sistema habilitará al usuario para el período académico activo al guardarlo o registrar su acceso. |
| RF-04 | Si un documento no existe al registrar acceso, el sistema permitirá crear un invitado con identificación, nombre y programa. |
| RF-05 | El sistema exigirá seleccionar una sala y permitirá omitir el número de equipo. |
| RF-06 | El sistema guardará el acceso con fecha y hora en SQLite y en el archivo plano CSV. |
| RF-07 | Para docentes, el sistema solicitará curso y facultad y registrará la hora de salida cuando el auxiliar la ingrese. |
| RF-08 | El sistema permitirá solicitar un portátil con ubicación y celular obligatorios. |
| RF-09 | El sistema mostrará en la gestión de solicitudes una alerta cuando el usuario esté bloqueado. |
| RF-10 | El sistema impedirá asignar un equipo a un usuario bloqueado o a una solicitud que no esté pendiente. |
| RF-11 | Al asignar un equipo, el sistema registrará portátil, nombre del auxiliar y hora de entrega. |
| RF-12 | Al devolver un portátil, el sistema registrará hora de devolución y lo dejará disponible. |
| RF-13 | El administrador podrá bloquear y desbloquear usuarios; las solicitudes pendientes se actualizarán con el estado de alerta correspondiente. |
| RF-14 | El sistema permitirá registrar salas, portátiles y ubicaciones. |
| RF-15 | El sistema consultará detalle de accesos y préstamos por rango de fechas, programa e identificación. |
| RF-16 | El sistema mostrará conteos de estudiantes por fecha y programa, y por identificación. |
| RF-17 | El sistema exportará los resultados consultados a un libro `.xlsx`. |

### 3.3 Requisitos no funcionales

#### 3.3.1 Rendimiento

Las operaciones habituales de consulta y registro deberán finalizar en pocos segundos para los volúmenes de datos de un proyecto académico local.

#### 3.3.2 Seguridad

Las consultas SQL usan parámetros para reducir el riesgo de inyección. La base se almacena localmente; el acceso a la carpeta del proyecto debe limitarse a las personas autorizadas. La versión actual no ofrece autenticación ni cifrado de SQLite.

#### 3.3.3 Fiabilidad

La aplicación utiliza claves foráneas y transacciones para operaciones relacionadas. Los errores de validación se muestran al usuario. SQLite es el repositorio principal; el CSV de accesos es una copia adicional.

#### 3.3.4 Disponibilidad

El sistema está disponible cuando el equipo local está encendido y el archivo de base de datos puede abrirse. No contempla operación multiusuario simultánea en red.

#### 3.3.5 Mantenibilidad

El código separa modelos, controladores, servicios, utilidades y vistas. Las consultas y reglas se concentran en el servicio y el esquema en la inicialización de SQLite.

#### 3.3.6 Portabilidad

La aplicación usa Java y SQLite JDBC multiplataforma. Se entrega como proyecto Maven y puede ejecutarse en sistemas que dispongan de Java 8 o superior.

#### 3.3.7 Escalabilidad

La solución cubre un uso local de pequeña escala. Para uso institucional concurrente se debe migrar a una base cliente-servidor y agregar autenticación, autorización y respaldos administrados.

### 3.4 Otros requisitos

- La información de usuarios deberá tener identificación, nombres y tipo.
- La ubicación y el celular son obligatorios en una solicitud de portátil.
- El número de equipo es opcional en el registro de acceso a salas.
- Una solicitud puede existir antes de asignar equipo y auxiliar.
- El estudiante debe incluir archivos de datos de ejemplo sin divulgar datos personales reales.

## 4. Apéndices de análisis

### 4.1 Listado de actores

| Actor | Responsabilidad |
|---|---|
| Estudiante | Acceder a salas y solicitar portátiles. |
| Docente | Acceder a salas indicando curso y facultad; solicitar portátiles. |
| Funcionario | Solicitar portátiles y utilizar las salas habilitadas. |
| Invitado | Solicitar registro de acceso a sala. |
| Auxiliar de TI | Gestionar solicitudes, equipos y devoluciones. |
| Administrador | Mantener usuarios e inventario, importar información y consultar informes. |

### 4.2 Stakeholders

| Stakeholder | Interés |
|---|---|
| Oficina de Gestión Tecnológica | Control de salas, inventario, solicitudes y reportes. |
| Auxiliares de TI | Registro eficiente de entregas, alertas y devoluciones. |
| Estudiantes, docentes y funcionarios | Registro de servicios y consulta rápida de su información. |
| Coordinación académica | Conteos de uso por fecha, programa e identificación. |
| Docente evaluador | Verificar análisis, diseño, código y manuales de la asignatura. |

### 4.3 Atributos de calidad

| Atributo | Medida propuesta |
|---|---|
| Usabilidad | Formularios agrupados por tarea y mensajes de validación comprensibles. |
| Integridad | Claves foráneas y restricciones para evitar duplicar identificaciones. |
| Trazabilidad | Fecha/hora en accesos, solicitudes, entregas y devoluciones. |
| Mantenibilidad | Paquetes separados y modelos con atributos encapsulados. |
| Portabilidad | Dependencias Maven y base SQLite local. |
| Confidencialidad | Acceso a la carpeta de datos limitado a usuarios autorizados; autenticación pendiente. |

### 4.4 Listado de casos de uso

1. Importar usuarios.
2. Registrar/actualizar usuario.
3. Registrar acceso a sala.
4. Registrar salida de sala.
5. Solicitar portátil.
6. Gestionar alerta de bloqueo.
7. Asignar portátil.
8. Registrar devolución.
9. Administrar inventario.
10. Bloquear o desbloquear usuario.
11. Consultar informe.
12. Exportar informe a Excel.

Los actores y casos se representan en `diagramas/casos-uso.mmd`.

## 5. Especificación de casos de uso

### CU-01 Importar usuarios

- **Actor principal:** Administrador.
- **Precondición:** El archivo existe y sigue el orden de diez columnas definido en el README.
- **Flujo principal:** abre Usuarios y bloqueos; selecciona Importar; elige un TXT, CSV, TSV o Excel; el sistema valida cada fila y crea o actualiza cada usuario; muestra el total importado.
- **Excepciones:** extensión no compatible, semestre no numérico o fila incompleta; el sistema informa la causa y no continúa silenciosamente.
- **Postcondición:** los usuarios y su estado de habilitación quedan guardados para el período activo.

### CU-02 Registrar acceso a sala

- **Actor principal:** Estudiante, docente, funcionario o invitado; el auxiliar puede operar el formulario.
- **Precondición:** Hay al menos una sala registrada.
- **Flujo principal:** se digita el documento; se selecciona sala; si el usuario no existe, se completan nombre y programa; se completa equipo opcional; si es docente se indica curso y facultad; se registra el ingreso.
- **Postcondición:** el acceso queda en SQLite y en `datos/accesos_salas.csv` con fecha y hora.

### CU-03 Registrar salida de sala

- **Actor principal:** Auxiliar de TI.
- **Precondición:** Existe un ingreso sin hora de salida.
- **Flujo principal:** selecciona el ingreso de la tabla y elige Registrar salida.
- **Postcondición:** el sistema registra hora de salida y retira el evento de la lista de ingresos abiertos.

### CU-04 Solicitar y asignar portátil

- **Actores:** Solicitante y auxiliar de TI.
- **Precondición:** Usuario y ubicación están registrados.
- **Flujo principal:** el usuario ingresa documento, ubicación y celular; el sistema guarda la solicitud; el auxiliar selecciona la solicitud pendiente, portátil disponible y escribe su nombre; el sistema registra entrega y cambia el estado del equipo.
- **Flujo alterno:** si el usuario está bloqueado, la solicitud queda marcada `ALERTA_BLOQUEO`, se muestra en rojo y el sistema impide asignar portátil hasta desbloquearlo.
- **Postcondición:** queda registrada la solicitud y, si aplica, el préstamo activo.

### CU-05 Registrar devolución

- **Actor principal:** Auxiliar de TI.
- **Precondición:** El préstamo está en estado PRESTADO.
- **Flujo principal:** selecciona el préstamo y registra devolución.
- **Postcondición:** se almacena hora de devolución y el portátil queda disponible.

### CU-06 Consultar y exportar informe

- **Actor principal:** Administrador o Gestión Tecnológica.
- **Precondición:** Hay registros almacenados.
- **Flujo principal:** selecciona tipo de informe, rango de fechas, programa o documento; consulta detalle o conteo; exporta la tabla a `.xlsx`.
- **Postcondición:** el archivo Excel queda guardado en la ubicación seleccionada.

## 6. Diagrama de clases

Ver `diagramas/clases.mmd`. Las clases del dominio incluyen `Usuario`, `Sala`, `Portatil`, `Ubicacion`, `RegistroAcceso` y `PrestamoPortatil`; `SistemaService` aplica reglas y persistencia; las utilidades crean el esquema y manejan archivos.

## 7. Diagramas de secuencia

Ver `diagramas/secuencia-salas.mmd` y `diagramas/secuencia-prestamo.mmd`.

## 8. Diagrama de componentes

Ver `diagramas/componentes.mmd`.

## 9. Diagrama de despliegue

Ver `diagramas/despliegue.mmd`.

## 10. Análisis de riesgos

| Riesgo | Probabilidad | Impacto | Tratamiento |
|---|---|---|---|
| Pérdida del archivo SQLite del equipo local | Media | Alto | Copiar periódicamente la carpeta `datos` a un medio institucional autorizado. |
| Importar archivo con columnas o tipos incorrectos | Media | Medio | Validar diez columnas, encabezado y semestre; conservar el archivo original. |
| Asignar equipo a usuario bloqueado | Baja | Alto | Mostrar alerta, actualizar estados de solicitudes y bloquear la asignación en la lógica. |
| Registrar devolución en equipo equivocado | Media | Medio | Seleccionar el préstamo activo desde la tabla y mostrar equipo asociado. |
| Inconsistencia entre SQLite y CSV espejo | Baja | Medio | SQLite es fuente principal; revisar errores de escritura y respaldar ambos archivos. |
| Exposición de identificaciones personales | Media | Alto | Usar datos ficticios en demostraciones; restringir acceso a la carpeta de datos. |
| Falla al descargar bibliotecas Maven | Media | Medio | Importar el proyecto como Maven con conexión a Internet; conservar el `pom.xml`. |
| Cambios en requisitos del período académico | Media | Medio | Verificar el período activo al inicio del semestre; la selección manual del período queda como mejora. |

## 11. Manual de usuario

### 11.1 Inicio

Compila y ejecuta el proyecto como se indica en el README. En el primer inicio se crean la base de datos local y datos iniciales de inventario. No se incluyen usuarios reales.

### 11.2 Registrar acceso

Abre **Salas**, digita el documento y selecciona la sala. Si se trata de un invitado nuevo, completa nombre y programa. El número de equipo es opcional. Para un docente, completa curso y facultad. Pulsa **Registrar ingreso**. Para cerrar el registro, selecciona la fila correspondiente en Ingresos sin hora de salida y pulsa **Registrar salida del seleccionado**.

### 11.3 Solicitar portátil

En **Portátiles**, digita el documento, el celular y selecciona ubicación; pulsa **Enviar solicitud**. El usuario debe estar registrado previamente.

### 11.4 Entregar y recibir portátil

El auxiliar revisa la tabla de gestión. Las filas `ALERTA_BLOQUEO` aparecen resaltadas y no se pueden asignar. Selecciona una solicitud `SOLICITADO`, elige un equipo disponible, escribe su nombre y pulsa **Asignar equipo**. Al recibir el portátil, selecciona la fila PRESTADO y pulsa **Registrar devolución**.

### 11.5 Administrar usuarios y bloqueos

Usa el formulario para crear o actualizar un usuario. También puedes importar un archivo compatible. Para cambiar un bloqueo, selecciona la persona en la tabla y pulsa **Bloquear / desbloquear seleccionado**. Los cambios se reflejan en las solicitudes pendientes.

### 11.6 Administrar inventario

En **Inventario**, agrega salas con capacidad y ubicación, portátiles con su código y ubicaciones de préstamo. El sistema inicia con valores de ejemplo que puedes conservar o complementar.

### 11.7 Consultar informes

En **Informes**, selecciona un informe de detalle o conteo, completa opcionalmente fechas en formato `AAAA-MM-DD`, programa y documento, y pulsa **Consultar**. Pulsa **Exportar a Excel** y elige dónde guardar el libro `.xlsx`.

### 11.8 Respaldo

Cierra la aplicación y copia la carpeta `datos`. Contiene la base SQLite, la copia CSV de accesos y los archivos de importación de ejemplo. No compartas una copia con identificaciones reales fuera de los canales autorizados.

## 12. Manual de programador

### 12.1 Tecnologías

- Java 8 o superior.
- Swing para interfaz gráfica.
- SQLite y Xerial JDBC para persistencia.
- Apache POI para importación Excel y exportación XLSX.
- Maven para resolución de dependencias y empaquetado.

### 12.2 Paquetes

- `modelos`: clases del dominio.
- `controladores`: coordinación de flujos de interfaz, como ingreso y salida de salas.
- `servicios`: validaciones, reglas de negocio, consultas e importación.
- `utilidades`: conexión e inicialización SQLite, exportación Excel y copia CSV.
- `vistas`: ventana Swing y punto de entrada.

### 12.3 Base de datos

`BaseDatos.inicializar()` crea las tablas `usuarios`, `salas`, `portatiles`, `ubicaciones`, `periodos`, `habilitaciones`, `accesos` y `prestamos`, claves foráneas e índices. La aplicación crea la carpeta `datos` al iniciar. El periodo activo se inicializa según año y semestre calendario; la UI no permite modificarlo todavía.

### 12.4 Compilación y depuración

1. Importar el directorio del proyecto como proyecto Maven.
2. Esperar la resolución de `sqlite-jdbc` y `poi-ooxml`.
3. Ejecutar `vistas.Main` desde el IDE o `mvn clean package` y luego el JAR generado.
4. Para reiniciar los datos de demostración, cerrar la aplicación y respaldar/eliminar `datos/gestion_tecnologica.db`. La eliminación borra los registros guardados.

### 12.5 Formato de importación

El lector procesa la primera hoja en Excel o una línea por usuario en TXT/CSV/TSV. Orden esperado: identificación, nombres, apellidos, tipo, programa, semestre, jornada, facultad/dependencia, bloqueado y activo. Las columnas faltantes generan error; identificación es la clave de actualización.

### 12.6 Extensión

Para añadir una función nueva, agregar o modificar atributos de modelo, actualizar el esquema y sus consultas en `SistemaService`, y exponer la operación en una pestaña Swing. Mantener consultas parametrizadas y registrar tiempos como `LocalDateTime`.

## 13. Estado de la entrega

La aplicación proporciona una primera versión funcional de los módulos descritos. Antes de entregar, el estudiante debe ejecutar el proyecto en su IDE, verificar que la base se crea en su equipo, revisar las pantallas y exportaciones, capturar pantallas reales de ejecución, completar sus datos de portada y adaptar el formato final a ICONTEC. La autenticación y la administración de período desde interfaz no forman parte de esta versión.
