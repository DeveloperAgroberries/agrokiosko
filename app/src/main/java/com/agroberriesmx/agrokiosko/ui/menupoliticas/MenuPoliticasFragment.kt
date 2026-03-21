package com.agroberriesmx.agrokiosko.ui.menupoliticas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.agroberriesmx.agrokiosko.R
import com.agroberriesmx.agrokiosko.databinding.FragmentMenuPoliticasBinding

class MenuPoliticasFragment : Fragment() {

    private var _binding: FragmentMenuPoliticasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuPoliticasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val containerSexual = binding.btnPoliticaSexual
        val containerRepresalias = binding.btnPoliticaRepresalias
        val containerJobNoforzado = binding.btnPoliticaJobNoforzado
        val containerInfantil = binding.btnPoliticaInfantil
        val containerBuzon = binding.btnPolitica5
        val containerLibertad = binding.btnPolitica6
        val containerDiscrimincion = binding.btnPolitica7
        val containerDH = binding.btnPolitica8
        val containerSanciones = binding.btnPolitica9

        containerSexual.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-010%20Politica%20de%20no%20acoso%20sexual.pdf"
            navigateToPolitica(url)
        }

        containerRepresalias.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-011%20Politica%20de%20no%20represalias.pdf"
            navigateToPolitica(url)
        }

        containerJobNoforzado.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-012%20Politica%20de%20no%20trabajo%20forzado.pdf"
            navigateToPolitica(url)
        }

        containerInfantil.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-006%20Politica%20de%20no%20trabajo%20infantil.pdf"
            navigateToPolitica(url)
        }

        containerBuzon.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-015%20Politica%20de%20buzón.pdf"
            navigateToPolitica(url)
        }

        containerLibertad.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-018%20Politica%20de%20libertad%20de%20asociación.pdf"
            navigateToPolitica(url)
        }

        containerDiscrimincion.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-033%20Politica%20de%20no%20discriminación.pdf"
            navigateToPolitica(url)
        }

        containerDH.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-034%20Politica%20de%20derechos%20humanos.pdf"
            navigateToPolitica(url)
        }

        containerSanciones.setOnClickListener {
            val url = "https://raw.githubusercontent.com/DeveloperAgroberries/politicas/main/PO-RH-035%20Politica%20de%20aplicación%20de%20sanciones.pdf"
            navigateToPolitica(url)
        }

        return root
    }

    private fun navigateToPolitica(url: String) {
        val bundle = Bundle().apply {
            putString("pdf_url", url)
        }
        findNavController().navigate(R.id.action_menuPoliticasFragment_to_politicasFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}