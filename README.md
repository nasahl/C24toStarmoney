# C24toStarmoney

## Verwendung
      Usage: csv2camt [-k <konfigurationsdatei>] <invput-csv-file>
                
Das Programm konvertiert eine aus der C24 Bank Weboberfläche heruntergeladene CSV Datei in ein CAMT Format, 
um diese dann in Starmoney wieder einzulesen.

Beim ersten Start wird eine Konfigurationedatei angelegt, die nach den eignenen Bedürfnissen anzupassen ist.  
Die Angabe des Namens einer Konfigurationsdatei ist optional.
Bei der Nutzung mehrere Konten (Pockets) kann die Stapelverarbeitung des PowerShell Skriptes (PS1) hilfreich sein.
                

## Workflow
Der Workflow ist folgendermassen:
1. Herunterladen einer CSV Datei aus der C24 Bank Weboberfläche
2. Ausführen dieses Programms nach Anpassung der Konfigurationsdatei
3. Öffnen von Starmoney (mein Test lief mit Version 14)
4. Verwaltung -> Datenimport -> Importdaten: Import
   - Ziel: Auswählen des Kontos in das importiert wird
   - Format: Umsätze in camt-Format 053.001.08  
   - jetzt Importieren drücken und und auswählen der in 2 erzeugten CAMT Datei

  
ALTERNATIVE ZU 2. 
Ist die Verwendung der Stapelverarbeitungsfunktion des PS1-Skripts:

- Wird das PS1-Skript ohne Argumente gestartet, wird die Stapelverarbeitung zum Erzeugen mehrerer XML-Dateien ausgefuehrt.
- Für jede CSV-Datei wird eine eigene "properties.txt" benötigt.
- Beide Dateien müssen mit exakt derselben Nummer beginnen, gefolgt von einem nicht-numerischen Zeichen.
  Beispiel: 123_KontoName.csv und 123_KontoName-properties.txt (z.B. die letzten 3 Ziffern der Kontonummer).
- Wird das PS1-Skript ohne Argumente gestartet, werden zu jedem passenden .csv/.txt-Paar entsprechende XML-Dateien erzeugt.
- Existiert bereits eine XML-Datei mit der jeweiligen Nummer, wird diese übersprungen.

ACHTUNG:
- Die Nummern in den Dateinamen müssen exakt übereinstimmen!
- Bereits vorhandene XML-Dateien werden NICHT überschrieben.


## Ausführbare Executable

Im Verzeichnis `distribution` muss das `bin` und `lib` Verzeichnis heruntergeladen werden.  
Im `bin` Verzeichnis stehen Dateien für die Ausführung unter Windows und Linux

Voraussetzung für die Ausführung ist eine Installation von Java 17 oder höher.
