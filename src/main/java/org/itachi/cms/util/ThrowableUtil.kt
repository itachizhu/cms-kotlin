package org.itachi.cms.util

import org.itachi.cms.exception.BaseException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.jdbc.BadSqlGrammarException
import java.sql.SQLException

/**
 * Created by itachi on 2017/6/5.
 * User: itachi
 * Date: 2017/6/5
 * Time: 16:06
 */
class ThrowableUtil {
    companion object {
        fun create() : ThrowableUtil = ThrowableUtil()
    }

    private val MESSAGE = "message"
    private val CODE = "code"

    private val LOGGER = LoggerFactory.getLogger(ThrowableUtil::class.java)

    @Throws(Exception::class)
    fun handleThrowable(result: Map<String, Any?>, throwable: Throwable): HttpStatus {
        LOGGER.debug("==========handleThrowable==========")
        return handleCommonThrowable(result, throwable)
    }

    fun handleException(result: Map<String, Any?>, cause: Throwable, message: String): HttpStatus {
        try {
            if (cause is BaseException) {
                return handleBaseException(result, cause as BaseException)
            }
            if (cause is SQLException) {
                return handleSQLException(result, cause)
            }
            if (cause is BadSqlGrammarException) {
                return handleBadSqlGrammarException(result, cause)
            }
            return handleCommonThrowable(result, cause, message)
        } catch (e: Exception) {
            return handleCommonThrowable(result, cause, message)
        }

    }

    fun handleException(result: Map<String, Any?>, cause: Throwable): HttpStatus {
        return handleException(result, cause, "web server handle data exception!")
    }

    private fun handleBadSqlGrammarException(result: Map<String, Any?>, cause: BadSqlGrammarException): HttpStatus {
        LOGGER.error("====BadSqlGrammarException: {} \n\n {} \n\n {}", cause, cause.sql, cause.message)
        result.plus(Pair(CODE, if (cause.sqlException == null || cause.sqlException.errorCode == 200) 417 else cause.sqlException.errorCode))
        result.plus(Pair(MESSAGE, cause.message))
        return HttpStatus.SERVICE_UNAVAILABLE
    }

    private fun handleSQLException(result: Map<String, Any?>, cause: SQLException): HttpStatus {
        LOGGER.error("====SQLException: {} \n\n {} \n\n {}", cause, cause.errorCode, cause.message)
        result.plus(Pair(CODE, if (cause.errorCode == 200) 417 else cause.errorCode))
        result.plus(Pair(MESSAGE, cause.message))
        return HttpStatus.SERVICE_UNAVAILABLE
    }

    private fun handleBaseException(result: Map<String, Any?>, cause: BaseException): HttpStatus {
        var status = HttpStatus.SERVICE_UNAVAILABLE
        if (cause.status != null) {
            try {
                status = HttpStatus.valueOf(cause.status!!)
            } catch (e: Exception) {
                status = HttpStatus.SERVICE_UNAVAILABLE
            }

        }
        result.plus(Pair(CODE, cause.code))
        result.plus(Pair(MESSAGE, cause.message))
        return status
    }

    fun handleCommonThrowable(result: Map<String, Any?>, cause: Throwable): HttpStatus {
        return handleCommonThrowable(result, cause, "web server handle data exception!")
    }

    fun handleCommonThrowable(result: Map<String, Any?>, cause: Throwable?, message: String?): HttpStatus {
        if (cause != null && cause.message != null && !cause.message.isNullOrEmpty()) {
            result.plus(Pair(MESSAGE, cause.message))
        } else if (message != null) {
            result.plus(Pair(MESSAGE, message))
        }

        return HttpStatus.SERVICE_UNAVAILABLE
    }
}