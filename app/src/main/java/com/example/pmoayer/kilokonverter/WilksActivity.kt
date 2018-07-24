package com.example.pmoayer.kilokonverter

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_wilks.*

class WilksActivity : BaseCompatActivity() {
    val m_a = -216.0475144
    val m_b = 16.2606339
    val m_c = -0.002388645
    val m_d = -0.00113732
    val m_e = 7.01863E-06
    val m_f = -1.291E-08


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wilks)

        //false == male; true == female
        calculateButton.setOnClickListener {
            var gender = false
            if (sexSwitch.isChecked) {
                gender = true
            }

            var liftedWeight = inputLiftedWeight.text.toString().toDouble()
            var bodyWeight = inputBodyweight.text.toString().toDouble()

            //if not checked, convert weights to kilos
            if (!unitSwitch.isChecked) {
                liftedWeight = liftedWeight / 2.20462
                bodyWeight = bodyWeight / 2.20462
            }


        }
    }


    fun calculateWilks(liftedWeight : Double, bodyWeight : Double, gender : Boolean) {

    }
}