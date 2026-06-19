package de.shopme.testing.system.tools.knowledge.fixture

import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.allergen.AllergenKnowledge

object KnowledgeFixtures {

    fun allergenKnowledge() =

        AllergenKnowledge(

            entries = mapOf(

                "milk" to setOf(
                    Allergen.MILK
                ),

                "peanut" to setOf(
                    Allergen.PEANUT
                ),

                "egg" to setOf(
                    Allergen.EGG
                ),

                "soy" to setOf(
                    Allergen.SOY
                ),

                "bread" to setOf(
                    Allergen.GLUTEN
                ),

                "shrimp" to setOf(
                    Allergen.CRUSTACEAN
                )

            )

        )

}