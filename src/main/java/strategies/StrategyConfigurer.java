package strategies;

import orm.EntityManagerBuilder;

public class StrategyConfigurer {
    private EntityManagerBuilder builder;

    public StrategyConfigurer(EntityManagerBuilder builder) {
        this.builder = builder;
    }

    public EntityManagerBuilder setDropCreateStrategy(){
        //this.builder.setStrategy("drop-create");
        return this.builder;
    }

    public EntityManagerBuilder setUpdateStrategy(){
        //this.builder.setStrategy("update");
        return this.builder;
    }
}
