package com.appfinanzas.prototype.domain.projection

import com.appfinanzas.prototype.domain.model.ProjectionStrategy

class ManualProjectionEngine(
    private val compound: CompoundInterestProjectionEngine = CompoundInterestProjectionEngine(),
) : ProjectionEngine {

    override val strategy: ProjectionStrategy = ProjectionStrategy.MANUAL

    override fun project(input: ProjectionInput): ProjectionResult =
        compound.project(input).copy(strategy = ProjectionStrategy.MANUAL)
}
