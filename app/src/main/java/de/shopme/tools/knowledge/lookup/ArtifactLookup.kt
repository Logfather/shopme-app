package de.shopme.tools.knowledge.lookup

import de.shopme.tools.knowledge.KnowledgeArtifact

interface ArtifactLookup {

    fun <T : KnowledgeArtifact> lookup(

        type: Class<T>

    ): T?

}