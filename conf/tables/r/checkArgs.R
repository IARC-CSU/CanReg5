#checkArgs(c("-out=C:/Documents and Settings/CinUser/My Documents/Anahita/outscript/test", "-pop=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/pop1404377193814741326.tsv", "-inc=C:/Documents and Settings/CinUser/My Documents/Anahita/Testfiles/inc5404848408254658419.tsv", "-logr"), "-ft")
 

checkArgs <- function(Args, variable){

		##length of variable name
		lengthVariable <- nchar(variable)
		
		if(variable %in% substr(Args, 1, lengthVariable)){ 

	if(variable %in% c("-logr", "-onePage")){

			return(as.logical(TRUE))
	
	}else{
		whichArgs <- which(substr(Args, 1, lengthVariable) == variable)
		##print(substring( Args[whichArgs], (lengthVariable+2), nchar(Args[whichArgs]) ))
		return(substring( Args[whichArgs], (lengthVariable+2), nchar(Args[whichArgs]) ))
		
	}#end if "logr", "onePage"
	
	
		}else{
	
			if(variable == "-ft"){
		
			return("pdf")

		}else{
			
			return(as.logical(FALSE))

		}
	
		}#End if, else


}#End function
