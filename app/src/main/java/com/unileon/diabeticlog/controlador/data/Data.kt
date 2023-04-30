package com.unileon.diabeticlog.controlador.data

import android.graphics.Bitmap

class DatosRegistrados {
    var id: Int = 0
    var tipoRegistro: String? = null
    var nombre: String? = null
    var datos: String? = null
    var datoAdicional: String? = null
    var fecha: String? = null
    var hora: String? = null
    var imagen: Int = 0
    var imagenBitmap: Bitmap? = null

    constructor(id: Int, tipoRegistro: String, nombre: String, datos: String, fecha: String, hora: String, imagen: Int) {
        this.id = id
        this.tipoRegistro = tipoRegistro
        this.nombre = nombre
        this.datos = datos
        this.fecha = fecha
        this.hora = hora
        this.imagen = imagen
    }

    constructor(id: Int, tipoRegistro: String, nombre: String, datos: String, datoAdicional: String, fecha: String, hora: String, imagen: Int) {
        this.id = id
        this.tipoRegistro = tipoRegistro
        this.nombre = nombre
        this.datos = datos
        this.datoAdicional = datoAdicional
        this.fecha = fecha
        this.hora = hora
        this.imagen = imagen
    }

    constructor(id: Int, tipoRegistro: String, nombre: String, datos: String, fecha: String, hora: String, imagenBitmap: Bitmap) {
        this.id = id
        this.tipoRegistro = tipoRegistro
        this.nombre = nombre
        this.datos = datos
        this.fecha = fecha
        this.hora = hora
        this.imagenBitmap = imagenBitmap
    }


    constructor() {}

}
