package org.blagochinnoved.clonegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName

fun FileSpec.Builder.addCloneExtensionFunctions(
    classToCopy: KSClassDeclaration,
    properties: Sequence<KSPropertyDeclaration>,
): FileSpec.Builder {
    addImports(
        classToCopy = classToCopy,
        properties = properties,
    )

    properties.forEach {
        addFunction(
            FunctionBuilder.createCloneExtensionFunction(classToCopy, it)
        )
    }

    return this
}

fun FileSpec.Builder.addImports(
    classToCopy: KSClassDeclaration,
    properties: Sequence<KSPropertyDeclaration>,
): FileSpec.Builder {
    val className = classToCopy.toClassName()

    addImport(className.packageName, className.simpleName)

    properties.forEach { property ->
        val ksType = property.type.resolve()

        if (ksType is KSClassDeclaration) {
            addImport(
                (ksType as KSClassDeclaration).toClassName(),
            )
        }
    }

    return this
}
