package org.itachi.cms.exception

/**
 * Created by itachi on 2017/6/5.
 * User: itachi
 * Date: 2017/6/5
 * Time: 16:13
 */
class BaseException : Exception {

    var code: Int? = null
        private set
    var status: Int? = null
        private set

    private val FAILURE = 0
    private val ERROR = 500

    constructor(status: Int?, code: Int?, message: String) : super(message) {
        this.status = status
        this.code = code
    }

    constructor(message: String) : super(message) {
        this.status = FAILURE
        this.code = ERROR
    }

    constructor() : super() {
        this.status = FAILURE
        this.code = ERROR
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.status = FAILURE
        this.code = ERROR
    }

    constructor(cause: Throwable) : super(cause) {
        this.status = FAILURE
        this.code = ERROR
    }

    constructor(status: Int?, code: Int?, cause: Throwable) : super(cause) {
        this.status = status
        this.code = code
    }
}