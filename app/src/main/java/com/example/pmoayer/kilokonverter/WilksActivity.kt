package com.example.pmoayer.kilokonverter

import android.os.Bundle
import android.view.Menu
import kotlinx.android.synthetic.main.activity_wilks.*
import kotlinx.android.synthetic.main.my_toolbar.*
import kotlin.math.pow

class WilksActivity : BaseCompatActivity() {
    val m_a = -216.0475144
    val m_b = 16.2606339
    val m_c = -0.002388645
    val m_d = -0.00113732
    val m_e = 7.01863E-06
    val m_f = -1.291E-08

    val f_a = 594.31747775582
    val f_b = -27.23842536447
    val f_c = 0.82112226871
    val f_d = -0.00930733913
    val f_e = 4.731582E-05
    val f_f = -9.054E-08

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_no_icons, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wilks)
        setSupportActionBar(my_toolbar)
        supportActionBar?.title = "Wilks Calculator"

        //false == male ... true == female
        calculateButton.setOnClickListener {
            var gender = sexSwitch.isChecked
            var liftedWeight = inputLiftedWeight.text.toString().toDouble()
            var bodyWeight = inputBodyweight.text.toString().toDouble()

            //if not checked, convert weights to kilos
            if (!unitSwitch.isChecked) {
                liftedWeight /= 2.20462
                bodyWeight /= 2.20462
            }
            displayResult.text = (calculateWilks(liftedWeight, bodyWeight, gender)).toString()
            Utils.hideKeyboard(this)
        }
    }

    fun calculateWilks(liftedWeight : Double, bodyWeight : Double, gender : Boolean) : Double {
            val x = bodyWeight
            var tmp = 0.0
            if (gender) {
                tmp = f_a + f_b*x + f_c*(x.pow(2)) + f_d*(x.pow(3)) + f_e*(x.pow(4)) + f_f*(x.pow(5))
            } else {
                tmp = m_a + m_b*x + m_c*(x.pow(2)) + m_d*(x.pow(3)) + m_e*(x.pow(4)) + m_f*(x.pow(5))
            }
            return liftedWeight * (500.0 / tmp)
    }
}