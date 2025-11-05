package robin.pe.turistea.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * EJEMPLO: Cómo convertir HttpURLConnection + AsyncTask a Volley
 * 
 * Este archivo muestra cómo convertir el LoginTask de Login.java
 * a usar Volley en lugar de HttpURLConnection + AsyncTask
 */
public class EjemploVolleyLogin {

    /**
     * MÉTODO 1: Usando JsonObjectRequest (recomendado para JSON)
     * 
     * Ventajas de Volley:
     * - Más simple y legible
     * - Manejo automático de threads (no necesitas AsyncTask)
     * - Caché automático de respuestas
     * - Cancelación fácil de peticiones
     * - Retry automático
     */
    public static void loginWithVolleyJson(Context context, String email, String password, 
                                          Response.Listener<JSONObject> successListener,
                                          Response.ErrorListener errorListener) {
        
        String url = "http://10.0.2.2:4001/api/signin";
        
        // Crear el objeto JSON con los parámetros
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("email", email);
            jsonParam.put("password", password);
            
            Log.d("VolleyLogin", "Iniciando petición a: " + url);
            Log.d("VolleyLogin", "Enviando datos: " + jsonParam.toString());
            
            // Crear la petición JSON con Volley
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParam,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyLogin", "Respuesta recibida: " + response.toString());
                        // Procesar respuesta exitosa
                        successListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Error de conexión";
                        if (error.networkResponse != null) {
                            errorMsg = "Error: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e("VolleyLogin", "Error response: " + responseBody);
                            } catch (Exception e) {
                                Log.e("VolleyLogin", "Error al leer respuesta de error", e);
                            }
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e("VolleyLogin", errorMsg, error);
                        errorListener.onErrorResponse(error);
                    }
                }
            ) {
                // Opcional: Agregar headers personalizados
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };
            
            // Agregar la petición a la cola usando el singleton
            VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);
            
        } catch (Exception e) {
            Log.e("VolleyLogin", "Error al crear petición: " + e.getMessage(), e);
            Toast.makeText(context, "Error al crear petición", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * MÉTODO 2: Usando StringRequest (para respuestas de texto plano o cuando quieres más control)
     */
    public static void loginWithVolleyString(Context context, String email, String password,
                                           Response.Listener<String> successListener,
                                           Response.ErrorListener errorListener) {
        
        String url = "http://10.0.2.2:4001/api/signin";
        
        // Crear el JSON como string
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("email", email);
            jsonParam.put("password", password);
        } catch (Exception e) {
            Log.e("VolleyLogin", "Error al crear JSON: " + e.getMessage(), e);
            return;
        }
        
        StringRequest stringRequest = new StringRequest(
            Request.Method.POST,
            url,
            successListener,
            errorListener
        ) {
            @Override
            public byte[] getBody() {
                try {
                    return jsonParam.toString().getBytes("UTF-8");
                } catch (Exception e) {
                    Log.e("VolleyLogin", "Error al convertir a bytes", e);
                    return null;
                }
            }
            
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        
        VolleySingleton.getInstance(context).getRequestQueue().add(stringRequest);
    }

    /**
     * MÉTODO 3: Con autenticación JWT (ejemplo para peticiones GET)
     */
    public static void getWithJWT(Context context, String url,
                                 Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            successListener,
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                
                // Agregar JWT desde SharedPreferences
                android.content.SharedPreferences prefs = 
                    context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                
                if (!jwt.isEmpty()) {
                    headers.put("Authorization", "Bearer " + jwt);
                }
                
                return headers;
            }
        };
        
        VolleySingleton.getInstance(context).getRequestQueue().add(jsonObjectRequest);
    }
}


