/*
 * Copyright 2011 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.parallel;

import java.util.ArrayList;
import java.util.List;

import com.gs.collections.api.block.procedure.primitive.ObjectIntProcedure;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.utility.ArrayListIterate;
import com.gs.collections.impl.utility.ListIterate;

public final class ObjectIntProcedureFJTask<T, BT extends ObjectIntProcedure<? super T>> implements Runnable
{
    private final ObjectIntProcedureFactory<BT> procedureFactory;
    private BT procedure;
    private final List<T> list;
    private final int start;
    private final int end;
    private final ObjectIntProcedureFJTaskRunner<T, BT> taskRunner;

    /**
     * Creates an array of ProcedureFJTasks wrapping Procedures created by the specified ProcedureFactory.
     */
    public ObjectIntProcedureFJTask(
            ObjectIntProcedureFJTaskRunner<T, BT> newFJTaskRunner,
            ObjectIntProcedureFactory<BT> newProcedureFactory,
            List<T> list,
            int index,
            int sectionSize,
            boolean isLast)
    {
        this.taskRunner = newFJTaskRunner;
        this.procedureFactory = newProcedureFactory;
        this.list = list;
        this.start = index * sectionSize;
        this.end = isLast ? this.list.size() : this.start + sectionSize;
    }

    public void run()
    {
        try
        {
            this.procedure = this.procedureFactory.create();
            int stop = this.end - 1;
            if (this.list instanceof MutableList)
            {
                ((MutableList<T>) this.list).forEachWithIndex(this.start, stop, this.procedure);
            }
            else if (this.list instanceof ArrayList)
            {
                ArrayListIterate.forEachWithIndex((ArrayList<T>) this.list, this.start, stop, this.procedure);
            }
            else
            {
                ListIterate.forEachWithIndex(this.list, this.start, stop, this.procedure);
            }
        }
        catch (Exception newError)
        {
            this.taskRunner.setFailed(newError);
        }
        finally
        {
            this.taskRunner.taskCompleted(this);
        }
    }

    public BT getProcedure()
    {
        return this.procedure;
    }
}
