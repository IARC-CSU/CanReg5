set zippath="C:\Program Files\7-Zip"
set zx=%zippath%\7z.exe
set distfolder=dist
REM if not exist %isx% set errormsg=%isx% not found && goto errorhandler
%zx% a -tzip CanReg5.zip conf
%zx% a -tzip CanReg5.zip demo
%zx% a -tzip CanReg5.zip scripts
%zx% a -tzip CanReg5.zip CanReg.exe
copy doc\CanReg5-Instructions\CanReg5-Instructions.pdf doc\
%zx% a -tzip CanReg5.zip doc\CanReg5-Instructions.pdf
%zx% a -tzip CanReg5.zip changelog.txt
%zx% a -tzip CanReg5-R-packages.zip r-packages
REM pull out translations and zip them
rsync -Rtrv --include "*/" --include "*.properties" --exclude "*" ./src/canreg/client/ ./translations/
%zx% a -tzip translations.zip translations\src
REM prepare distribution
cd %distfolder%
%zx% a -tzip ..\CanReg5.zip .
md web
copy ..\changelog.txt web\
copy ..\version.txt web\
copy ..\doc\CanReg5-Instructions\CanReg5-Instructions.pdf web\
move ..\CanReg5.zip web\
move ..\CanReg5-R-packages.zip web\
move ..\translations.zip .\
cd ..
goto :eof