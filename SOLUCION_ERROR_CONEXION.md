# 🚨 Solución: Error "Failed to connect to /10.0.2.2:4001"

## ⚠️ El Problema

```
E  Excepción: Failed to connect to /10.0.2.2:4001
java.net.ConnectException: Failed to connect to /10.0.2.2:4001
```

**Significa:** La app no puede conectarse al backend.

## 🔍 ¿Por Qué Pasa Esto?

La URL `10.0.2.2` es una **IP especial** que **SOLO funciona en emuladores** para conectarse a `localhost` de tu PC.

### ❌ Si Estás Usando un **CELULAR REAL**:
- `10.0.2.2` **NO funcionará**
- Necesitas usar la **IP real de tu PC** en la red WiFi
--192.168.100.9

### ❌ Si Estás Usando un **EMULADOR**:
- `10.0.2.2` debería funcionar
- Pero el backend puede no estar corriendo en tu PC

## ✅ Solución Según Tu Caso

### Caso 1: Usas CELULAR REAL (Más Común) 📱

**Problema:** `10.0.2.2` no funciona en celulares reales.

**Solución:**

#### Paso 1: Obtener la IP de tu PC

**Windows:**
```cmd
ipconfig
```
Busca: `Dirección IPv4: 192.168.x.x` (por ejemplo: `192.168.1.100`)

**Linux/Mac:**
```bash
ifconfig
```
Busca: `inet 192.168.x.x` (por ejemplo: `192.168.1.100`)

#### Paso 2: Cambiar la URL en el Código

Busca todos los archivos con esta URL:
```bash
# En Android Studio: Ctrl + Shift + F
# Buscar: 10.0.2.2:4001
```

**Archivos a modificar:**
- `Login.java`
- `Register.java`  
- `Inicio.java`
- `Profile.java`
- Cualquier otro archivo que use el backend

**Cambiar de:**
```java
http://10.0.2.2:4001
```

**A:**
```java
http://TU_IP:4001
```

**Ejemplo:**
```java
// Si tu IP es 192.168.1.100
http://192.168.1.100:4001
```

#### Paso 3: Verificar Requisitos

✅ **Celular y PC en la misma red WiFi:**
   - Celular conectado a WiFi
   - PC conectado al mismo WiFi

✅ **Backend corriendo en tu PC:**
   - Verifica que el servidor esté funcionando
   - Puedes probarlo desde el navegador: `http://localhost:4001`

✅ **Firewall permite conexiones:**
   - Windows puede bloquear conexiones entrantes
   - Ver "Permitir Firewall" abajo

### Caso 2: Usas EMULADOR 🖥️

**Problema:** El backend no está corriendo o hay un error de configuración.

**Soluciones:**

✅ **Verificar que el backend esté corriendo:**
```bash
# En tu PC, desde el navegador
http://localhost:4001
# Debe mostrar algo o responder
```

✅ **Verificar el puerto:**
   - Asegúrate de que el backend corra en el puerto `4001`
   - Si usa otro puerto (ej: 3000), cambia el código

✅ **Reiniciar el backend:**
   - Detener el servidor
   - Iniciarlo nuevamente
   - Verificar que no haya errores

✅ **Verificar que no haya otro proceso usando el puerto:**
```bash
# Windows
netstat -ano | findstr :4001

# Linux/Mac
lsof -i :4001
```

## 🔥 Permitir Firewall (Windows)

Si usas celular real, Windows Firewall puede bloquear conexiones:

### Opción 1: Permitir Temporalmente

1. **Buscar "Firewall de Windows":**
   - Presiona `Win + R`
   - Escribe: `wf.msc`

2. **Permitir Puerto:**
   - "Reglas de entrada" → "Nueva regla"
   - Tipo: Puerto → TCP → Puerto específico: `4001`
   - Acción: Permitir conexión
   - Aplicar a todos los perfiles
   - Nombre: "Turistea Backend"

### Opción 2: Deshabilitar Temporalmente (Solo para Pruebas)

⚠️ **Solo para desarrollo, NO recomendado para producción**

1. Control Panel → System and Security → Windows Defender Firewall
2. Turn Windows Firewall on or off
3. Desactivar temporalmente (solo redes privadas)

## 🧪 Probar la Conexión

### Desde tu Celular:

**Usar navegador del celular:**
```
http://TU_IP:4001
```

**Deberías ver:**
- Una respuesta del servidor
- O una página de error del backend (pero NO "can't reach")

### Desde tu PC:

**Desde navegador:**
```
http://localhost:4001
```

**Desde terminal:**
```bash
# Windows
curl http://localhost:4001

# Linux/Mac
curl http://localhost:4001
```

## 📚 Volley vs HttpURLConnection

**Pregunta común:** "¿Usar Volley soluciona el problema de conexión?"

**Respuesta:** ❌ NO. Volley NO soluciona el problema de conexión.

Ambas son **librerías HTTP**:
- `HttpURLConnection` = que usas actualmente
- `Volley` = alternativa más moderna

**El problema es la URL/IP, NO la librería:**
- ❌ `http://10.0.2.2:4001` → No funciona en celular real
- ✅ `http://192.168.x.x:4001` → Funciona en celular real

**Tu proyecto ya tiene Volley configurado** en `utils/VolleySingleton.java` y `utils/EjemploVolleyLogin.java`, pero usa `10.0.2.2`.

**Conclusión:** 
- Puedes quedarte con `HttpURLConnection` (ya funciona)
- O migrar a Volley (es más moderno y fácil)
- Pero **SIEMPRE** necesitas cambiar `10.0.2.2` por la IP real de tu PC

## 🔧 Cambiar IP Automáticamente (Solución Inteligente)

Puedes hacer que tu app detecte si está en emulador o celular real:

```java
private String getBaseUrl() {
    // Detectar si es emulador
    boolean isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.toLowerCase().contains("vbox")
            || android.os.Build.FINGERPRINT.toLowerCase().contains("test-keys")
            || android.os.Build.MODEL.contains("google_sdk")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion")
            || android.os.Build.HARDWARE.contains("ranchu");

    if (isEmulator) {
        return "http://10.0.2.2:4001";
    } else {
        // IP de tu PC (actualizar según tu red)
        return "http://192.168.1.100:4001";
    }
}
```

Luego usar `getBaseUrl()` en lugar de la URL hardcodeada.

**Ejemplo con HttpURLConnection:**
```java
String url = getBaseUrl() + "/api/signin";
// Ahora funciona en emulador Y celular real automáticamente
```

**Ejemplo con Volley:**
```java
String url = getBaseUrl() + "/api/signin";
JsonObjectRequest request = new JsonObjectRequest(
    Request.Method.POST,
    url,  // URL dinámica
    jsonParam,
    successListener,
    errorListener
);
```

## 📋 Checklist de Solución

```
☐ Identificar si usas emulador o celular real
☐ Backend corriendo en tu PC
☐ Celular y PC en la misma red WiFi
☐ IP de la PC obtenida correctamente
☐ URLs cambiadas en el código
☐ Firewall permitiendo conexiones
☐ Proyecto recompilado y reinstalado
☐ Probar desde navegador del celular
```

## 🎯 Resumen Rápido

### Si usas CELULAR REAL:
```
1. Obtener IP de tu PC: ipconfig
2. Cambiar en código: 10.0.2.2 → TU_IP
3. Misma red WiFi
4. Backend corriendo
5. Firewall permitido
```

### Si usas EMULADOR:
```
1. Verificar backend corriendo
2. Probar: http://localhost:4001
3. Verificar puerto correcto
4. Reiniciar emulador si es necesario
```

## 🆘 No Funciona Nada

Si después de todo esto aún no funciona:

1. **Verificar servidor:**
   ```bash
   # ¿El backend realmente está corriendo?
   # ¿En qué puerto?
   ```

2. **Cambiar temporalmente a producción:**
   - Si tienes el backend en un servidor de producción
   - Usar esa URL temporalmente

3. **Ver logs del backend:**
   - ¿Llegan las peticiones?
   - ¿Qué error muestra el servidor?

4. **Probar con Postman/curl:**
   - Verificar que el backend responda correctamente
   - Probar los endpoints manualmente

---

**Consejo:** El 99% de los casos se soluciona cambiando `10.0.2.2` por la IP real de tu PC cuando usas celular. ✅

