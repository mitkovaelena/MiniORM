package strategies;

import annotations.Column;
import annotations.Entity;
import scanner.EntityScanner;
import strategies.tableManipulator.TableCreator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpdateStrategy extends SchemaInitializationStrategyImpl {


    public UpdateStrategy(EntityScanner entityScanner, TableCreator updater, Connection connection, String dataSource) {
        super(entityScanner, updater, connection, dataSource);
    }

    @Override
    public void execute() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Class> entities = this.scanForEntities();

        for(Class<?> entity : entities){
            if (!checkIfTableExists(entity.getAnnotation(Entity.class).name())){
                this.creator.doCreate(entity);
            } else {
                doAlter(entity);
            }
        }
    }

    public void doAlter(Class entity) throws SQLException {
        String tableName = this.getTableName(entity);
        String query = "ALTER TABLE " + this.dataSource + "." + tableName +" ";
        Field[] fields = entity.getDeclaredFields();

        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class) && !checkIfFieldExist(tableName, field)) {
                String column = "ADD COLUMN `" + field.getAnnotation(Column.class).name()
                        + "`  " + this.getDatabaseType(field);
                columns.add(column);
            }
        }

        query += String.join(", ", columns) + ";";
        connection.prepareStatement(query).execute();
    }

    private boolean checkIfFieldExist(String tableName, Field field) throws SQLException {
        String fieldName = field.getAnnotation(Column.class).name();

        String query = "SELECT * " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'orm_db' " +
                "AND TABLE_NAME = '" + tableName + "' " +
                "AND COLUMN_NAME = '" + fieldName + "'";

        return  connection.prepareStatement(query).executeQuery().next();
    }

    private boolean checkIfTableExists(String tableName) throws SQLException {
        String query = "SELECT table_name " +
                "FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = 'orm_db' " +
                "AND TABLE_NAME = '" + tableName + "' " +
                "LIMIT 1;";

        ResultSet rs = this.connection.prepareStatement(query).executeQuery();

        if(!rs.first()){
            return false;
        }
        return true;
    }

    public String getTableName(Class<?> entity) {
        if (entity.isAnnotationPresent(Entity.class)) {
            return entity.getAnnotation(Entity.class).name();
        }
        throw new UnsupportedOperationException("Entity does not exist.");
    }

    public String getDatabaseType(Field field) {
        field.setAccessible(true);
        switch (field.getType().getSimpleName()){
            case "int":
            case "Integer":
                return "INT";
            case "String":
                return "VARCHAR(50)";
            case "Date":
                return "DATETIME";
        }
        return null;
    }
}
