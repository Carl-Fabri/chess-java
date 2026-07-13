package infraestructura.persistencia;

import dominio.modelos.Usuario;
import dominio.puertos.RepositorioUsuario;
import infraestructura.mapper.UsuarioMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioUsuarioMySQL implements RepositorioUsuario {

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        String sql = "SELECT * FROM usuario WHERE nombreUsuario = ?";
        try {
            Connection conn = DB.getInstancia().getConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return UsuarioMapper.mapearUsuario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombreUsuario, nombre, apellido, password, partidasGanadas, partidasPerdidas, partidasEmpatadas, esAdmin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DB.getInstancia().getConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, Usuario.hashearPassword(usuario.getPassword()));
            stmt.setInt(5, usuario.getPartidasGanadas());
            stmt.setInt(6, usuario.getPartidasPerdidas());
            stmt.setInt(7, usuario.getPartidasEmpatadas());
            stmt.setBoolean(8, usuario.getEsAdmin());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try {
            Connection conn = DB.getInstancia().getConexion();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                usuarios.add(UsuarioMapper.mapearUsuario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
