# XWVassal 
(X-Wing Vassal module and Web page for the X-Wing Vassal league in /docs/)

## Distance and geometry specs:

Plastic Bases
* Small base ship: 113 px² 
* Large base ship: 226 px²
* GR-75 / Gozanti / C-ROC Huge ship:  226 px by 551 px
* CR90 / Raider / ? Huge ship: 226 px by 635 px

Ranges
* Range 1 =  282.5 px
* Range 2 =  565.0 px
* Range 3 = 847.5 px
* Range 4 = 1130.0 px
* Range 5 = 1412.5 px

**1st edition**

Arc Angles:
* Small ship forward arc: 80.90 degrees
* Large ship forward arc: 84.05 degrees
* Large ship stardboard/port mobile turret arc: 95.95 degrees

(to help locate the coordinates of the forward arc's intersection to the forward edge)
The arc lines didn't quite go to the cardboard chit corner
* Small ship corner to intersection of forward arc to front edge: 8.3296 px
* Large ship corner to intersection of forward arc to front edge: 11.1650 px

**2nd edition**

*basic assumption: plastic bases are perfect squares.*

Total Sizes:
* Small  = 113 px / 40.00 mm 
* Medium = 171 px / 60.50 mm
* Large  = 226 px / 80.00 mm

Cardboard Base:
* All Chit Height = Plastic Base Height
* Small Chit Width = 85.77% of Plastic Width / 34.31 mm / 94.52 px
* Medium Chit Width = 88.16 % of Plastic Width / 53.35 mm / 150.75 px
* Large Chit Width = 89.29% of Plastic Width / 71.43 mm / 201.79 px

Bullseye Arc Width:
* Small Bullseye Arc = 36.97% of Plastic Width / 14.79 mm / 41.78 px
* Medium Bullseye Arc = 24.65% of Plastic Width / 14.79 mm / 41.78 px
* Large Bullseye Arc = 18.49% of Plastic Width / 14.79 mm / 41.78 px

Bullseye Arc Position:
* Small Plastic Base Corner to Bullseye Arc Start: ??% of Plastic Width / ?? mm / 35.61 px
* Medium Plastic Base Corner to Bullseye Arc Start: ??% of Plastic Width / 18.64 mm / 64.61 px
* Large Plastic Base Corner to Bullseye Arc Start: ??% of Plastic Width / ?? mm / 92.11 px

Arc Angle:
* Small ship forward arc: 81.24 degrees
* Medium ship forward arc: 82.80 degrees
* Large ship forward arc: 83.52 degrees


The arc lines definitely go to the cardboard chit corner
* Small ship cardboard chit width: 98.670 px
* Small ship corner of plastic to corner of cardboard: 7.165 px
* Medium ship cardboard chit width: 150.918 px
* Medium ship corner of plastic to corner of cardboard: ?? px
* Large ship cardboard chit width: ?? px
* Large ship corner of plastic to corner of cardboard: ?? px

## Updating the module
*The current way via the autoupdater is using this repository:*
https://github.com/Mu0n/XWVassalOTA

*The following is via using the vassal editor*
### Adding pilot and upgrade cards
1. Vassal editor
2. gradlew unpackVmod
3. gradlew downloadXwingData
4. push

### Adding new ships with dials
1. Vassal editor, create a ship-specific Protytpe for its actions, await Radarman5's ship art and combine it in a deep-layered photoshop file  (save as png), create a new ship-specific tab in the Pieces window, create its dial
2. create ordered, open dial+strip for the new ship here: http://xwvassal.info/dialgen/dialgen
3. gradlew unpackVmod
4. gradlew downloadXwingData
5. push

*The following is via using intellij*
### Adding code
1. Your IDE of choice
2. gradlew downloadXwingData
3. gradlew buildVmod
4. push

### Adding an importable class
1. Add it to the XWCounterFactory
2. gradlew buildVmod
3. Import through the Vassal editor
4. gradlew unpackVmod
