package de.shopme.tools.knowledge.compiler.writer

import de.shopme.tools.knowledge.compiler.CompilerContext

abstract class AbstractKnowledgeWriter<K : Comparable<K>, V> :

    FoodKnowledgeWriter {

    protected val entries =

        mutableMapOf<K, V>()

    override fun begin() {

        entries.clear()

    }

    override fun write(

        context: CompilerContext

    ) {

        val key =

            key(

                context

            ) ?: return

        val value =

            value(

                context

            ) ?: return

        entries[key] = value

    }

    protected abstract fun key(

        context: CompilerContext

    ): K?

    protected abstract fun value(

        context: CompilerContext

    ): V?

}