package app.syfre.syfre.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import app.syfre.syfre.ProgressBarAdapter
import app.syfre.syfre.utils.applyThemes
import app.syfre.syfre.utils.checkValidIntentData
import app.syfre.syfre.databinding.ActivitySummaryBinding
import app.syfre.syfre.shared.Addiction

class Summary : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding
    private lateinit var addiction: Addiction
    private lateinit var progressAdapter: ProgressBarAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addiction = Main.addictions[checkValidIntentData()]
        title = addiction.name


        progressAdapter = ProgressBarAdapter(addiction, this)
        binding.progressBarList.layoutManager = LinearLayoutManager(this)
        binding.progressBarList.setHasFixedSize(true)
        binding.progressBarList.adapter = progressAdapter
        (binding.progressBarList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun update() {
        progressAdapter.update()
    }
}
