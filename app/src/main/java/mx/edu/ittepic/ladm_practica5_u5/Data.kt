package mx.edu.ittepic.ladm_practica5_u5

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class Data {
    var nombre: String = ""
    var posicion1: GeoPoint = GeoPoint(0.0,0.0)
    var posicion2: GeoPoint = GeoPoint(0.0,0.0)
    var baseRemota = FirebaseFirestore.getInstance()

    override fun toString(): String {
        return nombre+"\n"+posicion1.latitude+","+posicion1.longitude+"\n"+
        posicion2.latitude+","+posicion2.longitude
    }

    fun estoyEn(posicionActual:GeoPoint): Boolean{
        if(posicionActual.latitude >= posicion1.latitude &&
            posicionActual.latitude <= posicion2.latitude){
            if(invertir(posicionActual.longitude) >= invertir(posicion1.longitude) &&
                invertir(posicionActual.longitude) <= invertir(posicion2.longitude)){
                return true
            }
        }
        return false
    }

    private fun invertir(valor:Double):Double{
        return valor*-1
    }
}