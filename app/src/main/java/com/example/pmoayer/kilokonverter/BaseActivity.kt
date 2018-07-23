package com.example.pmoayer.kilokonverter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.my_toolbar.*

open class BaseCompatActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.configureWeights -> {
                startActivity(Intent(this, ConfigureActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}