package org.jetbrains.compose.experimental.web.internal

import org.gradle.api.Action
import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink

object CreateLoadMjsAction : Action<Task> {
    override fun execute(t: Task) {
        t as KotlinJsIrLink
        val loadMjs = t.destinationDirectory.file("load.mjs")
        val mainOutput = t.destinationDirectory.file(t.moduleName.map { it + ".mjs" })
        val result = mainOutput.get().asFile.readLines().joinToString(separator = "\n") { s ->
            when {

                "import { instantiate }" in s -> {
                    val append = """
                                        import { loadAndInitSkikoWasm, SkikoCallbacks }  from './skiko.mjs';
                                        const skikoWasm = await loadAndInitSkikoWasm();
                                        """.trimIndent()
                    s + "\n" + append

                }

                "await instantiate" in s -> {
                    val append = """
                                            await instantiate({ skia:  skikoWasm.wasmExports, GL: skikoWasm.GL, SkikoCallbacks: SkikoCallbacks});
                                        """.trimIndent()

                    append
                }

                else -> s
            }
        }

        loadMjs.get().asFile.writeText(result)
    }
}