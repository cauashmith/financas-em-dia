package br.com.financasemdia.tips

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import br.com.financasemdia.R
import br.com.financasemdia.databinding.ActivityTipDetailBinding

class TipDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTipDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tip = MockTips.findById(intent.getIntExtra(EXTRA_TIP_ID, INVALID_ID))
        if (tip == null) {
            Toast.makeText(this, R.string.tip_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityTipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.toolbar.title = tip.category.label
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.detailContainer, TipDetailFragment.newInstance(tip.id))
            }
        }
    }

    companion object {
        private const val EXTRA_TIP_ID = "extra_tip_id"
        private const val INVALID_ID = -1

        fun newIntent(context: Context, tipId: Int): Intent =
            Intent(context, TipDetailActivity::class.java).putExtra(EXTRA_TIP_ID, tipId)
    }
}
