set isxpath="C:\Program Files (x86)\Inno Setup 5"
set isx=%isxpath%\iscc.exe
set iwz=CanReg5-InnoSetup.iss
REM if not exist %isx% set errormsg=%isx% not found && goto errorhandler
%isx% "%iwz%"
move dist\CanReg5-Setup.exe dist\web\
goto :eof