# Sistema de Gestión de Candidaturas — CLI (v1.0)

Proyecto desarrollado para los módulos de **Acceso a Datos** y **Desarrollo de Interfaces** (2º DAM).

## Integrantes del Equipo
- **Óscar Ramírez Toro**
- **Juan Antonio Moreno Hevilla**

---

## Descripción del Proyecto
Herramienta de línea de comandos (CLI) desarrollada en Java para la gestión integral de candidaturas de empleo. El sistema opera directamente sobre persistencia en ficheros de texto plano (delimitados por punto y coma `;`), ejecutando cada orden de forma atómica sin menús interactivos ni bloqueos de ejecución.

El diseño sigue estrictamente los principios **SOLID**, desacoplando la lógica de dominio, la persistencia de datos y la interfaz de usuario por consola.

---

## Requisitos de Ejecución
- **Java JDK:** 17 o superior.
- **IDE recomendado:** IntelliJ IDEA.
- **Formato del archivo de datos:** `id;titulo;empresa;estado;fecha` (fecha en formato `dd/MM/yyyy`).

---

## Guía de Comandos

Cada comando se invoca pasando los argumentos correspondientes en la ejecución:

### 1. Listar todas las candidaturas
```
java -jar app.jar listar
```
### 2. Listar con Filtros (estado y/o empresa)
```
java -jar app.jar listar --estado "En proceso" --empresa "Google"
java -jar app.jar listar --estado "Aceptada"
java -jar app.jar listar --empresa "Microsoft"
```
### 3. Añadir una nueva candidatura
Genera un ID incremental automáticamente y registra la candidatura en el fichero.
```
java -jar app.jar añadir --titulo "Desarrollador Java Junior" --empresa "Indra" --estado "Enviada" --fecha 15/10/2026
```
### 4. Consultar detalle de una candidatura
```
java -jar app.jar ver 13
```
### 5. Eliminar una candidatura por ID
```
java -jar app.jar borrar 13
```
### 6. Importar candidaturas desde archivo CSV
Carga registros desde un fichero externo delimitado por comas o punto y coma y los consolida en el archivo del sistema.
```
java -jar app.jar importar candidaturas_externas.csv
```
## Tratamiento de Errores

El sistema valida todas las entradas por línea de comandos y la integridad de los ficheros:

- **Argumentos incompletos o desconocidos:** Muestra mensaje descriptivo con la sintaxis correcta.
- **Registro no encontrado:** Avisa si un ID consultado o a borrar no existe en el fichero.
- **Fichero no encontrado:** Controla la ausencia del fichero CSV a importar sin exponer la traza de error (`stackTrace`).
- **Formato de datos erróneo:** Valida que las fechas sigan el patrón `dd/MM/yy` y los IDs sean valores numéricos válidos.
## Arquitectura y principios SOLID
- **S (Single Responsibility):** Separación nítida entre el modelo de dominio, el repositorio de acceso a archivos, el analizador de comandos (Command Parser) y el formateador de salida.
- **O (Open/Closed):** Comandos estructurados mediante el patrón Command, facilitando la adición de nuevas operaciones sin modificar las existentes.
- **L (Liskov Substitution):** Abstracciones e interfaces claras para el repositorio y la ejecución de comandos.
- **I (Interface Segregation):** Interfaces específicas y acotadas según las necesidades de cada componente.
- **D (Dependency Inversion):** Los servicios de alto nivel dependen de abstracciones (interfaces de repositorio y vista), no de implementaciones concretas.