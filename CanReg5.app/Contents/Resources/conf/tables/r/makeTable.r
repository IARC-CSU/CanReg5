makeTable <- function(object, outFileTable, i, fileType){

if(fileType == "txt"){
	
	##Add col.names on top of file
	if(i == 1){
		write.table(object, file = outFileTable, append = TRUE, quote = FALSE, row.names = FALSE, col.names = TRUE)
	}else{
		write.table(object, file = outFileTable, append = TRUE, quote = FALSE, row.names = FALSE, col.names = FALSE)
	}#End if, else

}else{
	is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

	if(!is.installed("xtable")){
		load.fun("xtable")
	}

	library("xtable")
	newobject<-xtable(object)
	print(newobject, type="html", file=outFileTable, append = TRUE)

}#End else

}#End function makeTable