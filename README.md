# SafePaw - App de Gestión de Refugios

SafePaw es una aplicación Android profesional diseñada para la gestión eficiente de animales en refugios, utilizando un stack tecnológico moderno y escalable.

## 🚀 Stack Tecnológico
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Arquitectura MVVM)
- **Backend:** Supabase (Auth, PostgreSQL, Storage)
- **Inyección de Dependencias:** Hilt
- **Escaneo:** CameraX + ML Kit

---

## 🛠️ Guía de Configuración Inicial

### 1. Configuración de Supabase
Para que la app funcione, debes tener un proyecto en [Supabase](https://supabase.com/).
1. **Base de Datos:** Ejecuta el script SQL de creación de tablas (`animales`, `tratamientos`, `usuarios`) en el editor SQL de Supabase.
2. **Credenciales:** Copia tu `Project URL` y `API Key` en el archivo:
   `app/src/main/java/com/safepaw/app/di/NetworkModule.kt`

### 2. Autenticación
La app utiliza **Supabase Auth**. Para acceder por primera vez:
1. Ve a **Authentication > Users** en tu panel de Supabase.
2. Crea un usuario manualmente (Email y Contraseña).
3. Asegúrate de que el usuario esté confirmado o desactiva "Confirm Email" en la configuración del proveedor.

---

## 📱 Guía de Uso de la Aplicación

### 1. Inicio de Sesión
- Al abrir la app, introduce las credenciales creadas en el paso anterior.
- Si el login es exitoso, serás redirigido al **Dashboard**.

### 2. Dashboard (Panel Principal)
- **Lista de Animales:** Visualiza todos los animales registrados.
- **Buscador:** Filtra animales introduciendo el número de **microchip** (mínimo 5 caracteres).
- **Escáner (FAB):** Pulsa el botón flotante "Scan" para abrir la cámara.

### 3. Módulo de Escaneo
- Apunta la cámara al código de barras del microchip del animal.
- ML Kit detectará el código automáticamente y te redirigirá a la **Ficha Técnica** del animal encontrado.

### 4. Ficha Técnica y Edición (CRUD)
- **Visualización:** Consulta nombre, especie y estado de adopción.
- **Edición:** Pulsa el icono del **Lápiz** para modificar los datos. Pulsa el **Disco (Save)** para sincronizar los cambios con Supabase.
- **Historial Médico:** Pulsa "Ver Tratamientos" para acceder al registro clínico.

### 5. Historial Médico y Roles
- Los usuarios con rol de **Veterinario (Vet)** o **Gestor** pueden añadir nuevos tratamientos pulsando el botón "+".
- El historial se guarda de forma inmutable y cronológica en la base de datos.

### 6. Generación de Contratos
- Dentro de la ficha del animal, puedes generar un **Contrato de Adopción en PDF**.
- El archivo se guardará automáticamente en la carpeta de Documentos de tu dispositivo.

---

## ⚠️ Solución de Problemas Comunes
- **Pantalla en blanco:** Asegúrate de haber hecho "Sync Project with Gradle Files" y que las credenciales de Supabase sean correctas.
- **Error de Cámara:** Verifica que has aceptado los permisos de cámara en el dispositivo.
- **Error de Red:** Comprueba que el emulador o móvil tiene acceso a internet (Supabase requiere HTTPS).
