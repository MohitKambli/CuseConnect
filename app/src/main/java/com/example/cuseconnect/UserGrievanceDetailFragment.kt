package com.example.cuseconnect

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserGrievanceDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserGrievanceDetailFragment : Fragment() {

    private var grievanceTitle: String? = null
    private var grievanceDescription: String = ""
    private var facility: String = ""
    private var subFacility: String = ""
    private var images: List<String> = emptyList()
    private var grievanceStatus: String = ""
    private var grievanceFeedback: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            grievanceTitle = it.getString(ARG_GRIEVANCE_TITLE)
            grievanceDescription = it.getString(ARG_GRIEVANCE_DESCRIPTION) ?: ""
            facility = it.getString(ARG_GRIEVANCE_FACILITY) ?: ""
            subFacility = it.getString(ARG_GRIEVANCE_SUBFACILITY) ?: ""
            images = it.getStringArrayList(ARG_GRIEVANCE_IMAGES) ?: emptyList()
            grievanceStatus = it.getString(ARG_GRIEVANCE_STATUS) ?: ""
            grievanceFeedback = it.getString(ARG_GRIEVANCE_FEEDBACK) ?: ""
        }
    }

    fun setMovieDetails(title: String, description: String, facility: String, subFacility: String, images: List<String>, status: String, feedback: String) {
        this.grievanceTitle = title
        this.grievanceDescription = description
        this.facility = facility
        this.subFacility = subFacility
        this.images = images
        this.grievanceStatus = status
        this.grievanceFeedback = feedback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_grievance_detail, container, false)

        val title = view.findViewById<TextView>(R.id.title)
        val description = view.findViewById<TextView>(R.id.description)
        val facilityView = view.findViewById<TextView>(R.id.facility)
        val subfacilityView = view.findViewById<TextView>(R.id.subFacility)
        val status = view.findViewById<TextView>(R.id.status)

        title.text = grievanceTitle
        description.text = grievanceDescription
        facilityView.text = facility
        subfacilityView.text = subFacility
        status.text = grievanceStatus

        // Find the parent layout
        val parentLayout = view.findViewById<LinearLayout>(R.id.detailLayout)

        if (images.isEmpty()) {
            // If no images, remove the RecyclerView
            val recyclerView = view.findViewById<RecyclerView>(R.id.imagesRecyclerView)
            parentLayout.removeView(recyclerView)

            // Add a TextView to inform the user
            val noImagesTextView = TextView(context)
            noImagesTextView.text = "No images uploaded."
            noImagesTextView.setTextColor(Color.WHITE)
            noImagesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            noImagesTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            noImagesTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add the TextView to the parent layout
            parentLayout.addView(noImagesTextView)
        } else {
            // If images exist, set up the RecyclerView
            val recyclerView = view.findViewById<RecyclerView>(R.id.imagesRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context)
            val imagesAdapter = GrievanceImagesAdapter(images)
            recyclerView.adapter = imagesAdapter
        }

        return view
    }

    companion object {
        private const val ARG_GRIEVANCE_TITLE = "grievanceTitle"
        private const val ARG_GRIEVANCE_DESCRIPTION = "grievanceDescription"
        private const val ARG_GRIEVANCE_FACILITY = "facility"
        private const val ARG_GRIEVANCE_SUBFACILITY = "subFacility"
        private const val ARG_GRIEVANCE_IMAGES = "images"
        private const val ARG_GRIEVANCE_STATUS = "status"
        private const val ARG_GRIEVANCE_FEEDBACK = "feedback"

        @JvmStatic
        fun newInstance(
            grievanceTitle: String?,
            grievanceDescription: String,
            facility: String,
            subFacility: String,
            images: List<String>,
            status: String,
            feedback: String
        ) = UserGrievanceDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GRIEVANCE_TITLE, grievanceTitle)
                putString(ARG_GRIEVANCE_DESCRIPTION, grievanceDescription)
                putString(ARG_GRIEVANCE_FACILITY, facility)
                putString(ARG_GRIEVANCE_SUBFACILITY, subFacility)
                putStringArrayList(ARG_GRIEVANCE_IMAGES, ArrayList(images))
                putString(ARG_GRIEVANCE_STATUS, status)
                putString(ARG_GRIEVANCE_FEEDBACK, feedback)
            }
        }
    }
}