package com.iarsdk

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_surface_ar_activity.*
import com.iar.surface_sdk.aractivity.IARSurfaceActivity

class SurfaceARActivityFragment : Fragment() {

  private lateinit var buttonPlace: Button

  var isAnchored = false
  var buttonPlaceUnAnchoredText = "Place"
  var buttonPlaceAnchoredText = "Move"
  var surfaceDetected = false
  lateinit var styleButtonPlace: HashMap<String, Any>

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.fragment_surface_ar_activity, container, false)
    buttonPlace = view.findViewById(R.id.buttonPlace)

    // Setup Button Styles
    setupButton(buttonPlace, styleButtonPlace)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Set an onClickListener for the button
    buttonPlace.setOnClickListener {
      (activity as SurfaceARActivity).logThis("Fragment Button Clicked")
      if (isAnchored) {
        (activity as? IARSurfaceActivity)?.unanchorAsset()
      }
      else {
        (activity as? IARSurfaceActivity)?.anchorAsset()
      }
    }
  }

  fun setPlaceButtonStyle(style: HashMap<String, Any>) {
    styleButtonPlace = style
  }

  private fun setupButton(button: Button, buttonStyle: HashMap<String, Any>) {
    val textColor: String = if (buttonStyle.containsKey("textColor")) buttonStyle["textColor"] as String else "#FFFFFF"
    val fontSize: Float = if (buttonStyle.containsKey("fontSize"))(buttonStyle["fontSize"] as Double).toFloat() else 0f
    val fontWeight: String = if (buttonStyle.containsKey("fontWeight")) buttonStyle["fontWeight"] as String else "normal"
    val backgroundColor: String = if (buttonStyle.containsKey("backgroundColor")) buttonStyle["backgroundColor"] as String else "#000000"
    val borderWidth: Int = if (buttonStyle.containsKey("borderWidth")) (buttonStyle["borderWidth"] as Double).toInt() else 0
    val cornerRadius: Float = if (buttonStyle.containsKey("borderRadius"))(buttonStyle["borderRadius"] as Double).toFloat() else 0f
    val borderColor: String = if (buttonStyle.containsKey("borderColor")) buttonStyle["borderColor"] as String else "#000000"
    buttonPlaceUnAnchoredText = if (buttonStyle.containsKey("unAnchoredText")) buttonStyle["unAnchoredText"] as String else "Place"
    buttonPlaceAnchoredText = if (buttonStyle.containsKey("anchoredText")) buttonStyle["anchoredText"] as String else "Move"

    // Set button background
    val gradientDrawable = GradientDrawable()
    gradientDrawable.shape = GradientDrawable.RECTANGLE
    gradientDrawable.cornerRadius = cornerRadius
    gradientDrawable.setColor(Color.parseColor(backgroundColor))
    gradientDrawable.setStroke(borderWidth, Color.parseColor(borderColor)
    )

    button.background = gradientDrawable

    // Set font size
    if(fontSize != 0f) {
      button.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    }

    // Set font weight
    if(fontWeight == "bold") {
      button.setTypeface(null, Typeface.BOLD)
    }

    // Set button text
    button.setTextColor(Color.parseColor(textColor))
    button.text = buttonPlaceUnAnchoredText

    // Set button width/height
    val layoutParams = button.layoutParams
    if(buttonStyle.containsKey("width")) {
      val width = (buttonStyle["width"] as Double).toInt();
      layoutParams.width = (buttonStyle["width"] as Double).toInt()
    } else {
      val width = layoutParams.width
    }
    if(buttonStyle.containsKey("height")) {
      layoutParams.height = (buttonStyle["height"] as Double).toInt()
    }
    button.layoutParams = layoutParams
  }

  fun surfaceDetected() {
    surfaceDetected = true
    buttonPlace.visibility = View.VISIBLE
  }

  fun onAssetAnchored(isPlaced: Boolean) {
    isAnchored = isPlaced
    Handler(Looper.getMainLooper()).post {
      var buttonText = buttonPlaceUnAnchoredText
      if (isPlaced) {
        buttonText = buttonPlaceAnchoredText
      }
      buttonPlace.text = buttonText
    }
  }
}
