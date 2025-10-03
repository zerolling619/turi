package robin.pe.turistea.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

import robin.pe.turistea.R;

public class Register extends Fragment {

    private EditText edtNombres, edtApellidos, edtFechNaci, edtDni, edtCelular, edtSexo, edtCorreo, edtPasswordd, edtConfirPasswordd;
    private TextInputLayout tilNombres, tilApellidos, tilFechNaci, tilDni, tilCelular, tilSexo, tilCorreo, tilPasswordd, tilConfirPasswordd;
    private Context context;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Inicializar campos
        edtNombres = view.findViewById(R.id.edtNombres);
        edtApellidos = view.findViewById(R.id.edtApellidos);
        edtFechNaci = view.findViewById(R.id.edtFechNaci);
        edtDni = view.findViewById(R.id.edtDni);
        edtCelular = view.findViewById(R.id.edtCelular);
        edtSexo = view.findViewById(R.id.edtSexo);
        edtCorreo = view.findViewById(R.id.edtCorreo);
        edtPasswordd = view.findViewById(R.id.edtPasswordd);
        edtConfirPasswordd = view.findViewById(R.id.edtConfirPasswordd);

        tilNombres = view.findViewById(R.id.tilNombres);
        tilApellidos = view.findViewById(R.id.tilApellidos);
        tilFechNaci = view.findViewById(R.id.tilFechNaci);
        tilDni = view.findViewById(R.id.tilDni);
        tilCelular = view.findViewById(R.id.tilCelular);
        tilSexo = view.findViewById(R.id.tilSexo);
        tilCorreo = view.findViewById(R.id.tilCorreo);
        tilPasswordd = view.findViewById(R.id.tilPasswordd);
        tilConfirPasswordd = view.findViewById(R.id.tilConfirPasswordd);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        navController = Navigation.findNavController(view);

        // Configurar selector de fecha (solo mayores de 18 años)
        edtFechNaci.setFocusable(false);
        edtFechNaci.setKeyListener(null);
        edtFechNaci.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int currentYear = c.get(Calendar.YEAR);
            int currentMonth = c.get(Calendar.MONTH);
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            
            // Calcular año máximo (18 años atrás)
            int maxYear = currentYear - 18;
            
            // Si es el día de cumpleaños, permitir el año actual - 18
            // Si no, usar el año anterior
            int year = maxYear;
            int month = currentMonth;
            int day = currentDay;

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year1, month1, dayOfMonth) -> {
                String fecha = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                edtFechNaci.setText(fecha);
            }, year, month, day);
            
            // Establecer fecha máxima (hoy - 18 años)
            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            
            // Establecer fecha mínima (hace 100 años)
            Calendar minDate = Calendar.getInstance();
            minDate.set(currentYear - 100, 0, 1);
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            
            datePickerDialog.show();
        });

        // Filtro para solo números
        InputFilter soloNumeros = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        edtDni.setFilters(new InputFilter[]{soloNumeros, new InputFilter.LengthFilter(8)});
        edtCelular.setFilters(new InputFilter[]{soloNumeros, new InputFilter.LengthFilter(9)});
        edtDni.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtCelular.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        // Configurar selector de sexo
        edtSexo.setFocusable(false);
        edtSexo.setKeyListener(null);
        edtSexo.setOnClickListener(v -> {
            String[] opciones = {"Hombre", "Mujer", "No especificado"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Seleccionar sexo");
            builder.setItems(opciones, (dialog, which) -> {
                edtSexo.setText(opciones[which].toLowerCase());
            });
            builder.show();
        });

        // Botón registrar
        view.findViewById(R.id.btnRegistrar).setOnClickListener(v -> btnRegister());

        // Botón iniciar sesión
        view.findViewById(R.id.tvIniciarSesion).setOnClickListener(v -> navController.navigate(R.id.navigation_login));
    }

    private void btnRegister() {
        // Limpiar errores
        tilNombres.setError(null);
        tilApellidos.setError(null);
        tilFechNaci.setError(null);
        tilDni.setError(null);
        tilCelular.setError(null);
        tilSexo.setError(null);
        tilCorreo.setError(null);
        tilPasswordd.setError(null);
        tilConfirPasswordd.setError(null);

        // Obtener valores
        String nombres = edtNombres.getText().toString().trim();
        String apellidos = edtApellidos.getText().toString().trim();
        String fechaNaci = edtFechNaci.getText().toString().trim();
        String dni = edtDni.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String sexo = edtSexo.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPasswordd.getText().toString().trim();
        String confirPassword = edtConfirPasswordd.getText().toString().trim();

        // Validaciones - TODOS LOS CAMPOS SON OBLIGATORIOS
        boolean hayErrores = false;
        
        if (nombres.isEmpty()) {
            tilNombres.setError("El nombre es obligatorio");
            hayErrores = true;
        }
        if (apellidos.isEmpty()) {
            tilApellidos.setError("Los apellidos son obligatorios");
            hayErrores = true;
        }
        if (fechaNaci.isEmpty()) {
            tilFechNaci.setError("La fecha de nacimiento es obligatoria");
            hayErrores = true;
        } else if (!esMayorDeEdad(fechaNaci)) {
            tilFechNaci.setError("Debe ser mayor de 18 años para registrarse");
            hayErrores = true;
        }
        if (dni.isEmpty()) {
            tilDni.setError("El DNI es obligatorio");
            hayErrores = true;
        } else if (dni.length() != 8) {
            tilDni.setError("El DNI debe tener exactamente 8 dígitos");
            hayErrores = true;
        }
        if (celular.isEmpty()) {
            tilCelular.setError("El celular es obligatorio");
            hayErrores = true;
        } else if (celular.length() != 9) {
            tilCelular.setError("El celular debe tener exactamente 9 dígitos");
            hayErrores = true;
        }
        if (sexo.isEmpty()) {
            tilSexo.setError("El sexo es obligatorio");
            hayErrores = true;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError("El correo es obligatorio");
            hayErrores = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Ingrese un correo válido");
            hayErrores = true;
        }
        if (password.isEmpty()) {
            tilPasswordd.setError("La contraseña es obligatoria");
            hayErrores = true;
        } else if (password.length() < 6 || password.length() > 16) {
            tilPasswordd.setError("La contraseña debe tener entre 6 y 16 caracteres");
            hayErrores = true;
        }
        if (confirPassword.isEmpty()) {
            tilConfirPasswordd.setError("La confirmación de contraseña es obligatoria");
            hayErrores = true;
        } else if (!password.equals(confirPassword)) {
            tilConfirPasswordd.setError("Las contraseñas no coinciden");
            hayErrores = true;
        }
        
        if (hayErrores) {
            android.widget.Toast.makeText(context, "Complete todos los campos obligatorios", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        // Ejecutar registro
        android.widget.Toast.makeText(context, "Registrando usuario...", android.widget.Toast.LENGTH_SHORT).show();
        new RegisterTask(nombres, apellidos, correo, celular, sexo, password, fechaNaci, dni).execute();
    }

    private class RegisterTask extends android.os.AsyncTask<Void, Void, String> {
        private String name, lastname, email, cellphone, sexo, password, date_of_birth, dni;
        private String errorMsg = "";

        RegisterTask(String name, String lastname, String email, String cellphone, String sexo, String password, String date_of_birth, String dni) {
            this.name = name;
            this.lastname = lastname;
            this.email = email;
            this.cellphone = cellphone;
            this.sexo = sexo;
            this.password = password;
            this.date_of_birth = date_of_birth;
            this.dni = dni;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                android.util.Log.d("RegisterTask", "Iniciando petición a: http://10.0.2.2:4001/api/signup");

                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/signup");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                org.json.JSONObject jsonParam = new org.json.JSONObject();
                jsonParam.put("name", name);
                jsonParam.put("lastname", lastname);
                jsonParam.put("email", email);
                jsonParam.put("cellphone", cellphone);
                jsonParam.put("sexo", sexo);
                jsonParam.put("password", password);
                jsonParam.put("date_of_birth", date_of_birth);
                jsonParam.put("dni", dni);

                android.util.Log.d("RegisterTask", "Enviando datos: " + jsonParam.toString());

                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                android.util.Log.d("RegisterTask", "Código de respuesta: " + responseCode);

                java.io.InputStream inputStream = (responseCode == java.net.HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream() : conn.getErrorStream();

                if (inputStream != null) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    android.util.Log.d("RegisterTask", "Respuesta recibida: " + response.toString());
                    return response.toString();
                }
                errorMsg = "Error de conexión: " + responseCode;
                android.util.Log.e("RegisterTask", errorMsg);
            } catch (Exception e) {
                errorMsg = "Error: " + e.getMessage();
                android.util.Log.e("RegisterTask", "Excepción: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            android.util.Log.d("RegisterTask", "onPostExecute llamado. Result: " + result);
            android.util.Log.d("RegisterTask", "ErrorMsg: " + errorMsg);

            if (result != null) {
                try {
                    // El backend puede responder con un JSON o un string directo
                    if (result.startsWith("{")) {
                        org.json.JSONObject json = new org.json.JSONObject(result);

                        // Buscar mensaje de error
                        if (json.has("error") || json.has("message")) {
                            String msg = json.has("error") ? json.getString("error") : json.getString("message");

                            // Verificar si es correo duplicado
                            if (msg.contains("email") || msg.contains("correo") || msg.contains("existe") || msg.contains("duplicate")) {
                                android.widget.Toast.makeText(context, "Este correo ya está registrado", android.widget.Toast.LENGTH_LONG).show();
                                tilCorreo.setError("Este correo ya está registrado");
                            } else {
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show();
                                tilCorreo.setError(msg);
                            }
                        } else {
                            // Registro exitoso
                            android.widget.Toast.makeText(context, "¡Registro exitoso! Revisa tu correo para verificar tu cuenta", android.widget.Toast.LENGTH_LONG).show();
                            navController.navigate(R.id.navigation_login);
                        }
                    } else {
                        // Respuesta es texto directo
                        if (result.contains("exito") || result.contains("éxito") || result.contains("creado")) {
                            android.widget.Toast.makeText(context, result, android.widget.Toast.LENGTH_LONG).show();
                            navController.navigate(R.id.navigation_login);
                        } else {
                            android.widget.Toast.makeText(context, result, android.widget.Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    android.widget.Toast.makeText(context, "Error al procesar la respuesta", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("RegisterTask", "Error al parsear respuesta: " + e.getMessage());
                }
            } else {
                String finalError = errorMsg.isEmpty() ? "Error desconocido" : errorMsg;
                android.widget.Toast.makeText(context, "Error: " + finalError, android.widget.Toast.LENGTH_LONG).show();
                tilCorreo.setError(finalError);
            }
        }
    }

    private boolean esMayorDeEdad(String fechaNacimiento) {
        try {
            // Parsear fecha de nacimiento (formato: YYYY-MM-DD)
            String[] partes = fechaNacimiento.split("-");
            int añoNacimiento = Integer.parseInt(partes[0]);
            int mesNacimiento = Integer.parseInt(partes[1]) - 1; // Calendar usa meses 0-11
            int diaNacimiento = Integer.parseInt(partes[2]);

            Calendar fechaNac = Calendar.getInstance();
            fechaNac.set(añoNacimiento, mesNacimiento, diaNacimiento);

            Calendar hoy = Calendar.getInstance();
            
            // Calcular edad
            int edad = hoy.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);
            
            // Ajustar si aún no ha cumplido años este año
            if (hoy.get(Calendar.MONTH) < fechaNac.get(Calendar.MONTH) ||
                (hoy.get(Calendar.MONTH) == fechaNac.get(Calendar.MONTH) && 
                 hoy.get(Calendar.DAY_OF_MONTH) < fechaNac.get(Calendar.DAY_OF_MONTH))) {
                edad--;
            }
            
            return edad >= 18;
        } catch (Exception e) {
            android.util.Log.e("Register", "Error al validar edad: " + e.getMessage());
            return false;
        }
    }
}
