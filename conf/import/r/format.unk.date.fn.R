format.unk.date.fn <- function(doc.data,
                               aux.data,
                               dt.type = "character",
                               date.format = "character"){
  date.columns <- doc.data[doc.data$variable_type == "Date" & doc.data$table == dt.type, c("short_name")]
  
  #If the dates have / or - probably they could not be converted to CanReg5 date format
  #because they have 99 as unknown
  
  #Detect if at least one of the dates has / or -
  aux.data.slash <- data.frame(lapply(aux.data[,date.columns], 
                                        function(x) str_detect(x,"/")))
  
  aux.data.hyp <- data.frame(lapply(aux.data[,date.columns], 
                                      function(x) str_detect(x,"-")))
  #Get the index of the dates with / or -
  if(any(aux.data.slash == TRUE)){
    errors.slash.index <- data.frame(which(aux.data.slash == TRUE, arr.ind=T))
  }else{
    errors.slash.index  <- "no slash"
  }
  
  if(any(aux.data.hyp == TRUE)){
    errors.hyp.index  <- data.frame(which(aux.data.hyp == TRUE, arr.ind=T))
  }else{
    errors.hyp.index  <- "no hyphen"
  }
  #merge index into one dataset
  if (class(errors.hyp.index) != "character" & class(errors.slash.index) != "character"){
    errors.index <- cbind(errors.slash.index,
                                errors.hyp.index)
  }else{
    if (class(errors.hyp.index) != "character"){
      errors.index <- errors.hyp.index
    }else{
      errors.index <- errors.slash.index
    }
  }
  aux.data <- change.date.format.fn(aux.data,
                                    errors.index,
                                    date.format,
                                    date.columns)
  
  
  return(aux.data)
}