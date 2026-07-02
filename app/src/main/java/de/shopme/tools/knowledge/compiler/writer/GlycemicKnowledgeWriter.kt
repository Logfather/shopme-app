package de.shopme.tools.knowledge.compiler.writer

import de.shopme.domain.food.GlycemicIndexLevel
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.glycemic.GlycemicKnowledge
import de.shopme.tools.knowledge.glycemic.GlycemicKnowledgeJsonWriter
import java.io.File

class GlycemicKnowledgeWriter :

    AbstractKnowledgeWriter<String, GlycemicIndexLevel>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.glycemicIndex

    fun knowledge() =

        GlycemicKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        GlycemicKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/glycemic.json"

                )

            )

    }

}