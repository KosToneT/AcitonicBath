#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

/* Установите здесь свои SSID и пароль */
const char* ssid = "NodeMCU";
const char* password = "12345678";

/* Настройки IP адреса */
IPAddress local_ip(192,168,1,1);
IPAddress gateway(192,168,1,1);
IPAddress subnet(255,255,255,0);

ESP8266WebServer server(80);

unsigned long timing;

#define VENT1 D0
#define VENT2 D5

#define BUTTON D3
#define LED D2

class VENT{
  long startTime = 0;
  long workingTime = 0;
  int id;
public:
  VENT(){}
  VENT(long workingTime, int id){
    this->workingTime = workingTime;
    this->id = id;
  }  
  
  long getWorkingTime(){
    return workingTime;
  }
  
  long getLeftTime(){
    return workingTime - getPastTime();  
  }
  
  long getPastTime(){
    long currentTime = millis();
    if(startTime==0){
      return 0;
    }
    return (currentTime - startTime);
  }
  
  void setStartTime(long time){
    startTime = time;
  }
  
  long getStartTime(){
    return startTime;
  }
  
  void off(){
    digitalWrite(id, LOW);
  }
  
  void on(){
    digitalWrite(id, HIGH);
  }
  
  void start(){
    startTime = millis();
    on();
  }
};

class Bath{
  float temp = 0;
  bool connection;
  String state;
  String lastState;
  VENT vent0;
  VENT vent1;
public:
  void setState(String state){
    this->state = state;
  }
  
  String getState(){
    return state;
  }

  void setTemp(float temp){
       this->temp = temp;
  }

  float getTemp(){
    return temp;
  }
  
  void setConnection(bool connection){
    this->connection = connection;   
  }
  
  bool getConnection(){
    return connection;
  }
  
  void setVent(int id, VENT vent){
    if(id==0){
      vent0 = vent; 
    }else{
      vent1 = vent;
    }
  }
  
  VENT getVent(int id){
    if(id==0){
      return vent0;
    }else{
      return vent1;
    }
  }
  
  void update(){
      if(vent0.getLeftTime()<=0 && state.equals("time0")){
        vent0.off();
        vent1.start();
        state = "time1";
      }
      if(vent1.getLeftTime()<=0 && state.equals("time1")){
        vent1.off();
        state = "ready";
      }
  }

  void start(){
    state = "time0";
    vent0.start();
  }
  
  void stop(){
    vent0.off();
    vent1.off();
    state = "ready";
    vent0 = VENT();
    vent1 = VENT();
  }
};

Bath bath = Bath();

void setup() {
  pinMode(VENT2, OUTPUT); 
  pinMode(VENT1, OUTPUT); 
  Serial.begin(115200);
  WiFi.softAP(ssid, password, 1, false, 8);
  delay(100);

  Serial.print("Soft-AP IP address = ");
  Serial.println(WiFi.softAPIP());
  
  server.on("/", handle_OnConnect);
  server.on("/send", handle_args);
  server.onNotFound(handle_NotFound);
  server.begin();
  Serial.println("HTTP server started"); 
  bath.setState("ready");
}


int  buttonPin = BUTTON;    // кнопка на выводе 2
int ledPin = LED;       // светодиод на выводе 13
int buttonPushCounter = 0;   // счётчик нажатия кнопки
int buttonState = 0;         // текущее состояние кнопки
int lastButtonState = 0;     // предыдущее состояние кнопки


void loop() {
  server.handleClient();
  bath.update();
  temp();
  //button();
}

void temp(){
    bath.setTemp(random(0, 100));


    return;
    //считываем напряжение датчика
    int reading = analogRead(0); /*аналоговый ввод для выхода датчика TMP36
    разрешение 10 мВ / градус цельсия со смещением на 500 мВ 
    для отрицательной температуры*/
    // преобразуем показания в напряжение, для 3.3v используйте значение 3.3
    float voltage = reading * 5.0;
    voltage /= 1024.0;
    // выводим напряжение
    //Serial.print(voltage);
    //Serial.println(" volts");
    // теперь выводим температуру
    float temperatureC = (voltage - 0.5) * 100 ; //исходя из 10 мВ на градус со смещением 500 мВ
    bath.setTemp(temperatureC);
    //Serial.print(temperatureC);
    //Serial.println(" degrees C");
  }

void button(){
    // считываем показания с вывода кнопки
  buttonState = digitalRead(buttonPin);
  // сравниваем состояние с предыдущим состоянием
  if (buttonState != lastButtonState) {
    // если состояние изменилось, увеличиваем счётчик
    if (buttonState == HIGH) {
      // если текущее состояние HIGH, значит кнопка включена
      buttonPushCounter++;
      bath.stop();
    // небольшая задержка для устранения эффекта дребезга
    delay(50);
    }
  }
  // сохраняем текущее состояние как последнее состояние для следующего раза
  lastButtonState = buttonState;
  // включаем светодиод при каждом втором нажатии, проверяя деление по остатку счётчика нажатий
  if (buttonPushCounter % 2 == 0) {
    digitalWrite(ledPin, HIGH);
  } else {
    digitalWrite(ledPin, LOW);
  }
}

void handle_args(){
 String command = "";
  for(int i=0; i<server.args(); i++){
    command = server.arg(i); 
  }

  if((command.substring(0, command.indexOf(','))).equals("connection")){
    server.send(200, "text/html", "" + command);
//    Serial.println(command);
  } else {
    readMessage(command);  
  }
}

void readMessage(String str){
  //Serial.println(str);
  int n=0;

  String command = str.substring(0, str.indexOf(','));
  String args = str.substring(str.indexOf(',')+1, str.length());

  if(command.equals("state")) {
    server.send(200, "text/html", "state," + bath.getState() + "," + String(bath.getTemp()));
  }
  if(command.equals("start")){
    if(bath.getState().equals("ready")){
      bath.start();
      server.send(200, "text/html", "start,"+bath.getState());
    }else{
      server.send(404, "text/html", "start,"+bath.getState());
    } 
  }

  if(command.equals("stop")){
    bath.stop();
    return;
  }

  if(command.equals("setTime0")) {
    long milliSecond = args.toInt()*1000;
    VENT vent = VENT(milliSecond, VENT1);
    bath.setVent(0, vent);
    return;
  } 
  
  if(command.equals("setTime1")) {
    long milliSecond = args.toInt()*1000;
    VENT vent = VENT(milliSecond, VENT2);
    bath.setVent(1, vent); 
    return;
  }

  if(command.equals("getTime")) {
    long second = bath.getVent(args.toInt()).getLeftTime()/1000;
    server.send(200, "text/html", "getTime,"+String(second));
    return;
  }
}


void handle_OnConnect() { 
  Serial.print("GPIO7 Status: ");
  server.send(200, "text/html", SendHTML()); 
}

void handle_NotFound()
{
  server.send(404, "text/plain", "Not found");
}

String SendHTML()
{
  String ptr = "<!DOCTYPE html> <html>\n";
  ptr +="<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">\n";
  ptr +="<title>LED Control</title>\n";
  ptr +="<style>html { font-family: Helvetica; display: inline-block; margin: 0px auto; text-align: center;}\n";
  ptr +="body{margin-top: 50px;} h1 {color: #444444;margin: 50px auto 30px;} h3 {color: #444444;margin-bottom: 50px;}\n";
  ptr +=".button {display: block;width: 80px;background-color: #1abc9c;border: none;color: white;padding: 13px 30px;text-decoration: none;font-size: 25px;margin: 0px auto 35px;cursor: pointer;border-radius: 4px;}\n";
  ptr +=".button-on {background-color: #1abc9c;}\n";
  ptr +=".button-on:active {background-color: #16a085;}\n";
  ptr +=".button-off {background-color: #34495e;}\n";
  ptr +=".button-off:active {background-color: #2c3e50;}\n";
  ptr +="p {font-size: 14px;color: #888;margin-bottom: 10px;}\n";
  ptr +="</style>\n";
  ptr +="</head>\n";
  ptr +="<body>\n";
  ptr +="<h1>ESP8266 Web Server</h1>\n";
  ptr +="<h3>Using Access Point(AP) Mode</h3>\n";
  ptr +="</body>\n";
  ptr +="</html>\n";
  return ptr;
}
