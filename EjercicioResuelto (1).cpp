#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
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
  cout<<"Ingrese nombre del libro";
  getline (cin, titulo); //lee este titulo, lleva getline porq sino no te toma los espacios
  cout <<"Ingrese nombre del autor";
  getline(cin, autor);
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

//DECLARACION DE FUNCIONES
// *punteros por valor (no se modifica el contenido)
// *&punteros por referencia (se modifica el contenido)
void regLibro(Libros *&);
void consLibro(Libros *);
void regUsuario(Usuarios *&);
void consUsuario(Usuarios *);
void prestar(Prestamos *&, Libros *,Usuarios *);
void consPrest(Prestamos *);
void devolver(Prestamos *&);
void histUsuario(Prestamos *, Usuarios *);
int menu();

//FUNCION PRINCIPAL
vector <string> lista;

int main() {
    //DECLARACION DE VARIABLES
    int op;
    Libros *libro=NULL;
    Usuarios *usuario=NULL;
    Prestamos *prestamo=NULL;
    //CUERPO DEL PROGRAMA
    do{
        op=menu();//LLAMADO A FUNCION MENU
        switch (op){//DE ACUERDO A OP SE LLAMAN A FUNCIONES
            case 1:
                regLibro(libro);
                break;
            case 2:
                regUsuario(usuario);
                break;
            case 3:
                consLibro(libro);
                break;
            case 4:
                if (libro!=NULL && usuario!=NULL){
                    prestar(prestamo,libro,usuario);
                }else{
                    cout<<"Es necesario cargar usuarios y/o libros";
                }
                break;
            case 5:
                if (prestamo!=NULL){
                    devolver(prestamo);
                }else{
                    cout<<"NO HAY PRESTAMOS REALIZADOS";
                }
                break;
            case 6:
                if (usuario!=NULL){
                    histUsuario(prestamo,usuario);
                }else{
                    cout<<"NO HAY USUARIOS CARGADOS";
                }
                break;
            case 7:
                break;
            default:
                cout<<"Ingrese opcion valida";
        }
    }while (op!=7);
}


void prestar(Prestamos *&prestamo, Libros *libro,Usuarios *usuario){
    Prestamos *nuevo;
    Prestamos *aux;
    string rta;
    do{
    nuevo = new Prestamos;
    consUsuario(usuario);
    cout<<"Ingrese Id del Usuario: ";
    cin>>nuevo->usuario;
    consLibro(libro);
    cout<<"Ingrese Id del Libro: ";
    cin>>nuevo->libro;
    cout<<"Ingrese fecha inicio del prestamo (DD/MM/AA): ";
    cin.ignore();
    getline(cin,nuevo->fprestamo);
    cout<<"Ingrese fecha inicio del prestamo (DD/MM/AA): ";
    getline(cin,nuevo->fdevolucion);
    nuevo->volvio=false;
    nuevo->sgte = NULL;
    if(prestamo == NULL){
        nuevo->id = 1;
        prestamo = nuevo;
    }else{
        aux = prestamo;
        while(aux->sgte != NULL){
            aux = aux->sgte;
        }
        nuevo->id = aux->id + 1;
        aux->sgte=nuevo;
    }
    nuevo->sgte=NULL;
    cout<<"S para seguir cargando: ";
    getline(cin,rta);
    }while(rta=="S" || rta=="s");
}

void consPrest(Prestamos *presta){
    if (presta==NULL){
        cout<<"NO HAY Prestamos CARGADOS\n";
    }else{
        cout<<setw(50)<<"PRESTAMOS"<<endl;
        cout<<string(120,'_')<<endl;
        cout<<'|'<<setw(10)<<"ID"<<setw(20)<<"USUARIO"<<setw(20)<<"LIBRO"<<setw(20)<<"INICIO"<<setw(20)<<"FIN"<<setw(20)<<"VOLVIO?"<<setw(10)<<'|'<<endl;

        while (presta!=NULL){
            cout<<"|"<<string(119,'-')<<"|"<<endl;
            cout<<'|'<<setw(10)<<presta->id<<setw(20)<<presta->usuario<<setw(20)<<presta->libro<<setw(20)<<presta->fprestamo<<setw(20)<<presta->fdevolucion<<setw(20)<<presta->volvio<<setw(10)<<'|'<<endl;
            presta=presta->sgte;
        }
        cout<<"|"<<string(119,'_')<<"|"<<endl;
    }
}

void devolver(Prestamos *&prestamo){
    int idprest;
    Prestamos *aux=prestamo;
    consPrest(prestamo);
    cout<<"Cual prestamo finalizo? 0 si ninguno";
    cin>>idprest;
    while (aux->id!=idprest){
        aux=aux->sgte;
    }
    aux->volvio=true;
}
void histUsuario(Prestamos *prestamo, Usuarios *user){
    int idUsuario;
    Prestamos *aux=prestamo;
    consUsuario(user);
    cout<<"Ingrese el usuario del que quiere consultar el historial: ";
    cin>>idUsuario;
    cout<<setw(50)<<"HISTORIAL USUARIO "<<idUsuario<<endl;
    cout<<string(100,'_')<<endl;
    cout<<'|'<<setw(10)<<"ID"<<setw(20)<<"LIBRO"<<setw(20)<<"INICIO"<<setw(20)<<"FIN"<<setw(20)<<"VOLVIO?"<<setw(10)<<'|'<<endl;
    while (aux!=NULL){
        if (aux->usuario==idUsuario){
        cout<<"|"<<string(99,'-')<<"|"<<endl;
        cout<<'|'<<setw(10)<<aux->id<<setw(20)<<aux->libro<<setw(20)<<aux->fprestamo<<setw(20)<<aux->fdevolucion<<setw(20)<<aux->volvio<<setw(10)<<'|'<<endl;
    }
    aux=aux->sgte;
    }
    cout<<"|"<<string(99,'_')<<"|"<<endl;
}
int menu(){
    int op;
    cout<<"\t\tBIENENIDO A LA BIBLIO!!!\n";
    cout<<"\t1) Registrar Libro\n";
    cout<<"\t2) Registrar Usuario\n";
    cout<<"\t3) Consultar Libro\n";
    cout<<"\t4) Realizar Prestamo\n";
    cout<<"\t5) Registrar Devolucion\n";
    cout<<"\t6) Historial Usuario\n";
    cout<<"\t7) Salir\n";
    cout<<"Ingrese una opcion: ";
    cin>>op;
    return op;
}