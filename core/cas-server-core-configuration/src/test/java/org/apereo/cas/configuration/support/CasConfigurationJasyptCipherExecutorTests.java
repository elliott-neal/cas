package org.apereo.cas.configuration.support;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.registry.AlgorithmRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationJasyptCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("CasConfiguration")
public class CasConfigurationJasyptCipherExecutorTests {
    static {
        System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
    }

    @Autowired
    private Environment environment;

    private CasConfigurationJasyptCipherExecutor jasypt;

    @BeforeEach
    public void initialize() {
        this.jasypt = new CasConfigurationJasyptCipherExecutor(this.environment);
    }

    @Test
    public void verifyDecryptionEncryption() {
        val result = jasypt.encryptValue(getClass().getSimpleName());
        assertNotNull(result);
        val plain = jasypt.decryptValue(result);
        assertEquals(plain, getClass().getSimpleName());
    }

    @Test
    public void verifyEncodeOps() {
        assertNotNull(jasypt.getName());
        val result = jasypt.encode(getClass().getSimpleName());
        assertNotNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairNotNeeded() {
        val result = jasypt.decryptValue("keyValue");
        assertNotNull(result);
        assertEquals("keyValue", result);

    }

    @Test
    public void verifyDecryptionEncryptionPairFails() {
        val encVal = CasConfigurationJasyptCipherExecutor.ENCRYPTED_VALUE_PREFIX + "keyValue";
        val result = jasypt.decode(encVal, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNull(result);
    }

    @Test
    public void verifyDecryptionEncryptionPairSuccess() {
        val value = jasypt.encryptValue("Testing");
        val result = jasypt.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertNotNull(result);
        assertEquals("Testing", result);
    }

    /**
     * Test all algorithms
     */
    @Test
    public void verifyAlgorithms() {
        val algorithms = (Set<String>) AlgorithmRegistry.getAllPBEAlgorithms();
        for (val algorithm : algorithms) {
            assertTrue(isAlgorithmFunctional(algorithm));
        }
    }

    private boolean isAlgorithmFunctional(final String algorithm) {
        val jasyptTest = new CasConfigurationJasyptCipherExecutor(this.environment);
        jasyptTest.setAlgorithmForce(algorithm);
        val testValue = "Testing_" + algorithm;
        val value = jasyptTest.encryptValue(testValue);
        val result = jasyptTest.decode(value, ArrayUtils.EMPTY_OBJECT_ARRAY);
        return testValue.equals(result);
    }
}

