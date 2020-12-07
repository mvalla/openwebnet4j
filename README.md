# openwebnet4j

**openwebnet4j** is a Java library for the [Open Web Net](https://developer.legrand.com/documentation/open-web-net-for-myhome/) protocol.

It enables a Java client to communicate locally with a gateway supporting the Open Web Net protocol and to control devices in a BTicino/Legrand  BUS/SCS system ([MyHOME_Up](https://www.bticino.com/products-catalogue/myhome_up-simple-home-automation-system/) &reg;) or ZigBee wireless system ([MyHOME_Play](http://www.homesystems-legrandgroup.com/BtHomeSystems/productDetail.action?lang=EN&productId=061) &reg;, now out of production).

Supported features:

* Device discovery
* Feedback (monitoring events) from BUS/SCS and ZigBee wireless network 

Supported frames:

* `WHO=1` Lighting
* `WHO=2` Automation
* `WHO=13` Gateway Management

Supported Open Web Net gateways:
- *IP gateways* or scenario programmers, such as: BTicino F453 / F454 / F455, MyHOMEServer1,  MyHOME_Screen10, MH201 / MH202 / MH200N 
- *ZigBee USB Gateways*, such as: [BTicino 3578](https://catalogo.bticino.it/BTI-3578-IT), also known as Legrand 088328, to connect to wireless devices

### TODO

- [x] Support numeric passwords (OPEN handshake)
- [x] Support alphanumeric passwords (HMAC handshake)
- [x] Support for `WHO=2` Automation (shutters)
- [x] ZigBee: check isOldFirmware and related gw bugfixes
- Add other `WHOs`
    - [ ] Energy
    - [ ] Thermo
    - [ ] CEN/CEN+
    - [ ] AUX
- [ ] add sendHighPriority with priority queue
- [ ] extend OpenConnector.listener to multiple listeners

## Dependency Management

### Maven
```xml   
    <dependency>
      <groupId>com.github.openwebnet4j</groupId>
      <artifactId>openwebnet4j</artifactId>
      <version>0.3.2</version>
    </dependency>
```

## Usage example
```java
// create BUS gateway with IP=192.168.1.50 and password=12345
BUSGateway myGateway = new BUSGateway("192.168.1.50", 20000, "12345");
myGateway.subscribe(this);
try {
    myGateway.connect();
    // turns light WHERE=51 ON
    Response res = myGateway.send(Lighting.requestTurnOn("51"));
    if (res.isSuccess()) {
        System.out.println("Request successful");
    }
    // requests status light WHERE=51
    myGateway.send(Lighting.requestStatus("51"));
} catch (OWNException e) {...}
```

## Building from Source

With Maven:

```
mvn clean install
```

## Disclaimer
- This library is not associated by any means with BTicino or Legrand companies
- The Open Web Net protocol is maintained and Copyright by BTicino/Legrand. The documentation of the protocol if freely accessible for developers on the [Legrand developer web site](https://developer.legrand.com/documentation/open-web-net-for-myhome/)
- "Open Web Net", "MyHOME_Up", "MyHome", "MyHome_Play" and "Living Now" are registered trademarks by BTicino/Legrand


