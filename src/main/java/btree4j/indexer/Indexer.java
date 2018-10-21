/*
 * Copyright (c) 2006-2018 Makoto Yui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package btree4j.indexer;

import btree4j.BTreeException;

public interface Indexer {

    public String getName();

    public long add(byte[] key, long value) throws BTreeException;

    public long remove(byte[] key) throws BTreeException;

    public long remove(byte[] key, long value) throws BTreeException;

    public void flush(boolean close) throws BTreeException;

    public void close() throws BTreeException;

    public IndexMatch find(IndexQuery cond) throws BTreeException;

}
