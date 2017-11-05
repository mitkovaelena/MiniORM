package orm;

import scanner.EntityScanner;
import strategies.tableCreator.DatabaseTableCreator;
import strategies.DropCreateStrategy;
import strategies.SchemaInitializationStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StrategyConfigurer {
    private EntityManagerBuilder builder;

    public StrategyConfigurer(EntityManagerBuilder builder) {
        this.builder = builder;
    }

    public <T extends SchemaInitializationStrategy> EntityManagerBuilder set(Class<T> strategyClass)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<SchemaInitializationStrategy> constructor =
                ((Constructor<SchemaInitializationStrategy>) strategyClass.getDeclaredConstructors()[0]);
        constructor.setAccessible(true);
        SchemaInitializationStrategy strategy = constructor.newInstance(
                new EntityScanner(),
                new DatabaseTableCreator(this.builder.getConnection(), this.builder.getDataSource()),
                this.builder.getConnection(),
                this.builder.getDataSource());

        this.builder.setStrategy(strategy);
        return this.builder;
    }
}
