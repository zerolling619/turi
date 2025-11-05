#!/bin/bash

echo "========================================"
echo "  Limpieza y Reconstruccion de Proyecto"
echo "  Turistea - Android Studio"
echo "========================================"
echo ""

echo "[1/4] Limpiando archivos de compilacion..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo al limpiar el proyecto"
    exit 1
fi

echo ""
echo "[2/4] Limpiando cache de Gradle..."
./gradlew clean --no-daemon
if [ $? -ne 0 ]; then
    echo "ADVERTENCIA: Cache de Gradle no se limpio correctamente"
fi

echo ""
echo "[3/4] Descargando y sincronizando dependencias..."
./gradlew build --refresh-dependencies
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo al construir el proyecto"
    exit 1
fi

echo ""
echo "[4/4] Construyendo proyecto..."
./gradlew build
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo al construir el proyecto"
    exit 1
fi

echo ""
echo "========================================"
echo "  ¡COMPLETADO EXITOSAMENTE!"
echo "========================================"
echo ""
echo "El proyecto ha sido limpiado y reconstruido."
echo "Ahora puedes abrirlo en Android Studio."
echo ""

