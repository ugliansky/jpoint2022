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

package org.sample;

import org.openjdk.jmh.annotations.Benchmark;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 20, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class YieldAndStay  {
    static final ContinuationScope SCOPE = new ContinuationScope() { };

    static class Arg {
        volatile int field;
    }

    static class InfiniteYielderAtLevel implements Runnable {
        private final int paramCount;
        private final int maxDepth;

        private InfiniteYielderAtLevel(int paramCount, int maxDepth) {
            if (paramCount < 1) {
                throw new IllegalArgumentException();
            }
            this.paramCount = paramCount;
            this.maxDepth = maxDepth;
        }

        @Override
        public void run() {
            switch (paramCount) {
                case 1: run1(maxDepth); break;
                case 2: run2(maxDepth, new Arg()); break;
                case 3: run3(maxDepth, new Arg(), new Arg()); break;
                default: throw new Error("should not happen");
            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        private void run1(int depth) {
            if (depth > 0) {
                run1(depth - 1);
            } if (depth == 0) {
                for (; ;) {
                    Continuation.yield(SCOPE);
                }
            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        private void run2(int depth, Arg arg2) {
            if (depth > 0) {
                run2(depth - 1, arg2);
            } if (depth == 0) {
                for (; ;) {
                    Continuation.yield(SCOPE);
                }
            } else {
                // never executed
                arg2.field = 0;
            }
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        private void run3(int depth, Arg arg2, Arg arg3) {
            if (depth > 0) {
                run3(depth - 1, arg2, arg3);
            } if (depth == 0) {
                for (; ;) {
                    Continuation.yield(SCOPE);
                }
            } else {
                // never executed
                arg2.field = 0;
                arg3.field = 0;
            }
        }

        static Continuation continuation(int paramCount, int maxDepth) {
            Runnable task = new InfiniteYielderAtLevel(paramCount, maxDepth);
            return new Continuation(SCOPE, task);
        }
    }

    @Param({"1", "2", "3"})
    public int paramsCount;

    @Param({"5", "10", "20", "100", "200", "500"})
    public int stackDepth;

    Continuation cont;

    @Setup(Level.Trial)
    public void setup() {
        cont = InfiniteYielderAtLevel.continuation(paramsCount, stackDepth);
        cont.run();
    }

    /**
     * Creates and runs a continuation that yields at a given stack depth.
     */
    @Benchmark
    public void yieldAtTheBottom() {
        cont.run();
    }
}
