package de.shopme.tools.knowledge.compiler.passes

import de.shopme.tools.knowledge.carbon.CarbonImpactClassifier
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.FoodKnowledgeCompilerPass
import de.shopme.tools.knowledge.foods.FoodLookup

class CarbonImpactCompilerPass(

    private val foodLookup: FoodLookup,

    private val classifier: CarbonImpactClassifier =
        CarbonImpactClassifier()

) : FoodKnowledgeCompilerPass {

    override fun process(
        context: CompilerContext
    ) {

        context.carbonImpact =

            foodLookup.carbonImpact(

                context.normalizedName

            )
                ?: context.carbonFootprint?.let { footprint ->

                    classifier.classify(

                        footprint

                    )
                }
    }
}