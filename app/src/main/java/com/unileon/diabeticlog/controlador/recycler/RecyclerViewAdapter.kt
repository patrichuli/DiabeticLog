package com.unileon.diabeticlog.controlador.recycler

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.unileon.diabeticlog.R
import com.unileon.diabeticlog.controlador.data.DatosRegistrados

class RecyclerViewAdapter(private val lista: ArrayList<DatosRegistrados>, private var listener: OnItemClickListener) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textViewNombre: TextView = view.findViewById(R.id.item_nombre)
        val textViewHora: TextView = view.findViewById(R.id.item_hora)
        val textViewDatos: TextView = view.findViewById(R.id.item_datos)
        val textViewImage: ImageView = view.findViewById(R.id.item_image)
        val textViewImageBitmap: ImageView = view.findViewById(R.id.item_image_bitmap)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(items: DatosRegistrados) {
            textViewNombre.setText(items.nombre)
            textViewHora.setText(items.hora)
            textViewDatos.setText(items.datos)
            textViewImage.setImageResource(items.imagen)
            textViewImageBitmap.setImageBitmap(items.imagenBitmap)


        }


    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.text_row_item, viewGroup, false)


        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(lista[position])

    }

    override fun getItemCount(): Int { return lista.size }


}