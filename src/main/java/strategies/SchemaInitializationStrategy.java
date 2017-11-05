package strategies;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface SchemaInitializationStrategy {
    void execute() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
