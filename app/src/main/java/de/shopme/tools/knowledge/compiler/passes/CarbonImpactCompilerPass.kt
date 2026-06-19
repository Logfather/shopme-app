package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.carbon.CarbonImpactClassifier
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass

class CarbonImpactCompilerPass(

    private val classifier:
    CarbonImpactClassifier =
        CarbonImpactClassifier()

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        val footprint =
            context.carbonFootprint
                ?: return

        context.carbonImpact =

            classifier.classify(
                footprint
            )

    }

}