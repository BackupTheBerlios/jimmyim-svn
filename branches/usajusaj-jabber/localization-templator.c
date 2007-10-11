#include <stdlib.h>
#include <string.h>
#include <stdio.h>

//#define debug

typedef struct{
  
  char* fname;
  int* line;
  char* locale_id;

} Locale;

Locale* getLocaleID(char* ss, FILE* f);


int main(int argv, char** argc){

  char* in = (char*)malloc(sizeof(char)*100);
  int a = 0;
  while(a != EOF){
    a = scanf("%s",in);
    FILE* f = fopen(in,"r");
    //    printf("%s",f);
    getLocaleID("",f);


#ifdef debug
    printf("%s\n",in);
#endif

  }

  return 1;
}

Locale* getLocaleID(char* ss, FILE* f){
  Locale* l = (Locale*)malloc(sizeof(Locale*));
  char* ln = (char*)malloc(sizeof(char)*1000);
  int lines = 0;
   while(!feof(f)){ 

     int read = fread(ln,1000,1,f);

     int i = 0;
     int j = 0;
     for(i; i < read; i++){
       if(ss[j] == ln[i]){
	 j++;
       }else if(ln[i] == '\n'){
	 j=0;
	 lines++;
       }else{
	 j=0;
       }

       if(j == strlen(ss)){
	 
	 //get the stuff out

       }
     }
     

     //     printf("%s\n",l->locale_id);
   } 
  return l;
}

