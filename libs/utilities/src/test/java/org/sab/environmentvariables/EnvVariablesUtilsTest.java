package org.sab.environmentvariables;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnvVariablesUtilsTest {

    @Test
    public void getEnvOrThrowThrowsNullPointerIfEnvVariableDoesNotExist(){
        String dummyEnvVariable = "ThisEnvVariableDoesNotExist";
        try {
            EnvVariablesUtils.getEnvOrThrow(dummyEnvVariable);
            fail("This test should have thrown an exception, but it didn't !");
        }
        catch (NullPointerException e){
            assertEquals(String.format("The Environment Variable \"%s\" cannot be null", dummyEnvVariable),e.getMessage());
        }
    }
}
