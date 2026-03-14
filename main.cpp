#include <iostream>
#include <vector>
#include <string>
using namespace std;

// 1. EL MOLDE (La ficha del libro)
struct Libro {
    int id;
    string titulo;
    string autor;
    int anio_publicacion;
    int ejemplares_disponibles;
};

struct Usuario {
    int id;
    string nombre;
    string apellido;
    string email;
};

struct Prestamo{
 int id_prestamo;
 int id_usuario;
 int id_libro;
 string fecha_prestamo;
 string fecha_devolucion;
};

// 2. LA FUNCIÓN DE REGISTRO
void registrarLibro(vector<Libro> &estante) { // Usamos & para que se guarde de verdad
    Libro fichaNueva; // Creamos una ficha vacía

    // Asignamos el ID automático basado en cuántos ya hay
    fichaNueva.id = estante.size() + 1;

    cout << "--- Registro de Libro ID: " << fichaNueva.id << " ---" << endl;
    
    cin.ignore(); // Limpieza necesaria antes de leer texto
    cout << "Titulo: ";
    getline(cin, fichaNueva.titulo);
    
    cout << "Autor: ";
    getline(cin, fichaNueva.autor);

    cout << "Año de publicación: ";
    cin >> fichaNueva.anio_publicacion;

    cout << "Ejemplares disponibles: ";
    cin >> fichaNueva.ejemplares_disponibles;

    cin.ignore(); // Limpieza final

    // 3. EL GUARDADO (Metemos la ficha en el estante)
    if (!fichaNueva.titulo.empty() && !fichaNueva.autor.empty() && fichaNueva.anio_publicacion > 0 && fichaNueva.ejemplares_disponibles >= 0) {
        estante.push_back(fichaNueva);
        cout << "Guardado en el vector correctamente." << endl;
    } else {
        cout << "Error: No puedes dejar campos vacios." << endl;
    }
}

void registarUsuario(vector<Usuario> &listaUsuarios){
    Usuario nuevoUsuario;
    nuevoUsuario.id = listaUsuarios.size() + 1;
    cout<<"--- Registro de Usuario ID: " << nuevoUsuario.id << " ---" << endl;
    cin.ignore();

    cout << "Nombre: ";
    getline(cin, nuevoUsuario.nombre);

    cout << "Apellido: ";
    getline(cin, nuevoUsuario.apellido);

    cout << "Email: ";
    getline(cin, nuevoUsuario.email);
}

void mostrarLibros(const vector<Libro> &estante) {
    if (estante.empty()) {
        cout << "La biblioteca esta vacia." << endl;
        return;
    }

    cout << "\n--- LISTA DE LIBROS REGISTRADOS ---" << endl;
    
    // Usamos un bucle for para recorrer el vector
    for (int i = 0; i < estante.size(); i++) {
        cout << "ID: " << estante[i].id << endl;
        cout << "Titulo: " << estante[i].titulo << endl;
        cout << "Autor: " << estante[i].autor << endl;
        cout << "Año: " << estante[i].anio_publicacion << endl;
        cout << "Ejemplares: " << estante[i].ejemplares_disponibles << endl;
        cout << "-------------------------------" << endl;
    }
}

int main() {
    // Definimos el contenedor principal
    vector<Libro> inventario; 
    vector<Usuario> listaUsuarios;
    vector<Prestamo> historialPrestamos;

    // Llamamos a la función
    registrarLibro(inventario);
    mostrarLibros(inventario);

    return 0;
}