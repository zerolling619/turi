package robin.pe.turistea.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import robin.pe.turistea.Config;
import robin.pe.turistea.R;

public class CompleteGoogleSignIn extends Fragment {

    private NavController navController;
    private TextInputEditText edtDni, edtFechNaci, edtCelular;
    private AutoCompleteTextView edtSexo;
    private Button btnGuardarPerfil;

    // Datos del usuario de Google
    private String name, lastname, email, googleId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recibir los datos del usuario pasados desde el fragmento de Login
        if (getArguments() != null) {
            name = getArguments().getString("name");
            lastname = getArguments().getString("lastname");
            email = getArguments().getString("email");
            googleId = getArguments().getString("googleId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_google_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Inicializar vistas
        edtDni = view.findViewById(R.id.edtDni);
        edtFechNaci = view.findViewById(R.id.edtFechNaci);
        edtCelular = view.findViewById(R.id.edtCelular);
        edtSexo = view.findViewById(R.id.edtSexo);
        btnGuardarPerfil = view.findViewById(R.id.btnGuardarPerfil);

        // Configurar selector de fecha de nacimiento
        setupDatePicker();

        // Configurar selector de sexo
        setupSexSelector();

        // Configurar botón de guardar
        btnGuardarPerfil.setOnClickListener(v -> {
            if (validateFields()) {
                saveUserProfile();
            }
        });

        ImageView icBack = view.findViewById(R.id.IcBack);
        icBack.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigateUp();
            }
        });
    }

    private void setupDatePicker() {
        edtFechNaci.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (datePicker, year, month, day) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                        edtFechNaci.setText(selectedDate);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void setupSexSelector() {
        String[] sexes = new String[]{"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sexes);
        edtSexo.setAdapter(adapter);
    }

    private boolean validateFields() {
        if (edtDni.getText().toString().trim().isEmpty() ||
            edtFechNaci.getText().toString().trim().isEmpty() ||
            edtCelular.getText().toString().trim().isEmpty() ||
            edtSexo.getText().toString().trim().isEmpty()) { 
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveUserProfile() {
        String dni = edtDni.getText().toString().trim();
        String dob = edtFechNaci.getText().toString().trim();
        String cellphone = edtCelular.getText().toString().trim();
        String sex = edtSexo.getText().toString().trim();

        new Thread(() -> {
            try {
                URL url = new URL(Config.SOCIAL_REGISTER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("name", name);
                jsonParam.put("lastname", lastname);
                jsonParam.put("password", "google_" + googleId);
                jsonParam.put("origin", "google");
                
                jsonParam.put("dni", dni);
                jsonParam.put("date_of_birth", dob);
                jsonParam.put("cellphone", cellphone);
                jsonParam.put("sexo", sex);
                jsonParam.put("path", "https://ui-avatars.com/api/?name=" + name + "+" + lastname + "&background=random");

                // **LA CORRECCIÓN ESTÁ AQUÍ**
                // Añadir el campo obligatorio que faltaba
                jsonParam.put("terms_and_conditions", true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String jwt = jsonResponse.optString("token");

                    if (jwt != null && !jwt.isEmpty()) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("jwt", jwt);
                        editor.putString("user_name", name + " " + lastname);
                        editor.putString("user_email", email);
                        editor.putString("user_role", "user");
                        editor.apply();

                        if(getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "¡Registro completado!", Toast.LENGTH_SHORT).show();
                                navController.navigate(R.id.action_navigation_completeGoogleSignIn_to_navigation_inicio);
                            });
                        }
                    }
                } else {
                     if(getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error del servidor: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("CompleteProfile", "Error al guardar perfil", e);
                if(getActivity() != null) {
                   getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}
