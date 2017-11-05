package strategies.tableCreator;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseTableCreator implements TableCreator {
    private Connection connection;
    private String dataSource;

    public DatabaseTableCreator(Connection connection, String dataSource) {
        this.connection = connection;
        this.dataSource = dataSource;
    }

    @Override
    public void doCreate(Class entity) throws SQLException {
        String tableName = this.getTableName(entity);
        String query = "CREATE TABLE IF NOT EXISTS " + this.dataSource + "." + tableName + "(";
        Field[] fields = entity.getDeclaredFields();

        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                String column = "`" + field.getAnnotation(Column.class).name() + "`  ";
                if (field.isAnnotationPresent(Id.class)) {
                    column += " BIGINT PRIMARY KEY AUTO_INCREMENT";
                } else {
                    column += this.getDatabaseType(field);
                }
                columns.add(column);
            }
        }

        query += String.join(", ", columns) + ");";
        connection.prepareStatement(query).execute();
    }


    @Override
    public String getFieldName(Field field) {
        String fieldName = "";

        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            fieldName = columnAnnotation.name();
        }

        if (fieldName.equals("")) {
            fieldName = field.getName();
        }

        return fieldName;
    }

    @Override
    public String getTableName(Class<?> entity) {
        if (entity.isAnnotationPresent(Entity.class)) {
            return entity.getAnnotation(Entity.class).name();
        }
        throw new UnsupportedOperationException("Entity does not exist.");
    }

    @Override
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
