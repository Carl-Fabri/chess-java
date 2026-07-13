package dominio.puertos;

import java.util.List;

import dominio.modelos.Usuario;

public interface RepositorioUsuario {
    Usuario buscarPorNombreUsuario(String nombreUsuario);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    List<Usuario> obtenerUsuarios();
}
