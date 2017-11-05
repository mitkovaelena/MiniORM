package strategies;

import annotations.Column;
import scanner.EntityScanner;
import strategies.tableCreator.TableCreator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateStrategy extends SchemaInitializationStrategyImpl {


    public UpdateStrategy(EntityScanner entityScanner, TableCreator creator, Connection connection, String dataSource) {
        super(entityScanner, creator, connection, dataSource);
    }

    @Override
    public void execute() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

    }


   /*
    private boolean doUpdate(Class entity, Field primary) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName(entity);
        String query = "UPDATE " + tableName + " SET ";
        Field[] fields = entity.getClass().getDeclaredFields();
        String where = " WHERE ";

        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals(primary.getName())) {
                where += field.getName() + " = " + field.get(entity).toString() + " ;";
            } else if (field.isAnnotationPresent(Column.class)) {
                String str = "`" + field.getAnnotation(Column.class).name() + "` = ";

                Object value = field.get(entity);
                if (field.getType() == Date.class) {
                    str += "\'" + new SimpleDateFormat("yyyyMMdd").format(value) + "\'";
                } else if (field.getType() == int.class || field.getType() == Integer.class
                        || field.getType() == double.class || field.getType() == Double.class) {
                    str += value.toString();
                } else {
                    str += "\'" + value.toString() + "\'";
                }
                values.add(str);
            }
        }

        query += String.join(", ", values);
        query += where;
        return connection.prepareStatement(query).execute();
    }
    */
}
