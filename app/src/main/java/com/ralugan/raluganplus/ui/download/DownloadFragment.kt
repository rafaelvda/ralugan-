package com.ralugan.raluganplus.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ralugan.raluganplus.databinding.FragmentDownloadBinding

class DownloadFragment : Fragment() {

    private var _binding: FragmentDownloadBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DownloadViewModel::class.java)

        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDownload
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}