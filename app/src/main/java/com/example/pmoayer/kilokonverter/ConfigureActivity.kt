package com.example.pmoayer.kilokonverter


import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Menu
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_configure.*
import kotlinx.android.synthetic.main.my_toolbar.*


class ConfigureActivity : BaseCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var kiloSet: ArrayList<Weight>
    private lateinit var kiloBars: ArrayList<Weight>
    private lateinit var poundSet: ArrayList<Weight>
    private lateinit var poundBars: ArrayList<Weight>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_no_icons, menu)
        return true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)
        setSupportActionBar(my_toolbar)

        //Get user's weight sets and barbell configuration
        val weightSets = Utils.verifyOrCreateSets(this)
        val barbellSets = Utils.verifyOrCreateBarbells(this)
        kiloBars = barbellSets.first
        kiloSet = weightSets.first
        poundBars = barbellSets.second
        poundSet = weightSets.second

        initializeRecyclerView(R.id.kiloBarbell_recycler_view, kiloBars)
        initializeRecyclerView(R.id.kiloWeight_recycler_view, kiloSet)
        initializeRecyclerView(R.id.poundBarbell_recycler_view, poundBars)
        initializeRecyclerView(R.id.poundWeight_recycler_view, poundSet)

        addKiloBarbellButton.setOnClickListener {
            addWeight(kiloBars, kiloBarbell_recycler_view, inputKiloBarbell)
        }
        addKiloWeightButton.setOnClickListener {
            addWeight(kiloSet, kiloWeight_recycler_view, inputKiloWeight)

        }
        addPoundBarbellButton.setOnClickListener {
            addWeight(poundBars, poundBarbell_recycler_view, inputPoundBarbell)
        }
        addPoundWeightButton.setOnClickListener {
            addWeight(poundSet, poundWeight_recycler_view, inputPoundWeight)
        }
    }

    //Save all sets when finished configuring (change to include "isChange" indicator?)
    override fun onPause() {
        super.onPause()
        Utils.saveListToPref("kilograms", kiloSet, this)
        Utils.saveListToPref("kiloBarbells", kiloBars, this)
        Utils.saveListToPref("pounds", poundSet, this)
        Utils.saveListToPref("poundBarbells", poundBars, this)
    }

    class WeightAdapter(private val weightSet: ArrayList<Weight>) : RecyclerView.Adapter<WeightAdapter.ViewHolder>() {

        class ViewHolder(val checkBox: CheckBox) : RecyclerView.ViewHolder(checkBox)

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): WeightAdapter.ViewHolder {
            val checkBox = CheckBox(p0.context)
            checkBox.setButtonDrawable(R.drawable.ic_action_delete)
            checkBox.setPadding(20,15,0,10)

            return ViewHolder(checkBox)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val weightObj = weightSet[position]
            holder.checkBox.text = weightObj.weight.toString()

            holder.checkBox.setOnClickListener {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Are you sure?")
                builder.setPositiveButton(R.string.cancel) { dialog, which ->
                    //Do nothing and exit alert dialog
                }
                builder.setNegativeButton(R.string.delete) { dialog, which ->
                    if (weightSet.get(position).weight == 0.0) {
                        Toast.makeText(holder.itemView.context,"Cannot delete zero weight",Toast.LENGTH_SHORT).show()
                    } else {
                        weightSet.removeAt(position)
                        Toast.makeText(holder.itemView.context, "Weight deleted", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                builder.show()
            }
        }

        override fun getItemCount() = weightSet.size
    }

    private fun initializeRecyclerView(id : Int, set : ArrayList<Weight>) {
        viewManager = GridLayoutManager(this, 4)
        viewAdapter = WeightAdapter(set)
        recyclerView = findViewById(id)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            overScrollMode = RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
    }

    private fun addWeight(list : ArrayList<Weight>, recyclerView : RecyclerView, textView : TextView) {
        if (TextUtils.isEmpty(textView.text)) {
            Toast.makeText(this,"Please enter weight",Toast.LENGTH_SHORT).show()
            return
        }

        val toAdd = textView.text.toString().toDouble()

        for (obj in list){
            if (obj.weight == toAdd) {
                Toast.makeText(this,"Weight already exists",Toast.LENGTH_SHORT).show()
                return
            }
        }
        list.add(Weight(toAdd, "barbell", true))
        list.sortByDescending{ it.weight }
        recyclerView.adapter?.notifyDataSetChanged()
        textView.text = ""
        textView.clearFocus()
        Utils.hideKeyboard(this)
    }


}
