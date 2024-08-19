package org.blagochinnoved.clonegen

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

class CloneAnnotationProcessor(
    private val environment: SymbolProcessorEnvironment,
    private val fileBuilder: FileBuilder,
): SymbolProcessor {
    private val processedClasses = mutableListOf<ClassName>()
    private val processedProperties = mutableListOf<KSPropertyDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val declarations = resolver.findCloneableAnnotations()

        processClassDeclarations(declarations.valid.filterIsInstance<KSClassDeclaration>())
        processPropertyDeclarations(declarations.valid.filterIsInstance<KSPropertyDeclaration>())

        return declarations.invalid.toList()
    }

    private fun Resolver.findCloneableAnnotations(): FindSymbolsResults {
        val annotationName = Cloneable::class.qualifiedName?: "org.blagochinnoved.clonegen.Cloneable"

        val map = this.getSymbolsWithAnnotation(annotationName, inDepth = false)
            .groupBy {
                it.validate()
            }

        return FindSymbolsResults(
            valid = map[true]?.asSequence().orEmpty(),
            invalid = map[false]?.asSequence().orEmpty()
        )
    }

    private fun processClassDeclarations(
        classDeclarations: Sequence<KSClassDeclaration>
    ) {
        classDeclarations.forEach { declaration ->
            val classSpec = declaration.asType(listOf()).toClassName()

            if (!processedClasses.contains(classSpec)) {
                processedClasses.add(classSpec)

                fileBuilder.buildFile(
                    classToCopy = declaration,
                    declaration = declaration,
                ) {
                    val properties = declaration.getDeclaredProperties()

                    processedProperties.addAll(properties)

                    this.addCloneExtensionFunctions(
                        classToCopy = declaration,
                        properties = properties,
                    )
                }
            }
        }
    }

    private fun processPropertyDeclarations(
        propertyDeclarations: Sequence<KSPropertyDeclaration>
    ) {
        val classPropertiesMap = buildClassPropertiesMap(propertyDeclarations)

        classPropertiesMap.forEach { entry ->
            val ksClass = entry.key
            val classProperties = entry.value
            val classSpec = ksClass.asType(listOf()).toClassName()

            if (!processedClasses.contains(classSpec)) {
                processedClasses.add(classSpec)

                fileBuilder.buildFile(
                    classToCopy = ksClass,
                    declaration = ksClass,
                ) {
                    processedProperties.addAll(classProperties)

                    this.addCloneExtensionFunctions(
                        classToCopy = ksClass,
                        properties = classProperties.asSequence(),
                    )
                }
            }
        }
    }

    private fun buildClassPropertiesMap(
        propertyDeclarations: Sequence<KSPropertyDeclaration>
    ): Map<KSClassDeclaration, Set<KSPropertyDeclaration>> {
        return buildMap {
            propertyDeclarations.forEach { declaration ->
                val parentClassDeclaration = declaration.parentDeclaration as? KSClassDeclaration

                if (parentClassDeclaration != null) {
                    val currentClassDeclarations = this.getOrElse(parentClassDeclaration) { null }

                    if (currentClassDeclarations.isNullOrEmpty()) {
                        put(parentClassDeclaration, setOf(declaration))
                    } else {
                        put(
                            parentClassDeclaration,
                            currentClassDeclarations.toMutableSet().apply {
                                add(declaration)
                            }
                        )
                    }
                }
            }
        }
    }
}

class CloneAnnotationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return CloneAnnotationProcessor(
            environment = environment,
            fileBuilder = FileBuilder(environment)
        )
    }
}