package org.itachi.cms.handler

import org.apache.commons.lang3.StringUtils
import org.apache.el.ExpressionFactoryImpl
import org.hibernate.validator.internal.engine.MessageInterpolatorContext
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator
import org.itachi.cms.util.LocaleUtil
import org.itachi.cms.util.ThrowableUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException
import javax.validation.MessageInterpolator
import javax.validation.ValidationException
import javax.validation.Validator

/**
 * Created by itachi on 2017/6/7.
 * User: itachi
 * Date: 2017/6/7
 * Time: 11:01
 */
@ControllerAdvice
//@Controller
class GlobalExceptionHandler {
    private val LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private val CODE = "code"
    private val MESSAGE = "message"
    private val ERRORS = "errors"
    private val FIELD = "field"

    @Autowired
    private val resourceBundleLocator: ResourceBundleLocator? = null

    @Autowired
    private val validator: Validator? = null

    private var request: HttpServletRequest? = null

    @ExceptionHandler(value = Throwable::class)
    fun handleException(throwable: Throwable, request: HttpServletRequest): ResponseEntity<*> {
        var result = mutableMapOf<String, Any?>()
        result.put(CODE, 999)
        // result[CODE] = 999
        var status = HttpStatus.SERVICE_UNAVAILABLE
        try {
            this.request = request
            status = handleThrowable(result, throwable)
        } catch (e: Exception) {
            LOGGER.error("=====ExceptionMapperProvider exception:", e)
            result.put(MESSAGE, "exception mapper provider have exception!")
        }

        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON_UTF8).body<Map<String, Any?>>(result)
    }

    @Throws(Exception::class)
    private fun handleThrowable(result: MutableMap<String, Any?>, throwable: Throwable?): HttpStatus {
        if (throwable is ConstraintViolationException) {
            return handleValidException(result, throwable as ConstraintViolationException?)
        }
        if (throwable is MethodArgumentNotValidException) {
            return handleValidException(result, throwable as MethodArgumentNotValidException?)
        }
        if (throwable is MissingServletRequestParameterException) {
            return handleValidException(result, throwable as MissingServletRequestParameterException?)
        }
        if (throwable is MissingPathVariableException) {
            return handleValidException(result, throwable as MissingPathVariableException?)
        }
        if (throwable is BindException) {
            return handleValidException(result, throwable as BindException?)
        }
        if (throwable != null && throwable.cause != null && throwable.cause !== throwable) {
            return ThrowableUtil.create().handleException(result, throwable.cause!!)
        }
        return ThrowableUtil.create().handleException(result, throwable!!)
    }

    @Throws(Exception::class)
    private fun handleValidException(result: MutableMap<String, Any?>, throwable: BindException?): HttpStatus {
        val violations = validator!!.validate(throwable!!.target)
        if (violations != null && !violations.isEmpty()) {
            return handleValidException(result, ConstraintViolationException(violations))
        }

        result.put(CODE, 104)
        result.put(MESSAGE, "Parameter Error!")
        if (throwable.allErrors != null && !throwable.allErrors.isEmpty()) {
            val list = listOf<Map<String, Any?>>()
            for (error in throwable.allErrors) {
                val map = HashMap<String, Any?>()
                map.put(FIELD, throwable.target)
                map.put(MESSAGE, error.defaultMessage)
                list.plus(map)
            }
            result.put(ERRORS, list)
        }
        return HttpStatus.BAD_REQUEST
    }

    @Throws(Exception::class)
    private fun handleValidException(result: MutableMap<String, Any?>, exception: ConstraintViolationException?): HttpStatus {
        result.put(CODE, 104)
        result.put(MESSAGE, "Parameter Error!")
        val messages = exception!!.constraintViolations
        if (messages != null && !messages.isEmpty()) {
            val locale = LocaleUtil.getLocale(request)
            val list = listOf<Map<String, String>>()
            for (message in messages) {
                val map = HashMap<String, String>()
                var field = StringUtils.substringAfterLast(message.propertyPath.toString(), ".")
                field = if (field == null || field.isEmpty()) message.propertyPath.toString() else field
                map.put(FIELD, field)
                if (!message.messageTemplate.isNullOrEmpty()) {
                    val context = MessageInterpolatorContext(message.constraintDescriptor, message.invalidValue, message.rootBeanClass, HashMap<String, Any>(0))
                    map.put(MESSAGE, interpolate(message.messageTemplate, context, locale))
                } else {
                    map.put(MESSAGE, message.message)
                }
                list.plus(map)
            }
            result.put(ERRORS, list)
        }

        return HttpStatus.BAD_REQUEST
    }

    @Throws(Exception::class)
    private fun handleValidException(result: MutableMap<String, Any?>, exception: MissingPathVariableException?): HttpStatus {
        result.put(CODE, 104)
        result.put(MESSAGE, "Parameter Error!")
        val list = listOf<Map<String, Any?>>()
        val map = HashMap<String, Any?>()
        map.put(FIELD, exception!!.variableName)
        map.put(MESSAGE, exception.message)
        list.plus(map)
        result.put(ERRORS, list)
        return HttpStatus.BAD_REQUEST
    }

    @Throws(Exception::class)
    private fun handleValidException(result: MutableMap<String, Any?>, exception: MissingServletRequestParameterException?): HttpStatus {
        result.put(CODE, 104)
        result.put(MESSAGE, "Parameter Error!")
        val list = listOf<Map<String, Any?>>()
        val map = HashMap<String, Any?>()
        map.put(FIELD, exception!!.parameterName)
        map.put(MESSAGE, exception.message)
        list.plus(map)
        result.put(ERRORS, list)
        return HttpStatus.BAD_REQUEST
    }

    @Throws(Exception::class)
    private fun handleValidException(result: MutableMap<String, Any?>, exception: MethodArgumentNotValidException?): HttpStatus {
        val violations = validator!!.validate(exception!!.bindingResult.target)
        if (violations != null && !violations.isEmpty()) {
            return handleValidException(result, ConstraintViolationException(violations))
        }

        result.put(CODE, 104)
        result.put(MESSAGE, "Parameter Error!")
        if (exception.bindingResult != null && exception.bindingResult.allErrors != null
                && !exception.bindingResult.allErrors.isEmpty()) {
            val list = listOf<Map<String, Any?>>()
            for (error in exception.bindingResult.allErrors) {
                val map = HashMap<String, Any?>()
                map.put(FIELD, exception.bindingResult.target)
                map.put(MESSAGE, error.defaultMessage)
                list.plus(map)
            }
            result.put(ERRORS, list)
        }
        return HttpStatus.BAD_REQUEST
    }

    private fun interpolate(message: String, context: MessageInterpolator.Context, locale: Locale): String {
        var interpolatedMessage = message
        try {
            interpolatedMessage = interpolateMessage(message, context, locale)
        } catch (e: ValidationException) {
            LOGGER.warn(e.message)
        }

        return interpolatedMessage
    }

    /**
     * Runs the message interpolation according to algorithm specified in the Bean Validation specification.
     * <br></br>
     * Note:
     * <br></br>
     * Look-ups in user bundles is recursive whereas look-ups in default bundle are not!

     * @param message the message to interpolate
     * *
     * @param context the context for this interpolation
     * *
     * @param locale  the `Locale` to use for the resource bundle.
     * *
     * @return the interpolated message.
     */
    @Throws(MessageDescriptorFormatException::class)
    private fun interpolateMessage(message: String, context: MessageInterpolator.Context, locale: Locale): String {
        // LocalizedMessage localisedMessage = new LocalizedMessage(message, locale);
        var resolvedMessage: String? = null

        // if the message is not already in the cache we have to run step 1-3 of the message resolution
        if (resolvedMessage == null) {
            // ResourceBundleLocator resourceBundleLocator = null;

            val userResourceBundle = resourceBundleLocator!!
                    .getResourceBundle(locale)
            val defaultResourceBundle = resourceBundleLocator
                    .getResourceBundle(locale)

            var userBundleResolvedMessage: String
            resolvedMessage = message
            var evaluatedDefaultBundleOnce = false
            do {
                // search the user bundle recursive (step1)
                userBundleResolvedMessage = interpolateBundleMessage(
                        resolvedMessage!!, userResourceBundle, locale, true
                )

                // exit condition - we have at least tried to validate against the default bundle and there was no
                // further replacements
                if (evaluatedDefaultBundleOnce && !hasReplacementTakenPlace(userBundleResolvedMessage, resolvedMessage)) {
                    break
                }

                // search the default bundle non recursive (step2)
                resolvedMessage = interpolateBundleMessage(
                        userBundleResolvedMessage,
                        defaultResourceBundle,
                        locale,
                        false
                )
                evaluatedDefaultBundleOnce = true
            } while (true)
        }

        // resolve parameter expressions (step 4)
        var tokens: List<Token>? = null
        if (tokens == null) {
            val tokenCollector = TokenCollector(resolvedMessage, InterpolationTermType.PARAMETER)
            tokens = tokenCollector.tokenList
        }
        resolvedMessage = interpolateExpression(
                TokenIterator(tokens!!),
                context,
                locale
        )

        // resolve EL expressions (step 5)
        val tokenCollector = TokenCollector(resolvedMessage, InterpolationTermType.EL)
        tokens = tokenCollector.tokenList

        resolvedMessage = interpolateExpression(
                TokenIterator(tokens!!),
                context,
                locale
        )

        // last but not least we have to take care of escaped literals
        resolvedMessage = replaceEscapedLiterals(resolvedMessage)

        return resolvedMessage
    }

    private fun replaceEscapedLiterals(resolvedMessage: String): String {
        var resolvedMessage = resolvedMessage
        resolvedMessage = resolvedMessage.replace("\\{", "{")
        resolvedMessage = resolvedMessage.replace("\\}", "}")
        resolvedMessage = resolvedMessage.replace("\\\\", "\\")
        resolvedMessage = resolvedMessage.replace("\\$", "$")
        return resolvedMessage
    }

    private fun hasReplacementTakenPlace(origMessage: String, newMessage: String): Boolean {
        return origMessage != newMessage
    }

    @Throws(MessageDescriptorFormatException::class)
    private fun interpolateBundleMessage(message: String, bundle: ResourceBundle, locale: Locale, recursive: Boolean): String {
        val tokenCollector = TokenCollector(message, InterpolationTermType.PARAMETER)
        val tokenIterator = TokenIterator(tokenCollector.tokenList)
        while (tokenIterator.hasMoreInterpolationTerms()) {
            val term = tokenIterator.nextInterpolationTerm()
            val resolvedParameterValue = resolveParameter(
                    term, bundle, locale, recursive
            )
            tokenIterator.replaceCurrentInterpolationTerm(resolvedParameterValue)
        }
        return tokenIterator.interpolatedMessage
    }

    @Throws(MessageDescriptorFormatException::class)
    private fun interpolateExpression(tokenIterator: TokenIterator, context: MessageInterpolator.Context, locale: Locale): String {
        while (tokenIterator.hasMoreInterpolationTerms()) {
            val term = tokenIterator.nextInterpolationTerm()

            val expression = InterpolationTerm(term, locale, ExpressionFactoryImpl())
            val resolvedExpression = expression.interpolate(context)
            tokenIterator.replaceCurrentInterpolationTerm(resolvedExpression)
        }
        return tokenIterator.interpolatedMessage
    }

    @Throws(MessageDescriptorFormatException::class)
    private fun resolveParameter(parameterName: String, bundle: ResourceBundle?, locale: Locale, recursive: Boolean): String {
        var parameterValue: String
        try {
            if (bundle != null) {
                parameterValue = bundle.getString(removeCurlyBraces(parameterName))
                if (recursive) {
                    parameterValue = interpolateBundleMessage(parameterValue, bundle, locale, recursive)
                }
            } else {
                parameterValue = parameterName
            }
        } catch (e: MissingResourceException) {
            // return parameter itself
            parameterValue = parameterName
        }

        return parameterValue
    }

    private fun removeCurlyBraces(parameter: String): String {
        return parameter.substring(1, parameter.length - 1)
    }
}