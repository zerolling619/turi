# Instrucciones para Copiar el Proyecto a Otra PC

## Problema Común

Cuando copias un proyecto Android a otra PC, pueden aparecer errores relacionados con:
- MainActivity
- View Binding
- Archivos generados
- Referencias a rutas locales

## Solución Paso a Paso

### 1. Limpiar el Proyecto (ANTES de Copiarlo)

Antes de compartir o copiar el proyecto, **NO incluyas** estas carpetas/archivos:

```
❌ app/build/                    (se regenera automáticamente)
❌ build/                        (se regenera automáticamente)
❌ .gradle/                      (caché de Gradle)
❌ .idea/caches/                 (caché del IDE)
❌ local.properties              (contiene ruta del SDK de tu PC)
```

Asegúrate de que estos archivos estén en `.gitignore` (ya lo están en este proyecto ✅).

### 2. Instalar en la Nueva PC

Cuando recibas/copies el proyecto en la nueva PC:

#### Paso 2.1: Abrir el Proyecto
```
1. Abre Android Studio
2. File → Open → Selecciona la carpeta del proyecto
3. Espera a que Android Studio sincronice
```

#### Paso 2.2: Si Aparecen Errores - Sincronizar Gradle
```
1. File → Sync Project with Gradle Files
   O presiona: Ctrl + Shift + O (en Windows/Linux)
   O presiona: Cmd + Shift + O (en Mac)
```

#### Paso 2.3: Limpiar y Reconstruir (SI PERSISTEN ERRORES)
```
1. Build → Clean Project
2. Build → Rebuild Project
```

#### Paso 2.4: Invalidar Caché (ÚLTIMA OPCIÓN)
```
1. File → Invalidate Caches / Restart...
2. Marca "Invalidate and Restart"
3. Espera a que Android Studio se reinicie
4. Espera a que sincronice el proyecto nuevamente
```

### 3. Verificar Configuración

#### Google Services
✅ El archivo `app/google-services.json` DEBE estar incluido (ya está en el proyecto)

#### ⚠️ Google Sign-In (IMPORTANTE)
Si usas login con Google, debes configurar el SHA-1 de la nueva PC:
- Ver documentación detallada: **SOLUCION_GOOGLE_SIGNIN.md**
- Resumen rápido:
  1. Obtener SHA-1: Gradle → Tasks → android → signingReport
  2. Agregar SHA-1 a Firebase Console
  3. Descargar nuevo `google-services.json`
  4. Reemplazar en el proyecto

#### Facebook (opcional)
Si usas Facebook Login, actualiza:
```xml
app/src/main/res/values/strings.xml:
- facebook_app_id
- fb_login_protocol_scheme
- facebook_client_token
```

#### Google Maps API Key
El API Key está en `AndroidManifest.xml`. Si cambias de proyecto o quieres usar otro, actualiza:
```xml
app/src/main/AndroidManifest.xml:
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
```

### 4. Comandos Útiles (Consola)

Si prefieres usar la línea de comandos:

```bash
# Windows
cd C:\ruta\al\proyecto\Turistea
gradlew clean
gradlew build

# Linux/Mac
cd /ruta/al/proyecto/Turistea
./gradlew clean
./gradlew build
```

### 5. Verificar que Funciona

Una vez que el proyecto se sincronice correctamente:

1. ✅ No debe haber errores en rojo en `MainActivity.java`
2. ✅ El archivo `app/build/generated/data_binding_base_class_source_out/` debe generarse
3. ✅ Debe compilar sin errores: Build → Make Project

## Problemas Específicos con MainActivity

Si `MainActivity.java` muestra errores relacionados con `ActivityMainBinding`:

**Causa:** View Binding no se ha generado correctamente

**Solución:**
1. Asegúrate que `app/build.gradle.kts` tenga:
```kotlin
buildFeatures {
    viewBinding = true
}
```
✅ Ya está configurado en este proyecto

2. Sincroniza el proyecto: File → Sync Project with Gradle Files

3. Limpia y reconstruye: Build → Clean Project → Build → Rebuild Project

## Archivos Importantes que SÍ deben Copiarse

✅ `app/src/` - Todo el código fuente
✅ `app/res/` - Todos los recursos (layouts, drawables, values, etc.)
✅ `gradle/` - Configuración de Gradle
✅ `app/build.gradle.kts` - Configuración del módulo
✅ `build.gradle.kts` - Configuración del proyecto
✅ `settings.gradle.kts` - Configuración de módulos
✅ `gradle.properties` - Propiedades de Gradle
✅ `gradlew` y `gradlew.bat` - Wrapper de Gradle
✅ `app/google-services.json` - Configuración de Firebase
✅ `app/proguard-rules.pro` - Reglas de ProGuard

## Verificación Final

Antes de considerar que todo funciona:

```bash
✅ El proyecto sincroniza sin errores
✅ Build → Make Project completa sin errores
✅ La app se puede ejecutar en un emulador/dispositivo
✅ Las vistas se muestran correctamente
✅ La navegación funciona
```

## Contacto y Soporte

Si después de seguir estos pasos aún tienes problemas, verifica:

1. Versión de Android Studio (recomendado: versiones recientes)
2. Versión de JDK (requerido: Java 11)
3. SDK de Android instalado correctamente
4. Conectividad a internet (para descargar dependencias)

---

**Nota:** Este documento se actualiza automáticamente cuando se hacen cambios en la configuración del proyecto.

