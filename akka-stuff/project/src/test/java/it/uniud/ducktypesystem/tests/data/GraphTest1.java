package it.uniud.ducktypesystem.tests.data;

import it.uniud.ducktypesystem.distributed.data.DataFacade;
import it.uniud.ducktypesystem.errors.SystemError;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class GraphTest1 {
    private DataFacade facade;

    @Test
    public void initFacade() {
        facade = null;
        try {
            String basePath = new File("").getAbsolutePath();
            facade = DataFacade.create(basePath + "/src/test/resources/graphTest01.DGS");

            facade.setOccupied(2);
            facade.setNumSearchGroups(5);

            Assert.assertEquals(facade.getOccupied().size(), 2);
            Assert.assertEquals(facade.getNumSearchGroups(), 5);
        } catch (SystemError systemError) {
            systemError.printStackTrace();
            facade = null;
        }
    }

    @Test
    public void test01() throws SystemError {
        String basePath = new File("").getAbsolutePath();
        facade = DataFacade.create(basePath + "/src/test/resources/graphTest01.DGS");

        Assert.assertEquals(facade.getNumSearchGroups(), 3);
        Assert.assertEquals(facade.getOccupied().size(), 0);
    }

}
