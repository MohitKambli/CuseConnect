package com.example.cuseconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.Locale

class RestaurantAdapter(
    private val context: Context,
    private val itemClickListener: ItemClickListener,
    private val recyclerView: RecyclerView,
    private val restaurantsList: ArrayList<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>()  {

    private val tempRestaurantsList = ArrayList<Restaurant>(restaurantsList)
    private var lastPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.name)
        val restaurantRating: RatingBar = itemView.findViewById(R.id.rating)
        val restaurantPrice: TextView = itemView.findViewById(R.id.price)
        val restaurantImage: ImageView = itemView.findViewById(R.id.image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedRestaurant = restaurantsList[position]
                    itemClickListener.onItemClicked(
                        selectedRestaurant.name,
                        selectedRestaurant.rating,
                        selectedRestaurant.price,
                        selectedRestaurant.image,
                        selectedRestaurant.address,
                        selectedRestaurant.phoneNumber,
                        selectedRestaurant.latitude,
                        selectedRestaurant.longitude,
                        selectedRestaurant.url
                    )
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.restaurant_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurantsList[position]
        holder.restaurantName.text = restaurant.name
        holder.restaurantRating.rating = restaurant.rating
        holder.restaurantPrice.text = restaurant.price

        Picasso.get()
            .load(restaurant.image)
            .into(holder.restaurantImage, object : Callback {
                override fun onSuccess() {
                    // Image loaded successfully
                }

                override fun onError(e: Exception?) {
                    println("Error: ${e.toString()}")
                    println("Error ST: ${e?.printStackTrace()}")
                    e?.printStackTrace()
                }
            })

        setAnimation(holder.restaurantImage, position)
    }

    private fun setAnimation(imageView: ImageView, position: Int) {
        if (position != lastPosition) {
            when (getItemViewType(position)) {
                1 -> {
                    val animation =
                        AnimationUtils.loadAnimation(imageView.context, android.R.anim.slide_in_left)
                    animation.duration = 300
                    animation.startOffset = position * 50L
                    imageView.startAnimation(animation)
                }
                2 -> {
                    val animation = AlphaAnimation(0.0f, 1.0f)
                    animation.duration = 300
                    animation.startOffset = position * 25L
                    imageView.startAnimation(animation)
                }
                else -> {
                    val animation = ScaleAnimation(
                        0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    )
                    animation.duration = 200
                    animation.startOffset = position * 100L
                    imageView.startAnimation(animation)
                }
            }
            //animation.startOffset = position * 100L
            lastPosition = position
        }


    }

    override fun getItemCount(): Int {
        return restaurantsList.size
    }

    interface ItemClickListener {
        fun onItemClicked(restaurantName: String?, restaurantRating: Float, restaurantPrice: String, restaurantImage: String?, restaurantAddress: List<String>, restaurantPhoneNumber: String?, restaurantLatitude: Double, restaurantLongitude: Double, restaurantUrl: String)
    }

    fun filter(query: String?) {
        val filteredList = if (!query.isNullOrBlank()) {
            tempRestaurantsList.filter {
                it.name.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))
            }.toMutableList()
        } else {
            tempRestaurantsList.toMutableList()
        }

        restaurantsList.clear()
        restaurantsList.addAll(filteredList)
        notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            // Scroll to the first restaurant and display a message
            recyclerView.smoothScrollToPosition(0)
            // Toast.makeText(context, "No matching restaurants found", Toast.LENGTH_SHORT).show()
        }
    }

//    fun sortByName(ascending: Boolean) {
//        restaurantsList.sortBy { it.name }
//        if (!ascending) {
//            restaurantsList.reverse()
//        }
//        notifyDataSetChanged()
//    }

//    fun filterEvents(query: String) {
//        val lowercaseQuery = query.lowercase()
//        eventsToShow = if (query.isEmpty()) {
//            allEvents
//        } else {
//            allEvents.filter {
//                it.name.lowercase().contains(lowercaseQuery)
//            }
//        }
//        if (eventsToShow.isEmpty()) {
//            Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
//        }
//        notifyDataSetChanged()
//    }
}