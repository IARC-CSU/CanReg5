unknown.date.fn <- function(aux.data,
                            aux.data.yyyymmdd,
                            date.columns){
  
  aux.data.unknown <- data.frame(lapply(aux.data[,date.columns], 
         function(x) str_detect(x,"([0-9][0-9]99[1-2][0|9][0-9][0-9])|(99[0-1][0-9][1-2][0|9][0-9][0-9])|(9999[1-2][0|9][0-9][0-9])|([0-9][0-9][0-9][0-9]9999)")))
  
  errors.data <- data.frame(which(aux.data.unknown == TRUE, arr.ind=T))
  if (nrow(errors.data) > 1){
    col.error <- unique(errors.data$col)
    
    #Replace the error value for the name of the column value
    for (i in 1:length(col.error)){
      aux.data.yyyymmdd[errors.data$row[errors.data$col == col.error[i]], col.error[i]] <- TRUE
    }
  }else{
    col.error <- unique(errors.data$col)
    if (nrow(errors.data) == 1){
      aux.data.yyyymmdd[errors.data$row[errors.data$col == col.error], col.error] <- TRUE
      
    }else{NULL}
  }
  return(aux.data.yyyymmdd)
  
  
}