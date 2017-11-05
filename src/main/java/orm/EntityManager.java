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
    private final String INSERT_QUERY = "INSERT INTO %s(%s) VALUES(%s);";
    private final String UPDATE_QUERY = "UPDATE %s SET %s WHERE %s;";
    private final String DELETE_QUERY = "DELETE FROM %s  WHERE 1 %s LIMIT 1;";
    private final String SELECT_QUERY = "SELECT * FROM %s WHERE 1 %s %s;";

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

        if (value == null || (int) value <= 0) {
            return this.doInsert(entity, primary);
        }
        return this.doUpdate(entity, primary);
    }

    public Iterable<E> find(Class<E> table) throws SQLException,
            IllegalAccessException, InstantiationException {
        String query = String.format(SELECT_QUERY, this.getTableName(table), "", "");
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
        String query = String.format(SELECT_QUERY,
                this.getTableName(table), (where != null ? " AND " + where : ""), "");
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
        String query = String.format(SELECT_QUERY, this.getTableName(table), "", "");
        ResultSet rs = connection.prepareStatement(query).executeQuery();
        E entity = table.newInstance();
        if (rs.next()) {
            this.fillEntity(table, rs, entity);
        }
        return entity;
    }

    public E findFirst(Class<E> table, String where) throws SQLException,
            IllegalAccessException, InstantiationException {
        String query = String.format(SELECT_QUERY,
                this.getTableName(table), (where != null ? " AND " + where : ""), "LIMIT 1");
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

    private boolean doInsert(E entity, Field primary) throws SQLException,
            IllegalAccessException, InstantiationException {
        String tableName = this.getTableName(entity.getClass());


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

        String query = String.format(
                INSERT_QUERY, tableName,
                String.join(", ", columns),
                String.join(", ", values));

        boolean result = connection.prepareStatement(query).execute();
        String q = "SELECT LAST_INSERT_ID() AS id;";
        ResultSet rs = connection.prepareStatement(q).executeQuery();
        rs.next();
        primary.set(entity, rs.getInt("id"));

        return result;
    }

    private boolean doUpdate(E entity, Field primary) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName(entity.getClass());
        Field[] fields = entity.getClass().getDeclaredFields();
        String where = "";

        List<String> values = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals(primary.getName())) {
                where += field.getName() + " = " + field.get(entity).toString();
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
        String query = String.format(UPDATE_QUERY, tableName, String.join(", ", values), where);
        return connection.prepareStatement(query).execute();
    }

    public boolean doDelete(Class<?> table, String where) throws Exception {
        String tableName = this.getTableName(table);

        String query = String.format(DELETE_QUERY,
                tableName, (where != null ? " AND " + where : ""));
        return connection.prepareStatement(query).execute();
    }

    private String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Entity.class)) {
            return dataSource + "." + entityClass.getAnnotation(Entity.class).name();
        }
        throw new UnsupportedOperationException("Entity does not exist.");
    }

    private void fillEntity(Class<E> table, ResultSet rs, E entity)
            throws SQLException, IllegalAccessException {
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
}
