package me.owdding.repo.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.gson.JsonParser
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import me.owdding.repo.processor.data.Definition
import me.owdding.repo.processor.data.parse
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.walk

internal class Processor(
    private val codeGenerator: CodeGenerator,
    private val paths: Sequence<Definition>,
) : SymbolProcessor {
    var hasRan = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (hasRan) return emptyList()
        hasRan = true

        paths.forEach { parse(it) }

        val build = FileSpec.builder("me.owdding.repo.extensions", "RepoExtension")
            .indent("    ")
            .addType(
                TypeSpec.classBuilder("RepoExtension").apply {
                    this.addModifiers(KModifier.OPEN)
                    paths.forEach {
                        this.addProperty(
                            PropertySpec.builder(
                                it.name,
                                Lazy::class.asTypeName().parameterizedBy(it.toClassName()),
                                KModifier.PRIVATE
                            ).initializer("lazy { ${it.getClassName()}() }").build()
                        )
                        this.addFunction(
                            FunSpec.builder(it.name)
                                .addParameter(
                                    "config",
                                    LambdaTypeName.get(it.toClassName(), returnType = Unit::class.asTypeName())
                                ).addCode(
                                    CodeBlock.builder()
                                        .addStatement("${it.name}.value.config()")
                                        .build()
                                ).build()
                        )
                    }
                }.addFunction(
                    FunSpec.builder("collectInitialized")
                        .returns(
                            List::class.asTypeName()
                                .parameterizedBy(ClassName("me.owdding.repo.extensions", "BaseExtension"))
                        )
                        .addCode(
                            CodeBlock.builder()
                                .addStatement("val initialized: MutableList<BaseExtension> = mutableListOf()")
                                .apply {
                                    paths.forEach {
                                        addStatement("if (${it.name}.isInitialized()) initialized.add(${it.name}.value)")
                                    }
                                }
                                .addStatement("return initialized")
                                .build())
                        .build()
                ).build()
            ).build()

        build.writeTo(codeGenerator, Dependencies(false))

        return emptyList()
    }

    private fun parse(
        definition: Definition,
    ) {
        val fileSpec = FileSpec.builder("me.owdding.repo.extensions.generated", definition.name).indent("    ").addType(
            TypeSpec.classBuilder(definition.getClassName())
                .superclass(ClassName("me.owdding.repo.extensions", "BaseExtension"))
                .addSuperclassConstructorParameter("\"${definition.name}\"").apply {
                    definition.definitions.forEach {
                        addFunction(
                            FunSpec.builder(it.name).receiver(
                                ClassName(
                                    "kotlin.collections", "MutableSet"
                                ).parameterizedBy(String::class.asTypeName())
                            ).addCode(
                                CodeBlock.builder().apply {
                                    addStatement("this.addAll(${it.name})")
                                }.build()
                            ).build()
                        )
                        addProperty(
                            PropertySpec.builder(
                                it.name,
                                List::class.asTypeName().parameterizedBy(String::class.asTypeName()),
                                KModifier.PRIVATE
                            ).initializer(
                                CodeBlock.builder().apply {
                                    add("listOf(\n")

                                    it.paths.forEach { path ->
                                        add("    \"$path\",\n")
                                    }

                                    add(")")
                                }.build()
                            ).build()
                        )
                    }
                }.build()
        )

        fileSpec.build().writeTo(codeGenerator, Dependencies(true))
    }
}

internal class ModuleProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {
        val definitions = Path.of(environment.options["meowdding.processor.definitions"]!!)


        return Processor(
            environment.codeGenerator,
            definitions.walk().map { JsonParser.parseString(it.readText()).asJsonArray.parse(it) })
    }
}
