package com.example.fakestore.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.fakestore.data.model.Product
import com.example.fakestore.databinding.ItemProductBinding
import com.example.fakestore.ui.common.formatPrice

/**
 * RecyclerView adapter. [ListAdapter] + [DiffUtil] compute what changed between two lists
 * on a background thread and update only those rows.
 */
class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding, onProductClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Holds the views of one row so they can be recycled for other products while scrolling. */
    class ProductViewHolder(
        private val binding: ItemProductBinding,
        onProductClick: (Product) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var product: Product? = null

        init {
            // Set the listener once per holder, not on every bind.
            binding.root.setOnClickListener { product?.let(onProductClick) }
        }

        fun bind(product: Product) {
            this.product = product
            binding.title.text = product.title
            binding.category.text = product.category
            binding.price.text = product.price.formatPrice()
            binding.image.load(product.imageUrl) { crossfade(true) }
        }
    }

    private object ProductDiff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
