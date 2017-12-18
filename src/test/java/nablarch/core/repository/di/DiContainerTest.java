package nablarch.core.repository.di;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.CoreMatchers;

import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.di.config.DuplicateDefinitionPolicy;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.repository.di.test.Component1;
import nablarch.core.repository.di.test.Component2;
import nablarch.core.repository.di.test.Component3;
import nablarch.core.repository.di.test.Component4;
import nablarch.core.repository.di.test.Component6;
import nablarch.core.repository.di.test.Component8;
import nablarch.core.repository.di.test.NestedComponent;
import nablarch.core.repository.di.test.SurrogatePair;
import nablarch.core.repository.test.OnMemoryLogWriter;
import nablarch.core.repository.test.SystemPropertyResource;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * DiContainerクラスのテスト。
 *
 * @author asano
 */
public class DiContainerTest {

    @Rule
    public final SystemPropertyResource systemPropertyResource = new SystemPropertyResource();

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
    }

    /**
     * ObjectLoader が、Mapからの取得順にロードされることを確認する。
     */
    @Test
    public void testObjectLoaderSequential() {

        final ObjectLoader childLoader0 = createSimpleLoader("key1", "val0");
        final ObjectLoader childLoader1 = createSimpleLoader("key1", "val1");
        final ObjectLoader childLoader2 = createSimpleLoader("key1", "val2");
        final ObjectLoader childLoader3 = createSimpleLoader("key1", "val3");
        final ObjectLoader childLoader4 = createSimpleLoader("key1", "val4");
        final ObjectLoader childLoader5 = createSimpleLoader("key1", "val5");
        final ObjectLoader childLoader6 = createSimpleLoader("key1", "val6");
        final ObjectLoader childLoader7 = createSimpleLoader("key1", "val7");
        final ObjectLoader childLoader8 = createSimpleLoader("key1", "val8");
        final ObjectLoader childLoader9 = createSimpleLoader("key1", "val9");

        ComponentDefinitionLoader cdl = new ComponentDefinitionLoader() {

            @Override
            public List<ComponentDefinition> load(DiContainer container) {
                List<ComponentDefinition> defs = new ArrayList<ComponentDefinition>();
                defs.add(new ComponentDefinition(0, "child0", new StoredValueComponentCreator(childLoader0),
                        childLoader0.getClass()));
                defs.add(new ComponentDefinition(1, "child1", new StoredValueComponentCreator(childLoader1),
                        childLoader1.getClass()));
                defs.add(new ComponentDefinition(2, "child2", new StoredValueComponentCreator(childLoader2),
                        childLoader2.getClass()));
                defs.add(new ComponentDefinition(3, "child3", new StoredValueComponentCreator(childLoader3),
                        childLoader3.getClass()));
                defs.add(new ComponentDefinition(4, "child4", new StoredValueComponentCreator(childLoader4),
                        childLoader4.getClass()));
                defs.add(new ComponentDefinition(5, "child5", new StoredValueComponentCreator(childLoader5),
                        childLoader5.getClass()));
                defs.add(new ComponentDefinition(6, "child6", new StoredValueComponentCreator(childLoader6),
                        childLoader6.getClass()));
                defs.add(new ComponentDefinition(7, "child7", new StoredValueComponentCreator(childLoader7),
                        childLoader7.getClass()));
                defs.add(new ComponentDefinition(8, "child8", new StoredValueComponentCreator(childLoader8),
                        childLoader8.getClass()));
                defs.add(new ComponentDefinition(9, "child9", new StoredValueComponentCreator(childLoader9),
                        childLoader9.getClass()));
                return defs;
            }
        };

        DiContainer container = new DiContainer(cdl);

        assertThat(container.getComponentByName("key1"), CoreMatchers.<Object>is("val9"));
    }

    private ObjectLoader createSimpleLoader(final String key, final Object value) {
        return new ObjectLoader() {

            @Override
            public Map<String, Object> load() {
                Map<String, Object> ret = new LinkedHashMap() {
                    {
                        put(key, value);
                    }
                };

                return ret;
            }
        };
    }

    @Test
    public void testLoad() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoad.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.getComponent2()
                               .getProp1(), is("prop2"));
        // 初期化の確認
        assertThat(comp1.getInitValue(), is("init"));
        Map<String, String> map = comp1.getComponent2()
                                       .getInitMap();
        assertThat(map.get("1"), is("10"));
        assertThat(map.get("2"), is("20"));
        assertThat(map.get("3"), is("30"));
    }

    @Test
    public void testIgnorePropertyWithReasonMessage_shouldWriteWarnLog() throws Exception {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testIgnorePropertyWithReasonMessage_shouldWriteWarnLog/test.xml");
        new DiContainer(loader);
        
        final List<String> messages = OnMemoryLogWriter.getMessages("writer.appLog");
        String warnMessage = null;
        for (final String message : messages) {
            if (message.contains("WARN")) {
                if (warnMessage != null) {
                    fail("WARNレベルのログは１つだけであること");
                }
                warnMessage = message;
            }
        }

        assertThat(warnMessage, allOf(
                containsString("Setting to this property is invalid(invalid reason:廃止されました。)."),
                containsString("It is recommended to delete the setting."),
                containsString("class:nablarch.core.repository.di.test.IgnorePropertyBean propertyName:ignore")
        ));
    }
    
    @Test
    public void testIgnorePropertyWithoutReasonMessage_shouldWriteWarnLog() throws Exception {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testIgnorePropertyWithoutReasonMessage_shouldWriteWarnLog/test.xml");
        new DiContainer(loader);

        final List<String> messages = OnMemoryLogWriter.getMessages("writer.appLog");
        String warnMessage = null;
        for (final String message : messages) {
            if (message.contains("WARN")) {
                if (warnMessage != null) {
                    fail("WARNレベルのログは１つだけであること");
                }
                warnMessage = message;
            }
        }

        assertThat(warnMessage, allOf(
                containsString("Setting to this property is invalid."),
                containsString("It is recommended to delete the setting."),
                containsString("class:nablarch.core.repository.di.test.IgnorePropertyBean2 propertyName:ignore")
        ));
    }

    @Test
    public void testLoadLiteralValue() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadLiteralValue.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.isBoolProp(), is(true));
        assertThat(comp1.getWrapBoolProp(), is(true));
        assertThat(comp1.getIntProp(), is(2));
        assertThat(comp1.getWrapIntProp(), is(3));
        assertThat(comp1.getLongProp(), is(5l));
        assertThat(comp1.getWrapLongProp(), is(6l));

        assertThat(comp1.getArrayProp1().length, is(5));
        assertThat(comp1.getArrayProp1()[0], is("abc"));
        assertThat(comp1.getArrayProp1()[1], is("def"));
        assertThat(comp1.getArrayProp1()[2], is(""));
        assertThat(comp1.getArrayProp1()[3], is("ghi"));
        assertThat(comp1.getArrayProp1()[4], is(""));

        assertThat(comp1.getArrayProp2().length, is(3));
        assertThat(comp1.getArrayProp2()[0], is("abc"));
        assertThat(comp1.getArrayProp2()[1], is("def"));
        assertThat(comp1.getArrayProp2()[2], is("ghi"));

        assertThat(comp1.getIntArrayProp(), is(new int[] {1, 2, 3}));
        assertThat(comp1.getIntegerArrayProp(), is(new Integer[] {4, 5, 6}));
    }

    @Test
    public void testLoadAutowireByType() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireByType.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.getComponent2().getProp1(), is("prop2"));
    }

    @Test
    public void testLoadAutowireByTypeWithInterface() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireByTypeWithInterface.xml");
        DiContainer container = new DiContainer(loader);

        Component6 comp6 = container.getComponentByName("comp6");
        // "this is messsage"が出力される
        System.out.println(comp6.callTest());


        assertThat(comp6.callTest(), is("this is message"));
    }

    @Test
    public void testLoadAutowireByTypeWithAncesterInterface() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireByTypeWithAncesterInterface.xml");
        DiContainer container = new DiContainer(loader);

        Component6 comp6 = container.getComponentByName("comp6");
        // "this is messsage"が出力される
        System.out.println(comp6.callTest());
        assertThat(comp6.callTest(), is("this is message"));
    }

    @Test
    public void testLoadAutowireByName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireByName.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.getComponent2().getProp1(), is("prop2"));
    }

    @Test
    public void testLoadAutowireNone() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireNone.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.getComponent2(), nullValue());
    }


    @Test
    public void testLoadNestedComponent() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedComponent.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp1.getComponent2()
                               .getProp1(), is("prop2"));
    }


    @Test
    public void testLoadMapDefinition() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadMapDefinition.xml");
        DiContainer container = new DiContainer(loader);

        Component3 comp3 = container.getComponentByName("component3");
        Map<Object, Object> mapProp = comp3.getMapProp();
        Component2 comp2_1 = (Component2) mapProp.get("key2");
        Component2 comp2_2 = (Component2) mapProp.get("key3");
        Object val2 = null;
        Object val3 = null;
        for (Entry<Object, Object> entry : mapProp.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof Component2) {
                Component2 comp2 = (Component2) key;
                if ("prop1-3".equals(comp2.getProp1())) {
                    val2 = value;
                } else if ("prop1-4".equals(comp2.getProp1())) {
                    val3 = value;
                }
            }
        }


        assertThat(mapProp.size(), is(5));
        assertThat(mapProp.get("key1"), CoreMatchers.<Object>is("value1"));
        assertThat(comp2_1.getProp1(), is("prop1-1"));
        assertThat(comp2_2.getProp1(), is("prop1-2"));
        assertThat(val2, CoreMatchers.<Object>is("value2"));
        assertThat(val3, CoreMatchers.<Object>is("value3"));
    }

    @Test
    public void testLoadMapWithNameDefinition() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadMapWithNameDefinition.xml");
        DiContainer container = new DiContainer(loader);

        Map<Object, Object> mapProp = container.getComponentByName("testMap");
        Object val2 = null;
        Object val3 = null;
        for (Entry<Object, Object> entry : mapProp.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (key instanceof Component2) {
                Component2 comp2 = (Component2) key;
                if ("prop1-3".equals(comp2.getProp1())) {
                    val2 = value;
                } else if ("prop1-4".equals(comp2.getProp1())) {
                    val3 = value;
                }
            }
        }

        assertThat(mapProp.size(), is(5));
        assertThat(mapProp.get("key1"), CoreMatchers.<Object>is("value1"));
        assertThat(val2, CoreMatchers.<Object>is("value2"));
        assertThat(val3, CoreMatchers.<Object>is("value3"));
    }


    @Test
    public void testLoadList() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadList.xml");
        DiContainer container = new DiContainer(loader);

        Component3 comp3 = container.getComponentByName("component3");
        List<Object> listProp = comp3.getListProp();
        Component2 comp2_1 = (Component2) listProp.get(0);
        String str1 = (String) listProp.get(1);
        Component2 comp2_2 = (Component2) listProp.get(2);
        String str2 = (String) listProp.get(3);

        assertThat(comp2_1.getProp1(), is("prop1-1"));
        assertThat(str1, is("strval1"));
        assertThat(comp2_2.getProp1(), is("prop1-2"));
        assertThat(str2, is("strval2"));
    }


    @Test
    public void testLoadNamedList() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNamedList.xml");
        DiContainer container = new DiContainer(loader);

        List<Object> listProp = container.getComponentByName("testList");
        Component2 comp2_1 = (Component2) listProp.get(0);
        String str1 = (String) listProp.get(1);
        Component2 comp2_2 = (Component2) listProp.get(2);
        Component2 comp2_3 = (Component2) listProp.get(3);
        String str2 = (String) listProp.get(4);

        assertThat(comp2_1.getProp1(), is("prop1-1"));
        assertThat(str1, is("strval1"));
        assertThat(comp2_2.getProp1(), is("prop1-2"));
        assertThat(comp2_3.getProp1(), is("prop1-3"));
        assertThat(str2, is("strval2"));
    }


    @Test
    public void testLoadAutowireIgnoreString() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadAutowireIgnoreString.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");

        assertThat(comp1.getProp1(), is("string value"));
        assertThat(comp2.getProp1(), sameInstance(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadPropertyFile() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadPropertyFile.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");
        Map<String, String> map = container.getComponentByName("testMap");
        List<String> list = container.getComponentByName("testList");

        assertThat(comp1.getProp1(), is("value 01!"));
        assertThat(comp2.getProp1(), is("value 01!-value 02!"));
        assertThat(map.get("[value 03!]"), is("[value 04!]"));
        assertThat(list.get(0), is("[value 05!]"));
    }

    @Test
    public void testLoadPropertyFileFromClasspath() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadPropertyFileFromClasspath.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");

        assertThat(comp1.getProp1(), is("value 01!"));
        assertThat(comp2.getProp1(), is("value 01!-value 02!"));
    }


    /**
     * ディレクトリに格納された環境設定ファイルをワイルドカード指定で読み込めることの確認テスト。
     * <p/>
     * 必要なファイルだけ読み込まれ、不要なファイルやディレクトリは読み込まれないことを確認。
     */
    @Test
    public void testLoadPropertyFileWildcard() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadPropertyFileWildcard/testLoadPropertyFileWildcard.xml");
        DiContainer container = new DiContainer(loader);

        String value1 = container.getComponentByName("key.file1");
        String value2 = container.getComponentByName("key.file2");
        String value3 = container.getComponentByName("key.file3");

        assertThat(value1, is("value01"));
        assertThat(value2, is("value02"));
        assertThat(value3, sameInstance(null));
    }

    /**
     * 存在しないディレクトリおよび、条件に合致するファイルが見つからない場合でも、例外が発生せず、正常に処理が行われることのテスト。
     */
    @Test
    public void testLoadPropertyFileFromDirNotFoundDirOrFile() throws Throwable {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadPropertyFileFromDirNotFoundDirOrFile/test1.xml");
        try {
            new DiContainer(loader);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("directory not found."));
        }
        
        /*
         * 条件に合致するファイルが見つからない場合
         */

        loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadPropertyFileFromDirNotFoundDirOrFile/test2.xml");
        DiContainer container = new DiContainer(loader);

        assertThat(container.getComponentByName("key.file1"), nullValue());
        assertThat(container.getComponentByName("key.file2"), nullValue());
        assertThat(container.getComponentByName("key.file3"), nullValue());
    }


    /**
     * config-file要素のdir属性のテスト。
     * <p/>
     * コンポーネント設定ファイルからの相対パスで環境設定ファイルが取得できることを確認する。
     * <p/>
     * 以下の動作が正常に行われることを確認する。
     * <ul>
     * <li>dir属性に指定したコンポーネント設定ファイルの２階層下のディレクトリから環境設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの１階層下のディレクトリから環境設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの１階層上のディレクトリから環境設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの同階層のディレクトリから環境設定ファイルを読み込む</li>
     * </ul>
     */
    @Test
    public void testLoadPropertyFileFromDir() throws Throwable {
        loadPropertyFileFromDir();
    }


    private void loadPropertyFileFromDir() {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/loadPropertyFileFromDir/test1.xml");
        DiContainer container = new DiContainer(loader);

        String value1 = container.getComponentByName("key.file1");
        String value2 = container.getComponentByName("key.file2");
        String value3 = container.getComponentByName("key.file3");


        assertThat(value1, is("value01")); // test01.propertiesの内容が読み込まれる
        assertThat(value2, is("value02")); // test02.propertiesの内容が読み込まれる
        assertThat(value3, is(nullValue())); // test03.xmlの内容は読み込まれない
        
        
        /*
         * ファイル（test01.properties）名を直接指定するパターン。
         */
        loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/loadPropertyFileFromDir/test2.xml");
        container = new DiContainer(loader);

        value1 = container.getComponentByName("key.file1");
        value2 = container.getComponentByName("key.file2");
        value3 = container.getComponentByName("key.file3");

        assertThat(value1, is("value01")); // test01.propertiesの内容が読み込まれる
        assertThat(value2, is(nullValue())); // test02.propertiesの内容が読み込まれる
        assertThat(value3, is(nullValue())); // test03.xmlの内容は読み込まれない
    }

    /**
     * config-file要素のdir属性のテスト。
     * <p/>
     * コンポーネント設定ファイルがクラスパスに存在する場合（URLがclasspath:）でも正常にパスが解釈され、
     * １階層下のディレクトリ（パッケージ）に存在する環境設定ファイルが取得できることを確認。
     */
    @Test
    public void testLoadPropertyFileFromDirInClassPath() throws Throwable {

        // クラスパスに存在するコンポーネント設定ファイル
        URI componentFileUri = new URI("classpath:nablarch/core/repository/di/config/configfile_test_context.xml");

        // 上記設定ファイルには以下の定義が行われている
        // <component-configuration xmlns="http://tis.co.jp/nablarch/component-configuration" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        //     xsi:schemaLocation="http://tis.co.jp/nablarch/component-configuration /home/ssd/workspace/Nablarch/resources/component-configuration.xsd">
        //     <config-file dir="./configs" file="*.properties" />
        // </component-configuration>


        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(componentFileUri.toString());
        DiContainer container = new DiContainer(loader);

        String value1 = container.getComponentByName("key.file1");
        String value2 = container.getComponentByName("key.file2");

        assertThat(value1, is("value01")); // test01.propertiesの内容が読み込まれる
        assertThat(value2, is("value02")); // test02.propertiesの内容が読み込まれる

    }

    @Test
    public void testLoadNestedFile() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedFile.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component2 comp2 = comp1.getComponent2();

        assertThat(comp2.getProp1(), is("str01"));
    }


    /**
     * ディレクトリに格納されたコンポーネント設定ファイルをワイルドカード指定で読み込めることの確認テスト。
     * <p/>
     * 必要なファイルだけ読み込まれ、不要なファイルやディレクトリは読み込まれないことを確認。
     */
    @Test
    public void testLoadNestedFileWildcard() throws Throwable {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedFileWildcard/test.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");

        assertThat(comp1.getProp1(), is("str01"));
        assertThat(comp2.getProp1(), is("str02"));
        assertThat(container.getComponentByName("comp3"), sameInstance(null));
    }


    /**
     * 存在しないディレクトリおよび、条件に合致するファイルが見つからない場合でも、例外が発生せず、正常に処理が行われることのテスト。
     */
    @Test
    public void testLoadNestedFileFromDirNotFoundDirOrFile() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedFileFromDirNotFoundDirOrFile/test1.xml");
        try {
            new DiContainer(loader);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("directory not found."));
        }
        

        loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedFileFromDirNotFoundDirOrFile/test2.xml");
        DiContainer container = new DiContainer(loader);


        assertThat(container.getComponentByName("comp1"), nullValue());
        assertThat(container.getComponentByName("comp2"), nullValue());
    }

    /**
     * import要素のdir属性のテスト。
     * <p/>
     * コンポーネント設定ファイルからの相対パスで他のコンポーネント設定ファイルが取得できることを確認する。
     * <p/>
     * 以下の動作が正常に行われることを確認する。
     * <ul>
     * <li>dir属性に指定したコンポーネント設定ファイルの２階層下のディレクトリからコンポーネント設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの１階層下のディレクトリからコンポーネント設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの１階層上のディレクトリからコンポーネント設定ファイルを読み込む</li>
     * <li>dir属性に指定したコンポーネント設定ファイルの同階層のディレクトリからコンポーネント設定ファイルを読み込む</li>
     * </ul>
     */
    @Test
    public void testLoadNestedFileFromDir() throws Throwable {
        loadNestedFileFromDir();
    }


    private void loadNestedFileFromDir() {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/loadNestedFileFromDir/root.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");
        Component1 comp5 = container.getComponentByName("comp5");
        Component1 comp6 = container.getComponentByName("comp6");

        assertThat(comp1.getProp1(), is("str01"));
        assertThat(comp2.getProp1(), is("str02"));
        assertThat(container.getComponentByName("comp3"), sameInstance(null));
        assertThat(comp5.getProp1(), is("str05"));
        assertThat(comp6.getProp1(), is("str06"));

    }

    /**
     * import要素のdir属性のテスト。
     * <p/>
     * コンポーネント設定ファイルがクラスパスに存在する場合（URLがclasspath:）でも正常にパスが解釈され、
     * １階層下のディレクトリ（パッケージ）に存在するコンポーネント設定ファイルが取得できることを確認。
     */
    @Test
    public void testLoadNestedFileFromDirInClasspath() throws Throwable {

        // クラスパスに存在するコンポーネント設定ファイル
        URI componentFileUri = new URI("classpath:nablarch/core/repository/di/config/importfile_test_context.xml");

        // 上記設定ファイルには以下の定義が行われている
        // <component-configuration xmlns="http://tis.co.jp/nablarch/component-configuration" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        //     xsi:schemaLocation="http://tis.co.jp/nablarch/component-configuration /home/ssd/workspace/Nablarch/resources/component-configuration.xsd">
        //     <config-file dir="./imports" file="*.xml" />
        // </component-configuration>


        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(componentFileUri.toString());
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component2 comp2 = comp1.getComponent2();

        assertThat(comp2.getProp1(), is("str02"));
    }


    @Test
    public void testLoadExtendTypeInjection() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadExtendTypeInjection.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component2 comp2 = comp1.getComponent2();

        assertThat(comp2.getProp1(), is("str01"));
        assertThat(comp2 instanceof Component4, is(true));
    }


    @Test
    public void testFactoryInjection() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testFactoryInjection.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component2 comp2 = comp1.getComponent2();

        assertThat(comp2.getProp1(), is("str01"));
    }


    @Test
    public void testObjectLoad() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testObjectLoad.xml");
        DiContainer container = new DiContainer(loader);

        Component2 comp2 = container.getComponentByName("comp1");
        assertThat(comp2.getProp1(), is("value1"));
    }

    @Test
    public void testLoadFromClasspath() throws Throwable {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/classpath-context.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("test"));
    }


    @Test
    public void testLoadNestedComponentName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadNestedComponentName.xml");
        DiContainer container = new DiContainer(loader);

        NestedComponent nest1 = container.getComponentByName("nest1");
        NestedComponent nest2 = container.getComponentByName("nest1.child");
        NestedComponent nest3 = container.getComponentByName("nest1.child.child");

        assertThat(nest1.getStringProp(), is("nest1"));
        assertThat(nest2.getStringProp(), is("nest2"));
        assertThat(nest3.getStringProp(), is("nest3"));
    }


    @Test
    public void testLoadRecursive() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadRecursive.xml");
        try {
            DiContainer container = new DiContainer(loader);
            fail("例外が発生するはず。");
        } catch (ContainerProcessException e) {
            // OK
        }
    }

    @Test
    public void testLoadRecursiveAutowire() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadRecursiveAutowire.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず。");
        } catch (ContainerProcessException e) {
            // OK
        }
    }

    @Test
    public void testLoadRecursiveAutowire2() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadRecursiveAutowire2.xml");
        try {
            DiContainer diContainer = new DiContainer(loader);
            diContainer.getComponentByName("comp1");
            fail("例外が発生するはず。");
        } catch (ContainerProcessException e) {
            // OK
        }
    }

    @Test
    public void testLoadInject() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadInject.xml");
        DiContainer container = new DiContainer(loader);

        NestedComponent parent = container.getComponentByName("parent");
        NestedComponent child = container.getComponentByName("child");

        assertThat(parent.getStringProp(), is("parent"));
        assertThat(child.getStringProp(), is("child"));
    }

    @Test
    public void testLoadDuplicateDefinitionPolicyDeny() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadDuplicateDefinitionPolicyDeny.xml",
                DuplicateDefinitionPolicy.DENY);
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
        }

    }

    @Test
    public void testLoadFromClasspathUrl() throws Throwable {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "classpath:nablarch/core/repository/di/classpath-context.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");

        assertThat(comp1.getProp1(), is("test"));
    }

    @Test
    public void testLoadInvalidProperty() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadInvalidProperty.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }

    @Test
    public void testLoadInvalidXml() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadInvalidXml.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }

    @Test
    public void testLoadFactory() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadFactory.xml");
        DiContainer container = new DiContainer(loader);
        Map<String, Object> loadedObjs = container.load();

        // ファクトリから取ったオブジェクトは、インスタンス化されたオブジェクトのはず
        Component2 createdObj = (Component2) loadedObjs.get("factory");

        assertThat(createdObj.getProp1(), is("str01"));

        // 2回ロードしても、正しく動くことを確認。
        Map<String, Object> loadedObjs2 = container.load();
        assertThat(loadedObjs2, is(loadedObjs));
    }


    /**
     * コンポーネント名、クラス名がまったく一致する定義があり、かつコンポーネントを直接参照している場合に、
     * エラーにならないことを確認するテスト。
     * (#5146 不具合の再現)
     */
    @Test
    public void testLoadDuplicateDefinedValue() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadDuplicateDefinedValue.xml");
        DiContainer container = new DiContainer(loader);
        Map<String, Object> loadedObjs = container.load();

        // ファクトリから取ったオブジェクトは、インスタンス化されたオブジェクトのはず
        Component6 createdObj = (Component6) loadedObjs.get("comp6");

        assertThat(createdObj.callTest(), is("message2"));
    }

    @Test
    public void testClassNotFound() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testClassNotFound.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }

    @Test
    public void testNoPropertyValue() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testNoPropertyValue.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }


    @Test
    public void testMapEntryHasNoValue() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testMapEntryHasNoValue.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }

    @Test
    public void testMapEntryHasNoKey() throws Throwable {
        try {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testMapEntryHasNoKey.xml");
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            // OK
        }
    }

    @Test
    public void testPropertyNotFound() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testPropertyNotFound.xml");

        DiContainer container = new DiContainer(loader);
        Component1 comp1 = container.getComponentByName("comp1");
        assertThat(comp1.getProp1(), is("${test.prop}"));
    }

    @Test
    public void testPropertyIsNotString() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testPropertyIsNotString.xml");

        DiContainer container = new DiContainer(loader);
        Component1 comp1 = container.getComponentByName("comp1");
        assertThat(comp1.getProp1(), is("${comp2}"));
    }


    @Test
    public void testLoadRecursiveRef() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadRecursiveRef.xml");

        try {
            DiContainer container = new DiContainer(loader);
            fail("コンポーネントファクトリ間で循環参照がある場合、例外が発生するはず");
        } catch (ContainerProcessException e) {
            // OK
        }
    }

    @Test
    public void testLoadUnregisterdName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadUnregisterdName.xml");

        DiContainer container = new DiContainer(loader);
        // 存在しない名前を指定
        Object obj = container.getComponentByName("xxx");
        assertThat(obj, is(nullValue()));
    }

    @Test
    public void testLoadUnregisterdId() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadUnregisterdId.xml");

        try {
            DiContainer container = new DiContainer(loader);
            // 存在しないIDを指定
            container.getComponentById(-1);
            fail("例外が発生するはず");
        } catch (ContainerProcessException e) {
            // OK
        }
    }

    @Test
    public void testLoadUnregisterdType() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadUnregisterdType.xml");

        DiContainer container = new DiContainer(loader);
        // 存在しない型を指定
        Object obj = container.getComponentByType(DiContainer.class);
        assertThat(obj, is(nullValue()));
    }

    @Test
    public void testLoadUnsupportedLiteralValue() throws Throwable {
        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadUnsupportedLiteralValue1.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        }

        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadUnsupportedLiteralValue2.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        }


        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadUnsupportedLiteralValue3.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        }

        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadUnsupportedLiteralValue4.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        }
    }


    @Test
    public void testLoadListUnknownComponentName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadListUnknownComponentName.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ContainerProcessException e) {

        }

    }

    @Test
    public void testLoadMapKeyUnknownComponentName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadMapKeyUnknownComponentName.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ContainerProcessException e) {

        }

    }

    @Test
    public void testLoadMapValueUnknownComponentName() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadMapValueUnknownComponentName.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ContainerProcessException e) {

        }
    }

    @Test
    public void testLoadInvalidSetterComponent() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadInvalidSetterComponent.xml");
        DiContainer container = new DiContainer(loader);

        Object component8 = container.getComponentByName("component8");
        assertThat(component8, instanceOf(Component8.class));
    }

    @Test
    public void testLoadComponentCreationFail() throws Throwable {
        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadComponentCreationFail1.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ContainerProcessException e) {

            }
        }
        
        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadComponentCreationFail2.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ContainerProcessException e) {

            }
        }
    }


    @Test
    public void testLoadMapCreationFail() throws Throwable {
        {
            XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                    "nablarch/core/repository/di/DiContainerTest/testLoadMapCreationFail1.xml");
            try {
                new DiContainer(loader);
                fail("例外が発生するはず");
            } catch (ContainerProcessException e) {

            }
        }

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testLoadMapCreationFail2.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ContainerProcessException e) {

        }
    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    /**
     * コンポーネント定義ファイル内でのインポートで循環が発生した場合、
     * 例外が発生すること。
     *
     * @throws IOException 予期しない入出力例外
     */
    @Test
    public void testImportCircularFail() throws IOException {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testImportCircularFail1.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (ConfigurationLoadException e) {
            assertThat(e.getCause()
                        .getMessage(), containsString("testImportCircularFail1.xml"));
            assertThat(e.getCause()
                        .getMessage(), containsString("testImportCircularFail2.xml"));
        }

    }


    /**
     * インポート対象のファイルが存在しない場合、例外が発生し、
     * インポートに失敗したリソース名が例外メッセージに含まれること。
     *
     * @throws IOException 予期しない入出力例外
     */
    @Test
    public void testImportFileNotFound() throws IOException {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testImportFileNotFound.xml");
        try {
            new DiContainer(loader);
            fail();
        } catch (ConfigurationLoadException e) {
            assertThat(e.getCause()
                        .getMessage(),
                    is("file to import not found. path=[classpath:fileNotFound.xml]"));
        }
    }
    
    /**
     * xml内に書かれたサロゲートペアが扱えることを検証するケース
     */
    @Test
    public void testSurrogatePair() {
        // Java 1.8.0 Update 77以前では以下のバグがあるため、Java6, 7環境ではテストをスキップする。
        // https://bugs.openjdk.java.net/browse/JDK-8145969
        Assume.assumeThat(System.getProperty("java.specification.version"), allOf(not("1.6"), not("1.7")));
        
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testSurrogatePair.xml");
        final DiContainer sut = new DiContainer(loader);

        final SurrogatePair case2 = sut.getComponentByName("component");
        assertThat(case2.getValue(), is("🍣🍣🍣!!!"));
    }


    /**
     * configファイルを参照した場合(place holder)を使用した場合サロゲートペアが扱えることを検証するケース
     */
    @Test
    public void testSurrogatePair_usePlaceHolder() {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/DiContainerTest/testSurrogatePair.xml");
        final DiContainer sut = new DiContainer(loader);

        final SurrogatePair case2 = sut.getComponentByName("placeHolder");
        assertThat(case2.getValue(), is("🍣🍣🍣!!!"));
    }
    

    private static final class MockObjectLoader implements ObjectLoader {

        private final Map<String, Object> values;

        public MockObjectLoader(Map<String, Object> values) {
            this.values = values;
        }

        @Override
        public Map<String, Object> load() {

            return values;
        }
    }


}
