# Changelog

## [0.3.2] - 2020-10-xxTODO

### Added
- `message`, `message.event`, `keepalive`, and `handshake` sub-logs for BUSConnector
- checkFirmwareVersion() check to USBConnector
- fixInvertedUpDownBug() to invert UP/DOWN for older USB gateways

### Fixed
- Response to device info 2-UNITS for buggy ZigBee gateways

## [0.3.1] - 2020-10-03

### Added
- GatewayManagement.requestModel()
- detection of green switch (WHAT 34/39). Limit dimmers detection to WHAT=2-10 values.

### Fixed
- add support for OPEN password nonce with less than 8 digits [Fixes #1]
- added check to verify gw is still reachable after MON rcv timetout expires before closing MON connection


## [0.3.0] - 2020-09-09

### Added
- Support for WHO=2 Automation
- OpenConnector.getLastCmdFrameSentTs() and OpenGateway.isCmdConnectionReady()


## [0.2.0] - 2020-08-07

### Added
- Support for numeric (OPEN) and alphanumeric (HMAC) passwords

### Changed
- changed monKeepaliveTimer to schedule to avoid MON message flood after standby


## [0.1.0] - 2020-07-11

First public version of openwebnet4j