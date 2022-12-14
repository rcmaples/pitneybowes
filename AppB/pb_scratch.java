// Method to create instance of SharedPref
private fun getPref(context: Context): SharedPref? {
    this.context = context
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    if(sharedPreferences != null){
        sharedPreferences = EncryptedSharedPreferences
        .create(
            sharedPrefFile,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }
    return sharedPreferences
}

private fun putString(key: String, data: String) {
    val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
    editor?.putString(key, data)
    editor?.apply()
    editor?.commit()
}

// Generic method for getting String data in preference
private fun getString(key: String): String? {
    return sharedPreferences?.getString(key, null)
 }

// Method to access
fun getDepartmentId(context: Context) :String {
    return SharedPref.getPref(context)?.getString(“dept_id”) ?: ""
}