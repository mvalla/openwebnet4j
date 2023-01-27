# Changelog

## [0.10.0] - 2023-xx-xx

### Changed
- updated copyright year


## [0.9.1] - 2022-11-04

### Fixed
- WhereAlarm for Alarm system messages with null where (e.g. *5*6*##)


## [0.9.0] - 2022-10-01

### Added
- Initial support for Basic Scenarios (WHO=0)


## [0.8.1] - 2022-03-24

### Changed
- Thermo: extended OperationMode enumeration (added link between "complex" OperationMode and "base" mode)


## [0.8.0] - 2022-03-18

### Added
- Support for Burglar Alarm (WHO=5)
- checks to Where base class constructor
- getWhatParams()

### Changed
- DEPRECATED getCommandParams()


## [0.7.1] - 2022-01-14

### Fixed
- [zigbee] Fixes #27: shutter status request blocking USB processing in older gateways

### Added
- Support for AUX discovery
- Thermo: support for LocalOffset
- Thermo: support for WEEKLY and SCENARIO

### Changed
- [zigbee] improved error handling
- code cleanup
- moved to Log4j-2 binding for SLF4J
- Thermo: refactored WhatThermo, isStandAlone param


## [0.7.0] - 2021-11-12

### Added
- Support for AUX (WHO=9)


## [0.6.0] - 2021-10-16

### Added
- Support for CEN and CEN+ (WHO=15/25)


## [0.5.3] - 2021-06-14

### Changed
- CamelCase for all enums
- new method name requestActuatorsStatus
- renamed to SCS_THERMO_SENSOR and SCS_THERMO_ZONE

### Added
- Thermo: support for PROGRAM and HOLIDAY


## [0.5.2] - 2021-05-28

### Changed
- CamelCase for enums in Thermo message


## [0.5.1] - 2021-05-26

### Changed
- updated VALVE_OR_ACTUATOR_STATUS (14, 15, 16)


## [0.5.0] - 2021-05-23

### Fixed
- Maven is unable to find javadoc command
- ignored unsupported frames in response instead of returning error

### Added
- Initial support for Thermoregulation (WHO=4) with WereThermo, Thermoregulation and ThermoregulationDigagnostic classes
NOTE for this release only Thermostats in standalone installations have been tested

### Changed
- project description
- removed .gitignore
- removed test-jar causing bundle conflict


## [0.4.1] - 2021-03-27

### Fixed
- OpenConnector.sendCommandSynch is now synchronized to avoid cmd fails while opening a new CMD connection (fixes #12)

### Changed
- update groupId and POM for publication to Maven Central via OSSRH (fixes #13)


## [0.4.0] - 2021-02-17

### Added
- Initial support for Energy Management (WHO=18)
- Spotless check and JavaDoc maven plugins

### Fixed
- Add support for OPEN password nonce with any number of digits (fixes #1 again)

### Changed
- const to FORMAT_DIMENSION_REQUEST and FORMAT_DIMENSION_WRITING_1P_1V
- const SCS_ENERGY_CENTRAL_UNIT to SCS_ENERGY_METER


## [0.3.4] - 2021-01-07

### Fixed
- [zigbee] Disconnect serial port if USB dongle returns NACK during connection (fixes #7)
- [zigbee] Fixed (again) fixInvertedUpDownBug() not converting commands
- removed warn logs when reconnecting command connection

### Changed
- renamed dimmer levels in Lighting WHAT enum


## [0.3.3] - 2021-01-02

### Fixed
- [zigbee] Fixed fixInvertedUpDownBug() not converting commands

### Added
- [zigbee] Handling of disconnection/reconnection of ZigBee USB Gateway from USB port
- Frame length and bad char checks to BaseOpenMessage
- added isCurrentlyOwned()/is serial port checks in USBConnector

### Changed
- Bumped nrjavaserial to 5.2.1


## [0.3.2-1] - 2020-11-21

### Fixed
- Import-Package in bundle manifest to accept gnu.io versions [3.12,6)
- Updated dependencies versions

## [0.3.2] - 2020-11-17

### Added
- `message`, `message.event`, `keepalive`, and `handshake` sub-logs in BUSConnector
- checkFirmwareVersion() check to USBConnector
- [zigbee] fixInvertedUpDownBug() to invert UP/DOWN for older ZigBee USB gateways

### Fixed
- [zigbee] Response to device info 2-UNITS for buggy older ZigBee USB gateways
- [zigbee] Discovery of 2-UNITS Zigbee switch modules
- Improved message parsing for late parsing and sub-parts and related tests; changed isCommand() to abstract
- [zigbee] Improved USBConnector for concurrent events and request/response interleaving


## [0.3.1] - 2020-10-03

### Added
- GatewayManagement.requestModel()
- Detection of green switch (WHAT 34/39). Limit dimmers detection to WHAT=2-10 values.

### Fixed
- Add support for OPEN password nonce with less than 8 digits (fixes #1)
- Added check to verify gw is still reachable after MON rcv timetout expires before closing MON connection


## [0.3.0] - 2020-09-09

### Added
- Support for WHO=2 Automation
- OpenConnector.getLastCmdFrameSentTs() and OpenGateway.isCmdConnectionReady()


## [0.2.0] - 2020-08-07

### Added
- Support for numeric (OPEN) and alphanumeric (HMAC) passwords

### Changed
- Changed monKeepaliveTimer to schedule to avoid MON message flood after standby


## [0.1.0] - 2020-07-11

First public version of openwebnet4j
