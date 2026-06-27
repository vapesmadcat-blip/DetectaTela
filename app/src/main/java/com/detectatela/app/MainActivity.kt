package com.detectatela.app

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.mediarouter.media.MediaRouter

class MainActivity : AppCompatActivity() {

    private lateinit var mediaRouter: MediaRouter
    private var callback: MediaRouter.Callback? = null
    private lateinit var displayManager: DisplayManager
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStatus = findViewById(R.id.txtStatus)
        val btnBuscar: Button = findViewById(R.id.btnBuscar)

        mediaRouter = MediaRouter.getInstance(this)

        callback = object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
                Log.d("DetectaTela", "Rota adicionada: ${route.name}")
                atualizar("Nova rota encontrada: ${route.name}")
            }

            override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo) {
                val display = route.presentationDisplay
                if (display != null) {
                    atualizar("✅ Conectado na TV: ${display.name}")
                } else {
                    atualizar("Rota selecionada: ${route.name}")
                }
            }

            override fun onRouteUnselected(router: MediaRouter, route: MediaRouter.RouteInfo, reason: Int) {
                atualizar("Desconectado da TV")
            }
        }

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                val d = displayManager.getDisplay(displayId)
                atualizar("Display externo detectado: ${d?.name}")
            }
            override fun onDisplayRemoved(displayId: Int) {}
            override fun onDisplayChanged(displayId: Int) {}
        }, null)

        btnBuscar.setOnClickListener {
            mediaRouter.addCallback(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO,
                callback!!,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY
            )
            atualizar("Buscando TVs e telas externas...")
        }

        atualizar("Pronto! Toque em Buscar para procurar TVs")
    }

    private fun atualizar(texto: String) {
        runOnUiThread { txtStatus.text = texto }
        Log.d("DetectaTela", texto)
    }

    override fun onDestroy() {
        super.onDestroy()
        callback?.let { mediaRouter.removeCallback(it) }
    }
}
