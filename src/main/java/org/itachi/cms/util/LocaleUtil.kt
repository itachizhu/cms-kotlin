package org.itachi.cms.util

import java.util.*
import javax.servlet.http.HttpServletRequest

/**
 * Created by itachi on 2017/6/7.
 * User: itachi
 * Date: 2017/6/7
 * Time: 11:08
 */
object LocaleUtil {

    fun getLocale(request: HttpServletRequest?): Locale {
        try {
            if (request != null) {
                val locale = request.getParameter("locale")
                if (!locale.isNullOrEmpty()) {
                    val locales = locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (locales.size < 2) {
                        return Locale(locale)
                    } else {
                        return Locale(locales[0], locales[1])
                    }
                } else if (request.locale != null) {
                    return request.locale
                }
            }
        } catch (e: Exception) {
        }

        return Locale.getDefault()
    }


}