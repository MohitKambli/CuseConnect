package com.example.cuseconnect

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyEventsAdapter(private var events: List<Event>, private val context: Context) : RecyclerView.Adapter<MyEventsAdapter.EventViewHolder>() {
    private var lastPosition = -1
    private var allEvents: List<Event> = events
    private var eventsToShow: List<Event> = events

    fun updateEvents(newEvents: List<Event>) {
        allEvents = newEvents
        eventsToShow = newEvents

        println("EVENTSTOSHOW LOADED: " +eventsToShow)
        notifyDataSetChanged()
    }

    fun sortEvents(sortFunction: (List<Event>) -> List<Event>) {
        eventsToShow = sortFunction(eventsToShow)
        notifyDataSetChanged()
    }

    fun filterEvents(query: String) {
        val lowercaseQuery = query.lowercase()
        eventsToShow = if (query.isEmpty()) {
            allEvents
        } else {
            allEvents.filter {
                it.name.lowercase().contains(lowercaseQuery)
            }
        }
        if (eventsToShow.isEmpty()) {
            Toast.makeText(context, "No results found", Toast.LENGTH_SHORT).show()
        }
        notifyDataSetChanged()
    }

    fun resetFilter() {
        eventsToShow = allEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsToShow[position] // Use eventsToShow
        println("ON BIND: "+event)
        val imageUrl = event.images.firstOrNull { it.width == 100 && it.height == 56 }?.url
        val startDate = event.dates.start.localDate // Adjust according to your needs
        holder.bind(event)

        setAnimation(holder.imageView, position)

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


    override fun getItemCount(): Int = eventsToShow.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.event_name_text_view)
        private val startDateTextView: TextView =
            itemView.findViewById(R.id.event_start_date_text_view)
        val imageView: ImageView = itemView.findViewById(R.id.event_image_view)



        fun bind(event: Event) {
            // Binding event name
            nameTextView.text = event.name

            // Binding event image using Glide
            val imageUrl = event.images.firstOrNull { it.width == 100 && it.height == 56 }?.url ?: ""
            if (imageUrl.isNotEmpty()) {
                Glide.with(itemView.context).load(imageUrl).into(imageView)
            }

            // Binding event date
            startDateTextView.text = "Date: ${event.dates.start.localDate}"

            // Binding event start time
            val startTimeTextView: TextView = itemView.findViewById(R.id.event_start_time_text_view)
            startTimeTextView.text = "Start Time: ${event.dates.start.localTime}"

            // Set OnClickListener to open EventDetailActivity with the current event
            itemView.setOnClickListener {
                Log.d("EventViewHolder", "Event Clicked: ${event.name}")

                val intent = Intent(itemView.context, EventDetailActivity::class.java).apply {
                    putExtra("EVENT_DATA", event)
                }

                try {
                    itemView.context.startActivity(intent)
                    Log.d("EventViewHolder", "Intent started for ${event.name}")
                } catch (e: Exception) {
                    Log.e("EventViewHolder", "Error starting intent: ${e.message}")
                }
            }
        }
    }
}