Set WshShell = CreateObject("WScript.Shell")
strPath = Wscript.ScriptFullName
Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objFile = objFSO.GetFile(strPath)
strFolder = objFSO.GetParentFolderName(objFile) 
WshShell.CurrentDirectory = strFolder

intReturn = WshShell.Run("cmd.exe /c where javaw", 0, True)

If intReturn <> 0 Then
    MsgBox "Java (JDK) tidak ditemukan di komputer ini!" & vbCrLf & vbCrLf & "Pastikan Anda sudah menginstal Java (minimal versi 17) dan menambahkannya ke System Environment Variables (PATH).", 16, "Error - MusicPlayerFX"
    WScript.Quit
End If

WshShell.Run "javaw Launcher.java", 0, False
