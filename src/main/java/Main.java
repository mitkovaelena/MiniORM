import entities.User;
import orm.Connector;
import orm.EntityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter username: ");
        String username = reader.readLine();
        username = username.equals("") ? "root" : username;


        System.out.println("Enter password: ");
        String password = reader.readLine().trim();
        password = password.equals("") ? "root" : password;


        Connector.createConnection(username, password, "orm_db");
        Connection connection = Connector.getConnection();
        EntityManager em = new EntityManager(connection);

        User pesho = new User("peshoo", "abcdef", 31, new Date());
        User eli = new User("els", "123", 21, new Date());
        User ivo = new User("jelev", "wsedrf", 22, new Date());
        em.persist(pesho);
        em.persist(eli);
        em.persist(ivo);

        Iterable<User> found = em.find(User.class, "registration_date >= \"01.01.2014\" AND age >= 18");
        for (User user : found) {
            System.out.println(user.getUsername());
        }
    }
}
