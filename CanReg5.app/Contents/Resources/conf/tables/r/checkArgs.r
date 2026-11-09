##Function checks if varibale is given as an argument
checkArgs <- function(Args, variable){

##Length of variable name
lengthVariable <- nchar(variable)
		
if(variable %in% substr(Args, 1, lengthVariable)){ 

  if(variable %in% c("-logr")){
			return(as.logical(TRUE))
	}else{
			whichArgs <- which(substr(Args, 1, lengthVariable) == variable)
		
			return(substring( Args[whichArgs], (lengthVariable+2), nchar(Args[whichArgs]) ))
		
	}#end else
		
}else{
	
	if(variable == "-ft"){
		
		return("pdf")

	}else{
			
		return(as.logical(FALSE))

	}
	
}#End if, else

}#End function
