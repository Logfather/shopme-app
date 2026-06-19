package de.shopme.tools.knowledge.lookup

import de.shopme.tools.knowledge.KnowledgeArtifact

class DefaultArtifactLookup(

    private val artifacts: List<KnowledgeArtifact>

) : ArtifactLookup {

    override fun <T : KnowledgeArtifact> lookup(

        type: Class<T>

    ): T? {

        return artifacts.firstOrNull {

            type.isInstance(it)

        }?.let(type::cast)

    }

}