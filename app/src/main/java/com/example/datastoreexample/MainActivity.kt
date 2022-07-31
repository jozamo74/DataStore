package com.example.datastoreexample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.datastoreexample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Delegate -> singleton
val Context.dataStore by preferencesDataStore(name = "USER_PREFERENCES_NAME")

private lateinit var binding: ActivityMainBinding



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun setListener() {
        binding.btnSave.setOnClickListener {

           if (validateFields()) {
               val nombre = binding.etNombre.text.toString().trim()
               Toast.makeText(this, "guardamos '$nombre'", Toast.LENGTH_SHORT).show()
               lifecycleScope.launch(Dispatchers.IO) {

                   saveFields(nombre, binding.cbVIP.isChecked)
               }
           } else {

               Toast.makeText(this, "No guardamos", Toast.LENGTH_SHORT).show()

           }
        }

        binding.btnRecover.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){

                getUserProfile().collect{ userProfile ->
                    withContext(Dispatchers.Main){

                        Snackbar.make(binding.root, "nombre:${userProfile.name} es VIP: ${userProfile.vip}", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    private fun getUserProfile() = dataStore.data.map { preferences -> (
            UserProfile(
            name = preferences[stringPreferencesKey("NAME")].orEmpty(),
            vip = preferences[booleanPreferencesKey("VIP")] ?: false
        ))
    }

    private suspend fun saveFields(name: String, checked: Boolean) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("NAME")] = name
            preferences[booleanPreferencesKey("VIP")] = checked

        }
    }

    private fun validateFields(): Boolean {

        var isValid = true

        val name = binding.etNombre.text.toString().trim()
        if (binding.etNombre.text.toString().trim().isEmpty()) {
            binding.tilNombre.error = getString(R.string.required_name)
            binding.tilNombre.requestFocus()
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        return isValid
    }
}