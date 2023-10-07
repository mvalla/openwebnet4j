# openwebnet4j

**openwebnet4j** is a Java library for the [Open Web Net](https://developer.legrand.com/documentation/open-web-net-for-myhome/) protocol.

It enables a Java client to communicate locally with a gateway supporting the **Open Web Net** protocol and to control devices in a **BTicino/Legrand BUS/SCS system** ([MyHOME](https://www.bticino.com/products-catalogue/myhome_up-simple-home-automation-system/) &reg;) or **ZigBee wireless system** ([MyHOME_Play](https://www.homesystems-legrandgroup.com/myhomeswupdate/MyHomePlay/BT-CASA_CONNESSA_LUCI_E_TAPPARELLE.pdf) &reg;, now out of production).

Supported features:

* Device discovery
* Feedback (monitoring events) from BUS/SCS and ZigBee wireless network

Supported frames:

* `WHO=0` Scenarios
* `WHO=1` Lighting
* `WHO=2` Automation
* `WHO=4` Thermoregulation
* `WHO=5`  Alarm 
* `WHO=9`  Auxiliary 
* `WHO=13` Gateway Management
* `WHO=15 & 25` CEN/CEN+ scenarios
* `WHO=18` Energy Management

Supported Open Web Net gateways:
- **IP gateways** or scenario programmers, such as: BTicino F453 / [F454](https://catalogue.bticino.com/product/smart-home-solutions/my-home---home-automation-system/integration-and-control/BTI-F454-EN) / F455, [MyHOMEServer1](https://catalogue.bticino.com/product/smart-home-solutions/my-home---home-automation-system/integration-and-control/BTI-MYHOMESERVER1-EN),  MyHOME_Screen10, MH201 / MH202 / MH200N
- **ZigBee USB Gateways**, such as: [BTicino 3578](https://catalogo.bticino.it/low_res/395950_501016_MQ00493_b_IT.pdf), also known as Legrand 088328, to connect to wireless devices

### TODO

- [ ] add sendHighPriority with priority queue
- [ ] extend OpenConnector.listener to multiple listeners

## Dependency Management

### Maven

This library is available via Maven Central repository by adding the dependency in your POM.xml:

```xml   
    <dependency>
      <groupId>io.github.openwebnet4j</groupId>
      <artifactId>openwebnet4j</artifactId>
      <version>0.11.0</version>
    </dependency>
```

## Usage example
```java
// create BUS gateway connection with IP=192.168.1.50 and password=12345
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
- The Open Web Net protocol is maintained and Copyright by BTicino/Legrand. The documentation of the protocol if freely accessible for developers on the [Legrand developer web site](https://developer.legrand.com/local-interoperability/#PDF%20documentation)
- "Open Web Net", "SCS", "MyHOME_Up", "MyHOME", "MyHOME_Play" and "Living Now" are registered trademarks by BTicino/Legrand
