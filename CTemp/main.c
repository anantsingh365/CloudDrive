#include<stdio.h>

static int getNumber(char a);

int main(int argc, char** argv){

    printf("Hello world\n");
    int a = getNumber('y');

    printf("%d", a);    

    return 0;
}

static int getNumber(char a){
    return 10;
}