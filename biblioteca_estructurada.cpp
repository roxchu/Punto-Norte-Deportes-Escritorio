// Example program
#include <iostream>
#include <string>
using namespace std;

int comprobarRol {
  int id;
  cout<<"Ingrese su ID de usuario y su cntraseña"<<endl;
  cin>>id;
  cout<<id;
  //según la id que se nos ingrese, accedemos a la base de datos y nos fijamos que número tiene (1 admin, 2 owner, 3 cliente) y lo trae, para que le devuelve al programa el dato que obtuvo. Entonces, según lo que devuelve el switch case que muestro
}

int main()
{
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

