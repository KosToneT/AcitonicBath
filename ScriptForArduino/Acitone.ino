#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

/* Установите здесь свои SSID и пароль */
/* Установите здесь свои SSID и пароль */
const char* ssid = "NodeMCU";       // SSID
const char* password = "12345678"; 

/* Настройки IP адреса */
IPAddress local_ip(192,168,1,1);
IPAddress gateway(192,168,1,1);
IPAddress subnet(255,255,255,0);

ESP8266WebServer server(80);



int TimeFirstVentilator;
int TimeSecondVentilator;
boolean first, second;

unsigned long timing;

#define VENT1 D0
#define VENT2 D5

void setup() {
  pinMode(VENT2, OUTPUT); 
  pinMode(VENT1, OUTPUT); 
  Serial.begin(115200);
  WiFi.softAP(ssid, password, 1, false, 8);
  //WiFi.softAP(ssid, password);
  //WiFi.softAPConfig(local_ip, gateway, subnet);
  delay(100);

  Serial.print("Soft-AP IP address = ");
  Serial.println(WiFi.softAPIP());
  
  server.on("/", handle_OnConnect);
  server.on("/send",handle_args);
  server.onNotFound(handle_NotFound);
  server.begin();
  Serial.println("HTTP server started");
  
}
void loop() {
  server.handleClient(); 
   if (millis() - timing> TimeFirstVentilator * 1000 && first){
    timing = millis();
    digitalWrite(VENT1, LOW);
    digitalWrite(VENT2, HIGH);
        first = !first;
        second = true;
  }
  
  if (millis() - timing > TimeSecondVentilator * 1000 && second){
    timing = millis();
    digitalWrite(VENT2, LOW);
        second = !second;
  }
}
void handle_args(){
 String command = "";
  for(int i=0; i<server.args(); i++){
    command = server.arg(i); 
    
  }
  readMessage(command);
  server.send(200, "text/html", "send success"+ command); 
}



void readMessage(String str){
  Serial.println(str);
  str = str.substring(1, str.length()-1);
  Serial.println(str);
  int n=0;

  String command = str.substring(0, str.indexOf(','));
    str = str.substring(str.indexOf(','), str.length());
    n = str.lastIndexOf(',');

  if(command.equals("stop")){
    digitalWrite(VENT1, LOW);
    digitalWrite(VENT2, LOW);
    return;
  }
  
  if(command.equals("start")){
    String a, b;
    //Узнаем время работы первого вентилятора
        a = str.substring(str.indexOf(',')+1, str.lastIndexOf(','));
    TimeFirstVentilator = a.toInt();
        first = true;
    //Узнаем время работы второго вентилятора
    b = str.substring(n+1, str.length());
    TimeSecondVentilator = b.toInt();
    digitalWrite(VENT1, HIGH);
    Serial.println(a);
    Serial.println(b);
    timing = millis();
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
