package robin.pe.turistea;

/**
 * Configuración centralizada del proyecto
 * Cambia las URLs según tu entorno

 * URL BASE del backend
 *
 * CAMBIAR SEGÚN TU ENTORNO:
 *
 * - Para EMULADOR: "http://10.0.2.2:4001"
 * - Para CELULAR REAL: "http://TU_IP_LOCAL:4001"
 *   Ejemplo: "http://192.168.100.9:4001"
 *
 * COMO OBTENER TU IP LOCAL:
 * Windows: ipconfig (buscar "IPv4")
 */
public class Config {

    /**
     * URL BASE del backend
     *
     * CAMBIAR SEGÚN TU ENTORNO:
     * - Emulador Android: "http://10.0.2.2:4001"
     * - Celular real: "http://TU_IP_LOCAL:4001"
     *
     * Ejemplo IP local:
     * http://192.168.100.9:4001
     */
    public static final String BASE_URL = "http://192.168.100.9:4001";

    // Endpoints simples
    public static final String LOGIN_URL = BASE_URL + "/api/signin";
    public static final String REGISTER_URL = BASE_URL + "/api/signup";
    public static final String VERIFY_CODE_URL = BASE_URL + "/api/active/verifycode";
    public static final String USER_ACCOUNT_URL = BASE_URL + "/api/user-account";
    public static final String PACKAGES_URL = BASE_URL + "/api/user-account/packages";
    public static final String GOOGLE_SIGNIN_URL = BASE_URL + "/api/signin-google";
    public static final String SOCIAL_REGISTER_URL = BASE_URL + "/api/singup-social-network-user";
    public static final String CHANGE_PASSWORD_URL = BASE_URL + "/api/user-account/updatepassword";

    // Form reserves
    public static final String FORM_RESERVE_URL = BASE_URL + "/api/user-account/form_reserves";  // POST
    public static final String FORM_RESERVES_LIST_URL = BASE_URL + "/api/user-account/form_reserves"; // GET

    /**
     * Rutas dinámicas
     */
    public static String ROUTER_PACKAGES_URL(int id) {
        return BASE_URL + "/api/user-account/router-packages/" + id;
    }

    /**
     * Detecta si está ejecutando en emulador o dispositivo real
     */
    public static String getBaseUrl() {
        boolean isEmulator =
                android.os.Build.FINGERPRINT.startsWith("generic")
                        || android.os.Build.FINGERPRINT.toLowerCase().contains("vbox")
                        || android.os.Build.FINGERPRINT.toLowerCase().contains("test-keys")
                        || android.os.Build.MODEL.contains("google_sdk")
                        || android.os.Build.MODEL.contains("Emulator")
                        || android.os.Build.MODEL.contains("Android SDK built for x86")
                        || android.os.Build.MANUFACTURER.contains("Genymotion")
                        || android.os.Build.HARDWARE.contains("ranchu");

        // Emulador Android usa 10.0.2.2 automáticamente
        if (isEmulator) {
            return "http://10.0.2.2:4001";
        }

        // Celular real usa la IP configurada arriba
        return BASE_URL;
    }
}