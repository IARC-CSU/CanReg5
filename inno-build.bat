set isxpath="C:\Program Files (x86)\Inno Setup 5"
set isx=%isxpath%\iscc.exe
set iwz=CanReg5-InnoSetup.iss
set iwz-setup=CanReg5-R-packages-InnoSetup.iss
set zippath="C:\Program Files\7-Zip"
set zx=%zippath%\7z.exe
REM if not exist %isx% set errormsg=%isx% not found && goto errorhandler
%isx% "%iwz%"
%isx% "%iwz-setup%"
cd dist
%zx% a -tzip CanReg5-Setup.zip CanReg5-Setup.exe
%zx% a -tzip CanReg5-R-Packages-Setup.zip CanReg5-R-Packages-Setup.exe
move CanReg5-Setup.zip web\
move CanReg5-R-Packages-Setup.zip web\
goto :eof