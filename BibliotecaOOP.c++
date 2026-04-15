#include <iostream>
#include <vector>
#include <string>
#include <iomanip> // Para que las tablas se vean bien

using namespace std;

// --- CLASE LIBROS ---
class Libros {
private: //en private para que nadie pueda modificarlo, solo a traves de los metodos de la clase
    int id;
    string titulo;
    string autor;
    int anio_pub;
    int ejemplares;

public:
    void CargarLibro() { //metodo para cargar el libro.
        cout << "Ingrese el nombre del libro: ";
        getline(cin, titulo); //el getline se usa para leer strings con espacios, sino solo te toma la primera palabra
        cout << "Ingrese el autor del libro: ";
        getline(cin, autor);
        cout << "Ingrese el año de publicación: ";
        cin >> anio_pub; //acá uso cin porque es un numero
        cout << "Ingrese ejemplares disponibles: ";
        cin >> ejemplares;
        cin.ignore(); //el cin.ignore es para impiar el buffer de entrada, sino el siguiente getline se saltea
    }

    int getID() const {  //el const lo usamos para elctura. Es funcion int porque el id es un numero entero
        return id; //devuelve el valor del id que extrae de la misma clase, por eso esta funcon va definida dentro de la misma
    }

    void setID(int nuevoId) { //es una instancia de la clase, es void porque no devuelve nada, recibe un numero entero que es el nuevo id extraido del id
        id = nuevoId; 
    }

    void MostrarLibro() const { //const de vueta porque es lectura.
        cout << "| " << setw(3) << id << " | " << setw(20) << titulo << " | " << setw(15) << autor << " |" << endl; //el setw (3) es qué va a escribir un caracter y va a dejar 3 espacio para el próximo caracter
    }
};

// --- CLASE USUARIOS ---
class Usuarios {
private: //private para que solo se pueda modoficar con los métodos de la clase.
    int id;
    string nombre;
    string apellido;
    string email;

public:
    int getId() const { //es lectura del id del usuario, por eso us const, ademas es int porque el id es un número
        return id; 
    }

    void setId(int nuevoId) { //es void porque no devuelve nada, recibe el valor nuevoID de la clase usuario y con eso crea el ID
        id = nuevoId; 
    }

    void registrarUsuario() { //para llenar el registro del usuario, es void porque no deuelve nada
        cout << "Ingrese el nombre: ";
        getline(cin, nombre);
        cout << "Ingrese el apellido: ";
        getline(cin, apellido);
        cout << "Ingrese el email: ";
        getline(cin, email);
    }

    void consultarUsuario() const { //const de vuelta porque es de lectura
        cout << "| " << setw(3) << id << " | " << setw(15) << nombre << " | " << setw(15) << apellido << " |" << endl;
    }
};

// --- CLASE PRÉSTAMOS ---
class Prestamos {
private: //private para que no se puedan modificar los valores sino no es mediante un método de la clase. 
    int id;
    int id_usuario;
    int id_libro;
    string fecha_prestamo;
    bool volvio;

public:
    int getId() const { //de lectura por es el const, para leer el Id del préstamo
        return id; 
    }

    void setID(int nuevoId) { //le pasa al prestamo nuevo un nuevo id
        id = nuevoId; 
    }

    int getIdUsuario() const { //de lectura por es el const, para leer el Id del usuario. Usams get porque como es un atributo privado de otra case, el get lo instancia para poder acceder a el. 
        return id_usuario; 
    }

    bool getVolvio() const { //es de lectura de la variable volvio, si el libro volvio se llama a esta funcion que devuelve el estado booleano de la variable como que "volvio" o sea, true.
        return volvio; 
    }
    
    void setVolvio(bool estado) { //nos deja cambiar el estado de "volvio" mediante el parámetro booleano estado. Según el estado que le pasemos, el valor de volvio va a ser true o false. Por ejemplo, cuando se realiza un préstamo, el estado es false porque el libro no volvió, y cuando se registra una devolución, el estado es true porque el libro volvió.
        volvio = estado; 
    }

    void realizarPrestamo() {
        cout << "Ingrese ID del usuario: ";
        cin >> id_usuario;
        cout << "Ingrese ID del libro: ";
        cin >> id_libro;
        cout << "Fecha de prestamo (DD/MM/AAAA): ";
        cin.ignore();
        getline(cin, fecha_prestamo);
        volvio = false; //volvio es false porque recien salio el libro, entonces no volvió todavía. Cuando se registre la devolución, se va a cambiar a true mediante el método setVolvio.
    }

    void mostrarPrestamo() const {
        cout << "| " << setw(3) << id << " | User: " << setw(3) << id_usuario 
             << " | Libro: " << setw(3) << id_libro << " | Volvio: " << (volvio ? "SI" : "NO") << " |" << endl;
    }
};

//Funciones para los vectores. Como son de búsqueda,  estas estan por fuera de las clases porque si bien las clases saben sus id, y los objetos que tienen, no saben que cada objeto esta guardado en un vector accompañado de muchisimos otros. 

void mostrarLibros(const vector<Libros>& estante) { //va a pasar el vector de libros por referencia, es de lectura porque no va a modificar el vector, por eso el const. El vector se llama estante porque es donde se guardan los libros.
    if (estante.empty()) { //pregunta si el vector estante esta vacio, o sea, no hay libros cargados. Si esta vacio, muestra un mensaje y sale de la función con el return.
        cout << "No hay libros cargados." << endl;
        return;
    }
    cout << "\n--- LISTADO DE LIBROS ---" << endl; //si no esta vacio, muestra los libros que hay en e estante llamando a la funcion mostrarLibro previamente definida en la clase libros
    for (const auto& l : estante) { 
        l.MostrarLibro();
        //el for recorre por cada libro (l) del vector estante, y muestra su información llamando al método MostrarLibro de la clase Libros. 
        //El auto sirve para que el programa deduzca que dentro de estante hay solo libros, y el & apunta al libro que ya esta en el vector, sino imprimiria siempre el mismo. 
        //Es como el puntero al libro*sgte. "l" es el libro actual en el cual el bucle esta en el vector.
        //y : estante n os dice qué libro del estante vamos a mostrar
        
    }
}

void mostrarUsuarios(const vector<Usuarios>& lista) {
    for (const auto& u : lista) u.consultarUsuario();
    //aca lo mismo, el for es el bucle que recorre lista del vector de usuarios, asume que cada element de lista peretenece a la clase usuarios
    //y "u" es la poscion  actual del bucle en el vetcor.
    //por último, : lista nos dice de qué usuario vamos a sacar la info, y le dice que extraiga la informacion u de dicho usuari mediante consultarUsuario. 
}

void realizarPrestamo(vector<Prestamos>& historial, const vector<Usuarios>& usuarios, const vector<Libros>& libros) { //esto nos deja unir los tres vectores para ahcer el préstamo
    //pasa historial por referencia para modificar el vector en el main. Libros y usuarios los pasa con const porque es de lectura y no va a modificar esos vectores, solo va a leerlos para verificar que el usuario y el libro existan antes de realizar el préstamo.
    if (usuarios.empty() || libros.empty()) {
        cout << "Error: Necesitas usuarios y libros registrados." << endl;
        return;
    }
    //pregunta si los vectores usuarios y libros estan vacios. De ser así te da error. 
    Prestamos nuevo;
    int nuevoId = historial.empty() ? 1 : historial.back().getId() + 1;
    nuevo.setID(nuevoId);
    nuevo.realizarPrestamo();
    historial.push_back(nuevo);
    cout << "Prestamo registrado con exito." << endl;
    //si verifica que los vectores libros y usuarios no estan vacios. Asigna una nueva id al préstamo, lw pasa la id ccon setID, llama a la funcion nuevoPrestamo
    //luego con toda esa informacion, la manda al vector historial al final del vector con push_back
}

void registrarDevolucion(vector<Prestamos>& historial) { //pasa por fererencia el vector historial para cambiar el valor de volvio
    int idBuscado;
    //nueva variable 
    cout << "Ingrese el ID del prestamo a devolver: ";
    cin >> idBuscado;
    //lectura de variable

    for (auto& p : historial) { //asume que son objetos del vector historial y busca en el objeto "p" del vector préstamos.
        if (p.getId() == idBuscado) {
            if (p.getVolvio()) {
                cout << "Ya estaba devuelto." << endl;
            } else {
                p.setVolvio(true);
                cout << "Devolucion exitosa." << endl;
            }
            return;
        }
        //si el id de P es el mismo que idBuscado, pregunta si p ya volvio, si es asi muestra éxito
        //si no, llama a setVolvio, y cambia el estado de devuelto falso a true. 
    }
    cout << "ID de prestamo no encontrado." << endl;
    //si no encuentra ell id en el vector historial, muestra error.
}

int main() {
    vector<Libros> estante; //el nombre de vector es estante. Va a llenar el vector con la class libros
    vector<Usuarios> listaUsuarios; //el nombre del vector es listaUsuarios. Va a llenar el vector con la class usuarios
    vector<Prestamos> historialPrestamos; //el nombre del vector es historialPrestamos. Va a llenar el vector con la class prestamos
    int opcion;

    do {
        cout << "\n--- BIBLIOTECA EEST N1 ---" << endl;
        cout << "1. Registrar libro\n2. Mostrar libros\n3. Registrar usuario\n4. Realizar prestamo\n5. Devolucion\n6. Salir" << endl;
        cout << "Seleccione: ";
        cin >> opcion;
        cin.ignore();

        switch (opcion) {
            case 1: {
                Libros nuevo;
                nuevo.setID(estante.empty() ? 1 : estante.back().getID() + 1);
                //al objeto nuevo le llamamos setId para darle una id. Pregunta si el estante esta vacio (?), si lo está te da a la id un 1. 
                //sino (:) agarra el id del último objeto de estante y le suma 1 para asignarl la nueva id al nuevo objeto.
                nuevo.CargarLibro();
                estante.push_back(nuevo);
                break;
            }
            //crea un nuevo objeto en la class libros. Le asigna la Id, llama a cargar libro, y una vez que se creo envia al vector estante el libro nuevo.
            case 2:
                mostrarLibros(estante);
                break;
            //llama a la funcion mostrarLibros del vector estante
            case 3: {
                Usuarios nuevo;
                nuevo.setId(listaUsuarios.empty() ? 1 : listaUsuarios.back().getId() + 1);
                nuevo.registrarUsuario();
                listaUsuarios.push_back(nuevo);
                break;
            }
            //crea un objeto nuevo en la class usuarios, le da una id, llama a registarUsuario, y una vez qque lo registra, lo envia de vuelta al vector listaUsuarios.
            case 4:
                realizarPrestamo(historialPrestamos, listaUsuarios, estante);
                break;
            //hace un prestamo pasando los tres vectores para verificar que el usuario y el libro existan, y para agregar el préstamo al vector historial.
            case 5:
                registrarDevolucion(historialPrestamos);
                break;
            //le pasa el vector historialPrestamos a la funcion para cambiar el estado de "volvio" de false a true.
        }
    } while (opcion != 6);
    //todo eso se pregunta siempre que la opción no sea 6. si es 6 se sale del programa. 
    return 0;
}