package de.shopme.tools.knowledge.compiler.shared.validation.validators.consistency

import de.shopme.tools.knowledge.compiler.KnowledgeBuildContext
import de.shopme.tools.knowledge.compiler.shared.validation.BuildValidator
import de.shopme.tools.knowledge.lookup.DietRuleLookup
import de.shopme.tools.knowledge.lookup.FoodTagLookup

class DietConsistencyValidator(

    private val foodTagLookup: FoodTagLookup,

    private val dietRuleLookup: DietRuleLookup

) : BuildValidator {

    override fun validate(

        context: KnowledgeBuildContext

    ) {

        // implemented in next commit

    }

}