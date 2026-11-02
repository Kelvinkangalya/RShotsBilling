package com.example.rshotsbilling.data.local

import androidx.room.TypeConverter
import com.example.rshotsbilling.model.SaleItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromSaleItemList(value: List<SaleItem>?): String? {
        val gson = Gson()
        val type = object : TypeToken<List<SaleItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSaleItemList(value: String?): List<SaleItem>? {
        val gson = Gson()
        val type = object : TypeToken<List<SaleItem>>() {}.type
        return gson.fromJson(value, type)
    }
}
