package orm;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import strategies.SchemaInitializationStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class EntityManager<E> implements DbContext<E> {
    private Connection connection;
    private String dataSource;
    private SchemaInitializationStrategy strategy;

    public EntityManager(Connection connection, String dataSource, SchemaInitializationStrategy strategy) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.connection = connection;
        this.dataSource = dataSource;
        this.strategy = strategy;
        this.strategy.execute();
    }

    public boolean persist(E entity) throws IllegalAccessException, SQLException, InstantiationException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        //TODO: Update db when needed
        if (value == null || (int) value <= 0) {
            return this.doInsert(entity, primary);
        }
        return this.doUpdate(entity, primary);
    }

    public Iterable<E> find(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + this.getTableName(table) + ";";
        ResultSet rs = connection.prepareStatement(query).executeQuery();

        List<E> entities = new ArrayList<>();
        while (rs.next()) {
            E entity = table.newInstance();
            this.fillEntity(table, rs, entity);
            entities.add(entity);
        }
        return entities;
    }

    public Iterable<E> find(Class<E> table, String where) throws SQLException,
            IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + this.getTableName(table)
                + " WHERE 1" + (where != null ? " AND " + where : "") + ";";
        ResultSet rs = connection.prepareStatement(query).executeQuery();

        List<E> entities = new ArrayList<>();
        while (rs.next()) {
            E entity = table.newInstance();
            this.fillEntity(table, rs, entity);
            entities.add(entity);
        }
        return entities;
    }

    public E findFirst(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + this.getTableName(table) + " LIMIT 1;";
        ResultSet rs = connection.prepareStatement(query).executeQuery();
        E entity = table.newInstance();
        if (rs.next()) {
            this.fillEntity(table, rs, entity);
        }
        return entity;
    }

    public E findFirst(Class<E> table, String where) throws SQLException,
            IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + this.getTableName(table)
                + " WHERE 1" + (where != null ? " AND " + where : "") + " LIMIT 1;";
        ResultSet rs = connection.prepareStatement(query).executeQuery();
        E entity = table.newInstance();
        if (rs.next()) {
            this.fillEntity(table, rs, entity);
        }
        return entity;
    }

    private Field getId(Class entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(
                        () -> new UnsupportedOperationException("Entity does not have a primary key.")
                );
    }

    private void doAlter(Class entity) throws SQLException {
        String tableName = this.getTableName(entity);
        String query = "ALTER TABLE " + tableName +" ";
        Field[] fields = entity.getDeclaredFields();

        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class) && !checkIfFieldExistInDB(entity, field)) {
                String column = "ADD COLUMN `" + field.getAnnotation(Column.class).name()
                        + "`  " + this.getDBType(field);
                columns.add(column);
            }
        }

        query += String.join(", ", columns) + ";";
        connection.prepareStatement(query).execute();
    }

    private boolean doDelete(Class<?> table, String where) throws Exception {
        String tableName = this.getTableName(table);

        if(!this.checkIfTableExists(tableName)){
            throw new Exception("Table does not exist.");
        }
        String query = "DELETE FROM " + tableName
                + " WHERE 1" + (where != null ? " AND " + where : "") + ";";
        return connection.prepareStatement(query).execute();
    }

    private boolean doInsert(E entity, Field primary) throws SQLException,
            IllegalAccessException, InstantiationException {
        String tableName = this.getTableName(entity.getClass());

        if(!this.checkIfTableExists(tableName)){
           // doCreate(entity);
        }

        String query = "INSERT INTO " + tableName + " (";
        Field[] fields = entity.getClass().getDeclaredFields();

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (!field.getName().equals(primary.getName()) && field.isAnnotationPresent(Column.class)) {
                columns.add("`" + field.getAnnotation(Column.class).name() + "`");

                Object value = field.get(entity);
                if (field.getType() == Date.class) {
                    values.add("\'" + new SimpleDateFormat("yyyy-MM-dd").format(value) + "\'");
                } else if (field.getType() == int.class || field.getType() == Integer.class
                        || field.getType() == double.class || field.getType() == Double.class) {
                    values.add(value.toString());
                } else if (field.getType() == String.class) {
                    values.add("\'" + value.toString() + "\'");
                }
            }
        }

        query += String.join(", ", columns);
        query += ") VALUES(";
        query += String.join(", ", values);
        query += ");";
        //TODO: set id to the entity
        return connection.prepareStatement(query).execute();
    }

    private boolean doUpdate(E entity, Field primary) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName(entity.getClass());
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

    private String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Entity.class)) {
            return entityClass.getAnnotation(Entity.class).name();
        }
        throw new UnsupportedOperationException("Entity does not exist.");
    }

    private boolean checkIfFieldExistInDB(Class entity, Field field) throws SQLException {
        String fieldName = field.getAnnotation(Column.class).name();
        String tableName = this.getTableName(entity);

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

    private void fillEntity(Class<E> table, ResultSet rs, E entity) throws SQLException, IllegalAccessException {
        Field[] fields = table.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            this.fillField(field, entity, rs, field.getAnnotation(Column.class).name());
        }
    }

    private void fillField(Field field, E entity, ResultSet rs, String fieldName)
            throws SQLException, IllegalAccessException {
        if (field.getType() == int.class || field.getType() == Integer.class) {
            field.set(entity, rs.getInt(fieldName));
        } else if (field.getType() == String.class) {
            field.set(entity, rs.getString(fieldName));
        } else if (field.getType() == Date.class) {
            field.set(entity, rs.getDate(fieldName));
        } else if (field.getType() == double.class || field.getType() == Double.class) {
            field.set(entity, rs.getDouble(fieldName));
        }
    }

    private String getDBType(Field field) {
        field.setAccessible(true);

        switch (field.getType().getSimpleName()) {
            case "int":
                return "BIGINT";
            case "String":
                return "VARCHAR(50)";
            case "Date":
                return "TIMESTAMP";
            case "double":
                return "DOUBLE";
        }
        return null;
    }
}
