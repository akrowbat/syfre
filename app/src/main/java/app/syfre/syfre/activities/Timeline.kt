package app.syfre.syfre.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import app.syfre.syfre.R
import app.syfre.syfre.TimelineAdapter
import app.syfre.syfre.TimelineAdapterAlt
import app.syfre.syfre.databinding.ActivityTimelineBinding
import app.syfre.syfre.shared.Addiction
import app.syfre.syfre.utils.applyThemes
import app.syfre.syfre.utils.checkValidIntentData
import app.syfre.syfre.utils.convertSecondsToString
import app.syfre.syfre.utils.getAltTimelinePref
import app.syfre.syfre.utils.getSharedPref
import app.syfre.syfre.utils.toast

class Timeline : AppCompatActivity() {

    private lateinit var binding: ActivityTimelineBinding
    private lateinit var addiction: Addiction

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val useAltTimelinePref = getSharedPref().getAltTimelinePref()
        addiction = Main.addictions[checkValidIntentData()]
        binding.timelineNotice.text = getString(R.string.showing_timeline, addiction.name)
        with(binding.timelineList) {
            layoutManager = LinearLayoutManager(this@Timeline)
            setHasFixedSize(true)
            if (!useAltTimelinePref)
                addItemDecoration(DividerItemDecoration(this@Timeline, DividerItemDecoration.VERTICAL))
            adapter = if (useAltTimelinePref) TimelineAdapterAlt(this@Timeline, addiction)
            else TimelineAdapter(addiction, this@Timeline)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timeline, menu)
        return true
    }
    @SuppressLint("StringFormatMatches")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun buildNonZeroIndicesAsString(list: List<Int>): StringBuilder = StringBuilder().apply {
            list.forEachIndexed { index, i ->
                if (index > 0) {
                    append(", ${i + 1}")
                }
            }
        }

        val id = item.itemId
        if (id == R.id.calc_avg_duration) {
            if (addiction.history.size == 1 || (addiction.history.size == 2 && addiction.history.values.last() == 0L)) {
                toast(R.string.only_one_attempt)
            } else {
                val checked = when (addiction.status) {
                    Addiction.Status.Ongoing,
                    Addiction.Status.Future -> BooleanArray(addiction.history.size - 1)

                    Addiction.Status.Stopped -> BooleanArray(addiction.history.size)
                }
                val items = List(checked.size) { index -> getString(R.string.attempt, index + 1) }.toTypedArray()
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.calc_avg_durations)
                    .setMultiChoiceItems(items, checked) { _, which, isChecked -> checked[which] = isChecked }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val indices = checked.mapIndexed { index, b ->
                            if (b) index else -1
                        }.filter { it != -1 }
                        when (indices.size) {
                            0 -> toast(R.string.nothing_is_chosen)
                            1 -> toast(R.string.only_one_attempt_is_chosen)
                            else -> binding.avgDuration.text = StringBuilder(getString(R.string.avg_duration))
                                .append(" ${indices[0] + 1}")
                                .append(buildNonZeroIndicesAsString(indices))
                                .append(": ${convertSecondsToString(addiction.calculateAvgRelapseDuration(indices) / 1000)}")
                        }
                    }
                    .show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
