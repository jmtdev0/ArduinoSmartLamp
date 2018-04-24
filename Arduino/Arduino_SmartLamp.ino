/* Universidad Rey Juan Carlos - Campus de Móstoles
   Grado en Ingeniería Informática - Sistemas Empotrados y de Tiempo Real
   Práctica obligatoria - Proyecto Arduino SmartLamp
   Grupo 14: Alejandro Manuel Pazos Boquete, Javier Martín Torres, Sergio Mingorance Wagner
*/
  
// Declaración de pines, constantes y variables

// Tanto el zumbador como los cables que se conectan a la tira LED están ubicados en pines que pueden operan con señales analógicas.

#define REDPIN 5
#define GREENPIN 6
#define BLUEPIN 3

#define BUZZER 9

char charRead;  // Carácter que sirve para leer cada uno de los caracteres que se reciben mediante Bluetooth.
String command; // String que almacena todos los carácteres que se obtienen en la transmisión.

/* Los dos booleanos siguientes sirven para tener control del estado en el que se debe encontrar la lámpara y para saber si se quiere que el
   zumbador emita algún sonido.
*/

boolean ledOn = false; 
boolean buzzerOn = true;
 
int REDValue = 255;
int GREENValue = 255;
int BLUEValue = 255;

/* Inicialmente la lámpara está apagada y el zumbador se activa cuando es pertinente. Mientras no se especifique otro color, la lámpara
   emite luz de color blanco cuando se le solicite hacerlo.
*/

// Configuración de pines y puerto serial

void setup() {
  
  Serial.begin(9600);  
  /* Puerto serial que va a recibir instrucciones por Bluetooth; también pueden mostrarse por pantalla mensajes a través de él.
     Se determina el ratio de bits por segundo (baudio) para la transmisión serial de datos que se efectúa. Se asigna un valor
     estándar, 9600 bits por segundo.
  */
  
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  
}

// Programa principal (definición de un ciclo)

void loop() {

  /* Lectura carácter a carácter de la información recibida. Este fragmento es el resultado de adaptar el código disponible en el paso 8 
     de este tutorial: http://www.instructables.com/id/Android-Bluetooth-Control-LED-Part-2/.
  */  

  if (Serial.available() > 0) { // Cuando se recibe información por medio de la transmisión Bluetooth, se elimina el comando que pudiera haber anteriormente.
    
    command = "";
    
  }
  while(Serial.available() > 0) { // Mientras el puerto serial pueda leer nuevos caracteres, se añaden a command. Así hasta que llega un punto y coma.

      charRead = ((byte)Serial.read());
      
      if(charRead == ':') {
        break;
      }
      
      else {
        command += charRead;
      }
      
      delay(1);
    
  }

 // Obtenido el comando completo, se procede a actuar conforme a lo que pide. A continuación se enumeran las distintas acciones posibles.

  Serial.println(command); // Se imprime por pantalla el comando procesado. Esta instrucción es necesaria para que el comportamiento de la placa sea el buscado.

  if (command == "turnOn") {  // La lámpara se enciende y luce con la última configuración de color que se haya indicado, o en blanco si no hay ninguna.

   encenderTiraLED();
   delay(100); // Se añaden pequeños delays para mejorar la asimilación de las señales, pero no se ha comprobado que cumplan una labor indispensable.
   ledOn = true;
    
  }

  else if(command == "turnOff") { // Se apaga la lámpara.
      
    apagarTiraLED();
    delay(100);
    ledOn = false;
      
  } else if (command == "buzzerOn") { // El zumbador producirá sonido cuando sea conveniente a partir de este momento.
    
    buzzerOn = true;
    delay(100);

  } else if (command == "buzzerOff") { // El zumbador no se activará en ninguna situación.

    buzzerOn = false;
    delay(100);
    
  } else if (command.length() == 9) { 
  /* Se ha procurado que todos los comandos con 9 caracteres que no se correspondan con ninguno de las ramas if-else anteriores
     consten de tres tríos de caracteres que se asocian con los valores RGB que se necesitan para determinar el color con que 
     ilumina la tira LED. 
  */

    separarValoresLED(command); // Se separan los tríos de caracteres del String y se guardan los valores obtenidos en las variables pertinentes.

    if (ledOn) { // Solo si la lámpara ya está encendida, se indica que lo siga estando.
      encenderTiraLED();
    } else {
      apagarTiraLED();
    }
  
  } else if (((command.length() == 11) && (command.charAt(9) == 'N') && (command.charAt(10) == 'T')) ||
  ((command.length() == 22) && (command.charAt(20) == 'N') && (command.charAt(10) == 'T') && (command.substring(0,11).equals(command.substring(11,22))))) {
  /* Esta rama responde a los comandos que además de portar un código de color de 9 caracteres, se han marcado con la etiqueta "NT".
     Son aquellos que han sido enviados para informar de una notificación. Aquí se busca emitir un parpadeo de un segundo
     con el color que está asociado a la aplicación que ha mandado dicha notificación.
   */

    int valorNotiRED = command.substring(0,3).toInt(); // Se obtiene los valores RGB del comando.
    int valorNotiGREEN = command.substring(3,6).toInt();
    int valorNotiBLUE = command.substring(6,9).toInt();

    if (buzzerOn) { // Si el usuario tiene activado el sonido de la aplicación, el zumbador emite una serie de sonidos.

      playBeethoven();

    }

    encenderTiraLED (valorNotiRED, valorNotiGREEN, valorNotiBLUE);
    delay(1000);

    if (ledOn) {  // Si la lámpara está encendida, seguirá estándolo.
      encenderTiraLED();
    } else {
      apagarTiraLED();
    }
    
  }

  else if ((command.length() == 11) && (command.charAt(9) == 'V') && (command.charAt(10) == 'C')) {  
  /* Por último, se procesan los comandos de voz. Al igual que los propios de las notificaciones, constan del código de color
     y una etiqueta, "VC". Al recibir uno, el color con el que luce la lámpara cambia al que se recibe. Si está apagada, se 
     enciende.
  */

    separarValoresLED (command.substring(0,9)); // Se recoge el código de color y sus tres valores se asignan a las variables de valores RGB.

    ledOn = true;
    encenderTiraLED();
      
  }

  command = ""; // Se elimina el contenido de command para evitar bucles indeseados en el código.

} // Fin de loop

void encenderTiraLED () { // Método empleado para que la tira LED emita luz de acuerdo a los valores establecidos en REDValue, GREENValue y BLUEValue.

  analogWrite(REDPIN, REDValue);
  analogWrite(GREENPIN, GREENValue);
  analogWrite(BLUEPIN, BLUEValue);
  
}

void encenderTiraLED (int valorNotiRED, int valorNotiGREEN, int valorNotiBLUE) { 
// Se utiliza este método para iluminar la lámpara con un color específico de forma puntual (para informar de notificaciones recibidas).

  analogWrite(REDPIN, valorNotiRED);
  analogWrite(GREENPIN, valorNotiGREEN);
  analogWrite(BLUEPIN, valorNotiBLUE);
  
}

void apagarTiraLED () { // Se usa para apagar la lámpara. Es equivalente a encenderTiraLED (0,0,0).

  analogWrite(REDPIN, 0);
  analogWrite(GREENPIN, 0);
  analogWrite(BLUEPIN, 0);
  
}

void separarValoresLED (String valoresLED) {  
// Recibe un String con 9 caracteres que se separan en 3 tríos, asociados a los valores RGB. Se emplea cuando se quiere cambiar indefinidamente el color con el que luce la lámpara.

  REDValue = valoresLED.substring(0,3).toInt();
  GREENValue = valoresLED.substring(3,6).toInt();
  BLUEValue = valoresLED.substring(6,9).toInt();
  
}

void playBeethoven () {
// Primeras ocho notas de Para Elisa de Beethoven. Acompañan los avisos de notificaciones si el sonido está activo. Extraído de https://gist.github.com/spara/1832855.

  // play e4
  delay(150);
  tone(9, 329.63, 300);
  delay(87);
  // play d4# 
  tone(9, 311.13, 300);
  delay(87);
  // play e4
  tone( 9, 329.63, 300);
  delay(87);
  // play d4# 
  tone( 9,311.13, 300);
  delay(87);
  // play e4
  tone(9, 329.63, 300);
  delay(87);
  // play b3
  tone( 9, 246.94, 300);
  delay(100);
  // play d4
  tone(9, 293.66,300);
  delay(100);
  // play c4
  tone(9, 261.63,300);
  delay(100);
  
}
