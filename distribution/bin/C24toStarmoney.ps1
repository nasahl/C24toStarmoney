<#
.SYNOPSIS
    C24toStarmoney Startup-Skript fuer PowerShell v20250415-001

.DESCRIPTION
    Dieses Skript startet die C24toStarmoney-Anwendung.
#>

#$env:DEBUG = $true

#region --- Debug-Ausgabesteuerung ---
if (-not $env:DEBUG) {
    $VerbosePreference = 'SilentlyContinue'
}else {
    $VerbosePreference = "Continue"
}
#endregion


#region --- Variablen einrichten ---
Write-Verbose "Variablen werden eingerichtet"

$PSScriptDirectory = Split-Path -Path $MyInvocation.MyCommand.Path -Parent
Write-Verbose "Skriptverzeichnis: $($PSScriptDirectory)"

$APP_HOME = Resolve-Path (Join-Path -Path $PSScriptDirectory -ChildPath "..")
Write-Verbose "APP_HOME aufgeloest: $($APP_HOME)"

# Hier Standard-JVM-Optionen hinzufuegen. Sie koennen auch JAVA_OPTS und C24TO_STARMONEY_OPTS verwenden, um JVM-Optionen an dieses Skript zu uebergeben.
$DEFAULT_JVM_OPTS = ""
Write-Verbose "DEFAULT_JVM_OPTS: $($DEFAULT_JVM_OPTS)"
#endregion

#region --- Java finden ---
Write-Verbose "java.exe wird gesucht"

if ($env:JAVA_HOME) {
    Write-Verbose "JAVA_HOME ist definiert: $($env:JAVA_HOME)"
    $JAVA_HOME = $env:JAVA_HOME
    # Anfuehrungszeichen entfernen, falls in JAVA_HOME vorhanden (aehnlich wie im Batch-Skript)
    $JAVA_HOME = $JAVA_HOME.Replace('"', '')
    $JAVA_EXE = Join-Path -Path $JAVA_HOME -ChildPath "bin\java.exe"
    Write-Verbose "JAVA_EXE aus JAVA_HOME: $($JAVA_EXE)"

    if (!(Test-Path -Path $JAVA_EXE -PathType Leaf)) {
        Write-Error "FEHLER: JAVA_HOME ist auf ein ungueltiges Verzeichnis gesetzt: $($env:JAVA_HOME)"
        Write-Error "Bitte setzen Sie die JAVA_HOME-Variable in Ihrer Umgebung so, dass sie dem Speicherort Ihrer Java-Installation entspricht."
        return 1 # Mit Fehlercode beenden
    }
} else {
    Write-Verbose "JAVA_HOME ist nicht definiert, PATH wird ueberprueft"
    $JAVA_EXE = "java.exe"

    # Ueberpruefen, ob java.exe im PATH vorhanden ist, indem versucht wird, sie auszufuehren und auf Fehler zu pruefen
    try {
        Write-Verbose "Versuche auszufuehren: $($JAVA_EXE) -version"
        $null = & $JAVA_EXE -version 2>&1 # Fehlerstrom umleiten, um die Ausgabe zu unterdruecken
        Write-Verbose "java.exe im PATH gefunden"
    } catch {
        Write-Error "FEHLER: JAVA_HOME ist nicht gesetzt und kein 'java'-Befehl konnte in Ihrem PATH gefunden werden."
        Write-Error "Bitte setzen Sie die JAVA_HOME-Variable in Ihrer Umgebung so, dass sie dem Speicherort Ihrer Java-Installation entspricht."
        return 1 # Mit Fehlercode beenden
    }
}
#endregion

#region --- Klassenpfad einrichten ---
Write-Verbose "Klassenpfad wird eingerichtet"
$CLASSPATH = @(
    Join-Path -Path $APP_HOME -ChildPath "lib\C24toStarmoney-1.0.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlin-csv-jvm-1.10.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlinx-coroutines-core-jvm-1.5.2.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlin-stdlib-jdk8-1.9.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlin-stdlib-jdk7-1.9.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlin-stdlib-1.9.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\kotlin-stdlib-common-1.9.0.jar"
    Join-Path -Path $APP_HOME -ChildPath "lib\annotations-13.0.jar"
) -join ";"
Write-Verbose "KLASSENPFAD: $($CLASSPATH)"
#endregion

#region --- Funktionen ---
function Get-Help {
    Write-Host "Java-Hilfe anzeigen: "

    $JavaArguments = @(
        if ($DEFAULT_JVM_OPTS) { $DEFAULT_JVM_OPTS } else { $null }
        if ($env:JAVA_OPTS) { $env:JAVA_OPTS } else { $null }
        if ($env:C24TO_STARMONEY_OPTS) { $env:C24TO_STARMONEY_OPTS } else { $null }
        "-classpath"
        $CLASSPATH
        "de.nasahl.csv2camt.Application"
    )
    # Java-Anwendung zur Anzeige der Hilfeaufforderung ausfuehren
    & $JAVA_EXE $JavaArguments

    Write-Host ""
    Write-Host ""
    Write-Host "ALTERNATIVE ZU 2. Ist die Verwendung der Stapelverarbeitungsfunktion dieses PS1-Skripts:"
    #Write-Host "HINWEIS ZUR STAPELVERARBEITUNG"
    Write-Host ""
    Write-Host "- Wird das PS1-Skript ohne Argumente gestartet, wird die Stapelverarbeitung zum Erzeugen mehrerer XML-Dateien"
    Write-Host "    ausgefuehrt."
    Write-Host "- Fuer jede CSV-Datei wird eine eigene "properties.txt" benoetigt."
    Write-Host "- Beide Dateien muessen mit exakt derselben Nummer beginnen, gefolgt von einem nicht-numerischen Zeichen."
    Write-Host "    Beispiel: 123_KontoName.csv und 123_KontoName-properties.txt (z.B. die letzten 3 Ziffern der Kontonummer)."
    Write-Host "- Existiert bereits eine XML-Datei mit der jeweiligen Nummer, wird diese uebersprungen."
    Write-Host ""
    Write-Host "ACHTUNG:"
    Write-Host "- Die Nummern in den Dateinamen muessen exakt uebereinstimmen!"
    Write-Host "- Bereits vorhandene XML-Dateien werden NICHT ueberschrieben."

}

function Invoke-JavaScript {

    if ($args.Count -eq 0) {
        Write-Verbose "Keine Argumente angegeben, suche automatisch nach Dateien."

        $txtFiles = Get-ChildItem -Path $PSScriptDirectory -Filter "*.txt" | Where-Object {$_.Name -match "^[0-9]+.*\.txt$"}
        $csvFiles = Get-ChildItem -Path $PSScriptDirectory -Filter "*.csv" | Where-Object {$_.Name -match "^[0-9]+.*\.csv$"}
        $xmlFiles = Get-ChildItem -Path $PSScriptDirectory -Filter "*.xml" | Where-Object {$_.Name -match "^[0-9]+.*\.xml$"}

        if ($null -eq $txtFiles) {
            Get-Help
        } else {
            foreach ($txtFile in $txtFiles) {
                $numberPrefix = $txtFile.Name -replace "^([0-9]+).*$", '$1'

                # Pruefen auf mehrere TXT-Dateien mit demselben Nummernpraefix (und ueberspringen, falls zutreffend)
                $matchingTxtFiles = $txtFiles | Where-Object {$_.Name -like "$numberPrefix*.txt"}
                if ($matchingTxtFiles.Count -gt 1) {
                    Write-Warning "Mehrere .txt-Dateien gefunden, die mit der Nummer '$numberPrefix' beginnen. Diese Dateien werden uebersprungen."
                    foreach ($file in $matchingTxtFiles) {
                        Write-Warning "    Ueberspringe: $($file.FullName)"
                    }
                    continue # Zur naechsten txtFile springen
                }


                $xmlFile = $xmlFiles | Where-Object {$_.Name -like "$numberPrefix*.xml"}

                if ($xmlFile) {
                    Write-Host "Info: .xml-Datei fuer das Nummernpraefix '$numberPrefix' gefunden. Die Verarbeitung fuer diese Nummer wird uebersprungen."
                    Write-Host "    Ueberspringe aufgrund von XML: $($xmlFile.FullName)"
                    continue # Zur naechsten txtFile springen
                }

                $matchingCsvFiles = $csvFiles | Where-Object {$_.Name -like "$numberPrefix*.csv"}
                if ($matchingCsvFiles.Count -gt 1) {
                    $csvFile = $matchingCsvFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1
                    Write-Host "Info: Mehrere .csv-Dateien fuer das Nummernpraefix '$numberPrefix' gefunden. Die neueste wird verwendet."
                    foreach ($file in $matchingCsvFiles) {
                        if ($file -ne $csvFile) {
                            Write-Host "    Ignoriere aeltere CSV: $($file.FullName)"
                        }
                    }
                    Write-Host "    Verwende neueste CSV: $($csvFile.FullName)"
                } elseif ($matchingCsvFiles.Count -eq 1) {
                    $csvFile = $matchingCsvFiles[0]
                } else {
                    Write-Warning "Warnung: Keine .csv-Datei fuer die .txt-Datei '$($txtFile.Name)' mit dem Nummernpraefix '$numberPrefix' gefunden."
                    continue # Ueberspringen, wenn keine entsprechende CSV vorhanden ist
                }


                $JavaArguments = @(
                    if ($DEFAULT_JVM_OPTS) { $DEFAULT_JVM_OPTS } else { $null }
                    if ($env:JAVA_OPTS) { $env:JAVA_OPTS } else { $null }
                    if ($env:C24TO_STARMONEY_OPTS) { $env:C24TO_STARMONEY_OPTS } else { $null }
                    "-classpath"
                    $CLASSPATH
                    "de.nasahl.csv2camt.Application"
                    "-k"
                    $txtFile.FullName
                    $csvFile.FullName
                )

                Write-Verbose "Java-Befehl: $($JAVA_EXE) $($JavaArguments)"
                Write-Host "Verarbeite TXT: $($txtFile.Name) mit CSV: $($csvFile.Name)"
                # Java-Anwendung ausfuehren und Exit-Code erfassen
                & $JAVA_EXE $JavaArguments

                $EXIT_CODE = $LASTEXITCODE
                Write-Verbose "Exit-Code: $($EXIT_CODE)"

                #region --- Fehlerbehandlung und Beenden (pro Dateipaar) ---
                if ($EXIT_CODE -ne 0) {
                    Write-Error "Fehler beim Verarbeiten von TXT: $($txtFile.Name) und CSV: $($csvFile.Name) aufgetreten, Exit-Code ist $($EXIT_CODE)"
                    # Entscheiden Sie, ob die Verarbeitung bei einem Fehler gestoppt oder mit der naechsten Datei fortgefahren werden soll.
                    # Im Moment fahren wir mit der naechsten Datei fort und protokollieren den Fehler nur.
                } else {
                    Write-Host "TXT: $($txtFile.Name) und CSV: $($csvFile.Name) erfolgreich verarbeitet."
                }
                #endregion --- Fehlerbehandlung und Beenden (pro Dateipaar) ---


            }
        }
    } else {
        Write-Verbose "Argumente ueber die Befehlszeile angegeben."
        $JavaArguments = @(
            if ($DEFAULT_JVM_OPTS) { $DEFAULT_JVM_OPTS } else { $null }
            if ($env:JAVA_OPTS) { $env:JAVA_OPTS } else { $null }
            if ($env:C24TO_STARMONEY_OPTS) { $env:C24TO_STARMONEY_OPTS } else { $null }
            "-classpath"
            $CLASSPATH
            "de.nasahl.csv2camt.Application"
            $args
        )

        Write-Verbose "Java-Befehl: $($JAVA_EXE) $($JavaArguments)"
        # Java-Anwendung ausfuehren und Exit-Code erfassen
        & $JAVA_EXE $JavaArguments

        $EXIT_CODE = $LASTEXITCODE
        Write-Verbose "Exit-Code: $($EXIT_CODE)"
    }
}

function Invoke-ErrorHandling {
    #Finale Fehlerbehandlung und Beenden (fuer den Befehlszeilenmodus) ---
    if ($EXIT_CODE -ne 0) {
        Write-Verbose "Fehler aufgetreten (Befehlszeilenmodus), Exit-Code ist $($EXIT_CODE)"
        # Setzen Sie die Variable C24TO_STARMONEY_EXIT_CONSOLE, falls Sie den Skript-Rueckgabewert anstelle des cmd.exe /c-Rueckgabewerts benoetigen!
        if (-not ([string]::IsNullOrEmpty($env:C24TO_STARMONEY_EXIT_CONSOLE))) {
            Write-Verbose "C24TO_STARMONEY_EXIT_CONSOLE ist gesetzt, beende mit Skript-Exit-Code: $($EXIT_CODE)"
            exit $EXIT_CODE
        } else {
            Write-Verbose "C24TO_STARMONEY_EXIT_CONSOLE ist nicht gesetzt, beende mit Prozess-Exit-Code: $($EXIT_CODE)"
            exit $EXIT_CODE # /b-Aequivalent ist einfach exit in PowerShell
        }
    }
}
#endregion

#region --- Argumentenverarbeitung und Ausfuehrung ---
Write-Verbose "Verarbeite Argumente und fuehre C24toStarmoney aus"

if ($args -contains "--help" -or $args -contains "-h" ) {
    Get-Help
} else {
    Invoke-JavaScript

    # Finale Fehlerbehandlung und Beenden (fuer den Befehlszeilenmodus)
    if ($args.Count -ne 0) { # Wenden Sie die Logik des finalen Exit-Codes nur an, wenn Argumente angegeben wurden. Fuer die automatische Verarbeitung moechten wir auch bei Fehlern in einigen Dateien fortfahren.
        Invoke-ErrorHandling
    } else {
        Write-Host "Automatische Dateiverarbeitung abgeschlossen."
        exit 0 # Beende mit 0, auch wenn die Verarbeitung einzelner Dateien fehlgeschlagen ist, wenn Sie den Erfolg des Skripts auch bei Fehlern einzelner Dateien anzeigen moechten. Aendern Sie dies in exit 1, wenn ein Fehler bei der Dateiverarbeitung zum Fehlschlagen des Skripts fuehren soll.
    }

    #remove "1"
    if (Test-Path -Path ".\1") {
        Remove-Item -Path ".\1" -Force
        Write-Verbose "Datei '1' nach Java-Ausfuehrung geloescht."
    }
}
#endregion --- Skriptende ---