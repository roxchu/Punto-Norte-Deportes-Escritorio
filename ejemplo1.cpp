#include <iostream>
using namespace std;

class cuadrado{
    private:
    float lado;

    public:
    cuadrado (){lado=1;}
    void set_lado(float l){
        lado=l;
    }

    float get_lado(){
        return lado;
    } 

    float Area(){
        return lado*lado;
    }
};

int main(){
    cuadrado x;
    x.set_lado(5);
    cout<<"El area del cuadrado del lado "<<x.get_lado();
    cout<<"es"<<x.Area();
    return 0;
}