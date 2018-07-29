package com.moayer.puya.kilokonverter


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.my_toolbar.*


class MainActivity : BaseCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var activeSet = ArrayList<Weight>()
    private var activeBar = Weight(0.00, "barbell", true)
    private lateinit var kiloSet: ArrayList<Weight>
    private lateinit var kiloBars: ArrayList<Weight>
    private lateinit var poundSet: ArrayList<Weight>
    private lateinit var poundBars: ArrayList<Weight>
    private lateinit var activeBarRadio : RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)
        //stop soft keyboard from pushing up views
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //Get user's weight sets and barbell configuration
        val weightSets = Utils.verifyOrCreateSets(this)
        val barbellSets = Utils.verifyOrCreateBarbells(this)
        kiloSet = weightSets.first
        kiloBars = barbellSets.first
        poundSet = weightSets.second
        poundBars = barbellSets.second

        //Programmatically create both Kilo and Pound Barbell Radio Groups
        setupBarbellRadio(kiloBars, kilo_barbell_buttons, this)
        setupBarbellRadio(poundBars, pound_barbell_buttons, this)

        //Switch information when units are changed
        unitsRadioGroup.setOnCheckedChangeListener{ group, checkedId ->
            //Switch active data and change visibilities
            activeSet.clear()
            if (checkedId == R.id.kiloButton) {
                activeSet.addAll(poundSet)
                kilo_barbell_buttons.visibility = View.GONE
                pound_barbell_buttons.visibility = View.VISIBLE
                activeBarRadio = pound_barbell_buttons
            } else {
                activeSet.addAll(kiloSet)
                kilo_barbell_buttons.visibility = View.VISIBLE
                pound_barbell_buttons.visibility = View.GONE
                activeBarRadio = kilo_barbell_buttons
            }
            viewAdapter.notifyDataSetChanged()

            //Change constraints for "Pick Barbell" Text View
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainConstraintLayout)
            constraintSet.clear(pickBarbell.id, ConstraintSet.BOTTOM)
            if (checkedId == R.id.kiloButton) {
                constraintSet.connect(pickBarbell.id, ConstraintSet.BOTTOM, pound_barbell_buttons.id, ConstraintSet.TOP)
            } else {
                constraintSet.connect(pickBarbell.id, ConstraintSet.BOTTOM, kilo_barbell_buttons.id, ConstraintSet.TOP)
            }
            constraintSet.applyTo(mainConstraintLayout)
        }

        //Recycler View setup for weight set
        viewManager = GridLayoutManager(this, 4)
        viewAdapter = WeightAdapter(activeSet)
        recyclerView = weightset_recycler_view.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            overScrollMode = RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        //Default to Kilo button
        unitsRadioGroup.check(R.id.kiloButton)

        convertButton.setOnClickListener {
            if (TextUtils.isEmpty(inputWeight.text)) {
                Toast.makeText(this,"Enter weight", Toast.LENGTH_SHORT).show()
            } else {
                //Remove old results.
                displayConvertedWeight.text = ""
                displayFrequencies.text = ""

                val weight: Double = inputWeight.text.toString().toDouble()
                val frequencyMap: HashMap<Weight, Int>
                val conversion: String

                //Holds the returned Triple from the appropriate util conversion method
                val tmp: Triple<Double, Weight, HashMap<Weight, Int>>

                if (unitsRadioGroup.checkedRadioButtonId == kiloButton.id) {
                    val barbellRadioButton = pound_barbell_buttons.findViewById<RadioButton>(pound_barbell_buttons.checkedRadioButtonId)
                    activeBar = poundBars[pound_barbell_buttons.indexOfChild(barbellRadioButton)]
                    tmp = Utils.kilogramsToPounds(weight, activeSet, activeBar, this)
                    conversion = "%.3f".format(tmp.first) + " lbs"
                } else {
                    val barbellRadioButton = kilo_barbell_buttons.findViewById<RadioButton>(kilo_barbell_buttons.checkedRadioButtonId)
                    activeBar = kiloBars[kilo_barbell_buttons.indexOfChild(barbellRadioButton)]
                    tmp = Utils.poundsToKilograms(weight, activeSet, activeBar, this)
                    conversion = "%.3f".format(tmp.first) + " kgs"
                }

                if (!convertBox.isChecked && !splitBox.isChecked) {
                    goatImage.visibility = View.VISIBLE
                    Toast.makeText(this,"Check \"Convert\" or \"Split\" option to do something", Toast.LENGTH_LONG).show()
                } else {
                    goatImage.visibility = View.GONE
                    if (convertBox.isChecked) {
                        displayConvertedWeight.text = conversion
                    }

                    if (splitBox.isChecked) {
                        val barbell = tmp.second
                        frequencyMap = tmp.third
                        val entries = frequencyMap.entries
                        var frequencyString = barbell.weight.toString() + " barbell\n"
                        for ((key, value) in entries.sortedByDescending { it.key.weight }) {
                            if (value != 0) frequencyString += value.toString() + "x" + key.weight + ", "
                        }
                        displayFrequencies.text = frequencyString.dropLast(2)
                    }
                }
                Utils.hideKeyboard(this)
            }
        }

        hideBarbells.setOnClickListener {
            if (activeBarRadio.visibility == View.GONE) {
                activeBarRadio.visibility = View.VISIBLE
            } else {
                activeBarRadio.visibility = View.GONE
            }
        }

        hideWeights.setOnClickListener {
            if (weightset_recycler_view.visibility == View.GONE) {
                weightset_recycler_view.visibility = View.VISIBLE
            } else {
                weightset_recycler_view.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Utils.saveListToPref("kilograms", kiloSet, this)
        Utils.saveListToPref("kiloBarbells", kiloBars, this)
        Utils.saveListToPref("pounds", poundSet, this)
        Utils.saveListToPref("poundBarbells", poundBars, this)
    }

    override fun onResume() {
        super.onResume()
        //Get user's weight sets and barbell configuration
        val weightSets = Utils.verifyOrCreateSets(this)
        val barbellSets = Utils.verifyOrCreateBarbells(this)
        kiloSet = weightSets.first
        kiloBars = barbellSets.first
        poundSet = weightSets.second
        poundBars = barbellSets.second

        //Re-create active set
        activeSet.clear()
        if (unitsRadioGroup.checkedRadioButtonId == kiloButton.id){
            activeSet.addAll(poundSet)
            val barbellRadioButton = pound_barbell_buttons.findViewById<RadioButton>(pound_barbell_buttons.checkedRadioButtonId)
            activeBar = poundBars[pound_barbell_buttons.indexOfChild(barbellRadioButton)]
        } else {
            activeSet.addAll(kiloSet)
            val barbellRadioButton = kilo_barbell_buttons.findViewById<RadioButton>(kilo_barbell_buttons.checkedRadioButtonId)
            activeBar = kiloBars[kilo_barbell_buttons.indexOfChild(barbellRadioButton)]
        }

        //Re-create barbell radio groups
        kilo_barbell_buttons.removeAllViewsInLayout()
        pound_barbell_buttons.removeAllViewsInLayout()
        setupBarbellRadio(kiloBars, kilo_barbell_buttons, this)
        setupBarbellRadio(poundBars, pound_barbell_buttons, this)
        viewAdapter.notifyDataSetChanged()
    }

    class WeightAdapter(private val weightSet: ArrayList<Weight>) : RecyclerView.Adapter<WeightAdapter.ViewHolder>() {

        class ViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): WeightAdapter.ViewHolder {
            val checkBox = CheckBox(p0.context)
            return ViewHolder(checkBox)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val weightObj = weightSet[position]
            holder.checkBox.text = weightObj.weight.toString()
            holder.checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                weightObj.selected = isChecked
            }
            holder.checkBox.setChecked(weightObj.selected)
        }

        override fun getItemCount() = weightSet.size
    }

    private fun setupBarbellRadio(barbells : ArrayList<Weight>, group : RadioGroup, context : Context) {
        var radioButton : android.support.v7.widget.AppCompatRadioButton
        for (barbell in barbells) {
            radioButton = android.support.v7.widget.AppCompatRadioButton(context)
            radioButton.text = barbell.weight.toString()
            group.addView(radioButton)

            if (barbell.selected) {
                group.check(radioButton.id)
            }
        }
    }
}

