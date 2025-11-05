@echo off
chcp 65001 > nul
cls
echo ============================================
echo   OBTENER SHA-1 PARA FIREBASE
echo ============================================
echo.

echo Buscando SHA-1 del keystore de debug...
echo.

keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android 2>nul | findstr "SHA1"

if %errorlevel% neq 0 (
    echo.
    echo ⚠️  ERROR: No se pudo obtener el SHA-1
    echo.
    echo Posibles causas:
    echo   1. Java/JDK no está instalado
    echo   2. Keystore de debug no existe
    echo   3. Ruta incorrecta
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================
echo   INSTRUCCIONES
echo ============================================
echo.
echo 1. Copia el SHA-1 de arriba (formato: XX:XX:XX:XX...)
echo.
echo 2. Ve a Firebase Console:
echo    https://console.firebase.google.com/project/turistea-397b4/settings/general/android:robin.pe.turistea
echo.
echo 3. Clic en "Add fingerprint"
echo    Pega el SHA-1
echo    Guarda
echo.
echo 4. Descarga el nuevo google-services.json
echo.
echo 5. Reemplaza en: app/google-services.json
echo.
echo ============================================
echo.
pause


