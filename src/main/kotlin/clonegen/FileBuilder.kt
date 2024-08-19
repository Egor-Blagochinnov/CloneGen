package org.blagochinnoved.clonegen

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.BufferedWriter
import java.io.OutputStream

class FileBuilder(
    private val environment: SymbolProcessorEnvironment,
) {
    fun buildFile(
        classToCopy: KSClassDeclaration,
        declaration: KSClassDeclaration,
        block: FileSpec.Builder.() -> FileSpec.Builder
    ) {
        val writer = environment.createFileWriter(
            classToCopy = classToCopy,
            declaration = declaration,
        )

        writer.append("//Generated file")
        writer.append("\n")

        val className = classToCopy.toClassName()
        val packageName = className.packageName
        val simpleName = className.simpleName
        val annotatedClassName = "${simpleName}CloneableExt"

        writer.flush()

        val fileBuilder = FileSpec.builder(
            packageName,
            "$annotatedClassName.kt"
        )

        val fileSpec = block(fileBuilder).build()

        fileSpec.writeTo(writer)
        writer.flush()
        writer.close()
    }

    private fun SymbolProcessorEnvironment.createFileWriter(
        classToCopy: KSClassDeclaration,
        declaration: KSClassDeclaration
    ): BufferedWriter {
        val className = classToCopy.toClassName()
        val packageName = className.packageName
        val simpleName = className.simpleName
        val annotatedClassName = "${simpleName}CloneableExt"

        val file = this.createFile(
            packageName = packageName,
            fileName = annotatedClassName,
            containingFile = declaration.containingFile,
        )

        return file.bufferedWriter()
    }

    private fun SymbolProcessorEnvironment.createFile(
        packageName: String,
        fileName: String,
        containingFile: KSFile?,
    ): OutputStream {
        val dependencies = if (containingFile != null) {
            Dependencies(false, containingFile)
        } else {
            Dependencies(false)
        }

        return try {
            this.codeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = packageName,
                fileName = fileName,
                extensionName = "kt"
            )
        } catch (ex: FileAlreadyExistsException) {
            ex.file.outputStream()
        }
    }
}