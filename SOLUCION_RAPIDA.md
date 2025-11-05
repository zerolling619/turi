# 🚀 Solución Rápida: Errores con MainActivity en Otra PC

## ⚡ Solución Inmediata (5 minutos)

### En la NUEVA PC, sigue estos pasos en orden:

```
1️⃣ Abrir Android Studio → File → Open → Seleccionar carpeta "Turistea"

2️⃣ ESPERAR: Deja que Android Studio descargue dependencias (primera vez puede tardar 5-10 min)

3️⃣ Si ves errores rojos → Build → Clean Project

4️⃣ Luego → Build → Rebuild Project

5️⃣ Si persisten errores → File → Invalidate Caches → Invalidate and Restart

✅ LISTO: El proyecto debe funcionar
```

## 🔍 ¿Por Qué Ocurre Este Problema?

| Problema | Causa | Solución |
|----------|-------|----------|
| **ActivityMainBinding no encontrado** | View Binding no se generó | `Clean → Rebuild` |
| **R no se puede resolver** | Archivos generados corruptos | `Clean → Sync → Rebuild` |
| **Fallo en sincronización** | Dependencias no descargadas | Sincronizar Gradle manualmente |
| **Rutas incorrectas** | `local.properties` de otra PC | Se regenera automáticamente |
| **⚠️ Google Sign-In falla** | SHA-1 no registrado en Firebase | Ver `SOLUCION_GOOGLE_SIGNIN.md` |

## 📁 Archivos Que SE REGENERAN Automáticamente

Estos NO necesitas copiarlos (Android Studio los crea):

```
❌ app/build/              → Archivos compilados
❌ build/                  → Construcción del proyecto
❌ .gradle/               → Caché de Gradle
❌ local.properties        → Ruta del SDK (específica por PC)
```

## ✅ Checklist de Verificación

Después de los pasos, verifica:

```
☐ Sin errores rojos en MainActivity.java
☐ Build → Make Project completa sin errores
☐ Puedes ejecutar la app (Run)
☐ Las vistas se muestran correctamente
☐ La navegación funciona
```

## 🆘 Si Nada Funciona

### Opción 1: Sincronización Forzada
```
File → Settings → Build → Gradle
- Marca: "Use Gradle from: 'wrapper' gradle-wrapper.properties file"
- Aplica y cierra
- File → Sync Project with Gradle Files
```

### Opción 2: Reinstalar Dependencias
```bash
# En la terminal dentro del proyecto:
gradlew clean
gradlew build --refresh-dependencies
```

### Opción 3: Crear Proyecto Nuevo y Copiar Src
```
1. Crear nuevo proyecto en Android Studio con mismo nombre
2. Copiar TODO el contenido de app/src/
3. Copiar gradle/, build.gradle.kts, settings.gradle.kts
4. Sincronizar proyecto
```

## 📝 Información del Proyecto

- **SDK Mínimo:** Android 9 (API 28)
- **SDK Objetivo:** Android 14 (API 36)
- **Java:** Versión 11
- **Gradle:** 8.13 (incluido en el proyecto)
- **View Binding:** ✅ Habilitado

## 🔗 Más Información

- **Errores generales:** `INSTRUCCIONES_COPIA_PROYECTO.md`
- **Google Sign-In falla:** `SOLUCION_GOOGLE_SIGNIN.md` ⚠️
- **Documentación completa:** `README.md`

---

**¿Sigue sin funcionar?** Revisa:
1. Versión de Android Studio (actualizada)
2. JDK instalado (Java 11+)
3. Android SDK instalado correctamente
4. Conexión a internet (para descargar dependencias)

