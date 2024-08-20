# C24toStarmoney

## Verwendung
      Usage: csv2camt [-k <konfigurationsdatei>] <invput-csv-file>
                
Das Programm konvertiert eine aus der C24 Bank Weboberfläche heruntergeladene CSV Datei in ein CAMT Format, 
um diese dann in Starmoney wieder einzulesen.

Beim ersten Start wird eine Konfigurationedatei angelegt, die nach den konkreten Bedürfnissen anzupassen sind.  
Die Angabe des Namens einer Konfigurationsdatei ist optional.
                

## Workflow
Der Workflow ist folgendermassen:
1. Herunterladen einer CSV Datei aus der C24 Bank Weboberfläche
2. Ausführen dieses Programms nach Anpassung der Konfigurationsdatei
3. Öffnen von Starmoney (mein Test lief mit Version 14)
4. Verwaltung -> Datenimport -> Importdaten: Import
   - Ziel: Auswählen des Kontos in das importiert wird
   - Format: Umsätze in camt-Format 053.001.08  
   - jetzt Importieren drücken und und auswählen der in 2 erzeugten CAMT Datei

## Ausführbare Executable

Im Verzeichnis `distribution` muss das `bin` und `lib` Verzeichnis heruntergeladen werden.  
Im `bin` Verzeichnis stehen Dateien für die Ausführung unter Windows und Linux

Voraussetzung für die Ausführung ist eine Installation von Java 17 oder höher.