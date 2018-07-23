package com.example.pmoayer.kilokonverter


import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.my_toolbar.*


class ConfigureActivity : BaseCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var activeSet: ArrayList<Weight>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)
        setSupportActionBar(my_toolbar)

        //Get user's weight sets and barbell configuration
        val weightSets = Utils.verifyOrCreateSets(this)
        val barbellSets = Utils.verifyOrCreateBarbells(this)
        val kiloSet = weightSets.first
        val kiloBars = barbellSets.first
        val poundSet = weightSets.second
        val poundBars = barbellSets.second

        //Arbitrarily set active set to Kilo (CHANGE TO BE SWITCH ON TOOLBAR)
        activeSet = kiloSet

        //Recycler View setup for weight set
        viewManager = GridLayoutManager(this.applicationContext, 4)
        viewAdapter = WeightAdapter(activeSet)
        recyclerView = findViewById(R.id.weightSet_recycler_view)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            overScrollMode = RecyclerView.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
    }

    class WeightAdapter(private val weightSet: ArrayList<Weight>) : RecyclerView.Adapter<WeightAdapter.ViewHolder>() {

        class ViewHolder(val button: Button) : RecyclerView.ViewHolder(button)

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): WeightAdapter.ViewHolder {
            val button = Button(p0.context)
            return ViewHolder(button)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var weightObj = weightSet[position]
            holder.button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_delete, 0, 0, 0)

            holder.button.text = weightObj.weight.toString()

            holder.button.setOnClickListener {
                var builder = AlertDialog.Builder(holder.itemView.context)
                builder.setTitle("Are you Sure?")
                builder.setPositiveButton(R.string.cancel) { dialog, which ->
                    //Do nothing and exit alert dialog
                }
                builder.setNegativeButton(R.string.delete) { dialog, which ->
                    weightSet.removeAt(position)
                    Toast.makeText(holder.itemView.context,"Weight deleted",Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
                builder.show()
            }
        }

        override fun getItemCount() = weightSet.size
    }
}
