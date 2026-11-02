package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Receipt

expect object PrintingService {
    fun printReceipt(receipt: Receipt)
}
