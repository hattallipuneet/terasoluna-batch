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

package jp.terasoluna.fw.beans.jxpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathBasicBeanInfo;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link jp.terasoluna.fw.beans.jxpath.BeanPointerEx} クラスのブラックボックステスト。
 * <p>
 * <h4>【クラスの概要】</h4> Beanポインタの拡張クラス。<br>
 * 前提条件：
 * <p>
 * @see jp.terasoluna.fw.beans.jxpath.BeanPointerEx
 */
public class BeanPointerExTest {

    /**
     * testBeanPointerExQname01() <br>
     * <br>
     * (正常系) <br>
     * 観点：A <br>
     * <br>
     * 入力値：(引数) name:not null<br>
     * (引数) bean:new Object()<br>
     * (引数) beanInfo:not null<br>
     * (引数) locale:Locale("ja")<br>
     * (状態) this.beanInfo:null<br>
     * <br>
     * 期待値：(状態変化) this.beanInfo:引数で設定された値。<br>
     * <br>
     * コンストラクタを呼び出すテスト。 <br>
     * @throws Exception このメソッドで発生した例外
     */
    @Test
    public void testBeanPointerExQname01() throws Exception {
        // 前処理
        QName qName = new QName("name");
        Object bean = new Object();
        JXPathBeanInfo beanInfo = new JXPathBasicBeanInfo(bean.getClass());
        Locale locale = new Locale("ja");

        // テスト実施
        BeanPointerEx result = new BeanPointerEx(qName, bean, beanInfo, locale);

        // 判定
        assertEquals(beanInfo, ReflectionTestUtils.getField(result,
                "beanInfo"));
    }

    /**
     * testBeanPointerExNodePointer01() <br>
     * <br>
     * (正常系) <br>
     * 観点：A <br>
     * <br>
     * 入力値：(引数) parent:not null<br>
     * (引数) name:not null<br>
     * (引数) bean:new Object()<br>
     * (引数) beanInfo:not null<br>
     * (状態) this.beanInfo:null<br>
     * <br>
     * 期待値：(状態変化) this.beanInfo:引数で設定された値。<br>
     * <br>
     * コンストラクタを呼び出すテスト。 <br>
     * @throws Exception このメソッドで発生した例外
     */
    @Test
    public void testBeanPointerExNodePointer01() throws Exception {
        // 前処理
        QName qName = new QName("name");
        Object bean = new Object();
        JXPathBeanInfo beanInfo = new JXPathBasicBeanInfo(bean.getClass());
        Locale locale = new Locale("ja");
        NodePointer nodePointer = NodePointer.newNodePointer(qName, bean,
                locale);

        // テスト実施
        BeanPointerEx result = new BeanPointerEx(nodePointer, qName, bean, beanInfo);

        // 判定
        assertEquals(beanInfo, ReflectionTestUtils.getField(result,
                "beanInfo"));
    }

    /**
     * testGetPropertyPointer01() <br>
     * <br>
     * (正常系) <br>
     * 観点：A <br>
     * <br>
     * 入力値：(状態) this.beanInfo:not null<br>
     * <br>
     * 期待値：(戻り値) PropertyPointer:new BeanPropertyPointerEx{<br>
     * parent=this<br>
     * beanInfo=前提条件のbeanInfo<br>
     * }<br>
     * <br>
     * プロパティポインタを取得するメソッドのテスト。 <br>
     * @throws Exception このメソッドで発生した例外
     */
    @Test
    public void testGetPropertyPointer01() throws Exception {
        // 前処理
        QName qName = new QName("name");
        Object bean = new Object();
        JXPathBeanInfo beanInfo = new JXPathBasicBeanInfo(bean.getClass());
        Locale locale = new Locale("ja");
        BeanPointerEx beanPointer = new BeanPointerEx(qName, bean, beanInfo, locale);

        // テスト実施
        PropertyPointer result = beanPointer.getPropertyPointer();

        // 判定
        assertSame(BeanPropertyPointerEx.class, result.getClass());
        assertSame(beanPointer, result.getParent());
        assertSame(beanInfo, ReflectionTestUtils.getField(result, "beanInfo"));
    }

}
