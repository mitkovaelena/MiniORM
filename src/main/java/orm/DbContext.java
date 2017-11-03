package orm;

import java.sql.SQLException;

public interface DbContext<E> {
    boolean persist(E entity) throws IllegalAccessException, SQLException, InstantiationException; // insert or update entity depending if it is attached to the context
   	Iterable<E> find(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException; // returns collection of all entity objects
   	Iterable<E> find(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException; // returns collection of all entity objects matching the criteria given in “where”
   	E findFirst(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException; // returns the first entity object
   	E findFirst(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException; // returns the first entity object matching the criteria given in “where”
}
