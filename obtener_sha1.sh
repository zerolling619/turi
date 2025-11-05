#!/bin/bash

# Configurar colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

clear
echo "============================================"
echo "   OBTENER SHA-1 PARA FIREBASE"
echo "============================================"
echo ""

echo "Buscando SHA-1 del keystore de debug..."
echo ""

SHA1=$(keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep -i "SHA1:" | sed 's/.*SHA1: //')

if [ -z "$SHA1" ]; then
    echo ""
    echo "⚠️  ERROR: No se pudo obtener el SHA-1"
    echo ""
    echo "Posibles causas:"
    echo "  1. Java/JDK no está instalado"
    echo "  2. Keystore de debug no existe"
    echo "  3. Ruta incorrecta"
    echo ""
    exit 1
fi

echo -e "${GREEN}$SHA1${NC}"
echo ""
echo "============================================"
echo "   INSTRUCCIONES"
echo "============================================"
echo ""
echo "1. Copia el SHA-1 de arriba"
echo ""
echo "2. Ve a Firebase Console:"
echo -e "${BLUE}https://console.firebase.google.com/project/turistea-397b4/settings/general/android:robin.pe.turistea${NC}"
echo ""
echo "3. Clic en \"Add fingerprint\""
echo "   Pega el SHA-1"
echo "   Guarda"
echo ""
echo "4. Descarga el nuevo google-services.json"
echo ""
echo "5. Reemplaza en: app/google-services.json"
echo ""
echo "============================================"
echo ""

# Intentar copiar al portapapeles (si está disponible)
if command -v xclip &> /dev/null; then
    echo "$SHA1" | xclip -selection clipboard
    echo -e "${GREEN}✓ SHA-1 copiado al portapapeles${NC}"
    echo ""
elif command -v pbcopy &> /dev/null; then
    echo "$SHA1" | pbcopy
    echo -e "${GREEN}✓ SHA-1 copiado al portapapeles${NC}"
    echo ""
fi


