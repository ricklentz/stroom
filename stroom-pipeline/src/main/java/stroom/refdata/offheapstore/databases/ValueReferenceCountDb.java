/*
 * Copyright 2018 Crown Copyright
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
 *
 */

package stroom.refdata.offheapstore.databases;

import com.google.inject.assistedinject.Assisted;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.refdata.lmdb.AbstractLmdbDb;
import stroom.refdata.offheapstore.ByteBufferPool;
import stroom.refdata.offheapstore.ValueStoreKey;
import stroom.refdata.offheapstore.serdes.IntegerSerde;
import stroom.refdata.offheapstore.serdes.ValueStoreKeySerde;
import stroom.util.logging.LambdaLogger;
import stroom.util.logging.LambdaLoggerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;

// TODO this was created to hold the value reference counter but then that counter was
// included in the value of the ValueStoreDb. It is debatable whether it is better
// to mutate the ValueStoreDb value (which will involve copying all of the value bytes
// into a new buffer and some values could be large) or use this table which would be a
// cheaper update but would incur an additional cursor lookup.
// Leaving it in in case perf testing reveals the cost of copying value bytes is too high.
@Deprecated
public class ValueReferenceCountDb extends AbstractLmdbDb<ValueStoreKey, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueReferenceCountDb.class);
    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(ValueReferenceCountDb.class);

    private static final String DB_NAME = "ValueReferenceCountStore";

    private final ValueStoreKeySerde keySerde;
    private final IntegerSerde valueSerde;

    @Inject
    public ValueReferenceCountDb(
            @Assisted final Env<ByteBuffer> lmdbEnvironment,
            final ByteBufferPool byteBufferPool,
            final ValueStoreKeySerde keySerde,
            final IntegerSerde valueSerde) {

        super(lmdbEnvironment, byteBufferPool, keySerde, valueSerde, DB_NAME);
        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
    }

    /**
     * increments the reference count by one for the key represented by the valueStoreKeyBuf.
     */
    public void incrementReferenceCount(final Txn<ByteBuffer> writeTxn, final ByteBuffer keyBuffer) {

        //TODO move this method to ValueStoreDb
        updateValue(writeTxn, keyBuffer, valueSerde::increment);
    }

    /**
     * Decrements the reference count by one for the key represented by the valueStoreKeyBuf.
     */
    public void decrementReferenceCount(final Txn<ByteBuffer> writeTxn, final ByteBuffer keyBuffer) {

        //TODO move this method to ValueStoreDb
        updateValue(writeTxn, keyBuffer, valueSerde::decrement);
    }

    public interface Factory {
        ValueReferenceCountDb create(final Env<ByteBuffer> lmdbEnvironment);
    }
}
