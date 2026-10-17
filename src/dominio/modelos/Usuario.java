package dominio.modelos;

import dominio.estructuras.ListaDoblementeEnlazada;
import dominio.estructuras.TADLista;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Usuario {
    private String id;
    private String nombreUsuario;
    private String nombre;
    private String apellido;
    private LocalDateTime fechaRegistro;
    private TADLista<Partida> partidasJugadas;
    private boolean esAdmin;
    private int partidasGanadas;
    private int partidasPerdidas;
    private int partidasEmpatadas;
    private String password;
    
    public Usuario(String nombreUsuario, String nombre, String apellido, String password) {
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.password = password;
        this.fechaRegistro = LocalDateTime.now();
        this.partidasJugadas = new ListaDoblementeEnlazada<>();
        this.partidasGanadas = 0;
        this.partidasPerdidas = 0;
        this.partidasEmpatadas = 0;
    }
    
    public void agregarPartida(Partida partida) {
        this.partidasJugadas.insertarAlFinal(partida);
    }
    
    public void incrementarVictorias() {
        this.partidasGanadas++;
    }
    
    public void incrementarDerrotas() {
        this.partidasPerdidas++;
    }
    
    public void incrementarEmpates() {
        this.partidasEmpatadas++;
    }
    
    public String getId() {
        return id;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    public TADLista<Partida> getPartidasJugadas() {
        return partidasJugadas;
    }
    public int getPartidasGanadas() {
        return partidasGanadas;
    }
    public int getPartidasPerdidas() {
        return partidasPerdidas;
    }
    public int getPartidasEmpatadas() {
        return partidasEmpatadas;
    }

    public void setPartidasGanadas(int partidasGanadas) {
        this.partidasGanadas = partidasGanadas;
    }

    public void setPartidasPerdidas(int partidasPerdidas) {
        this.partidasPerdidas = partidasPerdidas;
    }

    public void setPartidasEmpatadas(int partidasEmpatadas) {
        this.partidasEmpatadas = partidasEmpatadas;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public boolean getEsAdmin() {
        return esAdmin;
    }
    
    public boolean verificarPassword(String passwordAuth) {
        return this.password.equals(hashearPassword(passwordAuth));
    }

    public static String hashearPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear el password", e);
        }
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreUsuario='" + nombreUsuario + '\'' +
                ", nombre='" + nombre + " " + apellido + '\'' +
                ", victorias=" + partidasGanadas +
                ", derrotas=" + partidasPerdidas +
                ", empates=" + partidasEmpatadas +
                '}';
    }
}
