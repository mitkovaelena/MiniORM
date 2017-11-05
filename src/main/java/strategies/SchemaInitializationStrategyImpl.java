package strategies;

import scanner.EntityScanner;
import strategies.tableCreator.TableCreator;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.List;

public abstract class SchemaInitializationStrategyImpl implements SchemaInitializationStrategy{
    private EntityScanner entityScanner;
    TableCreator creator;
    Connection connection;
    String dataSource;

    public SchemaInitializationStrategyImpl(EntityScanner entityScanner, TableCreator creator, Connection connection, String dataSource) {
        this.entityScanner = entityScanner;
        this.creator = creator;
        this.connection = connection;
        this.dataSource = dataSource;
    }

   List<Class> scanForEntities() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.entityScanner
                .getAllEntities(System.getProperty("user.dir"));
   }

    public void setEntityScanner(EntityScanner entityScanner) {
        this.entityScanner = entityScanner;
    }

    public void setCreator(TableCreator creator) {
        this.creator = creator;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
