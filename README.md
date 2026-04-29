# SafePaw 🐾 - Gestión Integral de Protectoras de Animales

SafePaw es una aplicación móvil moderna desarrollada para optimizar la gestión diaria de refugios y protectoras de animales. Permite un control exhaustivo de los registros de animales, su historial médico y la gestión de adopciones/apadrinamientos, todo sincronizado en tiempo real.

## 🚀 Características Principales

- **Gestión de Perfiles**: Registro manual completo (raza, peso, edad, microchip, vacunas).
- **Galería Multimedia**: Subida de fotos de perfil y fotos adicionales directamente a Supabase Storage.
- **Historial Médico**: Registro detallado de tratamientos e intervenciones con descripción y duración.
- **Búsqueda Avanzada**: Filtros por especie, estado de adopción y búsqueda por chip o nombre.
- **Escáner de Microchip**: Integración con cámara para detección rápida de animales registrados.
- **Documentación Dinámica**: Generación y envío de contratos de adopción y apadrinamiento.
- **Compartir Perfiles**: Función para compartir el perfil completo (datos + fotos + historial) a través de apps externas.

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: [Kotlin](https://kotlinlang.org/)
- **Interfaz**: [Jetpack Compose](https://developer.android.com/jetpack/compose) con Material 3
- **Backend**: [Supabase](https://supabase.com/) (PostgreSQL, Auth & Storage)
- **Inyección de Dependencias**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Carga de Imágenes**: [Coil](https://coil-kt.github.io/coil/)
- **Escaneo**: [Google ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **Cámara**: [CameraX](https://developer.android.com/jetpack/androidx/releases/camera)

## 📦 Estructura del Proyecto

El código sigue una arquitectura **MVVM** (Model-View-ViewModel) organizada de la siguiente manera:

- `data/`: Modelos de datos y repositorios de Supabase.
- `di/`: Módulos de inyección de dependencias (Hilt).
- `ui/`: 
  - `screens/`: Todas las pantallas de la aplicación (Dashboard, Detail, Add, etc.).
  - `viewmodels/`: Lógica de negocio y gestión de estados.
  - `theme/`: Configuración de colores y estilos.
- `navigation/`: Configuración del flujo de navegación entre pantallas.

## ⚙️ Configuración del Backend (Supabase)

Para que la aplicación funcione correctamente, es necesario configurar las siguientes tablas en Supabase:

1. **Tabla `animales`**: Datos básicos y técnicos.
2. **Tabla `tratamientos`**: Historial médico.
3. **Tabla `animal_fotos`**: Galería de imágenes adicionales.
4. **Storage**: Bucket público llamado `animal-photos`.

## 📄 Licencia

Este proyecto ha sido desarrollado como parte del Trabajo de Fin de Ciclo (TFC) para el grado de Desarrollo de Aplicaciones Multiplataforma (DAM).

---
Desarrollado con ❤️ para el bienestar animal.
