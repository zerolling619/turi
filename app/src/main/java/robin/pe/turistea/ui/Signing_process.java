package robin.pe.turistea.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import robin.pe.turistea.R;


public class Signing_process extends Fragment {

    public Signing_process() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signing_process, container, false);
        TextView TvContrato = view.findViewById(R.id.TvContrato);

        TvContrato.setText("\"La experiencia del viaje no solo se mide por los kilómetros recorridos, sino también por la profundidad de los momentos vividos. Cada destino tiene una historia única, una esencia que se guarda en las calles, en la gente y en los pequeños detalles que muchas veces pasan desapercibidos. Viajar es, de alguna manera, una forma de descubrirnos a nosotros mismos a través del reflejo de otros mundos. Al caminar por un nuevo lugar, nuestros sentidos se abren y nos permitimos observar aquello que en la rutina diaria solemos ignorar: el sonido del viento entre los árboles, el aroma de una comida recién preparada, la sonrisa de un desconocido que nos saluda sin esperar nada a cambio.\n" +
                "\n" +
                "A lo largo del camino, encontramos paisajes que parecen sacados de un sueño, ciudades que nunca duermen y pueblos donde el tiempo avanza con una calma casi poética. En cada destino, aprendemos algo nuevo; no solo sobre el lugar, sino también sobre nuestra propia capacidad de adaptación, de asombro y de comprensión. Hay viajes que nos cambian la vida sin que lo notemos en el momento, pero con el pasar del tiempo entendemos que algo dentro de nosotros fue transformado. A veces, basta con mirar un amanecer desde un punto alto para recordar que el mundo es más grande que nuestras preocupaciones diarias.\n" +
                "\n" +
                "Sin embargo, no son solo los paisajes o las experiencias externas las que hacen que un viaje sea memorable. También lo son las personas que encontramos en el camino. Conversaciones breves pueden dejarnos lecciones profundas, y amistades inesperadas se convierten en historias que recordaremos por siempre. Viajar no es escapar; es aprender, crecer y conectar con aquello que nos rodea. Cada paso nos invita a mirar más allá de lo evidente y a valorar la diversidad que compone este enorme mosaico llamado mundo.\n" +
                "\n" +
                "Cuando regresamos, no volvemos siendo los mismos. Traemos con nosotros recuerdos que se quedan grabados en la memoria, fotografías que capturan instantes irrepetibles y una nueva forma de observar nuestra vida cotidiana. Los viajes no terminan cuando regresamos a casa; continúan en nuestra manera de pensar, de sentir y de relacionarnos con el mundo. Y así, cada vez que emprendemos un nuevo destino, abrimos una puerta hacia una versión más amplia y consciente de quienes somos.\"");

        return view;
    }
}