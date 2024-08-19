package org.blagochinnoved.clonegen

import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver

object DefaultTypeNameResolver: TypeParameterResolver {
    override val parametersMap: Map<String, TypeVariableName> = emptyMap()

    override fun get(index: String): TypeVariableName {
        return TypeVariableName.invoke(index)
    }
}