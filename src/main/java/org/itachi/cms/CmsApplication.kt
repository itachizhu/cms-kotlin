package org.itachi.cms

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Created by itachi on 2017/6/4.
 * User: itachi
 * Date: 2017/6/4
 * Time: 12:15
 */
@SpringBootApplication
open class CmsApplication {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(CmsApplication::class.java, *args)
        }
    }
}