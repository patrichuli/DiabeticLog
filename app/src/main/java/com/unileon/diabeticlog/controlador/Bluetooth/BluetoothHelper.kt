package com.unileon.diabeticlog.controlador.Bluetooth

class BluetoothHelper {

    fun getSfloat16(b0: Byte, b1: Byte): Float {
        val mantissa = unsignedToSigned(
            unsignedByteToInt(b0)
                    + (unsignedByteToInt(b1) and 0x0F shl 8), 12
        )
        val exponent = unsignedToSigned(unsignedByteToInt(b1) shr 4, 4)
        return (mantissa * Math.pow(10.0, exponent.toDouble())).toFloat()
    }

    fun unsignedByteToInt(b: Byte): Int {
        return b.toInt() and 0xFF
    }

    fun unsignedBytesToInt(b: Byte, c: Byte): Int {
        return (unsignedByteToInt(c) shl 8) + unsignedByteToInt(b) and 0xFFFF
    }

    private fun unsignedToSigned(unsigned: Int, size: Int): Int {
        var unsigned = unsigned
        if (unsigned and (1 shl size - 1) != 0) {
            unsigned = -1 * ((1 shl size - 1) - (unsigned and (1 shl size - 1) - 1))
        }
        return unsigned
    }
}