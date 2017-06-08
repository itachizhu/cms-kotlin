package org.itachi.cms.config

import org.itachi.cms.interceptor.AuthorizationInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * Created by itachi on 2017/6/5.
 * User: itachi
 * Date: 2017/6/5
 * Time: 22:22
 */
@Configuration
open class WebConfigurerAdapter : WebMvcConfigurerAdapter() {
    @Autowired
    private val authorizationInterceptor: AuthorizationInterceptor? = null

    override fun addInterceptors(registry: InterceptorRegistry?) {
        registry!!.addInterceptor(authorizationInterceptor!!).addPathPatterns("/**")
    }
}