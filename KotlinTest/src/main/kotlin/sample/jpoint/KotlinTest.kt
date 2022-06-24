/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package sample.jpoint

import org.openjdk.jmh.annotations.*
import java.util.concurrent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
open class KotlinTest {

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun validateRawNoInline(input: Int): Boolean {
        return input % 13 != 0
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun validateNoInline(input: Int) {
        val b = input == 42 
        if (!validateRawNoInline(input) && !b) {
            println("failed: input = $input, b = $b") //never happens
        }  
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun fooNoInline(): Int {
       validateNoInline(14)
       return 37
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun barNoInline(input: Int): Int {
       var res = 0
       validateNoInline(input)
       for (i in 1..300) {
           res += i*fooNoInline() 
       }
       return fooNoInline() + res*input  
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun bazNoInline(input: Int): Int {
       validateNoInline(input)
       return fooNoInline() - input*barNoInline(3)
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun testNoInline(input: Int): Int {
        val r = fooNoInline()
        validateNoInline(r)
        val l = barNoInline(r + 42)
        validateNoInline(l)
        return bazNoInline(r + l) 
    }


    fun validateRaw(input: Int): Boolean {
        return input % 13 != 0
    }

    fun validate(input: Int) {
        val b = input == 42 
        if (!validateRaw(input) && !b) {
            println("failed: input = $input, b = $b") //never happens
        }  
    }

    fun foo(): Int {
       validate(14)
       return 37
    }


    fun bar(input: Int): Int {
       var res = 0
       validate(input)
       for (i in 1..300) {
           res += i*foo() 
       }
       return foo() + res*input  
    }

    fun baz(input: Int): Int {
       validate(input)
       return foo() - input*bar(3)
    }

    fun test(input: Int): Int {
        val r = foo()
        validate(r)
        val l = bar(r + 42)
        validate(l)
        return baz(r + l) 
    }

    suspend fun validateRawSuspend(input: Int): Boolean {
        return input % 134125 != 0
    }

    suspend fun validateSuspend(input: Int) {                                                                                  
        val b = input == 42 
        if (!validateRawSuspend(input) && !b) {
            println("failed: input = $input, b = $b") //never happens
        }  
    }

    suspend fun fooSuspend(): Int {
       validateSuspend(14)
       return 37
    }


    suspend fun barSuspend(input: Int): Int {
       var res = 0
       validateSuspend(input)
       for (i in 1..300) {
           res += i*fooSuspend() 
       }
       return fooSuspend() + res*input  
    }

    suspend fun bazSuspend(input: Int): Int {
       validateSuspend(input)
       return fooSuspend() - input*barSuspend(3)
    }

    suspend fun testSuspend(input: Int): Int {
        val r = fooSuspend()
        validateSuspend(r)
        val l = barSuspend(r + 42)
        validateSuspend(l)
        return bazSuspend(r + l) 
    }


    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun validateRawSuspendNoInline(input: Int): Boolean {
        return input % 134125 != 0
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun validateSuspendNoInline(input: Int) {                                                                                  
        val b = input == 42 
        if (!validateRawSuspendNoInline(input) && !b) {
            println("failed: input = $input, b = $b") //never happens
        }  
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun fooSuspendNoInline(): Int {
       validateSuspendNoInline(14)
       return 37
    }


    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun barSuspendNoInline(input: Int): Int {
       var res = 0
       validateSuspendNoInline(input)
       for (i in 1..300) {
           res += i*fooSuspendNoInline() 
       }
       return fooSuspendNoInline() + res*input  
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun bazSuspendNoInline(input: Int): Int {
       validateSuspendNoInline(input)
       return fooSuspendNoInline() - input*barSuspendNoInline(3)
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    suspend fun testSuspendNoInline(input: Int): Int {
        val r = fooSuspendNoInline()
        validateSuspendNoInline(r)
        val l = barSuspendNoInline(r + 42)
        validateSuspendNoInline(l)
        return bazSuspendNoInline(r + l) 
    }

    @Benchmark              
    fun testRunBlocking() = runBlocking {
    }

    @Benchmark
    fun baseline() = runBlocking {
        test(42)
    }

    @Benchmark
    fun noInline() = runBlocking {
        testNoInline(42)
    }

    @Benchmark
    fun suspendUsual() = runBlocking {
        testSuspend(42)
    }

    @Benchmark
    fun suspendNoInline() = runBlocking {
        testSuspendNoInline(42)
    }


}
