import entities.User;
import orm.Connector;
import orm.EntityManager;
import orm.EntityManagerBuilder;
import strategies.DropCreateStrategy;
import strategies.UpdateStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException, IOException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


        System.out.println("Enter username: ");
        String username = reader.readLine();
        username = username.equals("") ? "root" : username;


        System.out.println("Enter password: ");
        String password = reader.readLine().trim();
        password = password.equals("") ? "root" : password;

        EntityManagerBuilder builder = new EntityManagerBuilder();
        EntityManager em = builder.configureConnectionString()
                .setDriver("jdbc")
                .setAdapter("mysql")
                .setHost("localhost")
                .setPort("3306")
                .setUser(username)
                .setPass(password)
                .createConnection()
                .setDataSource("orm_db")
                .configureCreationType().set(DropCreateStrategy.class)
                .build();


        User pesho = new User("peshoo", "abcdef", 31, new Date());
        User eli = new User("els", "123", 21, new Date());
        User ivo = new User("jelev", "wsedrf", 22, new Date());

        em.persist(pesho);
        em.persist(eli);
        em.persist(ivo);
        eli.setId(2);
        eli.setAge(12);

        em.persist(eli);
        Object o = em.findFirst(User.class, "age > 21");
        System.out.println();

    }
}
