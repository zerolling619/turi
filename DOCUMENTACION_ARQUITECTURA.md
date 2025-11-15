# Documentación de Arquitectura - Turistea App

## Índice
1. [Estructura General](#estructura-general)
2. [Archivos Principales](#archivos-principales)
3. [Carpeta UI (Interfaz de Usuario)](#carpeta-ui-interfaz-de-usuario)
4. [Carpeta Models](#carpeta-models)
5. [Carpeta Utils](#carpeta-utils)

---

## Estructura General

La aplicación Turistea está organizada en el paquete `robin.pe.turistea` con la siguiente estructura:

```
robin.pe.turistea/
├── Config.java                    # Configuración centralizada
├── MainActivity.java              # Actividad principal
├── models/
│   └── TourPackage.java         # Modelo de datos para paquetes turísticos
├── ui/
│   ├── Splash.java               # Pantalla de inicio/splash
│   ├── Start_app.java           # Pantalla de bienvenida
│   ├── Login.java               # Autenticación de usuarios
│   ├── Register.java            # Registro de nuevos usuarios
│   ├── Verification_code.java   # Verificación de código
│   ├── Forget_password.java     # Recuperación de contraseña
│   ├── Change_passwordd.java    # Cambio de contraseña
│   ├── Profile.java             # Perfil de usuario
│   ├── Settings.java            # Configuraciones
│   ├── Inicio.java              # Pantalla principal (usuarios normales)
│   ├── Inicio_VistaReservas.java # Pantalla principal (drivers/guides)
│   ├── Location.java            # Selección de ubicación
│   ├── PackageTour.java         # Detalle de paquete turístico
│   ├── Package_tourTracking.java # Seguimiento de paquete
│   ├── RouteDetail.java         # Detalle de ruta
│   ├── Reservation.java         # Reservas
│   └── ReservationDetail.java   # Detalle de reserva
└── utils/
    ├── VolleySingleton.java     # Singleton para peticiones HTTP
    └── EjemploVolleyLogin.java  # Ejemplo de uso de Volley
```

---

## Archivos Principales

### 1. Config.java

**Ubicación:** `robin.pe.turistea/Config.java`

**Propósito:** Configuración centralizada de URLs y endpoints del backend.

**Funcionalidad:**
- Define la URL base del backend (`BASE_URL`)
- Contiene todas las constantes de endpoints del API:
  - `LOGIN_URL`: `/api/signin`
  - `REGISTER_URL`: `/api/signup`
  - `VERIFY_CODE_URL`: `/api/active/verifycode`
  - `USER_ACCOUNT_URL`: `/api/user-account`
  - `PACKAGES_URL`: `/api/user-account/packages`
  - `CHANGE_PASSWORD_URL`: `/api/user-account/updatepassword`
  - Y más...

**Método Principal:**
- `getBaseUrl()`: Detecta automáticamente si la app está corriendo en emulador o dispositivo real:
  - **Emulador**: Retorna `http://10.0.2.2:4001`
  - **Dispositivo real**: Retorna la IP configurada en `BASE_URL`

**Cómo funciona:**
1. Verifica características del dispositivo (FINGERPRINT, MODEL, MANUFACTURER, etc.)
2. Si detecta que es emulador, usa `10.0.2.2` (IP especial para acceder al localhost de la PC desde el emulador)
3. Si es dispositivo real, usa la IP configurada manualmente

**Uso:**
```java
String url = Config.getBaseUrl() + "/api/endpoint";
```

---

### 2. MainActivity.java

**Ubicación:** `robin.pe.turistea/MainActivity.java`

**Propósito:** Actividad principal que gestiona la navegación y el menú lateral (Drawer).

**Funcionalidad Principal:**

1. **Inicialización (`onCreate`):**
   - Configura el Navigation Controller
   - Configura el Bottom Navigation View
   - Configura el Navigation Drawer (menú lateral)
   - Carga datos del usuario en el header del drawer

2. **Gestión del Drawer:**
   - `openDrawer()`: Abre el menú lateral
   - `loadUserDataInHeader()`: Carga nombre, email e imagen del usuario desde SharedPreferences
   - `updateDrawerHeader()`: Actualiza el header cuando cambian los datos del usuario

3. **Navegación basada en roles:**
   - `navigateBasedOnRole()`: Navega según el rol del usuario:
     - **driver/guide**: Va a `Inicio_VistaReservas`
     - **user/admin**: Va a `Inicio` normal

4. **Gestión de sesión:**
   - `handleLogout()`: Limpia SharedPreferences y navega al login
   - `navigateToLogin()`: Navega al fragmento de login

5. **Ocultar Bottom Navigation:**
   - Oculta el bottom navigation en pantallas de autenticación (splash, login, register, verification)

**Flujo de trabajo:**
1. La app inicia en `Splash`
2. Usuario navega a `Start_app` → `Login`
3. Después del login, según el rol, va a `Inicio` o `Inicio_VistaReservas`
4. El drawer permite navegar entre secciones
5. El bottom navigation se muestra/oculta según la pantalla actual

---

## Carpeta UI (Interfaz de Usuario)

### 3. Splash.java

**Propósito:** Pantalla inicial de la aplicación (splash screen).

**Funcionalidad:**
- Muestra la pantalla de bienvenida inicial
- Al hacer clic en "Iniciar", navega a `Start_app`

**Flujo:**
```
Splash → (click) → Start_app
```

---

### 4. Start_app.java

**Propósito:** Pantalla de bienvenida antes del login.

**Funcionalidad:**
- Pantalla intermedia entre splash y login
- Al hacer clic en "Siguiente", navega a `Login`

**Flujo:**
```
Start_app → (click) → Login
```

---

### 5. Login.java

**Propósito:** Autenticación de usuarios (email/password y Google Sign-In).

**Funcionalidad Principal:**

1. **Login con Email/Password:**
   - Valida campos (email y contraseña)
   - Envía petición POST a `/api/signin`
   - Recibe JWT token del backend
   - Guarda datos del usuario en SharedPreferences:
     - `jwt`: Token de autenticación
     - `user_name`: Nombre completo
     - `user_email`: Email
     - `user_cellphone`: Teléfono
     - `user_role`: Rol (user, admin, driver, guide)
     - `user_image_path`: URL de imagen de perfil
   - Obtiene perfil completo del usuario desde `/api/user-account`
   - Navega según el rol del usuario

2. **Login con Google:**
   - Configura Google Sign-In con OAuth
   - Solicita ID Token para validar con backend
   - Envía token a `/api/signin-google`
   - Si el usuario no existe, lo registra automáticamente
   - Guarda datos y navega según rol

3. **Navegación:**
   - Links a "Olvidé mi contraseña" → `Forget_password`
   - Link a "Registrarse" → `Register`
   - Botón de Google Sign-In

**Flujo:**
```
Login → (éxito) → Inicio o Inicio_VistaReservas (según rol)
Login → (click "Registrarse") → Register
Login → (click "Olvidé contraseña") → Forget_password
```

**Datos guardados en SharedPreferences:**
- `jwt`: Token JWT para autenticación
- `user_name`: Nombre completo
- `user_email`: Email
- `user_cellphone`: Teléfono
- `user_role`: Rol del usuario
- `user_image_path`: URL de imagen

---

### 6. Register.java

**Propósito:** Registro de nuevos usuarios.

**Funcionalidad:**

1. **Validación de campos:**
   - Nombres y apellidos (requeridos)
   - Fecha de nacimiento (selector de fecha)
   - DNI (8 dígitos)
   - Celular (9 dígitos)
   - Sexo (M/F)
   - Email (formato válido)
   - Contraseña (mínimo 6 caracteres, al menos 1 número y 1 carácter especial)
   - Confirmación de contraseña (debe coincidir)

2. **Registro:**
   - Envía petición POST a `/api/signup` con todos los datos
   - Si es exitoso, navega a `Verification_code` para verificar email

3. **Registro con redes sociales:**
   - Soporte para Google Sign-In
   - Soporte para Facebook (comentado/desactivado)

**Flujo:**
```
Register → (éxito) → Verification_code
```

---

### 7. Verification_code.java

**Propósito:** Verificación del código enviado por email.

**Funcionalidad:**
- Usuario ingresa el código recibido por email
- Envía petición a `/api/active/verifycode` con el código
- Si es correcto, activa la cuenta y navega al login

**Flujo:**
```
Verification_code → (código correcto) → Login
```

---

### 8. Forget_password.java

**Propósito:** Recuperación de contraseña olvidada.

**Funcionalidad:**
- Usuario ingresa su email
- Envía petición al backend para solicitar reset de contraseña
- Backend envía código por email
- Usuario puede restablecer su contraseña

---

### 9. Change_passwordd.java

**Propósito:** Cambio de contraseña para usuarios autenticados (solo drivers y guides).

**Funcionalidad:**

1. **Validación:**
   - Contraseña actual (requerida)
   - Nueva contraseña (mínimo 6 caracteres)
   - Confirmación de contraseña (debe coincidir)

2. **Cambio de contraseña:**
   - Envía petición **PUT** a `/api/user-account/updatepassword`
   - Incluye JWT en header `Authorization: Bearer {token}`
   - Body JSON: `{"current_password": "...", "new_password": "..."}`
   - Si es exitoso, muestra mensaje y regresa al perfil

3. **Restricción de acceso:**
   - Solo visible para usuarios con rol "driver" o "guide"
   - La visibilidad se controla desde `Profile.java`

**Flujo:**
```
Profile → (click "Cambiar contraseña") → Change_passwordd → (éxito) → Profile
```

---

### 10. Profile.java

**Propósito:** Perfil de usuario y gestión de cuenta.

**Funcionalidad:**

1. **Carga de datos:**
   - Carga datos del usuario desde SharedPreferences:
     - Nombre completo
     - Email
     - Teléfono
     - Imagen de perfil (usando Glide)
   - Muestra datos en la interfaz

2. **Opciones según rol:**
   - **driver/guide**: Muestra opción "Cambiar contraseña"
   - **otros roles**: Oculta la opción de cambiar contraseña

3. **Cerrar sesión:**
   - Muestra diálogo de confirmación
   - Limpia SharedPreferences
   - Navega al login

4. **Navegación:**
   - Botón "Atrás" navega según el rol:
     - driver/guide → `Inicio_VistaReservas`
     - otros → `Inicio`

**Flujo:**
```
Profile → (click "Cambiar contraseña") → Change_passwordd (solo driver/guide)
Profile → (click "Cerrar sesión") → Login
Profile → (back) → Inicio o Inicio_VistaReservas (según rol)
```

---

### 11. Settings.java

**Propósito:** Configuraciones de la aplicación.

**Funcionalidad:**
- Pantalla de configuraciones (actualmente básica)
- Puede expandirse para agregar más opciones

---

### 12. Inicio.java

**Propósito:** Pantalla principal para usuarios normales (user/admin).

**Funcionalidad Principal:**

1. **Gestión de ubicación:**
   - Solicita permisos de ubicación
   - Obtiene ubicación actual usando FusedLocationProviderClient
   - Convierte coordenadas a dirección usando Geocoder
   - Permite seleccionar ubicación manualmente (navega a `Location`)
   - Guarda ubicación seleccionada en SharedPreferences

2. **Carga de paquetes turísticos:**
   - Obtiene lista de paquetes desde `/api/user-account/packages`
   - Usa JWT para autenticación
   - Crea cards dinámicamente para cada paquete
   - Muestra: imagen, nombre, precio, ubicación, duración

3. **Navegación a detalle:**
   - Al hacer clic en un paquete, navega a `PackageTour` con los datos del paquete

4. **Menú lateral:**
   - Botón para abrir el drawer

**Flujo:**
```
Inicio → (click ubicación) → Location → (selecciona) → Inicio
Inicio → (click paquete) → PackageTour
```

**Datos guardados:**
- `selected_location`: Ubicación seleccionada por el usuario

---

### 13. Inicio_VistaReservas.java

**Propósito:** Pantalla principal para drivers y guides (muestra reservas).

**Funcionalidad:**
- Similar a `Inicio.java` pero enfocada en mostrar reservas
- Muestra las reservas asignadas al driver/guide
- Permite ver detalles de cada reserva

**Flujo:**
```
Inicio_VistaReservas → (click reserva) → ReservationDetail
```

---

### 14. Location.java

**Propósito:** Selección manual de ubicación usando mapa.

**Funcionalidad:**
- Muestra mapa interactivo
- Usuario puede seleccionar una ubicación
- Guarda la ubicación seleccionada
- Regresa a `Inicio` con la nueva ubicación

**Flujo:**
```
Inicio → Location → (selecciona ubicación) → Inicio
```

---

### 15. PackageTour.java

**Propósito:** Detalle completo de un paquete turístico.

**Funcionalidad:**
- Recibe datos del paquete como argumentos de navegación
- Muestra información detallada:
  - Imágenes
  - Descripción completa
  - Precio
  - Duración
  - Ubicación
  - Itinerario
- Permite reservar el paquete
- Navega a `Package_tourTracking` para seguimiento

**Flujo:**
```
Inicio → PackageTour → (reservar) → Package_tourTracking
```

---

### 16. Package_tourTracking.java

**Propósito:** Seguimiento de un paquete turístico reservado.

**Funcionalidad:**
- Muestra el estado actual del paquete
- Muestra rutas y puntos de interés
- Permite ver detalles de cada ruta
- Navega a `RouteDetail` para ver detalles específicos

**Flujo:**
```
PackageTour → Package_tourTracking → (click ruta) → RouteDetail
```

---

### 17. RouteDetail.java

**Propósito:** Detalle de una ruta específica dentro de un paquete.

**Funcionalidad:**
- Muestra información detallada de una ruta
- Puntos de interés
- Horarios
- Información adicional

---

### 18. Reservation.java

**Propósito:** Gestión de reservas (actualmente básico).

**Funcionalidad:**
- Fragmento para mostrar y gestionar reservas
- Puede expandirse según necesidades

---

### 19. ReservationDetail.java

**Propósito:** Detalle de una reserva específica.

**Funcionalidad:**
- Muestra información completa de una reserva:
  - Datos del cliente
  - Paquete reservado
  - Fechas
  - Estado
  - Información de contacto

**Flujo:**
```
Inicio_VistaReservas → ReservationDetail
```

---

## Carpeta Models

### 20. TourPackage.java

**Ubicación:** `robin.pe.turistea/models/TourPackage.java`

**Propósito:** Modelo de datos para representar un paquete turístico.

**Propiedades:**
- `id`: ID único del paquete
- `name`: Nombre del paquete
- `description`: Descripción detallada
- `image`: URL de la imagen principal
- `price`: Precio del paquete
- `location`: Ubicación/destino
- `duration`: Duración en días

**Funcionalidad:**
- Clase POJO (Plain Old Java Object)
- Getters y setters para todas las propiedades
- Constructor vacío y constructor con parámetros
- Usado para parsear respuestas JSON del backend

**Uso:**
```java
TourPackage pkg = new TourPackage();
pkg.setId(1);
pkg.setName("Tour a Machu Picchu");
// ...
```

---

## Carpeta Utils

### 21. VolleySingleton.java

**Ubicación:** `robin.pe.turistea/utils/VolleySingleton.java`

**Propósito:** Singleton para gestionar la cola de peticiones HTTP con Volley.

**Funcionalidad:**
- Implementa patrón Singleton
- Mantiene una única instancia de `RequestQueue` de Volley
- Optimiza el uso de recursos al reutilizar la misma cola

**Cómo funciona:**
1. Primera llamada: Crea la instancia y la cola de peticiones
2. Llamadas posteriores: Retorna la misma instancia
3. Todas las peticiones HTTP usan la misma cola

**Uso:**
```java
RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
```

**Ventajas:**
- Evita crear múltiples colas de peticiones
- Mejora el rendimiento
- Centraliza la gestión de peticiones HTTP

---

### 22. EjemploVolleyLogin.java

**Ubicación:** `robin.pe.turistea/utils/EjemploVolleyLogin.java`

**Propósito:** Archivo de ejemplo/documentación sobre cómo usar Volley para login.

**Funcionalidad:**
- Contiene código de ejemplo
- Muestra cómo hacer peticiones HTTP con Volley
- Puede usarse como referencia para implementar nuevas funcionalidades

---

## Flujo General de la Aplicación

### Flujo de Autenticación:
```
Splash → Start_app → Login → (éxito) → Inicio/Inicio_VistaReservas
                ↓
            Register → Verification_code → Login
```

### Flujo Principal (Usuario Normal):
```
Inicio → (seleccionar ubicación) → Location → Inicio
     → (ver paquete) → PackageTour → Package_tourTracking → RouteDetail
     → (perfil) → Profile → Change_passwordd (solo driver/guide)
```

### Flujo Principal (Driver/Guide):
```
Inicio_VistaReservas → ReservationDetail
                  → Profile → Change_passwordd
```

---

## Sistema de Navegación

La aplicación usa **Android Navigation Component** para gestionar la navegación entre fragmentos:

- **NavController**: Gestiona la navegación
- **Navigation Graph**: Define las rutas entre pantallas (`mobile_navigation.xml`)
- **Safe Args**: Pasa datos entre fragmentos de forma segura

**Ejemplo de navegación:**
```java
navController.navigate(R.id.action_navigation_login_to_navigation_inicio);
```

---

## Almacenamiento de Datos

### SharedPreferences
La app usa `SharedPreferences` con el nombre `"user_prefs"` para guardar:
- `jwt`: Token de autenticación
- `user_name`: Nombre del usuario
- `user_email`: Email
- `user_cellphone`: Teléfono
- `user_role`: Rol (user, admin, driver, guide)
- `user_image_path`: URL de imagen
- `selected_location`: Ubicación seleccionada

**Acceso:**
```java
SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
String jwt = prefs.getString("jwt", null);
```

---

## Autenticación

### JWT (JSON Web Token)
- Se obtiene después del login exitoso
- Se guarda en SharedPreferences
- Se envía en el header `Authorization: Bearer {token}` en todas las peticiones autenticadas

### Roles de Usuario
- **user**: Usuario normal
- **admin**: Administrador
- **driver**: Conductor
- **guide**: Guía turístico

Los roles determinan:
- A qué pantalla principal navegar después del login
- Qué opciones están disponibles en el perfil
- Qué funcionalidades pueden usar

---

## Comunicación con Backend

### Métodos HTTP usados:
- **POST**: Login, registro, verificación
- **GET**: Obtener datos (perfil, paquetes, etc.)
- **PUT**: Actualizar datos (cambio de contraseña)

### Headers comunes:
```java
conn.setRequestProperty("Authorization", "Bearer " + jwt);
conn.setRequestProperty("Content-Type", "application/json");
conn.setRequestProperty("Accept", "application/json");
```

### Manejo de respuestas:
- Código 200/204: Éxito
- Código 400/401: Error de validación/autenticación
- Código 500: Error del servidor

---

## Notas Importantes

1. **Configuración de IP**: Cambiar `BASE_URL` en `Config.java` según el entorno
2. **Permisos**: La app requiere permisos de ubicación para funcionar completamente
3. **Google Sign-In**: Requiere configuración de OAuth en Google Cloud Console
4. **Imágenes**: Se usa Glide para cargar imágenes desde URLs
5. **Navegación**: El bottom navigation se oculta automáticamente en pantallas de autenticación

---

## Mejoras Futuras Sugeridas

1. Implementar caché de paquetes turísticos
2. Agregar modo offline
3. Mejorar manejo de errores de red
4. Implementar refresh token para JWT
5. Agregar notificaciones push
6. Mejorar la UI/UX de algunas pantallas
7. Agregar más validaciones en formularios
8. Implementar búsqueda y filtros de paquetes

---

**Última actualización:** 2025-01-06
**Versión de la app:** Basada en Android Navigation Component y Material Design

