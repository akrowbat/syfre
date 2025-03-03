package app.syfre.syfre.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import app.syfre.syfre.NoteAdapter
import app.syfre.syfre.R
import app.syfre.syfre.databinding.ActivityDailyNotesBinding
import app.syfre.syfre.databinding.DialogAddNoteBinding
import app.syfre.syfre.shared.Addiction
import app.syfre.syfre.shared.CacheHandler
import app.syfre.syfre.utils.applyThemes
import app.syfre.syfre.utils.checkValidIntentData
import app.syfre.syfre.utils.getDateFormatPattern
import app.syfre.syfre.utils.getSharedPref
import app.syfre.syfre.utils.isInputEmpty
import app.syfre.syfre.utils.showConfirmDialog
import app.syfre.syfre.utils.write
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

class DailyNotes : AppCompatActivity() {

    private lateinit var binding: ActivityDailyNotesBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var dateFormat: DateTimeFormatter
    private lateinit var addiction: Addiction
    private lateinit var cacheHandler: CacheHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityDailyNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dateFormat = DateTimeFormatter.ofPattern(getSharedPref().getDateFormatPattern())
        cacheHandler = CacheHandler(this)

        addiction = Main.addictions[checkValidIntentData()]
        adapter = NoteAdapter(addiction, this, { showAddNoteDialog(true, it.first) },
            {
                val action: () -> Unit = {
                    addiction.dailyNotes.remove(it.first)
                    updateNotesList()
                }
                showConfirmDialog(getString(R.string.delete), getString(R.string.delete_note_confirm, dateFormat.format(it.first.toJavaLocalDate())), action)
            })
        binding.notesList.layoutManager = LinearLayoutManager(this)
        binding.notesList.adapter = adapter

        binding.addNoteFab.setOnClickListener { showAddNoteDialog(false, Clock.System.now().toLocalDateTime(
            TimeZone.currentSystemDefault()).date) }
    }

    private fun showAddNoteDialog(isEdit: Boolean, date: LocalDate) {
        var pickedDate = date
        val dialogViewBinding = DialogAddNoteBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogViewBinding.root)
        if (isEdit) {
            dialogViewBinding.dateStr.visibility = View.GONE
            dialogViewBinding.noteDate.visibility = View.GONE
            dialogViewBinding.noteInput.setText(addiction.dailyNotes[date])
        } else {
            dialogViewBinding.noteDate.text = dateFormat.format(pickedDate.toJavaLocalDate())
            dialogViewBinding.noteDate.setOnClickListener {
                with(MaterialDatePicker.Builder.datePicker().build()) {
                    addOnPositiveButtonClickListener {
                        pickedDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        dialogViewBinding.noteDate.text = dateFormat.format(pickedDate.toJavaLocalDate())
                        if (addiction.dailyNotes.contains(pickedDate)) {
                            dialogViewBinding.noteInput.setText(addiction.dailyNotes[pickedDate])
                        }
                    }
                    show(supportFragmentManager, null)
                }
            }
        }
        dialogViewBinding.btnSave.setOnClickListener {
            if (dialogViewBinding.noteInput.isInputEmpty()) {
                dialogViewBinding.noteInputLayout.error = getString(R.string.error_empty_note)
            } else {
                addiction.dailyNotes[pickedDate] = dialogViewBinding.noteInput.text.toString()
                updateNotesList()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun updateNotesList() {
        cacheHandler.write()
        adapter.update()
    }
}
