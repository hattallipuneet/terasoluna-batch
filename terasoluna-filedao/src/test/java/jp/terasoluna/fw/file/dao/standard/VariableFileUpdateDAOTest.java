package jp.terasoluna.fw.file.dao.standard;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import jp.terasoluna.fw.file.dao.FileLineWriter;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link jp.terasoluna.fw.file.dao.standard.VariableFileUpdateDAO} クラスのテスト。
 * <p>
 * <h4>【クラスの概要】</h4> 可変長ファイル用のFileLineWriterを生成する。<br>
 * AbstractFileUpdateDAOのサブクラス。
 * <p>
 * @see jp.terasoluna.fw.file.dao.standard.VariableFileUpdateDAO
 */
public class VariableFileUpdateDAOTest {

    /**
     * testExecute01() <br>
     * <br>
     * (正常系) <br>
     * 観点：E <br>
     * <br>
     * 入力値：(引数) fileName:VariableFileUpdateDAO_execute01.txt<br>
     * データを持たないファイルのパス<br>
     * (引数) clazz:VariableFileUpdateDAO_Stub01<br>
     * 空実装<br>
     * (状態) AbstractFileUpdateDAO.columnFormatterMap:以下の要素を持つMap<String, ColumnFormatter>インスタンス<br>
     * ・"java.lang.String"=NullColumnFormatterインスタンス<br>
     * <br>
     * 期待値：(戻り値) FileLineWriter:VariableFileLineWriterのインスタンス<br>
     * (状態変化) VariableFileLineWriter:コンストラクタが1回呼ばれること。<br>
     * 引数が呼び出しパラメータに渡ってくること。<br>
     * <br>
     * 引数がそれぞれnot nullであれば、戻り値が帰ってくることを確認する。<br>
     * このメソッドは、VariableFileLineWriterのコンストラクタを呼び出すだけなので、引数のバリエーションは一つしか行わない。 <br>
     * @throws Exception このメソッドで発生した例外
     */
    @Test
    public void testExecute01() throws Exception {
        // テスト対象のインスタンス化
        VariableFileUpdateDAO fileUpdateDAO = new VariableFileUpdateDAO();

        // 引数の設定
        URL url = this.getClass().getResource("File_Empty.txt");
        String fileName = url.getPath();
        Class<VariableFileUpdateDAO_Stub01> clazz = VariableFileUpdateDAO_Stub01.class;

        // 前提条件の設定
        Map<String, ColumnFormatter> columnFormatterMap = new HashMap<String, ColumnFormatter>();
        columnFormatterMap.put("java.lang.String", new NullColumnFormatter());
        ReflectionTestUtils.setField(fileUpdateDAO, "columnFormatterMap",
                columnFormatterMap);

        // テスト実施
        FileLineWriter<VariableFileUpdateDAO_Stub01> fileLineWriter = fileUpdateDAO
                .execute(fileName, clazz);

        // 返却値の確認
        assertEquals(VariableFileLineWriter.class, fileLineWriter.getClass());

        // 状態変化の確認
        assertEquals(fileName, ReflectionTestUtils.getField(fileLineWriter,
                "fileName"));
        assertEquals(clazz, ReflectionTestUtils.getField(fileLineWriter,
                "clazz"));
        assertEquals(columnFormatterMap, ReflectionTestUtils.getField(
                fileLineWriter, "columnFormatterMap"));
    }

}
