package dev.supergrecko.vexe.llvm.unit.ir

import dev.supergrecko.vexe.llvm.ir.Context
import dev.supergrecko.vexe.llvm.ir.Module
import dev.supergrecko.vexe.llvm.ir.types.FunctionType
import dev.supergrecko.vexe.llvm.ir.types.IntType
import dev.supergrecko.vexe.llvm.ir.types.StructType
import dev.supergrecko.vexe.llvm.ir.types.VoidType
import dev.supergrecko.vexe.llvm.support.VerifierFailureAction
import dev.supergrecko.vexe.llvm.utils.cleanup
import dev.supergrecko.vexe.test.TestSuite
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ModuleTest : TestSuite({
    describe("Modules can have identifiers") {
        val mod = Module("utils.ll").apply {
            setModuleIdentifier("utils")
        }

        assertEquals("utils", mod.getModuleIdentifier())

        cleanup(mod)
    }

    describe("Cloning a module clones the identifier") {
        val mod = Module("utils.ll").apply {
            setModuleIdentifier("utils")
        }

        val clone = mod.clone()

        assertEquals(mod.getModuleIdentifier(), clone.getModuleIdentifier())

        cleanup(mod, clone)
    }

    describe("Modifying the module's source file name") {
        val mod = Module("utils.ll").apply {
            assertEquals("utils.ll", getSourceFileName())

            setSourceFileName("test2.ll")

            assertEquals("test2.ll", getSourceFileName())
        }

        cleanup(mod)
    }

    describe("Fetching a function which does not exist returns null") {
        val module = Module("utils.ll")

        assertNull(module.getFunction("utils"))

        cleanup(module)
    }

    describe("Fetching an existing function returns said function") {
        val module = Module("utils.ll").apply {
            addFunction(
                "utils",
                FunctionType(
                    VoidType(),
                    listOf(),
                    false
                )
            )

            assertNotNull(getFunction("utils"))
        }

        cleanup(module)
    }

    describe("Write the module byte-code to file") {
        val file = getTemporaryFile("out.bc")
        val module = Module("utils.ll").also {
            it.writeBitCodeToFile(file)
        }

        assertTrue { file.length() > 0 }

        cleanup(module)
    }

    describe("Write the module byte-code to file path") {
        val file = getTemporaryFile("out.bc")
        val module = Module("utils.ll").also {
            it.writeBitCodeToFile(file.absolutePath)
        }

        assertTrue { file.exists() }

        cleanup(module)
    }

    describe("Writing to MemoryBuffer") {
        val context = Context()
        val module = Module("utils.ll", context)

        val buf = module.toMemoryBuffer()

        assertNotNull(buf.ref)

        cleanup(module, context)
    }

    describe("Get module from MemoryBuffer") {
        val context = Context()
        val module = Module("utils.ll", context)
        val buf = module.toMemoryBuffer()
        val mod = buf.getModule(context)

        assertEquals("utils.ll", mod.getSourceFileName())

        cleanup(mod, module, context)
    }

    describe("Verification of a valid module") {
        val context = Context()
        val module = Module("utils.ll", context)
        val res = module.verify(VerifierFailureAction.ReturnStatus)

        assertEquals(true, res)

        cleanup(module, context)
    }

    describe("Creation of function inside module") {
        val fnTy = FunctionType(IntType(32), listOf(), false)
        val module = Module("utils.ll")
        val fn = module.addFunction("utils", fnTy)

        assertEquals(0, fn.getParameterCount())

        cleanup(module)
    }

    describe("Assigning a data layout") {
        val module = Module("utils.ll").apply {
            setDataLayout("e-m:e-i64:64-f80:128-n8:16:32:64-S128")
        }

        assertEquals(
            "e-m:e-i64:64-f80:128-n8:16:32:64-S128", module.getDataLayout()
        )

        cleanup(module)
    }

    describe("Assigning a target triple") {
        val module = Module("utils.ll").apply {
            setTarget("x86_64-pc-linux")
        }

        assertEquals("x86_64-pc-linux", module.getTarget())

        cleanup(module)
    }

    describe("Retrieving LLVM IR") {
        val module = Module("utils.ll")
        val ir = module.toString()

        assertTrue { ir.isNotEmpty() }

        cleanup(module)
    }

    describe("Setting module inline assembly") {
        val module = Module("utils.ll").apply {
            setInlineAssembly(".example")
            appendInlineAssembly("    push 0")
            appendInlineAssembly("    ret")
        }
        val result = ".example\n    push 0\n    ret\n"

        // LLVM Appends a new line after any inline-asm changes
        assertEquals(result, module.getInlineAssembly())

        cleanup(module)
    }

    describe("Retrieving Context from Module") {
        val ctx = Context()
        val module = Module("utils.ll", ctx)

        assertEquals(ctx.ref, module.getContext().ref)

        cleanup(module, ctx)
    }

    describe("Retrieving a type from a module") {
        val ctx = Context()
        val type = StructType("TestStruct", ctx)
        val module = Module("utils.ll", ctx)
        val found = module.getTypeByName("TestStruct")

        assertNotNull(found)
        assertEquals(type.ref, found.ref)

        cleanup(module, ctx)
    }
})
