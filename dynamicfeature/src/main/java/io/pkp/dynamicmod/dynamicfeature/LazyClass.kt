package io.pkp.dynamicmod.dynamicfeature

import io.pkp.dynamicmod.test.LazyInterface

class LazyClass : LazyInterface {

    override fun message(): String = "Hello from lazy impl!"

}