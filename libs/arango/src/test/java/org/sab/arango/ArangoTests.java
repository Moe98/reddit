package org.sab.arango;

import com.arangodb.ArangoDB;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ArangoTests {

    @Test
    public void ArangoConnectionTest(){
        assertThat(Arango.initializeConnection(),instanceOf(ArangoDB.class));
    }

    

}
