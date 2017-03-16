package nablarch.core.repository.di.config.xml;

import static org.junit.Assert.fail;

import java.io.InputStream;

import nablarch.core.repository.di.ConfigurationLoadException;
import nablarch.core.util.FileUtil;

import org.junit.Test;


public class ComponentDefinitionFileParserTest {

    @Test
    public void testLoadFailIllegalTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }

    @Test
    public void testLoadFailNestComponentConfiguration() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailNestComponentConfiguration.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }


    @Test
    public void testLoadFailIllegalComponentTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalComponentTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    

    @Test
    public void testLoadFailIllegalKeyComponentTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalKeyComponentTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    
    @Test
    public void testLoadFailIllegalValueComponentTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalValueComponentTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }


    @Test
    public void testLoadFailIllegalPropertyTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalPropertyTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }

    @Test
    public void testLoadFailIllegalComponentRefTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalComponentRefTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }

    @Test
    public void testLoadFailIllegalConfigFileTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalConfigFileTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }

    @Test
    public void testLoadFailIllegalImportTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalImportTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }

    @Test
    public void testLoadFailIllegalListTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalListTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    

    @Test
    public void testLoadFailIllegalMapTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalMapTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    

    @Test
    public void testLoadFailIllegalEntryTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalEntryTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    

    @Test
    public void testLoadFailIllegalValueTag() throws Throwable {
        ComponentDefinitionFileParser parser = new ComponentDefinitionFileParser();
        InputStream in = FileUtil.getResource(
                "classpath:nablarch/core/repository/di/config/xml/ComponentDefinitionFileParserTest/testLoadFailIllegalValueTag.xml");
        try {
            try {
                parser.parse(in);
                fail("例外が発生するはず");
            } catch (ConfigurationLoadException e) {
                // OK
            }
        } finally {
            FileUtil.closeQuietly(in);
        }
    }
    
}
