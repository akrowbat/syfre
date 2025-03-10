package app.syfre.syfre.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import app.syfre.syfre.MilestoneAdapter
import app.syfre.syfre.R
import app.syfre.syfre.databinding.ActivityMilestonesBinding
import app.syfre.syfre.databinding.DialogAddMilestoneBinding
import app.syfre.syfre.shared.Addiction
import app.syfre.syfre.shared.CacheHandler
import app.syfre.syfre.utils.applyThemes
import app.syfre.syfre.utils.checkValidIntentData
import app.syfre.syfre.utils.isInputEmpty
import app.syfre.syfre.utils.showConfirmDialog
import app.syfre.syfre.utils.write
import kotlinx.datetime.DateTimeUnit

class Milestones : AppCompatActivity() {

    private lateinit var binding: ActivityMilestonesBinding
    private lateinit var addiction: Addiction
    private lateinit var cacheHandler: CacheHandler
    private lateinit var adapter: MilestoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityMilestonesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cacheHandler = CacheHandler(this)

        addiction = Main.addictions[checkValidIntentData()]
        adapter = MilestoneAdapter(addiction, this) {
            val action: () -> Unit = {
                addiction.milestones.remove(it)
                cacheHandler.write()
                update()
            }
            showConfirmDialog(getString(R.string.delete), getString(R.string.delete_milestone_confirm), action)
        }
        binding.milestoneList.layoutManager = LinearLayoutManager(this)
        binding.milestoneList.setHasFixedSize(true)
        binding.milestoneList.adapter = adapter

        binding.addMilestoneFab.setOnClickListener {
            val dialogViewBinding = DialogAddMilestoneBinding.inflate(layoutInflater)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(dialogViewBinding.root)
            val units = resources.getStringArray(R.array.time_units)
            dialogViewBinding.btnSaveMilestone.setOnClickListener {
                val inputFields = listOf(dialogViewBinding.milestoneNumberInputLayout, dialogViewBinding.milestoneTimeUnitInputLayout)
                when {
                    dialogViewBinding.milestoneNumberInput.isInputEmpty() ||
                            dialogViewBinding.milestoneNumberInput.text.toString().toInt() == 0 -> {
                        dialogViewBinding.milestoneNumberInputLayout.error = getString(R.string.error_empty_amount)
                        inputFields.forEach { if (it !== dialogViewBinding.milestoneNumberInputLayout) it.error = null }
                    }
                    dialogViewBinding.unitInput.text.toString().isEmpty() -> {
                        dialogViewBinding.milestoneTimeUnitInputLayout.error = getString(R.string.error_empty_unit)
                        inputFields.forEach { if (it !== dialogViewBinding.milestoneTimeUnitInputLayout) it.error = null }
                    }
                    else -> {
                        val num = dialogViewBinding.milestoneNumberInput.text.toString().toInt()
                        when (dialogViewBinding.unitInput.text.toString()) {
                            units[0] -> addiction.milestones.add(Pair(num, DateTimeUnit.HOUR))
                            units[1] -> addiction.milestones.add(Pair(num, DateTimeUnit.DAY))
                            units[2] -> addiction.milestones.add(Pair(num, DateTimeUnit.WEEK))
                            units[3] -> addiction.milestones.add(Pair(num, DateTimeUnit.MONTH))
                            units[4] -> addiction.milestones.add(Pair(num, DateTimeUnit.YEAR))
                        }
                        cacheHandler.write()
                        adapter.update()
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }
    }

    private fun update() {
        cacheHandler.write()
        adapter.update()
    }
}
