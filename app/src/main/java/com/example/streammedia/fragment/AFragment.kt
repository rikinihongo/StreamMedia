package com.example.streammedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.streammedia.R
import com.example.streammedia.databinding.FragmentABinding
import com.example.streammedia.sheet.ExampleBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AFragment : Fragment() {

    private var _binding: FragmentABinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentABinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToB.setOnClickListener {
            parentFragmentManager.beginTransaction()
                //.setReorderingAllowed(true)
                /*.setCustomAnimations(
                    R.anim.slide_in_right,   // enter
                    R.anim.slide_out_left,   // exit
                    R.anim.slide_in_left,    // popEnter
                    R.anim.slide_out_right   // popExit
                )*/
                .setCustomAnimations(
                    R.anim.slide_in, // enter
                    R.anim.fade_out, // exit
                    R.anim.fade_in, // popEnter
                    R.anim.slide_out // popExit
                )
                .replace(R.id.container, BFragment())
                .addToBackStack(null)
                .commit()

        }

        binding.btnShowBottomSheet.setOnClickListener {
            /*ExampleBottomSheet()
                .show(parentFragmentManager, ExampleBottomSheet::class.java.simpleName)*/
            showBottomSheetOnce(
                sheet = ExampleBottomSheet(),
                tag = ExampleBottomSheet::class.java.simpleName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

fun Fragment.showBottomSheetOnce(
    sheet: BottomSheetDialogFragment,
    tag: String
) {
    val fm = parentFragmentManager
    if (fm.isStateSaved) return
    if (fm.findFragmentByTag(tag) != null) return

    sheet.show(fm, tag)
}
