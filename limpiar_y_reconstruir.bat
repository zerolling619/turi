@echo off
echo ========================================
echo   Limpieza y Reconstruccion de Proyecto
echo   Turistea - Android Studio
echo ========================================
echo.

echo [1/4] Limpiando archivos de compilacion...
call gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: Fallo al limpiar el proyecto
    pause
    exit /b 1
)

echo.
echo [2/4] Limpiando cache de Gradle...
call gradlew clean --no-daemon
if %errorlevel% neq 0 (
    echo ADVERTENCIA: Cache de Gradle no se limpio correctamente
)

echo.
echo [3/4] Descargando y sincronizando dependencias...
call gradlew build --refresh-dependencies
if %errorlevel% neq 0 (
    echo ERROR: Fallo al construir el proyecto
    pause
    exit /b 1
)

echo.
echo [4/4] Construyendo proyecto...
call gradlew build
if %errorlevel% neq 0 (
    echo ERROR: Fallo al construir el proyecto
    pause
    exit /b 1
)

echo.
echo ========================================
echo   ¡COMPLETADO EXITOSAMENTE!
echo ========================================
echo.
echo El proyecto ha sido limpiado y reconstruido.
echo Ahora puedes abrirlo en Android Studio.
echo.
pause

