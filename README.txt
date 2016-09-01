Es gibt den "normalen Modus" der App, welcher normalerweise ausgeführt wird. Dieser beinhaltet GPS-Tracking und senden dieser Signale ins Internet.

Außerdem gibt es den "test Modus", welcher "aktiviert" wird durch folgende "Einstellung": im AndroidManifest die Zeile 
            android:name=".activities.MainActivity"
ändern in
            android:name=".activities.TestMainActivity"
In diesem "test Modus" wird kein GPS Tracking durchgeführt, und es werden Mock-Daten ins Internet geschickt. Dieser Modus wird verwendet für das Requesttesten. Siehe dazu auch FE-42 oder Confluence->Frontend->Android App->Testen des Stromverbrauchs.