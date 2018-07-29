package com.moayer.puya.kilokonverter

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.*

object Utils {

    //Hide the soft input keyboard
    fun hideKeyboard(activity: Activity) {
        // Check if no view has focus:
        val view = activity.currentFocus
        if (view != null) {
            val inputManager = activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun kilogramsToPounds(weight: Double, weightSet: ArrayList<Weight>, barbell: Weight, context: Context): Triple<Double, Weight, HashMap<Weight, Int>> {
//        var gson = Gson()
//        val listType = object : TypeToken<ArrayList<Weight>>(){}.type
//        val json = PreferenceManager.getDefaultSharedPreferences(context).getString("pounds", "")
//        var weightSet : ArrayList<Weight> = gson.fromJson(json, listType)

        val newWeight = weight * 2.20462 //Return this value as the converted weight
        val frequencyMap = findFrequencies(newWeight - barbell.weight, weightSet)

        return Triple(newWeight, barbell, frequencyMap)
    }

    fun poundsToKilograms(weight: Double, weightSet: ArrayList<Weight>, barbell: Weight, context: Context): Triple<Double, Weight, HashMap<Weight, Int>> {
//        var gson = Gson()
//        val listType = object : TypeToken<ArrayList<Weight>>(){}.type
//        val json = PreferenceManager.getDefaultSharedPreferences(context).getString("pounds", "")
//        var weightSet : ArrayList<Weight> = gson.fromJson(json, listType)

        val newWeight = weight / 2.20462 //Return this value as the converted weight
        val frequencyMap = findFrequencies(newWeight - barbell.weight, weightSet)

        return Triple(newWeight, barbell, frequencyMap)
    }

    //Destructive since sortByDescending will modify List's ordering
    fun findFrequencies(weight: Double, weightSet: ArrayList<Weight>) : HashMap<Weight, Int> {
        var frequencyMap = HashMap<Weight, Int>()
        var remainder = weight
        var frequency : Int
        var plate : Double

        var weightCopy = ArrayList<Weight>()
        weightCopy.addAll(weightSet)
        weightCopy.sortByDescending{ it.weight }


        for (i in 0 until weightSet.size) {
            plate = weightSet[i].weight

            frequency = (remainder / plate).toInt()
            if (weightSet[i].selected && frequency >= 2) {
                frequency = (frequency / 2) * 2
                remainder -= (frequency * plate)
                frequencyMap[weightSet[i]] = frequency
            }
        }

        return frequencyMap
    }

    //Return weight sets. Create if do not exist.
    fun verifyOrCreateSets(context: Context) : Pair<ArrayList<Weight>, ArrayList<Weight>>{
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var sharedPrefEditor = sharedPref.edit()
        var isChange = false
        var gson = Gson()

        var kiloStr = sharedPref.getString("kilograms", "")
        if (kiloStr == "") {
            var tempKilos = ArrayList<Weight>()
            tempKilos.add(Weight(50.00, "black", false))
            tempKilos.add(Weight(25.00, "red", true))
            tempKilos.add(Weight(20.00, "blue", true))
            tempKilos.add(Weight(15.00, "yellow", true))
            tempKilos.add(Weight(10.00, "green", true))
            tempKilos.add(Weight(5.00, "white", true))
            tempKilos.add(Weight(2.50, "red", true))
            tempKilos.add(Weight(1.50, "yellow", true))
            kiloStr = gson.toJson(tempKilos)
            sharedPrefEditor.putString("kilograms", kiloStr)
            isChange = true
        }

        var poundStr = sharedPref.getString("pounds", "")
        if (poundStr == "") {
            var tempPounds = ArrayList<Weight>()
            tempPounds.add(Weight(100.00, "black", false))
            tempPounds.add(Weight(45.00, "black", true))
            tempPounds.add(Weight(35.00, "black", false))
            tempPounds.add(Weight(25.00, "black", true))
            tempPounds.add(Weight(10.00, "black", true))
            tempPounds.add(Weight(5.00, "black", true))
            tempPounds.add(Weight(2.50, "black", true))
            poundStr = gson.toJson(tempPounds)
            sharedPrefEditor.putString("pounds", poundStr)
            isChange = true
        }

        if (isChange) {
            sharedPrefEditor.apply()
        }

        val listType = object : TypeToken<ArrayList<Weight>>(){}.type
        var kiloSet : ArrayList<Weight> = gson.fromJson(kiloStr, listType)
        var poundSet : ArrayList<Weight> = gson.fromJson(poundStr, listType)
        return Pair(kiloSet, poundSet)
    }

    //Return barbell sets. Create if do not exist.
    fun verifyOrCreateBarbells(context : Context) : Pair<ArrayList<Weight>, ArrayList<Weight>> {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var sharedPrefEditor = sharedPref.edit()
        var isChange = false
        var gson = Gson()

        var kiloStr = sharedPref.getString("kiloBarbells", "")
        if (kiloStr == "") {
            var tempKilos = ArrayList<Weight>()
            tempKilos.add(Weight(20.00, "barbell", true))
            tempKilos.add(Weight(15.00, "barbell", false))
            tempKilos.add(Weight(0.00, "barbell", false))
            kiloStr = gson.toJson(tempKilos)
            sharedPrefEditor.putString("kiloBarbells", kiloStr)
            isChange = true
        }

        var poundStr = sharedPref.getString("poundBarbells", "")
        if (poundStr == "") {
            var tempPounds = ArrayList<Weight>()
            tempPounds.add(Weight(70.00, "barbell", false))
            tempPounds.add(Weight(55.00, "barbell", false))
            tempPounds.add(Weight(45.00, "barbell", true))
            tempPounds.add(Weight(0.00, "barbell", false))
            poundStr = gson.toJson(tempPounds)
            sharedPrefEditor.putString("poundBarbells", poundStr)
            isChange = true
        }

        if (isChange) {
            sharedPrefEditor.apply()
        }

        val listType = object : TypeToken<ArrayList<Weight>>(){}.type
        var kiloBarbells : ArrayList<Weight> = gson.fromJson(kiloStr, listType)
        var poundBarbells : ArrayList<Weight> = gson.fromJson(poundStr, listType)
        return Pair(kiloBarbells, poundBarbells)
    }

    fun saveListToPref(key : String, list : ArrayList<Weight>, context : Context) {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        var sharedPrefEditor = sharedPref.edit()
        var gson = Gson()
        val listGson = gson.toJson(list)
        sharedPrefEditor.putString(key, listGson)
        sharedPrefEditor.commit()
    }
}