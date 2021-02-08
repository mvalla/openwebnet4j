# Changelog


## [0.4.0-SNAPSHOT] - 2021-02-??

### Added
- Initial support for Energy Manager (WHO=18)

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
