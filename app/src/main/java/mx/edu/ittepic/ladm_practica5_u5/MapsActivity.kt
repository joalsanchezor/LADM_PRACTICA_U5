package mx.edu.ittepic.ladm_practica5_u5

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*
import mx.edu.ittepic.ladm_practica5_u5.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var locacion : LocationManager
    var baseRemota = FirebaseFirestore.getInstance()
    var lugares = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Ocultar ActionBar
        getSupportActionBar()?.hide()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        getLugares()
        ubicacionText.setText("Tepic")
        btnUbicacion.setOnClickListener {

            if(busquedaLugar.text.toString().equals("")){
                Toast.makeText(this, "CAMPO VACÍO", Toast.LENGTH_LONG).show()
            }else{
                buscarLugar(busquedaLugar.text.toString())
            }
        }

        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,01f,oyente)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera: defecto -34.0, 151.0
        //21.510791, -104.899410


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                //var geoPosicion = GeoPoint(it.latitude, it.longitude)
                val tepic = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(tepic).title("Mi ubicación"))
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(tepic))
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.getLatitude(),
                            it.getLongitude()
                        ), 12.0f
                    )
                )
                mMap.uiSettings.isZoomControlsEnabled = true
                miUbicacion()
            }
    }

     fun miUbicacion(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPosicion = GeoPoint(it.latitude, it.longitude)
                for(item in lugares){
                    if(item.estoyEn(geoPosicion)){
                        AlertDialog.Builder(this)
                            .setMessage("Ver información de ${item.nombre}")
                            .setTitle("ATENCIÓN")
                            .setPositiveButton("OK"){p, q->
                                var nueva = Intent(this,DescripcionLugar::class.java)
                                nueva.putExtra("ubicacion",item.nombre)
                                startActivity(nueva)
                            }.setNegativeButton("No"){p, q->}
                            .show()
                        break
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this,"ERROR AL OBTENER UBICACIÓN", Toast.LENGTH_LONG).show()
            }
    }

    fun buscarLugar(ubicacion : String){
        for(lugar in lugares){
            if(ubicacion.equals(lugar.nombre)){
                AlertDialog.Builder(this)
                    .setMessage("¿Desea ver información del sitio?")
                    .setTitle("Información")
                    .setPositiveButton("Sí"){p, q->
                        busquedaLugar.setText("")
                        var nueva = Intent(this,DescripcionLugar::class.java)
                        nueva.putExtra("ubicacion",ubicacion)
                        startActivity(nueva)}
                       //setContentView(R.layout.activity_descripcion_lugar)}
                    .setNegativeButton("No"){p, q->}
                    .show()
            }
        }
    }

     fun getLugares(){
        baseRemota.collection("Lugares")
            .addSnapshotListener { value, error ->
                if(error != null){
                    Toast.makeText(this, "ERROR: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                lugares.clear()
                for(document in value!!){
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!

                    lugares.add(data)
                }
            }
    }

}

class Oyente(puntero:MapsActivity) : LocationListener {
    var p = puntero
    override fun onLocationChanged(location: Location) {
        var geoPosicion = GeoPoint(location.latitude, location.longitude)

        for(item in p.lugares){
            if(item.estoyEn(geoPosicion)){
                p.ubicacionText.setText(item.nombre)
                AlertDialog.Builder(p)
                    .setMessage("Usted se encuentra en ${item.nombre}")
                    .setTitle("ATENCIÓN")
                    .setPositiveButton("OK"){r, q->
                        p.buscarLugar(item.nombre)
                    }
                    .show()
            }//if
        }//for
    }
}