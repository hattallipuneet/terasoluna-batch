/*
 * Copyright (c) 2016 NTT DATA Corporation
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

package jp.terasoluna.fw.batch.executor.controller;

/**
 * ジョブの実行における、DIコンテナ上のエントリポイントとなるインタフェース。<br>
 *
 * @since 3.6
 */
public interface JobOperator {

    /**
     * ジョブを起動する。<br>
     *
     * @param args 起動時引数
     * @return 終了ステータスコード
     */
    int start(String[] args);
}
