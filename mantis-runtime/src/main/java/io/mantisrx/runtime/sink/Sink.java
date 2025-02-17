/*
 * Copyright 2019 Netflix, Inc.
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

package io.mantisrx.runtime.sink;

import io.mantisrx.runtime.Context;
import io.mantisrx.runtime.PortRequest;
import io.mantisrx.runtime.parameter.ParameterDefinition;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.functions.Action3;

public interface Sink<T> extends Action3<Context, PortRequest, Observable<T>>, Closeable {

    default void init(Context context) {}
    default List<ParameterDefinition<?>> getParameters() {
        return Collections.emptyList();
    }
}
