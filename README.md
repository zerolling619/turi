# Turistea - Aplicación Android

Aplicación de turismo desarrollada en Android con navegación, autenticación y gestión de reservas.

## 🚀 Inicio Rápido

### Primera Vez en Este Proyecto

**PASO 1:** Abrir en Android Studio
```
File → Open → Seleccionar carpeta "Turistea"
```

**PASO 2:** Esperar Sincronización
```
Android Studio descargará automáticamente las dependencias
```

**PASO 3:** Si Hay Errores
```
Build → Clean Project
Build → Rebuild Project
```

### Solución Rápida (Desde Terminal)

**Windows:**
```cmd
limpiar_y_reconstruir.bat
```

**Linux/Mac:**
```bash
chmod +x limpiar_y_reconstruir.sh
./limpiar_y_reconstruir.sh
```

## 📋 Requisitos Previos

- **Android Studio:** Arctic Fox o superior (2021+)
- **JDK:** Versión 11 o superior
- **Android SDK:** API 28 (Android 9) - API 36 (Android 14)
- **Gradle:** 8.13 (incluido en el proyecto)

## 🗂️ Estructura del Proyecto

```
Turistea/
├── app/
│   ├── src/main/
│   │   ├── java/robin/pe/turistea/
│   │   │   ├── MainActivity.java          # Actividad principal
│   │   │   ├── ui/                        # Fragmentos/Actividades
│   │   │   ├── models/                    # Modelos de datos
│   │   │   └── utils/                     # Utilidades (Volley, etc.)
│   │   ├── res/
│   │   │   ├── layout/                    # XML de layouts
│   │   │   ├── drawable/                  # Recursos gráficos
│   │   │   ├── values/                    # Strings, colores, estilos
│   │   │   └── navigation/                # Gráfico de navegación
│   │   ├── AndroidManifest.xml
│   │   └── google-services.json          # Firebase config
│   ├── build.gradle.kts                  # Configuración del módulo
│   └── proguard-rules.pro               # Reglas de ProGuard
├── gradle/
│   └── wrapper/                          # Gradle wrapper
├── build.gradle.kts                      # Configuración del proyecto
├── settings.gradle.kts                   # Configuración de módulos
├── gradle.properties                     # Propiedades de Gradle
├── limpiar_y_reconstruir.bat            # Script limpieza Windows
├── limpiar_y_reconstruir.sh             # Script limpieza Linux/Mac
└── README.md                            # Este archivo
```

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Java
- **View Binding:** Habilitado para vistas
- **Navigation Component:** Navegación entre pantallas
- **Google Maps:** Mapas y localización
- **Firebase:** Backend (configuración incluida)
- **Volley:** Peticiones HTTP
- **Glide:** Carga de imágenes
- **Material Design:** Componentes de UI

## 🔑 Configuración Necesaria

### Firebase
✅ El archivo `google-services.json` está incluido

### Google Maps
El API Key está configurado en `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
```

### Facebook Login (Opcional)
Configurar en `app/src/main/res/values/strings.xml`:
```xml
<string name="facebook_app_id">TU_APP_ID</string>
<string name="fb_login_protocol_scheme">fbTU_APP_ID</string>
```

## 🏗️ Compilar el Proyecto

### Desde Android Studio
```
Build → Make Project
```

### Desde Línea de Comandos
**Windows:**
```cmd
gradlew.bat build
```

**Linux/Mac:**
```bash
./gradlew build
```

## 📱 Ejecutar la Aplicación

1. Conectar un dispositivo o iniciar un emulador
2. Ejecutar: `Shift + F10` o botón ▶️ Run
3. Seleccionar dispositivo

**📖 Guía completa para instalar en tu celular:** `INSTALAR_EN_CELULAR.md`

## 🔍 Resolución de Problemas

### Error: "ActivityMainBinding cannot be resolved"
**Solución:** View Binding no se generó
```
1. Build → Clean Project
2. Build → Rebuild Project
```

### Error: "R cannot be resolved"
**Solución:** Archivos generados corruptos
```
1. Build → Clean Project
2. File → Sync Project with Gradle Files
3. Build → Rebuild Project
```

### Error: "Gradle sync failed"
**Solución:** Dependencias no descargadas
```
1. Verificar conexión a internet
2. File → Settings → Gradle → Use Gradle from wrapper
3. File → Sync Project with Gradle Files
```

### Proyecto se Copia a Otra PC
Ver archivo: **SOLUCION_RAPIDA.md**

Instrucciones completas: **INSTRUCCIONES_COPIA_PROYECTO.md**

## 📚 Recursos Adicionales

- `INSTALAR_EN_CELULAR.md` - 📱 Cómo instalar la app en tu celular
- `SOLUCION_RAPIDA.md` - Solución de errores comunes en 5 minutos
- `SOLUCION_GOOGLE_SIGNIN.md` - Solución para Google Sign-In
- `INSTRUCCIONES_COPIA_PROYECTO.md` - Guía detallada para copiar el proyecto

## 👥 Contribuidores

Robin PE - Turistea Team

## 📄 Licencia

Propietario - Todos los derechos reservados

## 📞 Soporte

Para problemas o preguntas:
1. Revisa `SOLUCION_RAPIDA.md`
2. Verifica que cumples con los requisitos previos
3. Intenta limpiar y reconstruir el proyecto
4. Contacta al equipo de desarrollo

---

**Versión:** 1.0  
**Última actualización:** 2025  
**Plataforma:** Android (API 28-36)

