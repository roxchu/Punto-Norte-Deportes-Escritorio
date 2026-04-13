#include <iostream>
#include <vector>
#include <string>
using namespace std;

// 1. Definicion estructuras
class Libros{
 private:
 int id;
 string titulo;
 string autor;
 int anio_publi;
 int ejemplares;
 Libros *sgte; //es el puntero

 public:
 Libros(){ //el metodo constructor lo q nos hace poner el "chocolate" dentro del "molde", todos los libros tienen los atributos a continuacion
  //cin.ignore;
  cout <<"Ingrese nombre del libro";
  getline (cin, titulo); //lee este titulo, lleva getline porq sino no te toma los espacios
  cout <<"Ingrese nombre del autor";
  getline (cin, autor);
  cout <<"Ingrese anio de publicacion del libro";
  cin>>anio_publi;
  cin.ignore();
  cout <<"Ingrese cantidad de ejemplares del libro";
  cin>>ejemplares;
  cin.ignore();
  sgte=NULL;
  } 
  //cuando vos creas un libro el pun tero siempra apunta al último pq sino nunca encontraria el final.
  void mostrarLibro(){ //metodo para mostrar el libro, empiezo mostrando el titulo nada más pq si
    cout<<titulo<<endl;
    cout<<autor<<endl;
    cout<<anio_publi<<endl;
    cout<<ejemplares<<endl;
  }
};

///Clase 
class Usuario {
    private:
    int id;
    string nombre;
    string apellido;
    string email;
    Usuario*sgte;

    public:
    Usuario(){
        cout<<"Ingrese nombre del usuario: ";
        getline(cin, nombre);
        cout<<"Ingrese apellido del usuario: ";
        getline(cin, apellido);
        cout<<"Ingrese email del usuario: ";
        getline(cin, email);
        sgte = NULL;
    };

    void mostrarUsuario(){
        cout<<"Nombre: "<<nombre<<endl;
        cout<<"Apellido: "<<apellido<<endl;
        cout<<"Email: "<<email<<endl;
    }
};

class Prestamo{
 private:
 int id_prestamo;
 int id_usuario;
 int id_libro;
 string fecha_prestamo;
 string fecha_devolucion;
 Prestamo*sgte;

    public:
    Prestamo(){
        cout<<"Ingrese ID del usuario: ";
        cin>>id_usuario;
        cin.ignore();
        cout<<"Ingrese ID del libro: ";
        cin>>id_libro;
        cin.ignore();
        cout<<"Ingrese fecha de prestamo (DD/MM/AAAA): ";
        getline(cin, fecha_prestamo);
        cout<<"Ingrese fecha de devolucion (DD/MM/AAAA): ";
        getline(cin, fecha_devolucion);
        sgte = NULL;
    }
};

// 2. LA FUNCIÓN DE REGISTRO
void registrarLibro(vector<Libro> &estante) { 
    Libro fichaNueva; // Creamos una ficha vacía

    // Asignamos el ID automático basado en cuántos ya hay
    fichaNueva.id = estante.size() + 1;

    cout << "--- Registro de Libro ID: " << fichaNueva.id << " ---" << endl;
    
    cin.ignore(); // Limpieza necesaria antes de leer texto
    cout << "Titulo: ";
    getline(cin, fichaNueva.titulo); ///Para variables string
    
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

void registrarUsuario(vector<Usuario> &listaUsuarios){
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

void realizarPrestamo(vector<Prestamo> &historialPrestamos, vector<Usuario> &listaUsuarios, vector<Libro> &inventario){
    Prestamo nuevoPrestamo;
    nuevoPrestamo.id_prestamo = historialPrestamos.size() + 1;

    cout << "--- Registro de Prestamo ID: " << nuevoPrestamo.id_prestamo << " ---" << endl;
    cin.ignore();

    ///cout << "ID Usuario: ";
    ///cin >> nuevoPrestamo.id_usuario;
    ///Esto no sé si debería ingresarlo el usuario, o deberia estar asignado directamente por el programa, ya que al momento de realizar el préstam ya se debe estar registrado/sesion iniciada, entonces ya se presume quien es el usuario haciendo el retiro

    ///cout << "ID Libro: ";
    ///cin >> nuevoPrestamo.id_libro;
    ///Misma situación acá, el usuario común no debe saber la id del libro, asi que con ingresar el nombre del libro seria suficiente, el programa lo busca y se encarga de asignar el id que ya tiene el libro, y si el libro no existe, se le notifica al usuario que no se encuentra registrado en el sistema, o que no hay ejemplares disponibles para préstamo

    cout << "Fecha de prestamo (DD/MM/AAAA): ";
    cin.ignore();
    getline(cin, nuevoPrestamo.fecha_prestamo);

    cout << "Fecha de devolucion (DD/MM/AAAA): ";
    getline(cin, nuevoPrestamo.fecha_devolucion);

    // Validación básica
    if (nuevoPrestamo.id_usuario > 0 && nuevoPrestamo.id_libro > 0 && !nuevoPrestamo.fecha_prestamo.empty() && !nuevoPrestamo.fecha_devolucion.empty()) {
        historialPrestamos.push_back(nuevoPrestamo);
        cout << "Prestamo registrado correctamente." << endl;
    } else {
        cout << "Error: No puedes dejar campos vacios." << endl;
    }
}

int main() {
    // Definimos el contenedor principal
    vector<Libro> inventario; 
    vector<Usuario> listaUsuarios;
    vector<Prestamo> historialPrestamos;

    int opcion;
    cout<<"Hola! Bienvenido a la biblioteca de la EEST N1.";

    do{
        cout << "\n--- MENU ---" << endl;
        cout << "1. Registrar libro" << endl;
        cout << "2. Mostrar libros" << endl;
        cout << "3. Realizar préstamo" << endl;
        cout << "4. Registrar usuario" << endl;
        cout << "5. Salir" << endl;
        cout << "Elija una opción: ";
        cin >> opcion;

        switch (opcion){
        case 1:
            registrarLibro(inventario);
            break;
        case 2:
            mostrarLibros(inventario);
            break;
        case 3:
            realizarPrestamo(historialPrestamos, listaUsuarios, inventario);
            break;
        case 4:
            registrarUsuario(listaUsuarios);
            break;
        case 5:
            cout << "Saliendo..."<<endl;
            break;
        default:
            cout << "Opción no válida." << endl;
       };
    } while (opcion != 5);
    return 0;
}
