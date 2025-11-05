# 🔐 SHA-1 para Múltiples PCs

## 🎯 La Pregunta Clave

**¿Debo agregar el SHA-1 de cada PC manualmente?**

Respuesta corta: **Sí, pero hay formas de hacerlo más fácil.**

## ⚙️ Opción 1: Agregar Todos los SHA-1 de Una Vez (Recomendada)

### Para un Equipo de Desarrollo

**Paso 1:** Obtener SHA-1 de todas las PCs del equipo:

```bash
# En cada PC, ejecutar:
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Paso 2:** Agregar TODOS los SHA-1 a Firebase:

1. Ir a Firebase Console → tu proyecto
2. Settings → Configuración del proyecto
3. En "Your apps" → Android app
4. Agregar **cada SHA-1** (uno por uno o todos)
5. Descargar nuevo `google-services.json`

**Ventaja:**
- ✅ Una vez hecho, todos pueden desarrollar
- ✅ No necesitas volver a tocar Firebase
- ✅ Funciona para todo el equipo

## ⚙️ Opción 2: Keystore Compartido (No Recomendada)

Crear un keystore de debug compartido para todo el equipo.

**NO recomendado porque:**
- ❌ Compartir archivos de seguridad
- ❌ Problemas si se pierde la contraseña
- ❌ Posibles conflictos
- ❌ Diferente de las prácticas de Android

## ⚙️ Opción 3: Script Automatizado

Crear un script que obtenga el SHA-1 y lo copie al portapapeles:

### Windows (`obtener-sha1.bat`):

```batch
@echo off
echo Obteniendo SHA-1...

keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"

echo.
echo Copia el SHA-1 de arriba (formato: XX:XX:XX:XX...)
echo Presiona cualquier tecla para salir...
pause > nul
```

### Linux/Mac (`obtener-sha1.sh`):

```bash
#!/bin/bash
echo "Obteniendo SHA-1..."
echo ""

keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

echo ""
echo "Copia el SHA-1 de arriba (formato: XX:XX:XX:XX...)"
```

**Uso:** Doble clic para obtener SHA-1 rápidamente.

## ⚙️ Opción 4: Configuración de Build Variants

Puedes usar `build.gradle.kts` para tener diferentes configuraciones:

```kotlin
android {
    buildTypes {
        debug {
            // No agregar SHA-1 de cada PC
        }
        release {
            // SHA-1 del keystore de producción
        }
    }
}
```

Pero esto **NO soluciona el problema de Firebase** - Firebase necesita conocer todos los SHA-1.

## 📊 Comparación de Opciones

| Opción | Ventajas | Desventajas | Recomendación |
|--------|----------|-------------|---------------|
| **Agregar todos a Firebase** | ✅ Una vez hecho, listo<br>✅ Equipo puede trabajar<br>✅ Seguro | ⚠️ Tiempo inicial | ⭐⭐⭐⭐⭐ **Mejor** |
| **Keystore compartido** | ✅ Un solo SHA-1 | ❌ Inseguro<br>❌ Conflictos<br>❌ Malas prácticas | ⭐ No recomendado |
| **Script automatizado** | ✅ Rápido obtener SHA-1 | ⚠️ Aún necesitas agregarlo | ⭐⭐⭐⭐ Bueno |
| **Build variants** | ✅ Organizado | ❌ No soluciona Firebase | ⭐⭐⭐ Medio |

## 🎯 Solución Recomendada: Proceso Simplificado

### Para un Equipo (Primera Vez)

```
1. Crear documento compartido (Excel/Google Sheets)
2. Columnas: Nombre, PC, SHA-1
3. Cada desarrollador completa su fila
4. Una persona agrega todos los SHA-1 a Firebase
5. Compartir el nuevo google-services.json con el equipo
```

### Para Futuros Desarrolladores

```
1. Nuevo desarrollador obtiene su SHA-1
2. Comparte el SHA-1 en el documento
3. Administrador lo agrega a Firebase
4. Descarga nuevo google-services.json
5. Comparte con el equipo
6. Todos actualizan el archivo
```

## 🔄 Automatizar el Proceso

Crear un script que:

1. Obtenga el SHA-1
2. Lo copie al portapapeles
3. Abra la consola de Firebase

### Script Avanzado (`firebase-sha1.ps1` para Windows):

```powershell
# Obtener SHA-1
$sha1 = keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android 2>$null | Select-String "SHA1:" | ForEach-Object { ($_ -split "SHA1: ")[1].Trim() }

# Copiar al portapapeles
$sha1 | Clip

Write-Host "SHA-1 copiado al portapapeles:" -ForegroundColor Green
Write-Host $sha1 -ForegroundColor Yellow
Write-Host "`nVe a Firebase Console para agregarlo:"
Write-Host "https://console.firebase.google.com/project/turistea-397b4/settings/general" -ForegroundColor Cyan

# Abrir navegador (opcional)
Start-Process "https://console.firebase.google.com/project/turistea-397b4/settings/general/android:robin.pe.turistea"
```

## 🏭 Para Producción (Release)

Una vez que la app esté lista para producción:

```kotlin
// En app/build.gradle.kts
signingConfigs {
    create("release") {
        storeFile = file("tu-keystore-release.jks")
        storePassword = "tu_password"
        keyAlias = "tu_alias"
        keyPassword = "tu_password"
    }
}

buildTypes {
    getByName("release") {
        isMinifyEnabled = true
        signingConfig = signingConfigs.getByName("release")
    }
}
```

**SHA-1 de Producción:**
```bash
keytool -list -v -keystore tu-keystore-release.jks -alias tu_alias
```

**Agregar a Firebase:** Solo una vez, y funciona para siempre en producción.

## 🚫 Por Qué No Se Puede Evitar

**Firebase/Google necesita:**
- Validar que la app que intenta usar Google Sign-In sea legítima
- El SHA-1 es como una "huella digital" única
- Cada keystore (debug/release) tiene su propio SHA-1
- Por seguridad, debe aprobarse manualmente

**Es similar a:**
- Validar un dominio en hosting
- Verificar propiedad de una cuenta
- Activar 2FA

## ✅ Recomendación Final

**Para Desarrollo:**

1. ⭐ **Agregar todos los SHA-1 del equipo a Firebase una vez**
2. ⭐ **Usar un script para obtener SHA-1 fácilmente**
3. ⭐ **Compartir google-services.json actualizado**

**Para Producción:**

1. ⭐ **Generar keystore de release**
2. ⭐ **Agregar SHA-1 de release a Firebase**
3. ⭐ **Listo para siempre**

**Tiempo estimado:**
- Setup inicial del equipo: 15-30 minutos
- Para cada nuevo desarrollador: 2 minutos

## 📝 Checklist

```
☐ Todos los SHA-1 del equipo obtenidos
☐ Todos agregados a Firebase
☐ google-services.json descargado y actualizado
☐ Compartido con el equipo
☐ Script de obtención de SHA-1 creado
☐ Proceso documentado
☐ keystore de release configurado
```

## 🔗 Referencias

- **Firebase Console:** https://console.firebase.google.com/
- **Tu proyecto:** turistea-397b4
- **Documentación:** `SOLUCION_GOOGLE_SIGNIN.md`

---

**Conclusión:** Agregar SHA-1s una vez al inicio es el mejor enfoque. Los scripts lo hacen más fácil. No hay forma de evitarlo completamente, pero puedes optimizar el proceso. ⚡


