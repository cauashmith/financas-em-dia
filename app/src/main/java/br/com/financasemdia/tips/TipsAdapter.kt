package br.com.financasemdia.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.financasemdia.R
import br.com.financasemdia.databinding.ItemTipBinding

class TipsAdapter(
    private val highlightedCategory: TipCategory?,
    private val onTipClick: (FinancialTip) -> Unit,
) : ListAdapter<FinancialTip, TipsAdapter.TipViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemTipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) = holder.bind(getItem(position))

    inner class TipViewHolder(private val binding: ItemTipBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: FinancialTip) {
            val context = binding.root.context
            binding.tipIcon.setImageResource(tip.category.iconRes)
            binding.tipCategoryTag.root.text = tip.category.label
            binding.tipTitle.text = tip.title
            binding.tipSummary.text = tip.summary

            binding.tipReadingTime.isVisible = tip.readingMinutes != null
            tip.readingMinutes?.let { minutes ->
                binding.tipReadingTime.text =
                    context.resources.getQuantityString(R.plurals.reading_time, minutes, minutes)
            }

            binding.tipRecommendedBadge.isVisible = tip.category == highlightedCategory

            binding.root.contentDescription = context.getString(
                R.string.tip_item_content_description, tip.title, tip.category.label, tip.summary,
            )
            binding.root.setOnClickListener { onTipClick(tip) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<FinancialTip>() {
            override fun areItemsTheSame(oldItem: FinancialTip, newItem: FinancialTip) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: FinancialTip, newItem: FinancialTip) = oldItem == newItem
        }
    }
}
