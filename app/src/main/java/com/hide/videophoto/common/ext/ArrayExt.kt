package com.hide.videophoto.common.ext

inline fun <reified T> Array<T>.append(vararg elements: T): Array<T> {
    val list: MutableList<T> = toMutableList()
    for (element in elements) {
        list.add(element)
    }
    return list.toTypedArray()
}