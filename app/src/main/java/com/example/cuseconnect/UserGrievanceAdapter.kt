package com.example.cuseconnect

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class UserGrievanceAdapter(
    private val context: Context,
    private val itemClickListener: UserGrievancesActivity,
    private val recyclerView: RecyclerView,
    private val grievancesList: ArrayList<UserGrievance>,
    private val feedbackButtonClickListener: OnFeedbackButtonClickListener
) : RecyclerView.Adapter<UserGrievanceAdapter.ViewHolder>() {

    private val tempGrievancesList = ArrayList<UserGrievance>(grievancesList)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val grievanceTitle: TextView = itemView.findViewById(R.id.grievance_name_text_view)
////        val grievanceUser: TextView = itemView.findViewById(R.id.grievance_user)
////        val grievanceFacility: TextView = itemView.findViewById(R.id.facility)
////        val grievanceSubfacility: TextView = itemView.findViewById(R.id.subFacility)
//        val grievanceStatus: TextView = itemView.findViewById(R.id.grievance_status)
//        val cardView: CardView = itemView.findViewById(R.id.cardView)
//        val viewFeedbackButton: Button = itemView.findViewById(R.id.viewFeedbackButton)

        val nameTextView: TextView = itemView.findViewById(R.id.grievance_name_text_view) // replace with your actual TextView ID
        val statusTextView: TextView = itemView.findViewById(R.id.grievance_status)
        val subFacilityTextView: TextView = itemView.findViewById(R.id.grievance_sub_facility)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val viewFeedbackButton: Button = itemView.findViewById(R.id.viewFeedbackButton)

        init {
            viewFeedbackButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    feedbackButtonClickListener.onFeedbackButtonClicked(grievancesList[position].feedback)
                }
            }
        }

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedGrievance = grievancesList[position]
                    itemClickListener.onItemClicked(
                        selectedGrievance.title,
                        selectedGrievance.description,
                        selectedGrievance.facility,
                        selectedGrievance.subFacility,
                        selectedGrievance.images,
                        selectedGrievance.status,
                        selectedGrievance.feedback
                    )
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.grievance_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grievance = grievancesList[position]

        // Set the background color based on the status
        val cardBackgroundColor = when (grievance.status) {
            "Open" -> Color.parseColor("#ff7f50") // Yellow color
            "Resolved" -> Color.parseColor("#69a765") // Green color
            else -> Color.parseColor("#FFFFFF") // Default color
        }
        holder.cardView.setCardBackgroundColor(cardBackgroundColor)

        val grievanceNameLabel = "Grievance Name: "
        val statusLabel ="Status: "
        val subFacilityLabel = "Sub-Facility: "

        holder.nameTextView.text = grievanceNameLabel + grievance.title
//        holder.grievanceDescription.text = grievance.description
//        holder.grievanceFacility.text = grievance.facility
//        holder.grievanceSubfacility.text = grievance.subFacility
        holder.statusTextView.text = statusLabel + grievance.status
        holder.subFacilityTextView.text = subFacilityLabel + grievance.subFacility

        println("Grievances: $grievancesList")

        if (grievance.status == "Resolved") {
            // Show "View Feedback" button
            holder.viewFeedbackButton.visibility = View.VISIBLE

            // Set click listener for the "View Feedback" button
            holder.viewFeedbackButton.setOnClickListener {
                val feedback = grievance.feedback
                itemClickListener?.onFeedbackButtonClicked(feedback)
            }
        } else {
            // Hide "View Feedback" button
            holder.viewFeedbackButton.visibility = View.GONE
        }


        // Load grievance image if available
//        grievance.imageUrls?.let { imageUrls ->
//            if (!imageUrls.isNullOrEmpty()) {
//                Picasso.get()
//                    .load(imageUrls[0])
//                    .into(holder.grievanceImage, object : Callback {
//                        override fun onSuccess() {
//                            // Image loaded successfully
//                        }
//
//                        override fun onError(e: Exception?) {
//                            println("Error: ${e.toString()}")
//                            println("Error ST: ${e?.printStackTrace()}")
//                            e?.printStackTrace()
//                        }
//                    })
//            }
//        }
    }

    override fun getItemCount(): Int {
        return grievancesList.size
    }

    interface ItemClickListener {
        fun onItemClicked(
            grievanceTitle: String?,
            grievanceDescription: String,
            facility: String,
            subfacility: String,
            images: List<String>,
            status: String,
            feedback: String
        )
    }

    interface OnFeedbackButtonClickListener {
        fun onFeedbackButtonClicked(feedback: String)
    }

    fun filter(query: String?) {
        val filteredList = ArrayList<UserGrievance>()
        if (!query.isNullOrBlank()) {
            for (grievance in grievancesList) {
                if (grievance.title.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                    filteredList.add(grievance)
                }
            }
        } else {
            filteredList.addAll(tempGrievancesList)
        }

        grievancesList.clear()
        grievancesList.addAll(filteredList)
        notifyDataSetChanged()

        if (filteredList.isEmpty()) {
            // Scroll to the first item and display a message
            recyclerView.smoothScrollToPosition(0)
            //Toast.makeText(context, "No matching items found", Toast.LENGTH_SHORT).show()
        }
    }
}
