package org.itachi.cms.config

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator

/**
 * Created by itachi on 2017/6/4.
 * User: itachi
 * Date: 2017/6/4
 * Time: 14:22
 */
@Configuration
open class MessageSourceConfig {
    @Bean
    open fun messageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("configuration/configuration")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    private fun validationMessageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("messages/ValidationMessages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }

    @Bean
    open fun resourceBundleLocator(): ResourceBundleLocator {
        return MessageSourceResourceBundleLocator(validationMessageSource())
    }
}