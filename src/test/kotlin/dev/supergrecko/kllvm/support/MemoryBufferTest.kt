package dev.supergrecko.kllvm.support

import dev.supergrecko.kllvm.ir.Context
import dev.supergrecko.kllvm.ir.Module
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

class MemoryBufferTest {
    @Test
    fun `The buffer byte-code starts with B for BC`() {
        val context = Context()
        val module = Module("test.ll", context)

        val buf = module.toMemoryBuffer()

        // module bytecode starts with "BC"
        assertEquals('B', buf.getStart())

        buf.dispose()
        module.dispose()
        context.dispose()
    }

    @Test
    fun `Create MemoryBuffer from byte-code file`() {
        val target = File("test.ll.2")
        val mod = Module("test.ll")
        mod.toFile(target)

        val buf = MemoryBuffer(target)
        assertNotNull(buf)

        buf.dispose()
        mod.dispose()
    }

    @Test
    fun `Creation from unknown path fails`() {
        assertFailsWith<IllegalArgumentException> {
            MemoryBuffer(File("unknown file which does not exist"))
        }
    }
}