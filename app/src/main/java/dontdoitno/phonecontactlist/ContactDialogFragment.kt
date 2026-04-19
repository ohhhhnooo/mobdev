package dontdoitno.phonecontactlist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment


// Диалоговый фрагмент для отображения имени и номера контакта
// Использует DialogFragment, чтобы диалог сохранялся при повороте экрана
class ContactDialogFragment : DialogFragment() {

    // Создаёт AlertDialog с именем контакта в заголовке и номером в тексте
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = requireArguments().getString("name", "")
        val number = requireArguments().getString("number", "")
        return AlertDialog.Builder(requireContext())
            .setTitle(name)
            .setMessage(number)
            .setPositiveButton(R.string.dialog_ok) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    companion object {
        // Создаёт экземпляр фрагмента с именем и номером, переданными через аргументы
        fun newInstance(name: String, number: String): ContactDialogFragment {
            val fragment = ContactDialogFragment()
            fragment.arguments = Bundle().apply {
                putString("name", name)
                putString("number", number)
            }
            return fragment
        }
    }
}