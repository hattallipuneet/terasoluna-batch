/*
 * Copyright (c) 2007 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.terasoluna.fw.util;

import java.util.Comparator;
import java.util.concurrent.Callable;


/**
 * {@link GenericsUtil}のテストのためのスタブクラス。
 * 
 */
public class GenericsUtil_CallableStub02 implements
        Callable<Integer>, Comparable<String>, Comparator<String>  {

    /**
     * {@inheritDoc}
     */
    public Integer call() throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(String o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(String o1, String o2) {
        return 0;
    }

}
