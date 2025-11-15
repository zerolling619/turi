# 🔐 Solución: Google Sign-In No Funciona en Otra PC

## ⚠️ El Problema

Cuando copias el proyecto a otra PC y Google Sign-In falla, el problema **NO es por dependencias faltantes**. Es porque **cada PC tiene un SHA-1 diferente** y Firebase necesita conocer **TODOS los SHA-1** de las PCs que usarán la app.

## 🔍 ¿Por Qué Funciona en Tu PC pero No en Otra?

Cada instalación de Android Studio genera un **certificado de depuración único** con un SHA-1 diferente. Firebase solo permite Google Sign-In desde dispositivos/PCs cuyos SHA-1 están registrados en la consola de Firebase.

## ✅ Solución PASO a PASO

### Paso 1: Obtener el SHA-1 de la Nueva PC

**Opción A: Desde Android Studio (Más Fácil)**

```
1. Abrir el proyecto en la nueva PC
2. Abrir panel lateral derecho "Gradle"
3. Expandir: Turistea → Tasks → android
4. Doble clic en "signingReport"
5. Copiar el SHA-1 que aparece (formato: XX:XX:XX:XX...)
```

**Opción B: Desde Terminal/CMD**
```
Usa el comando "./gradlew signingReport"
para obtener el Sha-1

```


**Windows:**
```cmd
cd C:\Users\TU_USUARIO\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Linux/Mac:**
```bash
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Paso 2: Registrar el SHA-1 en Firebase

1. **Ir a Firebase Console:**
   ```
   https://console.firebase.google.com/
   ```

2. **Seleccionar el proyecto:**
   ```
   turistea-397b4
   ```

3. **Ir a configuración:**
   ```
   Settings → Configuración del proyecto
   ```

4. **Bajar a la sección "Tus apps":**
   - Buscar la app **Android** (package: `robin.pe.turistea`)
   - Hacer clic en "Agregar huella digital"

5. **Agregar el nuevo SHA-1:**
   - Pegar el SHA-1 copiado
   - Guardar

6. **Descargar el nuevo `google-services.json`:**
   - En la misma sección, clic en "Descargar archivo de configuración"
   - Descargar el nuevo `google-services.json`

### Paso 3: Reemplazar el Archivo en el Proyecto

```
1. Eliminar el archivo actual: app/google-services.json
2. Copiar el nuevo archivo descargado a: app/google-services.json
3. Sincronizar proyecto: File → Sync Project with Gradle Files
4. Limpiar y reconstruir: Build → Clean → Build → Rebuild
```

### Paso 4: Probarlo

```
1. Ejecutar la app
2. Intentar login con Google
3. ¡Debería funcionar!
```

## 📝 Notas Importantes

### ¿Necesito Hacer Esto Para Cada PC?

**Sí**, cada vez que desarrolles en una PC diferente, necesitas:
1. Obtener su SHA-1
2. Agregarlo a Firebase
3. Actualizar `google-services.json`

### Proyecto en Múltiples PCs

Si tienes un equipo de desarrollo, **agrega TODOS los SHA-1** de las PCs del equipo a Firebase. Así todos pueden desarrollar sin problemas.

### Producción (Release)

Para la versión de **producción** necesitarás:
1. Generar un keystore de release
2. Obtener el SHA-1 del keystore de release
3. Agregarlo también a Firebase

```
keytool -list -v -keystore tu-release-key.keystore -alias tu-alias
```

## 🔧 Verificar Configuración Actual

Para verificar si tu `google-services.json` tiene configuraciones de OAuth:

```json
{
  "oauth_client": [
    {
      "client_id": "...",
      "client_type": 1,  // Web client
      "android_info": {...}
    },
    {
      "client_id": "...",
      "client_type": 3  // Android client
    }
  ]
}
```

Si `oauth_client` está vacío `[]`, **necesitas configurar Google Sign-In en Firebase**.

## 🎯 Configurar Google Sign-In en Firebase (Si No Está Configurado)

Si nunca configuraste Google Sign-In en Firebase:

1. **Ir a:**
   ```
   Firebase Console → Authentication → Sign-in method
   ```

2. **Habilitar:**
   - Buscar "Google" en la lista
   - Hacer clic en "Enable"
   - Guardar

3. **Agregar el SHA-1 de tu PC** (como se explicó arriba)

4. **Descargar** `google-services.json` actualizado

## 🚨 Errores Comunes

### Error: "DEVELOPER_ERROR"
**Causa:** SHA-1 no registrado  
**Solución:** Agregar SHA-1 a Firebase como se explicó

### Error: "10" o "Network Error"
**Causa:** Problema de conexión o configuración  
**Solución:**
- Verificar conexión a internet
- Verificar que Google Play Services esté actualizado
- Revisar `google-services.json`

### Error: "Sign in cancelled"
**Causa:** Usuario canceló  
**Solución:** Normal, no es un error

## 📞 Checklist de Verificación

```
☐ SHA-1 obtenido de la nueva PC
☐ SHA-1 agregado a Firebase Console
☐ google-services.json actualizado
☐ Proyecto sincronizado
☐ Clean + Rebuild realizado
☐ Google Sign-In probado y funciona
```

## 🔗 URLs Útiles

- **Firebase Console:** https://console.firebase.google.com/
- **Documentación:** https://firebase.google.com/docs/auth/android/google-signin
- **SHA-1 Generator:** Usar Android Studio (método recomendado arriba)

---

**⚠️ IMPORTANTE:** El archivo `google-services.json` contiene información sensible. Debes:
- ✅ Incluirlo en el repositorio (ya está en el proyecto)
- ❌ NO compartirlo públicamente
- ✅ Regenerarlo si cambia la configuración

---

## 🔄 ¿Múltiples PCs o Equipo de Desarrollo?

**Pregunta:** ¿Debo agregar SHA-1 cada vez para cada PC?

**Respuesta corta:** Sí, pero optimizado.

**Solución:** Ver `SHA1_MULTIPLES_PCs.md` para:
- ✅ Agregar todos los SHA-1 del equipo de una vez
- ✅ Scripts automatizados para obtener SHA-1
- ✅ Proceso optimizado para equipos
- ✅ Configuración de producción

**Script incluido:** `obtener_sha1.bat` (Windows) y `obtener_sha1.sh` (Linux/Mac)

---

¿Tienes problemas siguiendo estos pasos? Lee `SOLUCION_RAPIDA.md` para errores generales del proyecto.

