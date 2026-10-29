package com.lksn.voto.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lksn.voto.data.CameraSummary
import com.lksn.voto.databinding.ItemCameraBinding
import com.lksn.voto.util.Format
import com.lksn.voto.util.loadPhoto

class CameraAdapter(
    private val onClick: (CameraSummary) -> Unit
) : RecyclerView.Adapter<CameraAdapter.ViewHolder>() {

    private val items = mutableListOf<CameraSummary>()

    fun submit(list: List<CameraSummary>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemCameraBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val b: ItemCameraBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(camera: CameraSummary) {
            b.tvName.text = camera.name
            b.tvResolution.text = camera.resolution
            b.tvPrice.text = Format.rupiahDot(camera.price.toDouble())
            b.ivPhoto.loadPhoto(camera.photo)
            b.root.setOnClickListener { onClick(camera) }
        }
    }
}