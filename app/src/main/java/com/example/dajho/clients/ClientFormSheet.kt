package com.example.dajho.clients

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import java.util.Locale

/**
 * BottomSheetDialogFragment para crear y editar clientes (Fase 2).
 */
class ClientFormSheet : BottomSheetDialogFragment() {

    var onClientSaved: ((Client) -> Unit)? = null

    private lateinit var tvTitle: TextView
    private lateinit var tilName: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etInfo: TextInputEditText
    private lateinit var etDebt: TextInputEditText
    private lateinit var btnSave: View
    private lateinit var btnClose: ImageButton

    private var editClientId: Long = -1L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        @Suppress("DEPRECATION")
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_client_form, container, false)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tvClientFormTitle)
        tilName = view.findViewById(R.id.tilClientName)
        tilPhone = view.findViewById(R.id.tilClientPhone)
        etName = view.findViewById(R.id.etClientName)
        etPhone = view.findViewById(R.id.etClientPhone)
        etInfo = view.findViewById(R.id.etClientInfo)
        etDebt = view.findViewById(R.id.etClientDebt)
        btnSave = view.findViewById(R.id.btnSaveClient)
        btnClose = view.findViewById(R.id.btnCloseClientForm)

        btnClose.setOnClickListener { dismiss() }

        val isEdit = arguments?.getBoolean(ARG_IS_EDIT, false) ?: false
        if (isEdit) {
            tvTitle.text = getString(R.string.title_modal_edit_client)
            editClientId = arguments?.getLong(ARG_ID) ?: -1L
            etName.setText(arguments?.getString(ARG_NAME) ?: "")
            etPhone.setText(arguments?.getString(ARG_PHONE) ?: "")
            etInfo.setText(arguments?.getString(ARG_INFO) ?: "")
            val debtVal = arguments?.getDouble(ARG_DEBT) ?: 0.0
            etDebt.setText(String.format(Locale.US, "%.2f", debtVal))
        } else {
            tvTitle.text = getString(R.string.title_modal_new_client)
            etDebt.setText("0.00")
        }

        btnSave.setOnClickListener {
            tilName.error = null
            tilPhone.error = null

            val name = etName.text?.toString()?.trim().orEmpty()
            val phone = etPhone.text?.toString()?.trim().orEmpty()
            val info = etInfo.text?.toString()?.trim().orEmpty()
            val debt = etDebt.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            var valid = true
            if (name.isEmpty()) {
                tilName.error = getString(R.string.error_required_field)
                valid = false
            }
            if (phone.isEmpty()) {
                tilPhone.error = getString(R.string.error_required_field)
                valid = false
            }

            if (!valid) return@setOnClickListener

            val savedClient = Client(
                id = if (editClientId != -1L) editClientId else System.nanoTime(),
                name = name,
                phone = phone,
                info = info,
                debt = debt
            )
            onClientSaved?.invoke(savedClient)
            Toast.makeText(requireContext(), R.string.saved_client_ok, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ClientFormSheet"
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_PHONE = "arg_phone"
        private const val ARG_INFO = "arg_info"
        private const val ARG_DEBT = "arg_debt"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(client: Client?): ClientFormSheet {
            val sheet = ClientFormSheet()
            if (client != null) {
                val args = Bundle().apply {
                    putLong(ARG_ID, client.id)
                    putString(ARG_NAME, client.name)
                    putString(ARG_PHONE, client.phone)
                    putString(ARG_INFO, client.info)
                    putDouble(ARG_DEBT, client.debt)
                    putBoolean(ARG_IS_EDIT, true)
                }
                sheet.arguments = args
            }
            return sheet
        }
    }
}
