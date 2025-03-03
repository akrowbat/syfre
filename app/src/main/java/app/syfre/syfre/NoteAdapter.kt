package app.syfre.syfre

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import app.syfre.syfre.R
import app.syfre.syfre.databinding.ListItemNoteBinding
import app.syfre.syfre.shared.Addiction
import app.syfre.syfre.shared.SortMode
import app.syfre.syfre.utils.getDateFormatPattern
import app.syfre.syfre.utils.getSharedPref
import app.syfre.syfre.utils.getSortNotesPref
import app.syfre.syfre.utils.toggleVisibility
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

class NoteAdapter(
    private val addiction: Addiction, context: Context,
    private val editButtonAction: (Pair<LocalDate, String>) -> Unit,
    private val deleteButtonAction: (Pair<LocalDate, String>) -> Unit
) :
    ListAdapter<Pair<LocalDate, String>, NoteAdapter.NoteViewHolder>(object :
        DiffUtil.ItemCallback<Pair<LocalDate, String>>() {
        override fun areItemsTheSame(
            oldItem: Pair<LocalDate, String>,
            newItem: Pair<LocalDate, String>
        ): Boolean = oldItem.first == newItem.first

        override fun areContentsTheSame(
            oldItem: Pair<LocalDate, String>,
            newItem: Pair<LocalDate, String>
        ): Boolean = oldItem.second == newItem.second
    }) {
    private val preferences = context.getSharedPref()

    init { update() }

    private val dateFormat = DateTimeFormatter.ofPattern(preferences.getDateFormatPattern())

    fun update() {
        submitList(when (preferences.getSortNotesPref()) {
            "asc" -> addiction.getDailyNotesList(SortMode.ASC)
            "desc" -> addiction.getDailyNotesList(SortMode.DESC)
            else -> addiction.getDailyNotesList(SortMode.NONE)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding,
            { editButtonAction(currentList[it]) },
            { deleteButtonAction(currentList[it]) })
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val pair = currentList[position]
        holder.date.text = dateFormat.format(pair.first.toJavaLocalDate())
        holder.note.text = pair.second
    }

    class NoteViewHolder(
        binding: ListItemNoteBinding, editButtonAction: (Int) -> Unit,
        deleteButtonAction: (Int) -> Unit
    ) : ViewHolder(binding.root) {
        val date = binding.date
        val note = binding.note

        init {
            binding.btnEdit.setOnClickListener { editButtonAction(adapterPosition) }
            binding.btnDelete.setOnClickListener { deleteButtonAction(adapterPosition) }
            binding.btnExpandCollapse.setOnClickListener {
                binding.noteCard.toggleVisibility()
                binding.btnExpandCollapse.setImageResource(if (binding.noteCard.visibility == View.VISIBLE) R.drawable.expand_less_24px else R.drawable.expand_more_24px)
                binding.btnExpandCollapse.contentDescription =
                    if (binding.noteCard.visibility == View.VISIBLE) it.context.getString(R.string.collapse) else it.context.getString(
                        R.string.expand
                    )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    binding.btnExpandCollapse.tooltipText =
                        if (binding.noteCard.visibility == View.VISIBLE) it.context.getString(R.string.collapse) else it.context.getString(
                            R.string.expand
                        )
            }
        }
    }
}
