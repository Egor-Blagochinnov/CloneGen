package org.blagochinnoved.clonegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import java.util.Locale

private const val FUNCTION_PREFIX = "clone"
private const val LAMBDA_ARGUMENT_NAME = "block"

object FunctionBuilder {
    fun createCloneExtensionFunction(
        classToCopy: KSClassDeclaration,
        property: KSPropertyDeclaration,
    ): FunSpec {
        val parameterName = property.simpleName.getShortName()
        val functionName = "$FUNCTION_PREFIX${parameterName.capitalize()}"
        val lambdaType = resolveLambdaType(property)

        return FunSpec.builder(functionName)
            .applyClassDeclaration(classToCopy)
            .addParameter(
                ParameterSpec.builder(
                    LAMBDA_ARGUMENT_NAME,
                    lambdaType
                ).build()
            )
            .addCode(
                "return this.copy(${parameterName} = $LAMBDA_ARGUMENT_NAME(this.${parameterName}))"
            )
            .build()
    }

    private fun resolveLambdaType(property: KSPropertyDeclaration): LambdaTypeName {
        val type = property.type.toTypeName(DefaultTypeNameResolver)

        return LambdaTypeName.get(
            receiver = type,
            returnType = type,
        )
    }

    private fun resolveFunctionType(
        classToCopy: KSClassDeclaration
    ): TypeName {
        val typeVariables = classToCopy.typeParameters.map { it.toTypeVariableName() }

        return if (typeVariables.isNotEmpty()) {
            classToCopy.toClassName().parameterizedBy(typeVariables)
        } else {
            classToCopy.toClassName()
        }
    }

    private fun FunSpec.Builder.applyClassDeclaration(
        classToCopy: KSClassDeclaration
    ): FunSpec.Builder {
        val typeVariables = classToCopy.typeParameters
            .map { it.toTypeVariableName() }

        val functionType = resolveFunctionType(classToCopy)

        return this
            .receiver(functionType)
            .returns(functionType)
            .apply {
                if (typeVariables.isNotEmpty()) {
                    addTypeVariables(typeVariables)
                }
            }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}