package de.shopme.tools.knowledge.carbon.importer

import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeSource

interface CarbonSourceImporter :
    KnowledgeSource<CarbonKnowledgeCandidate>