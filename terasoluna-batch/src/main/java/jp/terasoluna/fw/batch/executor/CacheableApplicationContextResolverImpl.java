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

package jp.terasoluna.fw.batch.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;

import jp.terasoluna.fw.batch.constants.LogId;
import jp.terasoluna.fw.batch.executor.vo.BatchJobData;
import jp.terasoluna.fw.logger.TLogger;

/**
 * DIコンテナのキャッシュを実現する{@code ApplicationContextResolver}実装。
 * 非同期バッチ起動を行い同じジョブを繰り返し実行する場合、DIコンテナのキャッシュによる性能向上が見込まれる。
 * <p>
 * 本クラスではSpring Cache Abstractionを用いて、ジョブ業務コードをキーとしたDIコンテナのキャッシュを行う。
 * キャッシュの対象となるのはジョブBean定義ファイルにもとづいたDIコンテナのみであり、システム用アプリケーションコンテキストは対象としない。
 *
 * DIコンテナのキャッシュを使用するためには、Bean定義ファイル内に{@code CacheManager}の定義・インジェクションが必要となる。
 * </p>
 * <p>
 * Bean定義ファイルの記述例：
 * <code><pre>
 * &lt;!-- cache名前空間のXMLスキーマ定義を追加 --&gt;
 * &lt;beans xmlns=&quot;http://www.springframework.org/schema/beans&quot;
 *    xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;
 *    xsi:schemaLocation=&quot;http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd&quot;&gt;
 *    (略)
 *
 *   &lt;bean id=&quot;cacheManager&quot; class=&quot;org.springframework.cache.support.SimpleCacheManager&quot;&gt;
 *     &lt;property name=&quot;caches&quot;&gt;
 *       &lt;set&gt;
 *         &lt;bean class=&quot;org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean&quot;&gt;
 *           &lt;!-- DIコンテナのキャッシュ名はbusinessContext固定 --&gt;
 *           &lt;property name=&quot;name&quot; value=&quot;businessContext&quot;/&gt;
 *         &lt;/bean&gt;
 *       &lt;/set&gt;
 *     &lt;/property&gt;
 *   &lt;/bean&gt;
 *   &lt;bean id=&quot;blogicContextResolver&quot; class=&quot;jp.terasoluna.fw.batch.executor.CacheableApplicationContextResolverImpl&quot;&gt;
 *     &lt;!-- 共通コンテキストをDIコンテナの親とする場合、commonContextClassPathでBean定義ファイルのクラスパスを記述する。(複数指定時はカンマ区切り) --&gt;
 *     &lt;property name=&quot;commonContextClassPath&quot; value=&quot;beansDef/commonContext.xml,beansDef/dataSource.xml&quot;/&gt;
 *     &lt;!-- cacheManagerのsetter-injection --&gt;
 *     &lt;property name=&quot;cacheManager&quot; ref=&quot;cacheManager&quot;/&gt;
 *   &lt;/bean&gt;
 *   (略)
 * &lt;/beans&gt;
 * </pre></code>
 * </p>
 * <p>
 * 使用上の注意点として、上記記述例で使用しているキャッシュ名のbusinessContextは固定名であり、
 * 変更することはできない。
 * 既にSpring Cache Abstractionの{@code ConcurrentMapCacheFactoryBean}による
 * ローカルキャッシュを使用している場合、{@code cacheManager}のBean定義にbusinessContextのキャッシュ領域を追加し、
 * {@code cacheManager}をインジェクションすることで本機能によるDIコンテナのキャッシュと併用可能となる。
 *
 * また、{@code closeApplicationContext()}メソッドではキャッシュ対象のDIコンテナのクローズは行わず、
 * {@code #destroy()}メソッドで一括でクローズする。
 * </p>
 * @since 3.6
 */
public class CacheableApplicationContextResolverImpl
        extends ApplicationContextResolverImpl
        implements InitializingBean, DisposableBean {

    /**
     * ロガー
     */
    private static final TLogger LOGGER = TLogger.getLogger(
            CacheableApplicationContextResolverImpl.class);
    
    /**
     * ジョブ業務コードごとのロックモニタを格納するホルダー
     */
    private ConcurrentMap<String, Object> lockMonitorHolder = new ConcurrentHashMap<>();
    
    /**
     * DIコンテナキャッシュを管理するキャッシュマネージャー
     */
    protected CacheManager cacheManager;

    /**
     * キャッシュ対象となるDIコンテナのキャッシュキー
     */
    public static final String BLOGIC_CONTEXT_CACHE_KEY = "businessContext";

    /**
     * DIコンテナのキャッシュを保持するキャッシュマネージャを設定する。
     *
     * @param cacheManager キャッシュマネージャ
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * ジョブ業務コードをキーとして、キャッシュ済みのDIコンテナを返却する。<br>
     * キャッシュが行われていない場合、親クラスによってDIコンテナを生成し、結果をキャッシュする。
     * </p>
     * @param batchJobData ジョブ実行時のパラメータ（ジョブ業務コード{@code BatchJobData.jobAppCd}がキャッシュキーとなる）
     */
    @Override
    public ApplicationContext resolveApplicationContext(
            BatchJobData batchJobData) {
        
        Cache cache = this.cacheManager.getCache(BLOGIC_CONTEXT_CACHE_KEY);
        
        String jobAppCd = batchJobData.getJobAppCd();
        
        // すでにキャッシュされていれば、それを返却する
        ApplicationContext jobAppCtx = cache.get(jobAppCd, ApplicationContext.class);
        if (jobAppCtx != null) {
            return jobAppCtx;
        }
        
        // まだキャッシュされていない場合、ジョブ業務コードごとに同期化して、コンテキストを生成しキャッシュする
        Object lockMonitor = getLockMonitor(jobAppCd);
        synchronized (lockMonitor) {
            jobAppCtx = cache.get(jobAppCd, ApplicationContext.class);
            if (jobAppCtx == null) {
                LOGGER.info(LogId.IAL025019, batchJobData.getJobAppCd());
                jobAppCtx = super.resolveApplicationContext(batchJobData);
                cache.put(jobAppCd, jobAppCtx);
            }
        }
        return jobAppCtx;
    }
    
    /**
     * ジョブ業務コードに対応するロックモニタを返却する。
     * 
     * @param key ジョブ業務コード
     * @return ロックモニタオブジェクト
     */
    private Object getLockMonitor(String key){
        Object lockObjCandidate = new Object();
        Object lockObj = lockMonitorHolder.putIfAbsent(key, lockObjCandidate);
        // まだホルダーになかった場合はnullが返ってくるので、モニタ候補を正式版に格上げする
        // ホルダーにあった場合はそれを使う
        if (lockObj == null) {
            lockObj = lockObjCandidate;
        }
        return lockObj;
    }


    /**
     * {@inheritDoc}
     *
     * キャッシュ機能利用が前提となるため、本メソッドはスキップする。
     *
     * @param applicationContext 業務用Bean定義のアプリケーションコンテキスト
     */
    @Override
    public void closeApplicationContext(ApplicationContext applicationContext) {
        // キャッシュされたDIコンテナをクローズしない。
    }

    /**
     * 初期化処理としてキャッシュ機能が使用不可能な状態であるとき、{@code BeanCreationException}をスローする。
     */
    @Override
    public void afterPropertiesSet() {
        if (!isCacheEnabled()) {
            // キャッシュ機能使用不可の場合
            throw new BeanCreationException(LOGGER.getLogMessage(LogId.EAL025061, BLOGIC_CONTEXT_CACHE_KEY));
        }
        super.afterPropertiesSet();
    }

    /**
     * 本インスタンス破棄時、共有コンテキスト及びキャッシュとして保持されている
     * DIコンテナの破棄を行う。
     */
    @Override
    public void destroy() {
        destroyCachedContext();
        // 子コンテキストを破棄しても親コンテキストは破棄されないため、
        // DIコンテナ破棄の後で親である共通コンテキストの破棄を行う。
        super.destroy();
    }

    /**
     * キャッシュされたDIコンテナの破棄とキャッシュ自身の破棄を行う。
     */
    protected void destroyCachedContext() {
        Cache cache = this.cacheManager.getCache(BLOGIC_CONTEXT_CACHE_KEY);
        Collection<?> cacheValues = Map.class.cast(cache.getNativeCache())
                .values();
        for (Object obj : cacheValues) {
            if (obj instanceof ApplicationContext) {
                super.closeApplicationContext(ApplicationContext.class.cast(obj));
            }
        }
        cache.clear();
    }

    /**
     * DIコンテナがキャッシュ可能であるかを判定する。
     *
     * @return キャッシュ可能ならばtrue、キャッシュ機能を使用していないためキャッシュ不可能ならばfalse
     */
    protected boolean isCacheEnabled() {
        if (this.cacheManager == null || !this.cacheManager.getCacheNames()
                .contains(BLOGIC_CONTEXT_CACHE_KEY)) {
            return false;
        }
        Cache cache = this.cacheManager.getCache(BLOGIC_CONTEXT_CACHE_KEY);
        if (cache == null) {
            return false;
        }
        // NoOpCache使用時以外はConcurrentMapCacheとなる。
        return cache.getNativeCache() instanceof Map;
    }
}
