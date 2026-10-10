package br.com.financasemdia.tips

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import br.com.financasemdia.R
import br.com.financasemdia.databinding.ActivityTipsBinding

class TipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.toolbar.setNavigationOnClickListener { finish() }

        val highlighted = TipCategory.fromNameOrNull(intent.getStringExtra(EXTRA_HIGHLIGHT_CATEGORY))

        binding.recommendationBanner.isVisible = highlighted != null
        highlighted?.let {
            binding.recommendationBanner.text = getString(R.string.tips_recommendation_banner, it.label)
        }

        val tips = MockTips.all.sortedByDescending { it.category == highlighted }

        val adapter = TipsAdapter(highlighted) { tip ->
            startActivity(TipDetailActivity.newIntent(this, tip.id))
        }
        binding.tipsList.adapter = adapter
        adapter.submitList(tips)
    }

    companion object {
        private const val EXTRA_HIGHLIGHT_CATEGORY = "extra_highlight_category"

        fun newIntent(context: Context, highlightCategory: String?): Intent =
            Intent(context, TipsActivity::class.java).apply {
                highlightCategory?.let { putExtra(EXTRA_HIGHLIGHT_CATEGORY, it) }
            }
    }
}
