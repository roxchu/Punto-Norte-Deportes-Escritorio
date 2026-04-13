#include <iostream>
using namespace std;

int main() {
    int opcion;
    do {
        // 1. Mostrar menú
        cout << "\n--- MENU ---" << endl;
        cout << "1. Opción 1" << endl;
        cout << "2. Opción 2" << endl;
        cout << "3. Salir" << endl;
        cout << "Elija una opción: ";
        cin >> opcion;

        // 2. Evaluar opción
        switch (opcion) {
            case 1:
                cout << "Seleccionaste la opción 1." << endl;
                break;
            case 2:
                cout << "Seleccionaste la opción 2." << endl;
                break;
            case 3:
                cout << "Saliendo..." << endl;
                break;
            default:
                cout << "Opción no válida." << endl;
        }
    } while (opcion != 3); // 3. Repetir si no es 3

    return 0;
}
