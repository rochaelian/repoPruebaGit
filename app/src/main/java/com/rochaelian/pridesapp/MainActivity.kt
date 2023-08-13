package com.rochaelian.pridesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.rochaelian.pridesapp.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Json { ignoreUnknownKeys = true }
        binding.btnLogin.setOnClickListener{
            login()
        }

    }


    private fun login(){
        val usuario = binding.etNombreUsuario.text.toString()

        if (isValidForm(usuario)){
            goToNotification(usuario)
        }
    }

    // Valida que se ingrese un usuario
    private fun isValidForm(usuario:String): Boolean{
        if (usuario.isEmpty()){
            Toast.makeText(this, "Ingresa un nombre de usuario", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // LLeva a la pantalla de datos y notificaciones
    private fun goToNotification(usuario: String){
        startActivity(Intent(this, NotificationActivity::class.java).apply {
            putExtra("Usuario", usuario)
        })
    }


}