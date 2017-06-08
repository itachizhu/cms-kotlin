package org.itachi.cms.controller

import org.hibernate.validator.constraints.NotEmpty
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.ws.rs.QueryParam

/**
 * Created by itachi on 2017/6/7.
 * User: itachi
 * Date: 2017/6/7
 * Time: 15:53
 */
@Controller
@RequestMapping("/test")
@Validated
open class HelloController {
    @RequestMapping(value = "/myerror", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun error(): String {
        throw Exception("自定义尝试抛出异常!")
    }

    @RequestMapping(value = "/index", method = arrayOf(RequestMethod.GET))
    @Throws(Exception::class)
    fun index(model: Model): String {
        model.addAttribute("name", "itachi")
        return "index"
    }

    @RequestMapping(value = "/valid", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Throws(Exception::class)
    fun valid(@Size(min = 2, max = 20, message = "{id.size}") @RequestParam id: String): String {
        return id
    }

    @RequestMapping(value = "/valid", method = arrayOf(RequestMethod.DELETE))
    @ResponseBody
    @Throws(Exception::class)
    fun validJaxRs(@NotNull @NotEmpty @QueryParam("name") name: String): String {
        return name
    }
}