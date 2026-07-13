package infraestructura.persistencia;

import dominio.modelos.Usuario;
import infraestructura.mapper.UsuarioMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioPartidaOnline {

    public static class PartidaOnlineInfo {
        public String id;
        public String jugadorBlancas;
        public String jugadorNegras;
        public String estado;
        public String movimientos;
        public String turno;
    }

    public static void actualizarAcceso(String nombreUsuario) {
        String sql = "UPDATE usuario SET ultimo_acceso = NOW() WHERE nombreUsuario = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombreUsuario);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Usuario> obtenerUsuariosActivos(String nombreUsuarioExcluir) {
        List<Usuario> activos = new ArrayList<>();
        // Consider active if updated in the last 15 seconds
        String sql = "SELECT * FROM usuario WHERE ultimo_acceso >= DATE_SUB(NOW(), INTERVAL 15 SECOND) AND nombreUsuario != ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombreUsuarioExcluir);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        activos.add(UsuarioMapper.mapearUsuario(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activos;
    }

    public static void crearInvitacion(String idPartida, String creador, String oponente) {
        String sql = "INSERT INTO partida_online (id, jugador_blancas, jugador_negras, estado, movimientos, turno) VALUES (?, ?, ?, 'INVITACION_PENDIENTE', '', 'BLANCO')";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idPartida);
                stmt.setString(2, creador);
                stmt.setString(3, oponente);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> buscarInvitacionesRecibidas(String nombreUsuario) {
        List<String[]> invitaciones = new ArrayList<>();
        String sql = "SELECT id, jugador_blancas FROM partida_online WHERE jugador_negras = ? AND estado = 'INVITACION_PENDIENTE'";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombreUsuario);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        invitaciones.add(new String[]{rs.getString("id"), rs.getString("jugador_blancas")});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invitaciones;
    }

    public static void responderInvitacion(String idPartida, boolean aceptar) {
        String estado = aceptar ? "EN_PROGRESO" : "RECHAZADA";
        String sql = "UPDATE partida_online SET estado = ? WHERE id = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, estado);
                stmt.setString(2, idPartida);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PartidaOnlineInfo obtenerPartida(String idPartida) {
        String sql = "SELECT * FROM partida_online WHERE id = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idPartida);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        PartidaOnlineInfo info = new PartidaOnlineInfo();
                        info.id = rs.getString("id");
                        info.jugadorBlancas = rs.getString("jugador_blancas");
                        info.jugadorNegras = rs.getString("jugador_negras");
                        info.estado = rs.getString("estado");
                        info.movimientos = rs.getString("movimientos");
                        info.turno = rs.getString("turno");
                        return info;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void realizarMovimientoOnline(String idPartida, String nuevosMovimientos, String siguienteTurno) {
        String sql = "UPDATE partida_online SET movimientos = ?, turno = ? WHERE id = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nuevosMovimientos);
                stmt.setString(2, siguienteTurno);
                stmt.setString(3, idPartida);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void finalizarPartidaOnline(String idPartida, String estadoFinal) {
        String sql = "UPDATE partida_online SET estado = ? WHERE id = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, estadoFinal);
                stmt.setString(2, idPartida);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
