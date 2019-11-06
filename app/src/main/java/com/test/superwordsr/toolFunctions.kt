package com.test.superwordsr

import java.text.SimpleDateFormat
import java.util.*

val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH", Locale.CHINA)  // 日期格式

/**
 * 字符串转日期
 */
fun string2date(date:String): Date?{
    return formatter.parse(date)
}
/**
 * 日期转字符串
 */
fun date2string(date: Date):String{
    return formatter.format(date)
}