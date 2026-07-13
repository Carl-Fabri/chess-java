package infraestructura.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static DB instancia;
    private Connection conexion;
    private static final String URL = "jdbc:mysql://195.179.226.112:3306/chess?allowPublicKeyRetrieval=true";
    private static final String USUARIO = "chess-user";
    private static final String PASSWORD = "FZPp8G16C6OIDavKfvLD";

    private DB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error al conectar a la base de datos", e);
        }
    }

    public static synchronized DB getInstancia() {
        if (instancia == null) {
            instancia = new DB();
        } else {
            try {
                if (instancia.conexion.isClosed()) {
                    instancia = new DB();
                }
            } catch (SQLException e) {
                instancia = new DB();
            }
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }
}
