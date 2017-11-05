package orm;

import strategies.SchemaInitializationStrategy;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

public class EntityManagerBuilder {
    private Connection connection;
    private String dataSource;  //database name
    private SchemaInitializationStrategy strategy;


    public Connector configureConnectionString(){
        return new Connector(this);
    }

    public StrategyConfigurer configureCreationType(){
        return new StrategyConfigurer(this);
    }

    public EntityManager build() throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
     return new EntityManager(this.connection, this.dataSource, this.strategy);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getDataSource() {
        return dataSource;
    }

    public EntityManagerBuilder setDataSource(String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public SchemaInitializationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(SchemaInitializationStrategy strategy) {
        this.strategy = strategy;
    }
}
