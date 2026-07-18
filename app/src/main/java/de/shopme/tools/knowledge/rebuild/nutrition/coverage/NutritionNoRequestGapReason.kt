package de.shopme.tools.knowledge.rebuild.nutrition.coverage

enum class NutritionNoRequestGapReason {

    NO_CANDIDATES,

    MATCH_REPORT_ENTRY_WITHOUT_MATCH_STATUS,

    /**
     * Der Catalog Key ist im Catalog vorhanden, kommt aber in keinem
     * Nutrition-Match-Report vor.
     */
    MATCH_REPORT_ENTRY_MISSING,

    /**
     * Der Catalog Key ist im Match-Report enthalten und dort bereits als
     * deterministischer Match materialisiert. Der Coverage-Gap wäre damit
     * inkonsistent.
     */
    MATCH_REPORT_ALREADY_MATCHED,

    /**
     * Der Catalog Key ist als unmatched im Match-Report vorhanden, wurde aber
     * nicht in die Nutrition-Match-Requests übernommen.
     */
    UNMATCHED_ENTRY_NOT_CONVERTED_TO_REQUEST,

    /**
     * Laut Coverage-Report fehlt der Request, die persistierte Request-Datei
     * enthält den Catalog Key jedoch. Die Artefakte sind nicht synchron.
     */
    REQUEST_PRESENT_BUT_COVERAGE_REPORT_STALE,

    /**
     * Eine zentrale Catalog→Server-Zuordnung existiert bereits, obwohl der
     * Coverage-Report den Catalog Key als NO_REQUEST-Lücke aufführt.
     */
    MAPPING_PRESENT_BUT_COVERAGE_REPORT_STALE
}