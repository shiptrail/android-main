Es gibt den "normalen Modus" der App, welcher normalerweise ausgeführt wird. Dieser beinhaltet GPS-Tracking und senden dieser Signale ins Internet.

Außerdem gibt es den "test Modus", welcher "aktiviert" wird durch folgende "Einstellung": im AndroidManifest die Zeile 
            android:name=".activities.MainActivity"
ändern in
            android:name=".activities.TestMainActivity"

Damit der "test Modus" weiterhin funktioniert, muss, falls er aktiviert wird, in der Datei app/src/main/java/de/h3adless/gpstracker/services/HttpRequest.java in den Funktionen makeCertificateDialog und makeHttpsDialog 
        Intent intent = new Intent(context, MainActivity.class);
in 
        Intent intent = new Intent(context, TestMainActivity.class);
und das import-Statement ebenfalls entsprechend geändert werden. 

In diesem "test Modus" wird kein GPS Tracking durchgeführt, und es werden Mock-Daten ins Internet geschickt. Dieser Modus wird verwendet für das Requesttesten. Siehe dazu auch FE-42 oder Confluence->Frontend->Android App->Testen des Stromverbrauchs.