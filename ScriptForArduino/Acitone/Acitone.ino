#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

/**
██████╗░███████╗██╗░░░██╗███████╗██╗░░░░░░█████╗░██████╗░███╗░░░███╗███████╗███╗░░██╗████████╗░░░██████╗░██╗░░░██╗
██╔══██╗██╔════╝██║░░░██║██╔════╝██║░░░░░██╔══██╗██╔══██╗████╗░████║██╔════╝████╗░██║╚══██╔══╝░░░██╔══██╗╚██╗░██╔╝
██║░░██║█████╗░░╚██╗░██╔╝█████╗░░██║░░░░░██║░░██║██████╔╝██╔████╔██║█████╗░░██╔██╗██║░░░██║░░░░░░██████╦╝░╚████╔╝░
██║░░██║██╔══╝░░░╚████╔╝░██╔══╝░░██║░░░░░██║░░██║██╔═══╝░██║╚██╔╝██║██╔══╝░░██║╚████║░░░██║░░░░░░██╔══██╗░░╚██╔╝░░
██████╔╝███████╗░░╚██╔╝░░███████╗███████╗╚█████╔╝██║░░░░░██║░╚═╝░██║███████╗██║░╚███║░░░██║░░░██╗██████╦╝░░░██║░░░
╚═════╝░╚══════╝░░░╚═╝░░░╚══════╝╚══════╝░╚════╝░╚═╝░░░░░╚═╝░░░░░╚═╝╚══════╝╚═╝░░╚══╝░░░╚═╝░░░╚═╝╚═════╝░░░░╚═╝░░░

██╗░░██╗░█████╗░░██████╗████████╗░█████╗░███╗░░██╗███████╗████████╗
██║░██╔╝██╔══██╗██╔════╝╚══██╔══╝██╔══██╗████╗░██║██╔════╝╚══██╔══╝
█████═╝░██║░░██║╚█████╗░░░░██║░░░██║░░██║██╔██╗██║█████╗░░░░░██║░░░
██╔═██╗░██║░░██║░╚═══██╗░░░██║░░░██║░░██║██║╚████║██╔══╝░░░░░██║░░░
██║░╚██╗╚█████╔╝██████╔╝░░░██║░░░╚█████╔╝██║░╚███║███████╗░░░██║░░░
╚═╝░░╚═╝░╚════╝░╚═════╝░░░░╚═╝░░░░╚════╝░╚═╝░░╚══╝╚══════╝░░░╚═╝░░░

░██████╗██╗░░██╗██╗██████╗░██████╗░░█████╗░░██████╗░██╗░░░██╗░░░░░░░░█████╗░░█████╗░░█████╗░░░██╗██╗██████╗░
██╔════╝██║░██╔╝██║██╔══██╗██╔══██╗██╔══██╗██╔════╝░██║░░░██║░░██╗░░██╔══██╗██╔══██╗██╔══██╗░██╔╝██║██╔══██╗
╚█████╗░█████═╝░██║██████╔╝██║░░██║███████║██║░░██╗░██║░░░██║██████╗██║░░██║██║░░██║██║░░╚═╝██╔╝░██║██║░░██║
░╚═══██╗██╔═██╗░██║██╔═══╝░██║░░██║██╔══██║██║░░╚██╗██║░░░██║╚═██╔═╝██║░░██║██║░░██║██║░░██╗███████║██║░░██║
██████╔╝██║░╚██╗██║██║░░░░░██████╔╝██║░░██║╚██████╔╝╚██████╔╝░░╚═╝░░╚█████╔╝╚█████╔╝╚█████╔╝╚════██║██████╔╝
╚═════╝░╚═╝░░╚═╝╚═╝╚═╝░░░░░╚═════╝░╚═╝░░╚═╝░╚═════╝░░╚═════╝░░░░░░░░░╚════╝░░╚════╝░░╚════╝░░░░░░╚═╝╚═════╝░

██╗░░░░░██╗███╗░░░███╗██████╗░░█████╗░██╗░░██╗
██║░░░░░██║████╗░████║██╔══██╗██╔══██╗██║░██╔╝
██║░░░░░██║██╔████╔██║██████╔╝███████║█████═╝░
██║░░░░░██║██║╚██╔╝██║██╔══██╗██╔══██║██╔═██╗░
███████╗██║██║░╚═╝░██║██║░░██║██║░░██║██║░╚██╗
╚══════╝╚═╝╚═╝░░░░░╚═╝╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚═╝

██████╗░██╗░░░██╗████████╗████████╗██████╗░░█████╗░░█████╗░██╗░░██╗░█████╗░
██╔══██╗██║░░░██║╚══██╔══╝╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗██║░░██║██╔══██╗
██████╦╝██║░░░██║░░░██║░░░░░░██║░░░██████╔╝██║░░██║██║░░██║███████║███████║
██╔══██╗██║░░░██║░░░██║░░░░░░██║░░░██╔══██╗██║░░██║██║░░██║██╔══██║██╔══██║
██████╦╝╚██████╔╝░░░██║░░░░░░██║░░░██║░░██║╚█████╔╝╚█████╔╝██║░░██║██║░░██║
╚═════╝░░╚═════╝░░░░╚═╝░░░░░░╚═╝░░░╚═╝░░╚═╝░╚════╝░░╚════╝░╚═╝░░╚═╝╚═╝░░╚═╝ 
 */

/* Установите здесь свои SSID и пароль */
const char ssid[] = "AcitonicBath";
const char password[] = "12345678";

ESP8266WebServer server(80);

#define VENT1 D0//Куллер 1 на выводе 0
#define VENT2 D5//Куллер 2 на выводе 5
#define BUTTON D1// кнопка на выводе 1
#define SENSORPIN D2//Датчик на выводе 2
#define HEATINGPIN D3//Нагревательный элемент на выводе 3
/**
 * @brief class vent simulate cooleer
 * 
 */
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
  /**
   * @brief Get the Working Time object
   * 
   * @return long 
   */
  long getWorkingTime(){
    return workingTime;
  }
  /**
   * @brief Get the Left Time object
   * 
   * @return long 
   */
  long getLeftTime(){
    return workingTime - getPastTime();  
  }
  /**
   * @brief Get the Past Time object
   * 
   * @return long 
   */
  long getPastTime(){
    long currentTime = millis();
    if(startTime==0){
      return 0;
    }
    return (currentTime - startTime);
  }
  /**
   * @brief Set the Start Time object
   * 
   * @param time 
   */
  void setStartTime(long time){
    startTime = time;
  }
  /**
   * @brief Get the Start Time object
   * 
   * @return long 
   */
  long getStartTime(){
    return startTime;
  }
  /**
   * @brief staring vent and start time
   * 
   */
  void start(){
    startTime = millis();
    on();
  }
  /**
   * @brief stop vent and off
   * 
   */
  void stop(){
    off();
  }
private:
  /**
   * @brief vent off
   * 
   */
  void off(){
    digitalWrite(id, LOW);
  }
  /**
   * @brief vent on with plate
   * 
   */
  void on(){
    digitalWrite(id, HIGH);
  }
};
/**
 * @brief regulate tempurature in bath
 */
class AutoTemp{
  float needTemp = 50.0;// ограничитель температуры +-5
  float high;
  float low;
  bool FlagTemp = false;
  float nowTemp;
  void temp(){	
    //считываем напряжение датчика
    int reading = analogRead(SENSORPIN);
    // преобразуем показания в напряжение, для 3.3v используйте значение 3.3
    float voltage = reading * 5.0 / 1024.0;
    // теперь выводим температуру
    nowTemp = (voltage - 0.5) * 100 ; //исходя из 10 мВ на градус со смещением 500 мВ
    delay(1000); //ждем секунду
    // орграничители температуры в заданом диапазоне
    if (high <= nowTemp){
      digitalWrite(HEATINGPIN, LOW);
    }
    if (low >= nowTemp){
      digitalWrite(HEATINGPIN, HIGH);
    }  
  }
public:
/**
 * @brief Set the Need Temp object
 * 
 * @param needTemp 
 */
  void setNeedTemp(float needTemp){
    this->needTemp = needTemp;
    high = needTemp +5;
    low = needTemp -5;

  }
  /**
   * @brief Get the Now Temp object
   * 
   * @return float 
   */
  float getNowTemp(){
    return nowTemp;
  }
  /**
   * @brief Off auto temperature
   */
  void OffTemp(){
    FlagTemp = false;  
  	digitalWrite(3, LOW);
  }
  /**
   * @brief on auto temperature
   * 
   */
  void onTemp(){
    FlagTemp = true;
  }
  /**
   * @brief update every frame for control system
   * 
   */
  void update(){
    if (FlagTemp == true){
	    temp();
    }
  }
};
AutoTemp autoTemp = AutoTemp();
/**
 * @brief simulate bath 
 */
class Bath{
  float needTemp = 50;
  float temp = 0;
  bool connection;
  String state;
  String lastState;
  VENT vent0;
  VENT vent1;
public:
/**
 * @brief Set the State object
 * 
 * @param state 
 */
  void setState(String state){
    this->state = state;
  }
  /**
   * @brief Get the State object
   * 
   * @return String 
   */
  String getState(){
    return state;
  }
  /**
   * @brief Set the Need Temp object
   * 
   * @param temp 
   */
  void setNeedTemp(float temp){
    needTemp = temp;
    autoTemp.setNeedTemp(temp);
  }
  /**
   * @brief Set the Temp object
   * 
   * @param temp 
   */
  void setTemp(float temp){
       this->temp = temp;
  }
  /**
   * @brief Get the Temp object
   * 
   * @return float 
   */
  float getTemp(){
    return temp;
  }
  /**
   * @brief Set the Connection object
   * 
   * @param connection 
   */
  void setConnection(bool connection){
    this->connection = connection;   
  }
  /**
   * @brief Get the Connection object
   * 
   * @return true 
   * @return false 
   */
  bool getConnection(){
    return connection;
  }
  /**
   * @brief Set the Vent object
   * 
   * @param id 
   * @param vent 
   */
  void setVent(int id, VENT vent){
    if(id==0){
      vent0 = vent; 
    }else{
      vent1 = vent;
    }
  }
  /**
   * @brief Get the Vent object
   * 
   * @param id 
   * @return VENT 
   */
  VENT getVent(int id){
    if(id==0){
      return vent0;
    }else{
      return vent1;
    }
  }
  /**
   * @brief update state bath every frame
   * 
   */
  void update(){
      autoTemp.update();
      setTemp(autoTemp.getNowTemp());
      if(state.equals("temp")&&temp>needTemp){
        state = "time0";
        vent0.start();
        return;
      }

      if(vent0.getLeftTime()<=0 && state.equals("time0")){
        vent0.stop();
        vent1.start();
        state = "time1";
        return;
      }
      if(vent1.getLeftTime()<=0 && state.equals("time1")){
        stop();
        return;
      }
  }
/**
 * @brief start bath 
 * 
 */
  void start(){
      state = "temp";
      //Старт нагрев
      autoTemp.onTemp();
  }
  /**
 * @brief stop bath 
 * 
 */
  void stop(){
    vent0.stop();
    vent1.stop();
    state = "stop";
    vent0 = VENT();
    vent1 = VENT();
  }
};
Bath bath = Bath();
/**
 * @brief class button for simulate button
 */
class Button{
  int buttonPushCounter = 0;   // счётчик нажатия кнопки
  int buttonState = 0;         // текущее состояние кнопки
  int lastButtonState = 0;     // предыдущее состояние кнопки
public:
/**
 * @brief every frame check click
 * 
 */
  void update(){
    // считываем показания с вывода кнопки
    buttonState = digitalRead(BUTTON);
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
  }
};
Button button = Button();

void setup() {
  pinMode(VENT2, OUTPUT); 
  pinMode(VENT1, OUTPUT); 
  pinMode(BUTTON, INPUT); 
  pinMode(SENSORPIN, INPUT);

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

void loop() {
  server.handleClient();
  bath.update();
  button.update();
  
}

/**
 * @brief handle last arg from server
 */
void handle_args(){
 String command = "";
  for(int i=0; i<server.args(); i++){
    command = server.arg(i); 
  }
  if((command.substring(0, command.indexOf(','))).equals("connection")){
    server.send(200, "text/html", "" + command);
  } else {
    readCommand(command);  
  }
}
/**
 * @brief handle commands 
 * @param str commands in style({command,args})
 */
void readCommand(String str){
  int n=0;
  String command = str.substring(0, str.indexOf(','));
  String args = str.substring(str.indexOf(',')+1, str.length());
  if(command.equals("state")) {
    server.send(200, "text/html", "state," + bath.getState() + "," + String(bath.getTemp()));
    return;
  }
  if(command.equals("getTime")) {
    long second = bath.getVent(args.toInt()).getLeftTime()/1000;
    server.send(200, "text/html", "getTime,"+String(second));
    return;
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
  if(command.equals("setReady")){
    bath.stop();
    bath.setState("ready");
    return;
  }
  if(command.equals("setTemp")) {
    int temp = args.toInt();
    bath.setNeedTemp(temp);
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
}

void handle_OnConnect(){ 
  Serial.print("GPIO7 Status: ");
  server.send(200, "text/html", SendHTML()); 
}
void handle_NotFound(){
  server.send(404, "text/plain", "Not found");
}
String SendHTML() {
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
  ptr +="<h3>";
  ptr += "VENT1 = "+ String(bath.getVent(0).getLeftTime()/1000)+ " seconds\n";
  ptr += "VENT2 = "+ String(bath.getVent(1).getLeftTime()/1000)+ " seconds ";
  ptr += "</h3>\n";
  ptr +="</body>\n";
  ptr +="</html>\n";
  return ptr;
}
