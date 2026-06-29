package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.domain.catalog.CatalogItem
import de.shopme.testing.system.tools.knowledge.fixture.CompilerContextFixture
import de.shopme.testing.system.tools.knowledge.fixture.KnowledgeFixtures
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.allergen.DefaultAllergenResolver
import de.shopme.tools.knowledge.compiler.CompilerContext
import de.shopme.tools.knowledge.compiler.passes.AllergenCompilerPass
import de.shopme.tools.knowledge.foods.EmptyFoodLookup
import org.junit.Assert.assertEquals
import org.junit.Test

class AllergenCompilerPassTest {

    private val knowledge =
        KnowledgeFixtures
            .allergenKnowledge()

    private val resolver =
        DefaultAllergenResolver(
            knowledge
        )

    private val pass =
        AllergenCompilerPass(
            resolver,
            foodLookup = EmptyFoodLookup
        )

    @Test
    fun milk_receives_milk_allergen() {

        val context =
            CompilerContextFixture.milk()

        pass.process(context)

        assertEquals(

            setOf(Allergen.MILK),

            context.allergens

        )

    }

    @Test
    fun peanut_receives_peanut_allergen() {

        val context =
            CompilerContextFixture.create(
                nutritionReference = "peanut"
            )

        pass.process(context)

        assertEquals(

            setOf(Allergen.PEANUT),

            context.allergens

        )

    }

    @Test
    fun unknown_food_receives_no_allergens() {

        val context =
            CompilerContextFixture.create(
                nutritionReference = "unknown"
            )

        pass.process(context)

        assertEquals(

            emptySet<Allergen>(),

            context.allergens

        )

    }

    private fun createContext(
        nutritionReference: String?
    ): CompilerContext {

        return CompilerContext(

            catalogItem =
                CatalogItem(

                    itemname = "Test",

                    category = "",

                    production = "",

                    normalized =
                        nutritionReference ?: "",

                    plural = "",

                    colloquial = emptyList(),

                    phonetic_tokens = emptyList(),

                    autocomplete_tokens = emptyList(),

                    nutritionReference =
                        nutritionReference

                )

        )

    }

}