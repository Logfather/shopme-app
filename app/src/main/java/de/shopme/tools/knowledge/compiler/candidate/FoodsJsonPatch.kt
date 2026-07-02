package de.shopme.tools.knowledge.compiler.candidate

data class FoodsJsonPatch(
    val operations: List<FoodsJsonPatchOperation>
)