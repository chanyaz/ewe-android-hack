package com.expedia.layouttestandroid.util

import com.expedia.layouttestandroid.dataspecs.LayoutDataSpecValues
import com.expedia.layouttestandroid.extension.combine

object TestCombinations {

    private data class EntrySet(val key: String, val value: Any?)

    fun getAllCombinationsOfDataSpec(dataSpecForTest: Map<String, LayoutDataSpecValues>): List<Map<String, Any?>> {
        if (dataSpecForTest.isEmpty()) {
            return emptyList()
        }
        val list = convertMapRowToList(dataSpecForTest)
        val combinations = creteCombinations(list)
        return convertListToMap(combinations)
    }

    private fun convertMapRowToList(dataSpecForTest: Map<String, LayoutDataSpecValues>): MutableList<List<List<EntrySet>>> {
        val list: MutableList<List<List<EntrySet>>> = ArrayList()
        dataSpecForTest.entries.forEach { entry ->
            val tempList = ArrayList<ArrayList<EntrySet>>()
            entry.value.values().forEach { value ->
                tempList.add(arrayListOf(EntrySet(entry.key, value)))
            }
            list.add(tempList)
        }
        return list
    }

    private fun creteCombinations(list: MutableList<List<List<EntrySet>>>): List<List<EntrySet>> {
        while (list.size > 1) {
            val remove0 = list.removeAt(0)
            val remove1 = list.removeAt(0)

            list.add(ArrayList(remove0.combine(remove1)))
        }
        return list[0]
    }

    private fun convertListToMap(combinations: List<List<EntrySet>>): List<Map<String, Any?>> {
        return combinations.map { item ->
            val hashMap = HashMap<String, Any?>()
            item.map {
                hashMap[it.key] = it.value
            }
            hashMap
        }
    }
}
