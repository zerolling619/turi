# 📱 Cómo Instalar la App en Tu Celular

## 🎯 Opción 1: Instalación Directa desde Android Studio (Recomendada)

### Paso 1: Preparar Tu Celular

1. **Habilitar Opciones de Desarrollador:**
   - Ir a: Configuración → Acerca del teléfono
   - Tocar 7 veces en "Número de compilación" o "Versión del sistema"
   - Verás el mensaje: "¡Eres un desarrollador!"

2. **Activar Depuración USB:**
   - Ir a: Configuración → Opciones de desarrollador
   - Activar "Depuración USB"
   - Activar "Instalar vía USB" (opcional)

3. **Conectar el Celular:**
   - Conectar el celular a la PC con un cable USB
   - En el celular, aceptar el permiso "¿Permitir depuración USB?"
   - Marcar "Permitir siempre desde este equipo" (opcional)

### Paso 2: Verificar Conexión

En Android Studio:
```
1. Abre: Tools → Device Manager
2. O mira en la barra superior, deberías ver tu dispositivo listado
```

Si no aparece:
- Verificar que el cable USB funcione (debe soportar transferencia de datos)
- Verificar que los drivers USB estén instalados
- Cerrar y volver a abrir Android Studio
- Desconectar y reconectar el cable

### Paso 3: Ejecutar la App

```
1. En Android Studio, selecciona tu dispositivo en el dropdown de arriba
2. Clic en el botón ▶️ Run (o Shift + F10)
3. ¡La app se instalará y ejecutará en tu celular!
```

**✅ IMPORTANTE:** Una vez instalada, la app funciona **100% independiente**:
   - ✅ Puedes desconectar el cable USB
   - ✅ La app sigue funcionando normalmente
   - ✅ Solo necesitas reconectar si quieres actualizar o ver logs
   - ✅ Funciona igual que cualquier otra app del celular

## 🔧 Opción 2: Generar APK para Instalar Manualmente

### Crear APK de Debug (Para Pruebas)

1. **Build APK:**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

2. **Ubicación del APK:**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Instalar en el Celular:**
   - Enviar el APK al celular (por USB, email, Drive, etc.)
   - Abrir el archivo APK en el celular
   - Permitir "Instalar apps de fuentes desconocidas" si lo pide
   - Instalar

**✅ IMPORTANTE:** Una vez instalada la APK, la app funcionará **completamente independiente**:
   - ✅ Funciona sin cable USB
   - ✅ Funciona sin internet (para lo que no necesite conexión)
   - ✅ Funciona al reiniciar el celular
   - ✅ Se queda instalada permanentemente
   - ✅ Solo usarás el cable USB si quieres actualizar la app o ver logs en tiempo real

### Crear APK de Release (Para Distribución)

1. **Generar Keystore (Primera Vez):**
   ```
   Build → Generate Signed Bundle / APK → APK
   
   - Create new → Crear un keystore
   - Guardar en una ubicación segura
   - Guardar la contraseña
   ```

2. **Generar APK Firmado:**
   ```
   Build → Generate Signed Bundle / APK → APK
   - Seleccionar keystore existente
   - Ingresar contraseña
   - Build variant: release
   - Finish
   ```

3. **Ubicación del APK:**
   ```
   app/release/app-release.apk
   ```

## 📊 Comparación: ¿Cuándo Usar Cada Opción?

| Característica | Instalación USB (Android Studio) | APK Manual |
|---------------|----------------------------------|------------|
| **Funciona sin cable** | ✅ Sí, después de instalar | ✅ Sí, desde el inicio |
| **Actualización rápida** | ✅ Clase Run y listo | ❌ Debes regenerar APK |
| **Ver logs en tiempo real** | ✅ Sí, conectado | ❌ No disponible |
| **Debug/Depuración** | ✅ Todos los recursos | ⚠️ Limitado |
| **Compartir con otros** | ❌ No práctico | ✅ Sí, enviar APK |
| **Uso normal diario** | ✅ Sí, funciona solo | ✅ Sí, funciona solo |
| **Tamaño APK** | N/A | ⚠️ ~20-30 MB |

**💡 Recomendación:**
- **Desarrollo/Pruebas:** Usa instalación USB desde Android Studio
- **Compartir con usuarios:** Genera APK de Release
- **Uso personal:** Ambos funcionan igual, elige según prefieras

## ⚠️ Problemas Comunes y Soluciones

### El Celular No Aparece en Android Studio

**Problema:** Android Studio no detecta el dispositivo

**Soluciones:**
1. **Verificar Drivers USB (Windows):**
   - Instalar Google USB Driver desde Android Studio: Tools → SDK Manager → SDK Tools
   - O instalar drivers del fabricante (Samsung, Xiaomi, etc.)

2. **Verificar PTP/MTP:**
   - En el celular: Notificación de USB → Seleccionar "PTP" o "Transferencia de archivos"

3. **Reiniciar ADB:**
   ```
   En Android Studio: Tools → Device Manager
   O desde terminal:
   adb kill-server
   adb start-server
   ```

### Error: "App not installed"

**Problema:** El APK no se puede instalar

**Soluciones:**
1. **Desinstalar versión anterior:**
   - Desinstalar la app si ya existe
   - Instalar la nueva versión

2. **Verificar arquitectura:**
   - Verificar que el APK sea compatible (arm64-v8a, armeabi-v7a, x86)

3. **Espacio disponible:**
   - Verificar que haya suficiente espacio en el almacenamiento

### Error: "Installation failed with message failed to finalize session"

**Problema:** Error durante la instalación

**Solución:**
```
1. Desconectar y reconectar el celular
2. Reiniciar Android Studio
3. Limpiar proyecto: Build → Clean Project
4. Reconstruir: Build → Rebuild Project
```

## 📋 Comandos Útiles desde Terminal

### Ver Dispositivos Conectados
```bash
adb devices
```

### Instalar APK Manualmente
```bash
# Desde la PC
adb install app-debug.apk

# Forzar reinstalación
adb install -r app-debug.apk
```

### Ver Logs de la App (Útil para Debug)
```bash
# Ver todos los logs
adb logcat

# Ver solo logs de Turistea
adb logcat | grep Turistea
```

### Desinstalar App
```bash
adb uninstall robin.pe.turistea
```

### Limpiar Datos de la App
```bash
adb shell pm clear robin.pe.turistea
```

## 🔐 Configuración Especial para Google Sign-In

**IMPORTANTE:** Para que Google Sign-In funcione en tu celular:

1. **Obtener SHA-1 del Keystore de Debug:**
   ```bash
   # Windows
   keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android

   # Linux/Mac
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```

2. **Agregar SHA-1 a Firebase:**
   - Ve a Firebase Console → tu proyecto → Settings
   - En "Your apps" → Android app
   - "Add fingerprint"
   - Pega el SHA-1 obtenido

3. **Descargar google-services.json actualizado:**
   - Reemplazar en `app/google-services.json`

4. **Si instalas APK de Release:**
   - Necesitarás agregar el SHA-1 del keystore de release también

## 🌐 Usar la App con tu Backend

### Si usas Emulador:
```
URL: http://10.0.2.2:4001
```

### Si usas Celular Real:
Necesitas usar la IP de tu PC:

**Obtener IP de tu PC:**
```bash
# Windows
ipconfig
# Busca: Dirección IPv4: 192.168.x.x (o similar)

# Linux/Mac
ifconfig
# Busca: inet 192.168.x.x (o similar)
```

**Cambiar URL en el código:**
- Buscar: `http://10.0.2.2:4001`
- Reemplazar con: `http://TU_IP:4001`
- Por ejemplo: `http://192.168.1.100:4001`

**Asegúrate de:**
- Celular y PC estén en la misma red WiFi
- Backend esté corriendo en tu PC
- Firewall permita conexiones desde el celular

## 📱 Probar en Diferentes Celulares

Si quieres instalar en múltiples celulares:

1. **Cada celular necesita:**
   - Conectar por USB (con depuración habilitada)
   - Ejecutar desde Android Studio

2. **O generar APK:**
   - Una vez generado, puedes compartirlo con otros celulares
   - Ver "Opción 2: Generar APK" arriba

## ✅ Checklist de Instalación

Antes de instalar:
```
☐ Opciones de desarrollador habilitadas
☐ Depuración USB activada
☐ Celular conectado por USB
☐ Android Studio detecta el dispositivo
☐ Proyecto sincronizado sin errores
```

Para Google Sign-In:
```
☐ SHA-1 agregado a Firebase Console
☐ google-services.json actualizado
☐ Sincronizado proyecto en Android Studio
```

## 🎉 ¡Listo!

Una vez instalada la app en tu celular:
- Puedes probar todas las funcionalidades
- Los cambios en el código se actualizan al hacer "Run" de nuevo
- Los logs aparecen en Android Studio

**Tip:** Mantén el cable USB conectado para ver logs en tiempo real y hacer debugging más fácil.

---

## 📞 Más Ayuda

- **Errores al copiar el proyecto:** `SOLUCION_RAPIDA.md`
- **Google Sign-In no funciona:** `SOLUCION_GOOGLE_SIGNIN.md`
- **Documentación general:** `README.md`
