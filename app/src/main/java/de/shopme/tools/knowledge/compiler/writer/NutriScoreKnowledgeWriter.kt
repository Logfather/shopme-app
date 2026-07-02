package de.shopme.tools.knowledge.compiler.writer

import android.R.attr.entries
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutriscore.NutriScoreFactsKnowledge
import de.shopme.tools.knowledge.nutriscore.NutriScoreKnowledgeJsonWriter
import java.io.File

class NutriScoreKnowledgeWriter :

    AbstractKnowledgeWriter<String, NutriScore>() {

    override fun key(

        context: CompilerContext

    ) =

        context.nutritionReference

    override fun value(

        context: CompilerContext

    ) =

        context.nutriScore

    fun knowledge() =

        NutriScoreFactsKnowledge(

            entries =

                entries.toSortedMap()

        )

    override fun finish() {

        NutriScoreKnowledgeJsonWriter()

            .write(

                knowledge(),

                File(

                    "data/generated/nutri_score.json"

                )

            )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 NUTRI SCORE KNOWLEDGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Entries : ${entries.size}")
        println("📄 Output  : data/generated/nutri_score.json")
        println("🏁 FINISHED")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()

    }

}