makeTable <- function(object, outFileTable){

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

if(!is.installed("xtable")){
load.fun("xtable")
}

library("xtable")
newobject<-xtable(object)
print(newobject, type="html", file=outFileTable, append = TRUE)

#write.table(object, file = "C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/filename2.xml", append = TRUE)

}#End function makeTable