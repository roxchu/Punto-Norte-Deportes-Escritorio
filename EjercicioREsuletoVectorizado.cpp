#include <iostream>
#include <iomanip>
#include <string>
#include <vector> // Librería necesaria para usar vectores

using namespace std;

// DEFINICION DE ESTRUCTURAS (Sin punteros *sgte)
struct Libros {
    int id;
    string titulo;
    string autor;
    int anio_pub;
    int ejemplares;
};

struct Usuarios {
    int id;
    string nombre;
    string apellido;
    string email;
};

struct Prestamos {
    int id;
    int usuario; // Almacena ID del usuario
    int libro;   // Almacena ID del libro
    string fprestamo;
    string fdevolucion;
    bool volvio;
};

// DECLARACION DE FUNCIONES
// Usamos referencias (&) para que los cambios en los vectores persistan
void regLibro(vector<Libros> &vLibros);
void regUsuario(vector<Usuarios> &vUsuarios);
void consLibro(const vector<Libros> &vLibros);
void consUsuario(const vector<Usuarios> &vUsuarios);
void prestar(vector<Prestamos> &vPrestamos, const vector<Libros> &vLibros, const vector<Usuarios> &vUsuarios);
void consPrest(const vector<Prestamos> &vPrestamos);
void devolver(vector<Prestamos> &vPrestamos);
void histUsuario(const vector<Prestamos> &vPrestamos, const vector<Usuarios> &vUsuarios);
int menu();

int main() {
    int op;
    // Definición de vectores en lugar de punteros simples
    vector<Libros> libros;
    vector<Usuarios> usuarios;
    vector<Prestamos> prestamos;

    do {
        op = menu();
        switch (op) {
            case 1: regLibro(libros); break;
            case 2: regUsuario(usuarios); break;
            case 3: consLibro(libros); break;
            case 4:
                if (!libros.empty() && !usuarios.empty()) {
                    prestar(prestamos, libros, usuarios);
                } else {
                    cout << "Es necesario cargar usuarios y/o libros" << endl;
                }
                break;
            case 5:
                if (!prestamos.empty()) {
                    devolver(prestamos);
                } else {
                    cout << "NO HAY PRESTAMOS REALIZADOS" << endl;
                }
                break;
            case 6:
                if (!usuarios.empty()) {
                    histUsuario(prestamos, usuarios);
                } else {
                    cout << "NO HAY USUARIOS CARGADOS" << endl;
                }
                break;
            case 7: break;
            default: cout << "Ingrese opcion valida" << endl;
        }
    } while (op != 7);

    return 0;
}

// --- IMPLEMENTACIÓN DE FUNCIONES ---

void regLibro(vector<Libros> &vLibros) {
    string rta;
    cin.ignore();
    do {
        Libros nuevo;
        // Asignación de ID basada en el tamaño del vector
        nuevo.id = vLibros.empty() ? 1 : vLibros.back().id + 1;

        cout << "Ingrese Titulo del Libro: ";
        getline(cin, nuevo.titulo);
        cout << "Ingrese Autor del Libro: ";
        getline(cin, nuevo.autor);
        cout << "Ingrese Año de publicacion: ";
        cin >> nuevo.anio_pub;
        cout << "Ingrese Cantidad de ejemplares: ";
        cin >> nuevo.ejemplares;

        vLibros.push_back(nuevo); // Agregamos al final del vector

        cout << "¿Sigue cargando? (S/N): ";
        cin.ignore();
        getline(cin, rta);
    } while (rta == "S" || rta == "s");
}

void regUsuario(vector<Usuarios> &vUsuarios) {
    string rta;
    cin.ignore();
    do {
        Usuarios nuevo;
        nuevo.id = vUsuarios.empty() ? 1 : vUsuarios.back().id + 1;

        cout << "Ingrese Nombre: ";
        getline(cin, nuevo.nombre);
        cout << "Ingrese Apellido: ";
        getline(cin, nuevo.apellido);
        cout << "Ingrese mail: ";
        cin >> nuevo.email;

        vUsuarios.push_back(nuevo);

        cout << "¿Sigue cargando? (S/N): ";
        cin.ignore();
        getline(cin, rta);
    } while (rta == "S" || rta == "s");
}

void consLibro(const vector<Libros> &vLibros) {
    if (vLibros.empty()) {
        cout << "NO HAY LIBROS CARGADOS\n";
        return;
    }
    cout << setw(50) << "LIBROS" << endl;
    cout << string(100, '_') << endl;
    cout << '|' << setw(10) << "ID" << setw(20) << "Titulo" << setw(20) << "Autor" << setw(20) << "Publicado" << setw(20) << "Ejemplares" << setw(10) << '|' << endl;
    
    // Recorremos el vector con un for basado en rango (C++11)
    for (const auto &l : vLibros) {
        cout << "|" << string(99, '-') << "|" << endl;
        cout << '|' << setw(10) << l.id << setw(20) << l.titulo << setw(20) << l.autor << setw(20) << l.anio_pub << setw(20) << l.ejemplares << setw(10) << '|' << endl;
    }
    cout << "|" << string(99, '_') << "|" << endl;
}

void consUsuario(const vector<Usuarios> &vUsuarios) {
    if (vUsuarios.empty()) {
        cout << "NO HAY USUARIOS CARGADOS\n";
        return;
    }
    cout << setw(50) << "USUARIOS" << endl;
    cout << "|" << string(79, '-') << "|" << endl;
    cout << '|' << setw(10) << "ID" << setw(20) << "Nombre" << setw(20) << "Apellido" << setw(20) << "Email" << setw(10) << '|' << endl;
    for (const auto &u : vUsuarios) {
        cout << "|" << string(79, '-') << "|" << endl;
        cout << '|' << setw(10) << u.id << setw(20) << u.nombre << setw(20) << u.apellido << setw(20) << u.email << setw(10) << '|' << endl;
    }
    cout << "|" << string(79, '_') << "|" << endl;
}

void prestar(vector<Prestamos> &vPrestamos, const vector<Libros> &vLibros, const vector<Usuarios> &vUsuarios) {
    string rta;
    do {
        Prestamos nuevo;
        nuevo.id = vPrestamos.empty() ? 1 : vPrestamos.back().id + 1;

        consUsuario(vUsuarios);
        cout << "Ingrese Id del Usuario: ";
        cin >> nuevo.usuario;

        consLibro(vLibros);
        cout << "Ingrese Id del Libro: ";
        cin >> nuevo.libro;

        cout << "Fecha inicio (DD/MM/AA): ";
        cin.ignore();
        getline(cin, nuevo.fprestamo);
        cout << "Fecha devolución (DD/MM/AA): ";
        getline(cin, nuevo.fdevolucion);

        nuevo.volvio = false;
        vPrestamos.push_back(nuevo);

        cout << "¿Sigue cargando préstamos? (S/N): ";
        getline(cin, rta);
    } while (rta == "S" || rta == "s");
}

void consPrest(const vector<Prestamos> &vPrestamos) {
    cout << setw(50) << "PRESTAMOS" << endl;
    cout << string(120, '_') << endl;
    cout << '|' << setw(10) << "ID" << setw(20) << "USUARIO" << setw(20) << "LIBRO" << setw(20) << "INICIO" << setw(20) << "FIN" << setw(20) << "VOLVIO?" << setw(10) << '|' << endl;
    for (const auto &p : vPrestamos) {
        cout << "|" << string(119, '-') << "|" << endl;
        cout << '|' << setw(10) << p.id << setw(20) << p.usuario << setw(20) << p.libro << setw(20) << p.fprestamo << setw(20) << p.fdevolucion << setw(20) << (p.volvio ? "SI" : "NO") << setw(10) << '|' << endl;
    }
}

void devolver(vector<Prestamos> &vPrestamos) {
    int idprest;
    consPrest(vPrestamos);
    cout << "Cual prestamo finalizo? (ID) o 0 para cancelar: ";
    cin >> idprest;
    
    if (idprest == 0) return;

    for (auto &p : vPrestamos) {
        if (p.id == idprest) {
            p.volvio = true;
            cout << "Devolución registrada." << endl;
            return;
        }
    }
    cout << "ID de préstamo no encontrado." << endl;
}

void histUsuario(const vector<Prestamos> &vPrestamos, const vector<Usuarios> &vUsuarios) {
    int idUsuario;
    consUsuario(vUsuarios);
    cout << "Ingrese el ID del usuario para ver historial: ";
    cin >> idUsuario;

    cout << setw(50) << "HISTORIAL USUARIO " << idUsuario << endl;
    cout << string(100, '_') << endl;
    cout << '|' << setw(10) << "ID" << setw(20) << "LIBRO" << setw(20) << "INICIO" << setw(20) << "FIN" << setw(20) << "VOLVIO?" << setw(10) << '|' << endl;

    for (const auto &p : vPrestamos) {
        if (p.usuario == idUsuario) {
            cout << "|" << string(99, '-') << "|" << endl;
            cout << '|' << setw(10) << p.id << setw(20) << p.libro << setw(20) << p.fprestamo << setw(20) << p.fdevolucion << setw(20) << (p.volvio ? "SI" : "NO") << setw(10) << '|' << endl;
        }
    }
    cout << "|" << string(99, '_') << "|" << endl;
}

int menu() {
    int op;
    cout << "\n\t\tBIENVENIDO A LA BIBLIO (Vectores)!!!\n";
    cout << "\t1) Registrar Libro\n\t2) Registrar Usuario\n\t3) Consultar Libro\n\t4) Realizar Prestamo\n\t5) Registrar Devolucion\n\t6) Historial Usuario\n\t7) Salir\n";
    cout << "Ingrese una opcion: ";
    cin >> op;
    return op;
}