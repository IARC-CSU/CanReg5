set isxpath="C:\Program Files (x86)\Inno Setup 5"
set isx=%isxpath%\iscc.exe
set iwz=helpers\InnoSetup\CanReg5-InnoSetup.iss
set iwz-r=helpers\InnoSetup\CanReg5-R-packages-InnoSetup.iss
set iwz-w-r=helpers\InnoSetup\CanReg5-with-R-packages-InnoSetup.iss
set zippath="C:\Program Files\7-Zip"
set zx=%zippath%\7z.exe
pandoc changelog.txt -f markdown -t rtf -c helpers\pandoc\pandoc.css -s -o changelog.rtf
pandoc changelog.txt -f markdown -t html -c helpers\pandoc\pandoc.css -s -o changelog.html
REM if not exist %isx% set errormsg=%isx% not found && goto errorhandler
%isx% "%iwz%"
%isx% "%iwz-r%"
%isx% "%iwz-w-r%"
cd dist
%zx% a -tzip CanReg5-Setup.zip CanReg5-Setup.exe
%zx% a -tzip CanReg5-R-Packages-Setup.zip CanReg5-R-Packages-Setup.exe
%zx% a -tzip CanReg5-with-R-Packages-Setup.zip CanReg5-with-R-Packages-Setup.exe
move CanReg5-Setup.zip web\
move CanReg5-R-Packages-Setup.zip web\
move CanReg5-with-R-Packages-Setup.zip web\
goto :eof