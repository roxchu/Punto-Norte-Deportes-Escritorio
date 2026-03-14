// Example program
#include <iostream>
#include <string>
using namespace std;

struct Libro {
    int id;
    string titulo;
    string autor;
    bool disponible;
};

struct Usuario {
    int id;
    string nombre;
    string email;
};

struct Prestamo {
    int idLibro;
    int idUsuario;
    string fecha;
};

void agregar_libro(vector<Libro>&biblioteca){
  cout <<"Ingrese el título del libro: ";
  cin >> ignore;
  getline(cin, nuevo.titulo);

}

int main(){
  vector<Libro> inventario;
  vector<Usuario> listaUsuarios;
  vector<Prestamo> historialPrestamos;
    int opcion;
  cout<<"Hola! Bienvenido a la biblioteca de la EEST N1.";
  cout<<"Qué deseas hacer? Ingresar usuario (1) Solicitar una cuenta para la biblioteca (2)";
  cin >> opcion;
  
  switch (opcion) {
      case 1: 
      cout << "lol xd" << endl; //llamar funcion booleana de verificacion de rol
       break;

      case 2:
      cout<< "lol xddd" << endl; //solicitar cuenta para usuario (opcion dos)
      break; 
      
    }
  
}

