package de.shopme.tools.knowledge.agribalyse.model

object AgribalyseSynthesisColumns {

    // Product

    const val AGB_CODE =
        "Synthèse Résultats d'impacts pour 1 kg de produit consommé chez le consommateur, intègrant notamment les pertes aux étapes intermédiaires. Pour en savoir plus, voir la documentation scientifique. | Code AGB"

    const val CIQUAL_CODE =
        "Score unique EF 3.1 | Code CIQUAL"

    const val FOOD_GROUP =
        "Changement climatique | Groupe d'aliment"

    const val FOOD_SUBGROUP =
        "Appauvrissement de la couche d'ozone | Sous-groupe d'aliment"

    const val PRODUCT_NAME =
        "Rayonnements ionisants | Nom du Produit en Français"

    const val LCI_NAME =
        "Formation photochimique d'ozone | LCI Name"

    // Metadata

    const val SEASON_CODE =
        "Particules fines | code saison (0 : hors saison ; 1 : de saison ; 2 : mix de consommation FR)"

    const val AIR_TRANSPORT =
        "Effets toxicologiques sur la santé humaine : substances non-cancérogènes | code avion (1 : par avion)"

    const val DELIVERY =
        "Effets toxicologiques sur la santé humaine : substances cancérogènes | Livraison"

    const val PACKAGING_APPROACH =
        "Acidification terrestre et eaux douces | Approche emballage"

    const val PREPARATION =
        "Eutrophisation eaux douces | Préparation"

    const val DATA_QUALITY =
        "Eutrophisation marine | DQR - Note de qualité de la donnée (1 excellente ; 5 très faible)"

    // Environmental indicators

    const val SINGLE_SCORE =
        "Eutrophisation terrestre | mPt/kg de produit"

    const val CARBON =
        "Écotoxicité pour écosystèmes aquatiques d'eau douce | kg CO2 eq/kg de produit"

    const val OZONE =
        "Utilisation du sol | kg CVC11 eq/kg de produit"

    const val IONIZING_RADIATION =
        "Épuisement des ressources eau | kBq U-235 eq/kg de produit"

    const val PHOTOCHEMICAL_OZONE =
        "Épuisement des ressources énergétiques | kg NMVOC eq/kg de produit"

    const val PARTICULATES =
        "Épuisement des ressources minéraux | disease inc./kg de produit"

    const val HUMAN_TOXICITY_NON_CANCER =
        "Changement climatique - émissions biogéniques | CTUh/kg de produit"

    const val HUMAN_TOXICITY_CANCER =
        "Changement climatique - émissions fossiles | CTUh/kg de produit"

    const val ACIDIFICATION =
        "Changement climatique - émissions liées au changement d'affectation des sols | mol H+ eq/kg de produit"

    const val EUTROPHICATION_FRESHWATER =
        "kg P eq/kg de produit"

    const val EUTROPHICATION_MARINE =
        "kg N eq/kg de produit"

    const val EUTROPHICATION_TERRESTRIAL =
        "mol N eq/kg de produit"

    const val ECOTOXICITY =
        "CTUe/kg de produit"

    const val LAND_USE =
        "Pt/kg de produit"

    const val WATER =
        "m3 depriv./kg de produit"

    const val ENERGY =
        "MJ/kg de produit"

    const val MINERALS =
        "kg Sb eq/kg de produit"

    const val BIOGENIC_CARBON =
        "kg CO2 eq/kg de produit"

    const val FOSSIL_CARBON =
        "kg CO2 eq/kg de produit (2)"

    const val LAND_USE_CARBON =
        "kg CO2 eq/kg de produit (3)"
}