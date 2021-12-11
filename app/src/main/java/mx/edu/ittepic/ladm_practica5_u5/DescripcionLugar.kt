package mx.edu.ittepic.ladm_practica5_u5

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_descripcion_lugar.*
import java.net.URI

class DescripcionLugar : AppCompatActivity() {
    var imagenes = ArrayList<ImageView>()
    var baseRemota = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_descripcion_lugar)

        //Ocultar ActionBar
        getSupportActionBar()?.hide()

        var extra = intent.extras!!
        var nombreLugar = extra.getString("ubicacion")

        regresar.setOnClickListener {
            finish()
        }
        getImagenes(nombreLugar.toString())
    }

    fun getImagenes(lugar : String){
        var uris = ArrayList<Any>()
        titulo.setText(lugar)
        baseRemota.collection("Lugares")
            .whereEqualTo("nombre", lugar)
            .addSnapshotListener { value, error ->
                if(error != null){
                    Toast.makeText(this, "ERROR: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                uris.clear()
                for(document in value!!){
                    uris.add(document.get("img")!!)
                    contenidoDescripcion.setText(document.getString("descripcion"))
                }
                var datos = uris.get(0) as ArrayList<Any>
                cargarImagen(datos.get(0).toString(),imagen1)
                cargarImagen(datos.get(1).toString(),imagen2)
                cargarImagen(datos.get(2).toString(),imagen3)
            }
    }

    fun cargarImagen(direccion : String, imagen : ImageView){
        Glide.with(this)
            .load(Uri.parse(direccion))
            .into(imagen)
    }

}