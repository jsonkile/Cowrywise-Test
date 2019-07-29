package com.golde.cowrywise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.golde.cowrywise.Data.Bits
import com.golde.cowrywise.Util.REALM_KEY
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_api_key.*
import kotlinx.android.synthetic.main.activity_main.*

class ApiKeyActivity : AppCompatActivity() {

    private val realm = Realm.getDefaultInstance()
    private val key = realm.where(Bits::class.java).equalTo("id", REALM_KEY).findFirst()?.apiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_key)

        progressBar.visibility = View.INVISIBLE
        if(key.isNullOrBlank()) input_api.hint = "Api Key" else input_api.setText(key)

        enter_api_btn.setOnClickListener {
            saveKey()
        }
    }

    private fun saveKey(){
        if(!input_api.text.toString().isBlank()) {
            realm.executeTransaction {
                progressBar.visibility = View.VISIBLE
                it.copyToRealmOrUpdate(Bits(REALM_KEY, input_api.text.toString()))
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
