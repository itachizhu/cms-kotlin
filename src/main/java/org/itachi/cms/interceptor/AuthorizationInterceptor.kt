package org.itachi.cms.interceptor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.itachi.cms.constant.Constants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by itachi on 2017/6/5.
 * User: itachi
 * Date: 2017/6/5
 * Time: 16:41
 */
@Component
open class AuthorizationInterceptor() : HandlerInterceptor {
    // private val LOGGER = LoggerFactory.getLogger(AuthorizationInterceptor::class.java)

    @Autowired
    @Qualifier("messageSource")
    private val messageSource: MessageSource? = null

    private var request: HttpServletRequest? = null
    private var response: HttpServletResponse? = null

    private val mapper = ObjectMapper()

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        this.request = request
        this.response = response

        val result = HashMap<String, Any?>()
        result.put("code", HttpStatus.UNAUTHORIZED.value())

        try {
            val servletPath = request!!.servletPath
            if (validateIgnorePath(servletPath)) {
                return true
            }
            return checkSession(result, servletPath)
        } catch (e: Exception) {
            return false
        }
    }

    @Throws(Exception::class)
    private fun checkSession(result: Map<String, Any?>, servletPath: String): Boolean {
        val url = "login"
        val session = request!!.getSession(false)
        val apiFlag = validateApiPath(servletPath)
        if (session == null) {
            result.plus(Pair("code", 502))
            result.plus(Pair("message", "session不存在，用户没登陆"))
            clearCookies()
            if (apiFlag) {
                response!!.status = HttpStatus.UNAUTHORIZED.value()
                response!!.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE
                response!!.writer.append(mapper.writeValueAsString(result)).flush()
                response!!.writer.close()
            } else {
                response!!.sendRedirect(request!!.contextPath + "/" + url)
            }
            return false
        }

        if (session.getAttribute(Constants.SESSION_KEY) == null) {
            result.plus(Pair("code", 503))
            result.plus(Pair("message", "session不存在，用户没登陆"))
            clearCookies()
            if (apiFlag) {
                response!!.status = HttpStatus.UNAUTHORIZED.value()
                response!!.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE
                response!!.writer.append(mapper.writeValueAsString(result)).flush()
                response!!.writer.close()
            } else {
                response!!.sendRedirect(request!!.contextPath + "/" + url)
            }
            return false
        }
        return true
    }

    private fun clearCookies() {
        try {
            val session = request!!.getSession(false)
            if (session != null) {
                val names = session.attributeNames
                if (names != null) {
                    while (names.hasMoreElements()) {
                        val attribute = names.nextElement()
                        session.removeAttribute(attribute)
                    }
                }
                session.invalidate()
            }
        } catch (e: Exception) {
        }

    }

    @Throws(Exception::class)
    private fun validateApiPath(servletPath: String): Boolean {
        return validatePath("api.path", servletPath)
    }

    @Throws(Exception::class)
    private fun validateIgnorePath(servletPath: String): Boolean {
        return validatePath("ignore.path", servletPath)
    }

    @Throws(Exception::class)
    private fun validatePath(key: String, servletPath: String): Boolean {
        val paths = messageSource!!.getMessage(key, null, null, Locale.getDefault())
        if (paths != null && !paths.isEmpty()) {
            val pattern = Pattern.compile(paths.trim { it <= ' ' })
            if (pattern.matcher(servletPath).matches()) {
                return true
            }
        }
        return false
    }


    override fun postHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, modelAndView: ModelAndView?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterCompletion(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, ex: Exception?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}