package strategies;

import scanner.EntityScanner;
import strategies.tableCreator.TableCreator;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

public interface SchemaInitializationStrategy {
    void execute() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;

    void setConnection(Connection connection);

    void setDataSource(String dataSource);
    void setEntityScanner(EntityScanner scanner);
    void setCreator(TableCreator creator);
}
