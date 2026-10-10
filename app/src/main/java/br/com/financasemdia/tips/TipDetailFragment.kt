package br.com.financasemdia.tips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import br.com.financasemdia.R
import br.com.financasemdia.databinding.FragmentTipDetailBinding
import br.com.financasemdia.databinding.ViewTipStepBinding
import com.google.android.material.snackbar.Snackbar

class TipDetailFragment : Fragment() {

    private var _binding: FragmentTipDetailBinding? = null
    private val binding get() = _binding!!

    private var checkedSteps: BooleanArray = BooleanArray(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTipDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tip = MockTips.findById(requireArguments().getInt(ARG_TIP_ID)) ?: return

        checkedSteps = savedInstanceState?.getBooleanArray(STATE_CHECKED_STEPS)
            ?.takeIf { it.size == tip.steps.size }
            ?: BooleanArray(tip.steps.size)

        bindTip(tip)
        inflateSteps(tip)
        updateProgress(announceCompletion = false)
    }

    private fun bindTip(tip: FinancialTip) {
        binding.tipCategoryTag.root.text = tip.category.label
        binding.tipIcon.setImageResource(tip.category.iconRes)
        binding.tipTitle.text = tip.title
        binding.tipContent.text = tip.content

        binding.tipReadingTime.isVisible = tip.readingMinutes != null
        tip.readingMinutes?.let {
            binding.tipReadingTime.text = resources.getQuantityString(R.plurals.reading_time, it, it)
        }

        binding.tipSource.isVisible = tip.source != null
        tip.source?.let { binding.tipSource.text = getString(R.string.tip_source, it) }
    }

    private fun inflateSteps(tip: FinancialTip) {
        binding.stepsContainer.removeAllViews()
        tip.steps.forEachIndexed { index, step ->
            val stepBinding = ViewTipStepBinding.inflate(layoutInflater, binding.stepsContainer, true)
            stepBinding.stepCheckBox.apply {
                text = step
                isSaveEnabled = false
                isChecked = checkedSteps[index]
                setOnCheckedChangeListener { _, isChecked ->
                    checkedSteps[index] = isChecked
                    updateProgress(announceCompletion = true)
                }
            }
        }
    }

    private fun updateProgress(announceCompletion: Boolean) {
        val done = checkedSteps.count { it }
        val total = checkedSteps.size
        binding.stepsProgressText.text = getString(R.string.tip_steps_progress, done, total)
        binding.stepsProgress.max = total
        binding.stepsProgress.setProgressCompat(done, true)

        if (announceCompletion && total > 0 && done == total) {
            Snackbar.make(binding.root, R.string.tip_all_steps_done, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(STATE_CHECKED_STEPS, checkedSteps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TIP_ID = "arg_tip_id"
        private const val STATE_CHECKED_STEPS = "state_checked_steps"

        fun newInstance(tipId: Int) = TipDetailFragment().apply {
            arguments = bundleOf(ARG_TIP_ID to tipId)
        }
    }
}
