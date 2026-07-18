package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionUnclassifiedTokenPairMismatchClassification(
    val version: Int,
    val sourceRelationshipCount: Int,
    val sourceTokenPairObservationCount: Int,
    val classifiedRelationshipCount: Int,
    val classifiedTokenPairObservationCount: Int,
    val countsByMismatchType:
    Map<NutritionUnclassifiedTokenPairMismatchType, Int>,
    val countsByPrimaryRelationshipMismatchType:
    Map<NutritionUnclassifiedTokenPairMismatchType, Int>,
    val countsByRelationshipMismatchType:
    Map<NutritionUnclassifiedTokenPairMismatchType, Int>,
    val countsBySingleTokenPair:
    Map<Boolean, Int>,
    val topPairsByMismatchType:
    Map<
            NutritionUnclassifiedTokenPairMismatchType,
            List<NutritionUnclassifiedTokenPairMismatchCount>,
            >,
    val entries:
    List<NutritionUnclassifiedTokenPairMismatchEntry>,
)

data class NutritionUnclassifiedTokenPairMismatchEntry(
    val catalogKey: String,
    val serverKey: String,
    val rank: Int,
    val singleTokenPair: Boolean,
    val primaryMismatchType:
    NutritionUnclassifiedTokenPairMismatchType,
    val detectedMismatchTypes:
    List<NutritionUnclassifiedTokenPairMismatchType>,
    val observations:
    List<NutritionUnclassifiedTokenPairMismatchObservation>,
)

data class NutritionUnclassifiedTokenPairMismatchObservation(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val normalizedCatalogToken: String,
    val normalizedServerToken: String,
    val normalizedPairKey: String,
    val mismatchType:
    NutritionUnclassifiedTokenPairMismatchType,
    val reason: String,
)

data class NutritionUnclassifiedTokenPairMismatchCount(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val count: Int,
)

enum class NutritionUnclassifiedTokenPairMismatchType {

    /**
     * Mindestens eines der beiden Tokens enthält numerische Zeichen.
     *
     * Beispiele:
     * cloudy -> 100
     * spelt -> 1
     */
    NUMERIC_NOISE,

    /**
     * Beide Tokens werden durch die deterministische Normalisierung
     * auf denselben Wert reduziert.
     *
     * Beispiele:
     * sausage -> sausages
     * bulb -> bulbs
     */
    SAME_NORMALIZED_TOKEN,

    /**
     * Unterschiedliche Tierarten oder tierische Hauptzutaten.
     *
     * Beispiele:
     * redfish -> chicken
     * pork -> beef
     */
    ANIMAL_SPECIES_MISMATCH,

    /**
     * Unterschiedliche Pflanzen-, Hülsenfrucht- oder Getreidearten.
     *
     * Beispiele:
     * spelt -> oat
     * lupin -> almond
     */
    PLANT_OR_GRAIN_MISMATCH,

    /**
     * Unterschiedliche fachlich relevante Hauptzutaten, die nicht bereits
     * als Tier- oder Pflanzenart klassifiziert wurden.
     *
     * Beispiele:
     * chocolate -> garlic
     * cheese -> tuna
     */
    INGREDIENT_MISMATCH,

    /**
     * Unterschiedliche Produkt- oder Gerichtskategorien.
     *
     * Beispiele:
     * casserole -> sausage
     * lasagna -> meals
     */
    PRODUCT_CATEGORY_MISMATCH,

    /**
     * Unterschiedliche Produktform oder Zubereitungsbeschreibung.
     *
     * Beispiele:
     * fillet -> ground
     * roast -> chopped
     */
    PREPARATION_OR_FORM_MISMATCH,

    /**
     * Explizit bekannte, deterministisch gepflegte Synonym- oder
     * Sprachvarianten.
     *
     * Beispiele:
     * beetroot -> beet
     * courgette -> zucchini
     */
    POSSIBLE_SYNONYM,

    /**
     * Das Tokenpaar kann noch keiner belastbaren deterministischen
     * Fachklasse zugeordnet werden.
     */
    UNKNOWN,
}